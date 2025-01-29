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

import massbank.ProjectPropertiesLoader;
import massbank.Record;
import massbank.RecordParser;
import massbank.export.RecordToJson;
import massbank.export.RecordToJsonLD;
import massbank.export.RecordToNIST_MSP;
import massbank.export.RecordToRIKEN_MSP;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * This class exports one or several record files to different formats.
 *
 * Command line usage:
 * RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]
 *
 * Options:
 * -o, --outfile : name of output file (required)
 * -f, --format  : output format; possible values: RIKEN_MSP, NIST_MSP, json, jsonld (required)
 *
 * Example:
 * RecordExporter -o output.json -f json records/
 *
 * @author rmeier
 * @version 04-12-2024
 */
public class RecordExporter {
    private static final Logger logger = LogManager.getLogger(RecordExporter.class);
	
    public static void main(String[] arguments) {
        // load version and print
        Properties properties = ProjectPropertiesLoader.loadProperties();
        System.out.println("Exporter version: " + properties.getProperty("version"));

        // parse command line
        CommandLine cmd = parseCommandLine(arguments);

		List<Path> recordFiles = Validator.findRecordFiles(cmd.getArgList());
		if (recordFiles.isEmpty()) {
			logger.error("No files found for validation.");
			System.exit(1);
		}

		RecordParser recordparser = new RecordParser(new HashSet<>());
		List<Record> records =recordFiles.parallelStream()
			.map(Validator::readFile)
			.filter(Objects::nonNull)
			.map(recordString -> Validator.parseRecord(recordString, recordparser))
			.filter(Objects::nonNull)
			.toList();

        File outfile = new File(cmd.getOptionValue("o"));
		String format = cmd.getOptionValue("f");

        switch (format) {
            case "RIKEN_MSP":
                RecordToRIKEN_MSP.recordsToRIKEN_MSP(outfile, records);
                break;
            case "NIST_MSP":
                RecordToNIST_MSP.recordsToNIST_MSP(outfile, records);
                break;
            case "json":
                RecordToJson.recordsToJson(outfile, records);
                break;
            case "jsonld":
                RecordToJsonLD.recordsToJson(outfile, records);
                break;
            default:
                logger.error("This code should not run.");
                System.exit(1);
        }

    }

    private static CommandLine parseCommandLine(String[] arguments) {
        // parse command line
        Options options = new Options();
        options.addRequiredOption("o", "outfile", true, "name of output file");
        options.addRequiredOption("f", "format", true, "output format; possible values: RIKEN_MSP, NIST_MSP; json; jsonld");

        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(options, arguments);
        } catch (ParseException e) {
            System.err.println("Parsing command line failed. Reason: " + e.getMessage());
            new HelpFormatter().printHelp("RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
            System.exit(1);
        }
        if (cmd.getArgList().isEmpty()) {
            new HelpFormatter().printHelp("RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
            System.exit(1);
        }
        String format = cmd.getOptionValue("f");
        if (format != null) {
            if (!Arrays.asList("RIKEN_MSP", "NIST_MSP", "json", "jsonld").contains(format)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
                System.exit(1);
            }
        }
        return cmd;
    }
}
