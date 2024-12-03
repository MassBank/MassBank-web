/*******************************************************************************
 * Copyright (C) 2017 MassBank consortium
 *
 * This file is part of MassBank.
 *
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 ******************************************************************************/
package massbank.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.petitparser.context.Result;
import massbank.Record;
import massbank.RecordParser;
import massbank.RecordParserDefinition;
import massbank.db.DatabaseManager;

/**
 * This class validates a record file or String by using the syntax of {@link RecordParserDefinition}.
 * @author rmeier
 * @version 03-06-2020
 */
public class Validator {
	private static final Logger logger = LogManager.getLogger(Validator.class);
	private static final Pattern nonStandardCharsPattern = Pattern.compile("[\\w\\n\\-\\[\\].\"\\\\ ;:–=+,|(){}/$%@'°!?#`^*&<>µáćÉéóäöü©]+");
	private static AtomicBoolean doDatabase;
	private static RecordParser recordparser;


	public static void main(String[] arguments) throws SQLException, ConfigurationException {
		// load version and print
		Properties properties = loadProperties();
		System.out.println("Validator version: " + properties.getProperty("version"));

		// parse command line
		CommandLine cmd = parseCommandLine(arguments);

		// find all *.txt files in arguments
		// and if argument is a directory find all *.txt files in directories
		// and subdirectories of the argument
		List<Path> recordFiles = findRecordFiles(cmd.getArgList());
		if (recordFiles.isEmpty()) {
			logger.error("No files found for validation.");
			System.exit(1);
		}

		doDatabase = new AtomicBoolean(cmd.hasOption("db"));

		logger.trace("Found {} files for processing", recordFiles.size());

		// create parser from command line options
		Set<String> config = new HashSet<>();
		config.add("validate");
		if (cmd.hasOption("legacy")) config.add("legacy");
		if (cmd.hasOption("online")) config.add("online");
		recordparser = new RecordParser(config);

		List<String> accessions = recordFiles.parallelStream()
			.map(Validator::readFile)
			.filter(Objects::nonNull)
			.peek(Validator::checkNonStandardChars)
			.map(Validator::parseRecord)
			.filter(Objects::nonNull)
			.map(Validator::validateSerialization)
			.filter(Objects::nonNull)
			.toList();

		accessions = checkDuplicates(accessions);

		// return 1 if there were errors
		if (recordFiles.size() != accessions.size()) System.exit(1);
		else System.exit(0);
	}

	private static Properties loadProperties() {
		Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return properties;
	}

	private static CommandLine parseCommandLine(String[] arguments) {
		Options options = new Options();
		options.addOption(null, "db", false, "also read record from database and compare with original Record; Developer Feature!");
		options.addOption(null, "legacy", false, "less strict mode for legacy records with minor problems.");
		options.addOption(null, "online", false, "also do online checks, like PubChem CID check.");

		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse(options, arguments);
		} catch (ParseException e) {
			System.err.println("Parsing command line failed. Reason: " + e.getMessage());
			new HelpFormatter().printHelp("Validator [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
			System.exit(1);
		}
		if (cmd.getArgList().isEmpty()) {
			new HelpFormatter().printHelp("Validator [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
			System.exit(1);
		}

		if (cmd.hasOption("legacy")) System.out.println("Validation mode: legacy");
		return cmd;
	}

	private static List<Path> findRecordFiles(List<String> arguments) {
		return arguments.stream()
			.map(File::new)
			.flatMap(file -> {
				if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("txt")) {
					return Stream.of(file.toPath());
				} else if (file.isDirectory()) {
					try (Stream<Path> paths = Files.walk(file.toPath())) {
						return paths.filter(path -> Files.isRegularFile(path) && FilenameUtils.getExtension(path.toString()).equals("txt"));
					}
					catch (IOException e) {
						logger.warn("Error processing directory {}", file, e);
						return Stream.empty();
					}
				} else {
					logger.warn("Argument {} could not be processed.", file);
					return Stream.empty();
				}
			})
			.collect(Collectors.toList());
	}

