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

import massbank.Record;
import massbank.RecordParser;
import massbank.RecordParserDefinition;
import massbank.db.DatabaseManager;
import org.apache.commons.cli.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.petitparser.context.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class validates one or several record file/s by using the syntax of {@link RecordParserDefinition}.
 *
 * Command line usage:
 * Validator [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]
 *
 * Options:
 * --legacy   : less strict mode for legacy records with minor problems.
 * --db       : also read record from database and compare with original Record; Developer Feature!
 * --online   : also do online checks, like PubChem CID check.
 *
 * Example:
 * Validator --db records/
 *
 * @author rmeier
 * @version 04-12-2024
 */
public class Validator {
	private static final Logger logger = LogManager.getLogger(Validator.class);
	private static final Pattern nonStandardCharsPattern = Pattern.compile("[\\w\\n\\-\\[\\].\"\\\\ ;:–=+,|(){}/$%@'°!?#`^*&<>µáćÉéóäöü©]+");
	private static Boolean doDatabase;
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

		doDatabase = cmd.hasOption("db");

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
			.map(Validator::parseRecord)
			.filter(Objects::nonNull)
			.peek(Validator::checkNonStandardChars)
			.map(Validator::validateSerialization)
			.filter(Objects::nonNull)
			.toList();

		accessions = checkDuplicates(accessions);

		// return 1 if there were errors
		if (recordFiles.size() != accessions.size()) System.exit(1);
		else System.exit(0);
	}

	/**
	 * Reads the properties file and loads the properties.
	 *
	 * @return the loaded properties
	 */
	public static Properties loadProperties() {
		Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return properties;
	}

	/**
	 * Parses the command line arguments.
	 *
	 * @param arguments the command line arguments
	 * @return the parsed command line
	 */
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

	/**
	 * Finds all record files in the given arguments.
	 *
	 * @param arguments the list of file or directory paths
	 * @return the list of record file paths
	 */
	public static List<Path> findRecordFiles(List<String> arguments) {
		return arguments.stream()
			.map(Paths::get)
			.flatMap(path -> {
				if (Files.isRegularFile(path) && path.toString().endsWith("txt")) {
					return Stream.of(path);
				}
				else if (Files.isDirectory(path)) {
					try (Stream<Path> paths = Files.walk(path)) {
						return paths
							.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
							.toList()
							.stream();
					} catch (IOException e) {
						logger.warn("Error processing directory {}", path, e);
						return Stream.empty();
					}
				}
				else {
					logger.warn("Argument {} could not be processed.", path);
					return Stream.empty();
				}
			})
			.collect(Collectors.toList());
	}

	/**
	 * Reads the content of a file.
	 *
	 * @param filename the path of the file
	 * @return a SimpleEntry containing the file path and its content, or null if an error occurs
	 */
	public static SimpleEntry<Path, String> readFile(Path filename) {
		try {
			return new SimpleEntry<>(filename, Files.readString(filename, StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Error reading file: {}", filename, e);
			return null;
		}
	}

	/**
	 * Checks for non-standard characters in the record string and logs a warning if found.
	 *
	 * @param result class containing the file path and the record file content
	 */
	private static void checkNonStandardChars(ParseResult result) {
		if (result.record.DEPRECATED()) return;
		Matcher m = nonStandardCharsPattern.matcher(result.content);
		if (m.find()) {
			int position = m.end();
			if (position < result.content.length()) {
				logger.warn("Non standard ASCII character found. This might be an error. Please check carefully.");
				String[] tokens = result.content.split("\\n");
				int offset = 0;
				for (String token : tokens) {
					offset += token.length() + 1;
					if (position < offset) {
						int col = position - (offset - (token.length() + 1));
						logger.warn(token);
						logger.warn("{}^", StringUtils.repeat(" ", col));
						logger.warn("Check file {}.", result.filename);
						break;
					}
				}
			}
		} else {
			logger.warn("Standard character pattern does not work. Please check.");
		}
	}

	/**
	 * Parses the record string using the record parser.
	 *
	 * @param recordString the entry containing the file path and its content
	 * @return a SimpleEntry containing the original entry and the parsed record, or null if parsing fails
	 */
	public static ParseResult parseRecord(SimpleEntry<Path, String> recordString) {
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
			return new ParseResult(recordString.getKey(), recordString.getValue(), record);
		}
	}

	public static Record parseRecord(SimpleEntry<Path, String> recordString, RecordParser recordparser) {
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

	/**
	 * Logs the parse error details.
	 *
	 * @param recordString the entry containing the file path and its content
	 * @param res          the result of the parsing
	 */
	private static void logParseError(SimpleEntry<Path, String> recordString, Result res) {
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

	/**
	 * Tests the serialization of the record from text to internal data structure and back to text.
	 *
	 * @param result the ParseResult containing the the filename, the file content and the record object
	 * @return the accession of the record if validation is successful, or null if validation fails
	 */
	private static String validateSerialization(ParseResult result) {
		String recordStringFromRecord = result.record().toString();
		String originalRecordString = result.content().replaceAll("\\r\\n?", "\n");
		int position = StringUtils.indexOfDifference(new String[]{originalRecordString, recordStringFromRecord});
		if (position != -1) {
			logger.error("Error in file {}.", result.filename());
			logger.error("File content differs from generated record string.\nThis might be a code problem. Please Report!");
			logSerializationError(originalRecordString, position);
			return null;
		}
		if (doDatabase) {
			Record recordFromDatabase = DatabaseManager.getAccessionData(result.record().ACCESSION());
			if (recordFromDatabase == null) {
				logger.error("Retrieval of '{}' from database failed", result.record().ACCESSION());
				System.exit(1);
			}
			String recordStringFromDB = recordFromDatabase.toString();
			position = StringUtils.indexOfDifference(new String[]{originalRecordString, recordStringFromDB});
			if (position != -1) {
				logger.error("Error in file {}.", result.filename());
				logger.error("File content differs from generated record string from record retrieved from database.\nThis might be a code problem. Please Report!");
				logSerializationError(originalRecordString, position);
				return null;
			}
		}
		return result.record().ACCESSION();
	}

	/**
	 * Logs the serialization error details.
	 *
	 * @param originalRecordString the original record string
	 * @param position             the position of the difference
	 */
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

	/**
	 * Checks for duplicate accessions in the list.
	 *
	 * @param accessions the list of accessions
	 * @return the list of unique accessions
	 */
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

	public record ParseResult(Path filename, String content, Record record) {

	}


}
