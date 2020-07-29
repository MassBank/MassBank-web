package massbank;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.petitparser.parser.Parser;

import massbank.db.DatabaseManager;

/**
 * This class validates a record file or String by using the syntax of {@link RecordParserDefinition}.
 * @author rmeier
 * @version 03-06-2020
 */
public class Validator {
	private static final Logger logger = LogManager.getLogger(Validator.class);
	
	/**
	 * Returns <code>true</code> if there is any suspicious character in <code>recordString</code>.
	 */
	public static boolean hasNonStandardChars(String recordString) {
		// the following are allowed
		char[] myCharSet = new char[] {'–', 'ä', 'ö', 'ü', 'ó', 'é', 'µ', 'á', 'É'};
		Arrays.sort(myCharSet);
		for (int i = 0; i < recordString.length(); i++) {
			if (recordString.charAt(i) > 0x7F &&  (Arrays.binarySearch(myCharSet, recordString.charAt(i))<0)) {
				String[] tokens = recordString.split("\\r?\\n");
				logger.warn("Non standard ASCII character found. This might be an error. Please check carefully.");
				int line = 0, col = 0, offset = 0;
				for (String token : tokens) {
					offset = offset + token.length() + 1;
					if (i < offset) {
						col = i - (offset - (token.length() + 1));
						logger.warn(tokens[line]);
						StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", tokens[line].length()));
						error_at.setCharAt(col, '^');
						logger.warn(error_at);
						return true;
					}
				line++;
				}
			}
		}
		return false;
	}
	
	/**
	 * Validate a <code>recordString</code> and return the parsed information in a {@link Record} 
	 * or <code>null</code> if the validation was not successful. Be strict in validation.
	 */
	public static Record validate(String recordString, String contributor) {
		return validate(recordString, contributor, true);
	}
	
	/**
	 * Validate a <code>recordString</code> and return the parsed information in a {@link Record} 
	 * or <code>null</code> if the validation was not successful. Be less strict if 
	 * <code>strict</code> is <code>false</code>. This is useful in automatic repair routines.
	 */
	public static Record validate(String recordString, String contributor, boolean strict) {
		Record record = new Record(contributor);
		Parser recordparser = new RecordParser(record, strict);
		Result res = recordparser.parse(recordString);
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
		} 
		return record;
	}

	public static void main(String[] arguments) {
		// load version and print
		final Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Validator version: " + properties.getProperty("version"));

		// parse command line
		Options options = new Options();
		options.addOption(null, "db", false, "also read record from database and compare with original Record; Developer Feature!");
		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse( options, arguments);
		}
		catch(ParseException e) {
	        // oops, something went wrong
	        System.err.println( "Parsing command line failed. Reason: " + e.getMessage() );
	        HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Validator [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
	        System.exit(1);
	    }
		if (cmd.getArgList().size() == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Validator [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
	        System.exit(1);
		}
		
		// find all files in arguments and all *.txt files in directories and subdirectories
		// specified in arguments 
		List<File> recordfiles = new ArrayList<>();
		for (String argument : cmd.getArgList()) {
			File argumentf = new File(argument);
			if (argumentf.isFile() && FilenameUtils.getExtension(argument).equals("txt")) {
				recordfiles.add(argumentf);
			}
			else if (argumentf.isDirectory()) {
				recordfiles.addAll(FileUtils.listFiles(argumentf, new String[] {"txt"}, true));
			}
			else {
				logger.warn("Argument " + argument + " could not be processed.");
			}
		}
			

		// validate all files
		logger.trace("Validating " + recordfiles.size() + " files");
		AtomicBoolean haserror = new AtomicBoolean(false);
		AtomicBoolean doDatbase = new AtomicBoolean(cmd.hasOption("db"));
		List<String> accessions = recordfiles.parallelStream().map(filename -> {
			String recordString;
			Record record=null;
			try {
				recordString = FileUtils.readFileToString(filename, StandardCharsets.UTF_8);
				hasNonStandardChars(recordString);
				record = validate(recordString, "");
				if (record == null) {
					logger.error("Error in \'" + filename + "\'.");
					haserror.set(true);
					return null;
				}
				else {
					logger.trace("validation passed for " + filename);
					// compare ACCESSION with filename
					if (!record.ACCESSION().equals(FilenameUtils.getBaseName(filename.toString()))) {
						logger.error("Error in \'" + filename.getName().toString() + "\'.");
						logger.error("ACCESSION \'" + record.ACCESSION() + "\' does not match filename \'" + filename.getName().toString() + "\'");
						haserror.set(true);
					}
					
					// validate correct serialisation: String -> Record class -> String
					String recordStringFromRecord = record.toString();
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
								haserror.set(true);
								break;
							}
							line++;
						}
						
					}
				
					// validate correct serialisation with db: String -> Record class -> db -> Record class -> String
					if (doDatbase.get()) {
						Record recordDatabase = null;
						try {
							DatabaseManager dbMan = new DatabaseManager("MassBank");
							recordDatabase = dbMan.getAccessionData(record.ACCESSION());
							dbMan.closeConnection();
						} catch (SQLException | ConfigurationException e) {
							e.printStackTrace();
							System.exit(1);
						}
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
									haserror.set(true);
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
			return record.ACCESSION();
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());;
		
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
			haserror.set(true);
		}
		
		// return 1 if there were errors
		if (haserror.get()) System.exit(1);
		else System.exit(0);
	}
}