	public static AbstractMap.SimpleEntry<Path, String> readFile(Path filename) {
		try {
			return new AbstractMap.SimpleEntry<>(filename, Files.readString(filename, StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Error reading file: {}", filename, e);
			return null;
		}
	}

	private static void checkNonStandardChars(AbstractMap.SimpleEntry<Path, String> recordString) {
        if (hasNonStandardChars(recordString.getValue())) {
            logger.warn("Check {}.", recordString.getKey());
        }
    }

	public static AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<Path, String>, Record> parseRecord(AbstractMap.SimpleEntry<Path, String> recordString) {
        Result res = recordparser.parse(recordString.getValue());
        if (res.isFailure()) {
            logParseError(recordString, res);
            return null;
        } else {
			Record record = res.get();
			String accession = record.ACCESSION();
			if (!accession.equals(FilenameUtils.getBaseName(recordString.getKey().toString()))) {
				logger.error("Error in {}.", recordString.getKey().toString());
				logger.error("ACCESSION {} does not match filename '{}'", record.ACCESSION(), recordString.getKey().toString());
				return null;
			}
			return new AbstractMap.SimpleEntry<>(recordString, record);
        }
    }

	public static Record parseRecord(AbstractMap.SimpleEntry<Path, String> recordString, RecordParser recordparser) {
		Result res = recordparser.parse(recordString.getValue());
		if (res.isFailure()) {
			logParseError(recordString, res);
			return null;
		} else {
			Record record = res.get();
			String accession = record.ACCESSION();
			if (!accession.equals(FilenameUtils.getBaseName(recordString.getKey().toString()))) {
				logger.error("Error in {}.", recordString.getKey().toString());
				logger.error("ACCESSION {} does not match filename '{}'", record.ACCESSION(), recordString.getKey().toString());
				return null;
			}
			return record;
		}
	}

	private static void logParseError(AbstractMap.SimpleEntry<Path, String> recordString, Result res) {
		logger.error(res.getMessage());
		int position = res.getPosition();
		String[] tokens = recordString.getValue().split("\\n");
		int offset = 0;
		int lineNumber = 1;
		for (String token : tokens) {
			offset += token.length() + 1;
			if (position < offset) {
				int col = position - (offset - (token.length() + 1));
				logger.error(token);
				logger.error("{}^", StringUtils.repeat(" ", col));
				logger.error("Error in file {} at line {}.", recordString.getKey(), lineNumber);
				break;
			}
			lineNumber++;
		}
	}

	private static String validateSerialization(AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<Path, String>, Record> record) {
		String recordStringFromRecord = record.getValue().toString();
		String originalRecordString = record.getKey().getValue().replaceAll("\\r\\n?", "\n");
		int position = StringUtils.indexOfDifference(new String[]{originalRecordString, recordStringFromRecord});
		if (position != -1) {
			logger.error("Error in file {}.", record.getKey().getKey());
			logger.error("File content differs from generated record string.\nThis might be a code problem. Please Report!");
			logSerializationError(originalRecordString, position);
			return null;
		}
		if (doDatabase.get()) {
			Record recordFromDatabase = DatabaseManager.getAccessionData(record.getValue().ACCESSION());
			if (recordFromDatabase == null) {
				logger.error("Retrieval of '{}' from database failed", record.getValue().ACCESSION());
				System.exit(1);
			}
            String recordStringFromDB = recordFromDatabase.toString();
			position = StringUtils.indexOfDifference(new String[]{originalRecordString, recordStringFromDB});
			if (position != -1) {
				logger.error("Error in file {}.", record.getKey().getKey());
				logger.error("File content differs from generated record string from record retrieved from database.\nThis might be a code problem. Please Report!");
				logSerializationError(originalRecordString, position);
				return null;
			}
		}
		return record.getValue().ACCESSION();
	}

	private static void logSerializationError(String originalRecordString, int position) {
		String[] tokens = originalRecordString.split("\\n");
		int offset = 0;
		int lineNumber = 1;
		for (String token : tokens) {
			offset += token.length() + 1;
			if (position < offset) {
				int col = position - (offset - (token.length() + 1));
				logger.error(token);
				logger.error("{}^", StringUtils.repeat(" ", col));
				logger.error("Error in line {}.", lineNumber);
				break;
			}
			lineNumber++;
		}
	}

	private static List<String> checkDuplicates(List<String> accessions) {
		Set<String> uniqueAccessions = new HashSet<>();
		Set<String> duplicates = accessions.stream()
        	.filter(accession -> !uniqueAccessions.add(accession))
        	.collect(Collectors.toSet());
		if (!duplicates.isEmpty()) {
			logger.error("There are duplicates in all accessions:");
			logger.error(duplicates.toString());
		}
		return new ArrayList<>(uniqueAccessions);
	}

	/**
	 * Returns <code>true</code> if there is any suspicious character in <code>recordString</code>.
	 */
	public static boolean hasNonStandardChars(String recordString) {
		Matcher m = nonStandardCharsPattern.matcher(recordString);
		if (m.find()) {
			int position = m.end();
			if (position<recordString.length()) {
				logger.warn("Non standard ASCII character found. This might be an error. Please check carefully.");
				String[] tokens = recordString.split("\\n");
				int offset = 0;
                for (String token : tokens) {
                    offset += token.length() + 1;
                    if (position < offset) {
                        int col = position - (offset - (token.length() + 1));
                        logger.warn(token);
                        logger.warn("{}^", StringUtils.repeat(" ", col));
                        return true;
                    }
                }
			}
		} else {
			logger.warn("Standard character pattern does not work. Please check.");
			return true;
		}
		return false;
	}
}
