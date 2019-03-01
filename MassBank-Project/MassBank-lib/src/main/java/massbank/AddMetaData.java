package massbank;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;

/**
 * This class adds meta information automatically where feasible. Supported functions are:<p>
 * {@link doPub}<p>
 * {@link doName}<p>
 * 
 * @author rmeier
 * @version 01-03-2019
 */
public class AddMetaData {
	private static final Logger logger = LogManager.getLogger(AddMetaData.class);
	
	/**
	 * Automatically format a PUBLICATION tag according to ACS rules if a DOI could be identified.
	 */
	public static String doPub(Record record, String recordstring) {
		String publication = record.PUBLICATION();
		if (publication == null) return recordstring;
		
		String regex_doi = "10\\.\\d{3,9}\\/[\\-\\._;\\(\\)\\/:a-zA-Z0-9]+[a-zA-Z0-9]";
		Pattern pattern_doi = Pattern.compile(".*" + "(" + regex_doi + ")" + ".*");
		Matcher matcher_doi = pattern_doi.matcher(publication);

		String doi=null;
		if (matcher_doi.matches()) {
			doi = publication.substring(matcher_doi.start(1), matcher_doi.end(1));
		}
		if (doi == null) return recordstring;
		
		// look up https://www.doi.org/
		// curl -LH "Accept: application/x-bibtex" https://doi.org/<doi>
		String formated_citation=null;
		try {
			URL obj = new URL("https://www.doi.org/"+doi);
			URLConnection conn = obj.openConnection();
			conn.setRequestProperty("Accept", "application/x-bibtex");
			BibTeXItemDataProvider p = new BibTeXItemDataProvider();
			p.addDatabase(new BibTeXConverter().loadDatabase(conn.getInputStream()));
			CSL citeproc = new CSL(p, "MassBank");
			citeproc.setOutputFormat("text");
			citeproc.registerCitationItems(p.getIds());
			formated_citation=citeproc.makeBibliography().makeString().replace("\n", "");
			// call twice because of bug https://github.com/michel-kraemer/citeproc-java/issues/53
			formated_citation=citeproc.makeBibliography().makeString().replace("\n", "");
			formated_citation=formated_citation.replaceAll("\\$\\\\less\\$I\\$\\\\greater\\$", "");
			formated_citation=formated_citation.replaceAll("\\$\\\\less\\$/I\\$\\\\greater\\$", "");

			String fetched_doi=null;
			matcher_doi = pattern_doi.matcher(formated_citation);
			
			if (matcher_doi.matches()) {
				fetched_doi=formated_citation.substring(matcher_doi.start(1), matcher_doi.end(1));
			}
			if (!doi.equals(fetched_doi)) {
				System.err.println("doi mismatch in fetched formated citation:");
				System.err.println("Original: "+publication);
				System.err.println("Fetched: "+formated_citation);
			} else {
				recordstring=recordstring.replace(publication, formated_citation);
			}
		} catch (Exception exp) {
			System.err.println( "Fetching formated citation failed. Reason: " + exp.getMessage() );
			exp.printStackTrace();
		}
		return recordstring;
	}
	
	/**
	 * Automatically remove duplicate names.
	 */
	public static String doName(Record record, String recordstring) {
		List<String> ch_name = record.CH_NAME();
		Set<String> duplicates = new LinkedHashSet<String>();
		Set<String> uniques = new HashSet<String>();
		for(String c : ch_name) {
			if(!uniques.add(c)) {
				duplicates.add(c);
			}
		}
		if (duplicates.size()>0) {
			for (String d : duplicates) {
				// find first occurrence 
				String fullDup = "CH$NAME: " + d + "\n";
				int index = recordstring.indexOf(fullDup)+fullDup.length();
				String begining = recordstring.substring(0, index);
				String end = recordstring.substring(index, recordstring.length()).replace(fullDup, "");
				recordstring = begining + end;
			}
		}
		return recordstring;
	}

	public static void main(String[] arguments) throws Exception {
		boolean doPub = false;
		boolean doName = false;
		Options options = new Options();
		options.addOption("a", "all", false, "execute all operations");
		options.addOption("p", "publication", false, "format PUBLICATION tag from given DOI to follow the guidelines of ACS");
		options.addOption("n", "name", false, "fix common problems in CH$NAME tag");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, arguments);
		}
		catch(ParseException exp) {
	        // oops, something went wrong
	        System.err.println( "Parsing command line failed. Reason: " + exp.getMessage() );
	        System.exit(1);
	    }
		
		if (cmd.hasOption("a")) {
			// set all to true
			doPub = true;
			doName = true;
		}
		
		if (cmd.hasOption("p")) {
			doPub = true;
		}
		
		if (cmd.hasOption("n")) {
			doName = true;
		}

		if (!(doPub || doName) ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("AddMetaData [OPTIONS] <FILE>", options);
			System.exit(1);
		}
		
		String filename = null;
		if (cmd.getArgList().size() == 1) filename=cmd.getArgList().get(0);
		else {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("AddMetaData [OPTIONS] <FILE>", options);
			System.exit(1);
		}
		
		System.out.println("Formatting: \""+filename+"\"");
		String recordstring = null;
		try {
			recordstring = FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8);
		}
		catch(IOException exp) {
			System.err.println( "Reading file \""+ filename + "\" failed. Reason: " + exp.getMessage() );
			System.exit(1);
		}
		
		// read record in less strict mode
		Record record = Validator.validate(recordstring, "", false);
		if (record == null) {
			System.err.println( "Validation of  \""+ filename + "\" failed. Exiting.");
			System.exit(1);
		}
		
		String recordstring2 = recordstring;
		if (doPub) recordstring2=doPub(record, recordstring2);
		if (doName) recordstring2=doName(record, recordstring2);
		
		if (!recordstring.equals(recordstring2)) {
			Record record2 = Validator.validate(recordstring2, "");
			if (record2 == null) {
				System.err.println( "Validation of new created record file failed. Exiting.");
				System.exit(1);
			}
			try {
				FileUtils.write(new File(filename), recordstring2, StandardCharsets.UTF_8);
			}
			catch(IOException exp) {
				System.err.println( "Writing file \""+ filename + "\" failed. Reason: " + exp.getMessage() );
				System.exit(1);
			}
		}

		
		
		
		System.exit(0);
	}
}
