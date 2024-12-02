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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
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
	private static final Pattern nonStandardCharsPattern = Pattern.compile("[\\d\\w\\n\\-\\[\\]\\.\"\\\\ ;:–=+,|(){}/$%@'!?#`^*&<>µáćÉéóäöü©]+");

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

		AtomicBoolean hasError = new AtomicBoolean(false);
		AtomicBoolean doDatbase = new AtomicBoolean(cmd.hasOption("db"));

		logger.trace("Found {} files for processing", recordFiles.size());

		// create parser from command line options
		Set<String> config = new HashSet<>();
		config.add("validate");
		if (cmd.hasOption("legacy")) config.add("legacy");
		if (cmd.hasOption("online")) config.add("online");
		RecordParser recordparser = new RecordParser(config);

		// read files
		recordFiles.parallelStream()
			.map(filename -> {
				try {
					return new AbstractMap.SimpleEntry<>(filename, Files.readString(filename, StandardCharsets.UTF_8));
				} catch (IOException e) {
					logger.error("Error reading file: {}", filename, e);
					hasError.set(true);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.peek(entry -> {
				if (hasNonStandardChars(entry.getValue())) {
					logger.warn("Check {}.", entry.getKey());
				}
			})
			.forEach(System.out::println);



		List<String> accessions = recordFiles.parallelStream()
			.map(Path::toFile)
			.map(filename -> {
			String recordString;
			String accession=null;
			logger.info("Working on " + filename + ".");
			try {
				recordString = FileUtils.readFileToString(filename, StandardCharsets.UTF_8);
				
				if (hasNonStandardChars(recordString)) {
					logger.warn("Check " + filename + ".");
				};
				
				// basic validation

				Record record = validate(recordString, config);
				if (record == null) {
					logger.error("Error in \'" + filename + "\'.");
					hasError.set(true);
				}
				
				// additional tests
				else {
					logger.trace("validation passed for " + filename);
					// compare ACCESSION with filename
					accession=record.ACCESSION();
					if (!accession.equals(FilenameUtils.getBaseName(filename.toString()))) {
						logger.error("Error in \'" + filename.getName().toString() + "\'.");
						logger.error("ACCESSION \'" + record.ACCESSION() + "\' does not match filename \'" + filename.getName().toString() + "\'");
						hasError.set(true);
					}
					
					// validate correct serialization: String <-> (String -> Record class -> String)
					String recordStringFromRecord = record.toString();
					recordString = recordString.replaceAll("\\r\\n?", "\n");
					int position = StringUtils.indexOfDifference(new String [] {recordString, recordStringFromRecord});
					if (position != -1) {
						logger.error("Error in \'" + filename + "\'.");
						logger.error("File content differs from generated record string.\nThis might be a code problem. Please Report!");
						String[] tokens = recordStringFromRecord.split("\\n");
						int line = 0, col = 0, offset = 0;
						for (String token : tokens) {
							offset = offset + token.length() + 1;
							if (position < offset) {
								col = position - (offset - (token.length() + 1));
								logger.error("Error in line " + (line+1) + ".");
								logger.error(tokens[line]);
								StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", col));
								error_at.append('^');
								logger.error(error_at);
								hasError.set(true);
								break;
							}
							line++;
						}
					}
				
					// validate correct serialization with db: String <-> (db -> Record class -> String)
					if (doDatbase.get()) {
						Record recordDatabase = null;
						recordDatabase = DatabaseManager.getAccessionData(record.ACCESSION());
						if(recordDatabase == null) {
							String errormsg	= "retrieval of '" + record.ACCESSION() + "' from database failed";
							logger.error(errormsg);
							System.exit(1);
						}
						String recordStringFromDB = recordDatabase.toString();
						position = StringUtils.indexOfDifference(new String [] {recordString, recordStringFromDB});
						if (position != -1) {
							logger.error("Error in \'" + filename + "\'.");
							logger.error("File content differs from generated record string from database content.\nThis might be a code problem. Please Report!");
							String[] tokens = recordStringFromDB.split("\\n");
							int line = 0, col = 0, offset = 0;
							for (String token : tokens) {
								offset = offset + token.length() + 1;
								if (position < offset) {
									col = position - (offset - (token.length() + 1));
									logger.error("Error in line " + (line+1) + ".");
									logger.error(tokens[line]);
									StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", col));
									error_at.append('^');
									logger.error(error_at);
									hasError.set(true);
									break;
								}
								line++;
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return accession;
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
		
		// check duplicates
		Set<String> duplicates = new LinkedHashSet<String>();
		Set<String> uniques = new HashSet<String>();
		for(String c : accessions) {
			//System.out.println(c);
			if(!uniques.add(c)) {
				duplicates.add(c);
			}
		}
		if (duplicates.size()>0) {
			logger.error("There are duplicates in all accessions:");
			logger.error(duplicates.toString());
			hasError.set(true);
		}
		
		// return 1 if there were errors
		if (hasError.get()) System.exit(1);
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
		List<Path> recordFiles = new ArrayList<>();
		for (String argument : arguments) {
			Path argumentPath = new File(argument).toPath();
			if (Files.isRegularFile(argumentPath) && FilenameUtils.getExtension(argument).equals("txt")) {
				recordFiles.add(argumentPath);
			} else if (Files.isDirectory(argumentPath)) {
				try {
					Files.walk(argumentPath)
						.filter(path -> Files.isRegularFile(path) && FilenameUtils.getExtension(path.toString()).equals("txt"))
						.forEach(recordFiles::add);
				} catch (IOException e) {
                    logger.warn("Error processing directory {}", argument, e);
				}
			} else {
                logger.warn("Argument {} could not be processed.", argument);
			}
		}
		return recordFiles;
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

	/**
	 * Validate a <code>recordString</code> and return the parsed information in a {@link Record}
	 * or <code>null</code> if the validation was not successful. Options are given in
	 * <code>config</code>.
	 */
	public static Record validate(String recordString, Set<String> config) {
		RecordParser recordparser = new RecordParser(config);
		Result res =  recordparser.parse(recordString);
		if (res.isFailure()) {
			logger.error(res.getMessage());
			int position = res.getPosition();
			String[] tokens = recordString.split("\\n");

			int line = 0, col = 0, offset = 0;
			for (String token : tokens) {
				offset = offset + token.length() + 1;
				if (position < offset) {
					col = position - (offset - (token.length() + 1));
					logger.error(tokens[line]);
					StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", col));
					error_at.append('^');
					logger.error(error_at);
					break;
				}
				line++;
			}
			return null;
		} else {
			return res.get();
		}
	}
}
