package massbank.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;

import massbank.Record;

/**
 * This class converts a record file to a html file for inspection.
 * 
 * @author rmeier
 * @version 02-12-2022
 */
public class Inspector {
	private static final Logger logger = LogManager.getLogger(Inspector.class);

	static String getResourceFileAsString(String fileName) throws IOException {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		try (InputStream is = classLoader.getResourceAsStream(fileName)) {
			if (is == null)
				return null;
			try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
				return reader.lines().collect(Collectors.joining(System.lineSeparator()));
			}
		}
	}

	public static void main(String[] arguments) throws Exception {
		// load version and print
		final Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Inspector version: " + properties.getProperty("version"));

		// parse command line
		Options options = new Options();
		options.addOption(null, "only-jsonld", false, "only output structured data in JSON+LD format to file.");
		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse(options, arguments);
		} catch (ParseException e) {
			// oops, something went wrong
			System.err.println("Parsing command line failed. Reason: " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Inspector [OPTIONS] <INPUT FILE> <OUTPUT FILE>", options);
			System.exit(1);
		}
		if (cmd.getArgList().size() != 2) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Inspector [OPTIONS] <INPUT FILE> <OUTPUT FILE>", options);
			System.exit(1);
		}

		String input = FileUtils.readFileToString(new File(cmd.getArgList().get(0)), StandardCharsets.UTF_8);
		Validator.hasNonStandardChars(input);
		Set<String> config = new HashSet<String>();
		config.add("legacy");
		Record record = Validator.validate(input, config);
		if (record == null) {
			logger.error("Error in " + arguments[0] + ". Exiting...");
			System.exit(1);
		} else {
			logger.trace("Validation passed for " + arguments[0] + ".");
		}

		StringBuilder sb = new StringBuilder();
		if (cmd.hasOption("only-jsonld")) {
			sb.append(record.createStructuredData());
		} else {
			String accession = record.ACCESSION();
			String shortname = record.RECORD_TITLE().get(0) + " Mass Spectrum";
			// find InChIKey in CH_LINK
			String inchikey = record.CH_LINK().get("INCHIKEY");
			String keywords = accession + ", " + shortname + ", " + (inchikey != null ? inchikey + ", " : "")
					+ "mass spectrum, MassBank record, mass spectrometry, mass spectral library";
			String description = "This MassBank Record with Accession " + accession + " contains the "
					+ record.AC_MASS_SPECTROMETRY_MS_TYPE() + " mass spectrum" + " of '" + record.RECORD_TITLE().get(0)
					+ "'" + (inchikey != null ? " with the InChIKey '" + inchikey + "'" : "") + ".";
			String recordstring = record.createRecordString();
			String structureddata = record.createStructuredData();
			IAtomContainer mol = record.CH_SMILES_obj();
			String svg = new DepictionGenerator().withAtomColors().depict(mol).toSvgStr(Depiction.UNITS_PX);

			String css = "<style>\n" + getResourceFileAsString("massbank.css") + "\n"
					+ getResourceFileAsString("Common.css") + "\n" + getResourceFileAsString("st.css") + "\n"
					+ "</style>\n";

			String js = "<script type=\"text/javascript\">\n" + getResourceFileAsString("Common.js") + "\n"
					+ getResourceFileAsString("d3.v3.min.js") + "\n" + getResourceFileAsString("st.min.js") + "\n"
					+ getResourceFileAsString("massbank_specktackle.js") + "\n" + "</script>\n";

			sb.append("<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "	<title>" + shortname + "</title>\n"
					+ "	<meta charset=\"UTF-8\">\n"
					+ "	<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">\n" +
//			"	<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">\n" + 
//			"	<link rel=\"stylesheet\" href=\"https://www.w3schools.com/lib/w3-theme-grey.css\">\n" + 
					"	<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js\"></script>\n" +

					"	<!-- 	hier anpassen -->\n"
					+ "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
					+ "	<meta name=\"variableMeasured\" content=\"m/z\">\n"
					+ "<script type=\"application/ld+json\">\n"
					+ structureddata + "\n"
					+ "</script>\n"
					+ css + "\n" + "</head>\n");

			sb.append("<body class=\"w3-theme-gradient\">\n" + js + "\n"
					+ "  	<header class=\"w3-container w3-top w3-text-dark-grey w3-grey\">\n"
					+ "		<div class=\"w3-bar\">\n" + "			<div class=\"w3-left\">\n" + "				<h1>\n"
					+ "					<b>MassBank Record: " + accession + "</b>\n" + "				</h1>\n"
					+ "			</div>\n" + "	<div class=\"w3-padding\">\n" + "		<h3><b>"
					+ record.RECORD_TITLE1() + "</b></h3>\n" + "			<div class=\"w3-row w3-padding-small\">\n"
					+ "				<div class=\"w3-twothird w3-text-grey w3-small w3-padding-small\">\n"
					+ "					Mass Spectrum\n" + "					<div id=\"spectrum_canvas\" peaks=\""
					+ record.createPeakListForSpectrumViewer()
					+ "\" style=\"height:200px; width:600px; max-width:100%; background-color:white\"></div>\n"
					+ "				</div>\n"
					+ "				<div class=\"w3-third w3-text-grey w3-small w3-padding-small\">\n"
					+ "					Chemical Structure<br>\n" + svg + "\n" + "				</div>\n"
					+ "			</div>\n" + "	</div>\n" +

					"	<div class=\"monospace w3-padding w3-small\" style=\"height:auto;margin:auto\">\n"
					+ "		<hr>\n" + recordstring + "\n" + "	</div>\n" + "</body>\n" + "</html>");
		}

		File file = new File(cmd.getArgList().get(1));
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(sb.toString());
		}

	}
}
