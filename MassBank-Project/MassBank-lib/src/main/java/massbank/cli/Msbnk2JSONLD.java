package massbank.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.petitparser.context.Result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import massbank.Record;
import massbank.RecordParser;

/**
 * This class creates a JSON+LD structured data file for all records given.
 * @author rmeier
 * @version 05-12-2022
 */
public class Msbnk2JSONLD {
	private static final Logger logger = LogManager.getLogger(Msbnk2JSONLD.class);

	/**
	 * Validate a <code>recordString</code> and return the parsed information in a {@link Record} 
	 * or <code>null</code> if the validation was not successful. Options are given in 
	 * <code>config</code>.
	 */
	public static Record validate(String recordString, Set<String> config) {
		Record record = new Record();
		RecordParser recordparser = new RecordParser(record, config);
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
		System.out.println("Msbnk2JSONLD version: " + properties.getProperty("version"));

		// parse command line
		Options options = new Options();
		options.addRequiredOption("o", "out", true, "output file");
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
		File outFile = new File(cmd.getOptionValue("o"));
		System.out.println("Export to: " + outFile.toString());
		
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
		if (recordfiles.size() == 0 ) {
			logger.error("No files found for validation.");
			System.exit(1);
		}
		logger.trace("Found " + recordfiles.size() + " files");
			
		// process all files
		JsonArray structuredData = new JsonArray();
		recordfiles.parallelStream().map(filename -> {
			Record record=null;
			logger.info("Working on " + filename + ".");
			try {
				String recordString = FileUtils.readFileToString(filename, StandardCharsets.UTF_8);
				Set<String> config = new HashSet<String>();
				config.add("legacy");
				record = validate(recordString, config);
				if (record == null) {
					logger.error("Error in \'" + filename + "\'.");
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return record;
		})
		.filter(Objects::nonNull)
		.forEach(record -> {
			structuredData.addAll(record.createStructuredDataJsonArray());
		});
		
		//write results to file
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			writer.write(gson.toJson(structuredData));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
