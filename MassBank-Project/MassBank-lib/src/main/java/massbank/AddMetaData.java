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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import net.sf.jniinchi.INCHI_RET;

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
				logger.error("doi mismatch in fetched formated citation:");
				logger.error("Original: "+publication);
				logger.error("Fetched: "+formated_citation);
			} else {
				recordstring=recordstring.replace(publication, formated_citation);
			}
		} catch (Exception exp) {
			logger.error("Fetching formated citation failed. Reason: " + exp.getMessage() );
			exp.printStackTrace();
		}
		return recordstring;
	}
	
	/**
	 * Automatically remove duplicate and InChiKey from CH$NAME
	 */
	public static String doName(Record record, String recordstring) {
		// find and remove duplicates
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
		// find and remove InChiKeys
		recordstring=recordstring.replaceAll("CH\\$NAME: [A-Z]{14,14}-[A-Z]{10,10}-[A-Z]{1,1}\n", "");
		return recordstring;
	}
	
	/**
	 * Automatically process CH$LINK section.
	 */
	// https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/AADVZSXPNRLYLVadasd/JSON
	public static String doLink(Record record, String recordstring) {
		Pattern pattern = Pattern.compile("CH\\$IUPAC: .*\n");
		Matcher matcher = pattern.matcher(recordstring);
		int ch_linkPos = -1;
		if (!matcher.find()) {
			logger.error("Could not find end of \"CH$IUPAC\" line.");
			return recordstring;
		} else {
			ch_linkPos = matcher.end();
		}
		recordstring=recordstring.replaceAll("CH\\$LINK: .*\n", "");
		
		StringBuilder sb=new StringBuilder();
		boolean hasInchi=false;
		for (Pair<String, String> link : record.CH_LINK()) {
			if ("INCHIKEY".equals(link.getKey())) {
				hasInchi=true;
			}
			sb.append("CH$LINK: " + link.getKey() + " " + link.getValue() + "\n");
		}
		if (!hasInchi) {
			String ch_iupac = record.CH_IUPAC();
			if (!"N/A".equals(ch_iupac)) {
				try {
					// Get InChIToStructure
					InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(ch_iupac, DefaultChemObjectBuilder.getInstance());
					INCHI_RET ret = intostruct.getReturnStatus();
					if (ret == INCHI_RET.WARNING) {
						// Structure generated, but with warning message
						logger.warn("InChI warning: " + intostruct.getMessage());
						logger.warn(record.ACCESSION());
					} 
					else if (ret != INCHI_RET.OKAY) {
						// Structure generation failed
						logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed: " + ret.toString() + " [" + intostruct.getMessage() + "] for " + ch_iupac + ".");
					}
					// Structure generation succeeded
					IAtomContainer m = intostruct.getAtomContainer();
					// prepare an InChIGenerator
					InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(m);
					ret = inchiGen.getReturnStatus();
					if (ret == INCHI_RET.WARNING) {
						// InChI generated, but with warning message
						logger.warn("InChI warning: " + inchiGen.getMessage());
						logger.warn(record.ACCESSION());
					} else if (ret != INCHI_RET.OKAY) {
						// InChI generation failed
						logger.error("Can not create InChiKey from InChI string in \"CH$IUPAC\" field. Error: " + ret.toString() + " [" + inchiGen.getMessage() + "] for " + ch_iupac + ".");
					}
					sb.append("CH$LINK: INCHIKEY " + inchiGen.getInchiKey() + "\n");
					List<Pair<String, String>>ch_link = record.CH_LINK();
					ch_link.add(Pair.of("INCHIKEY",inchiGen.getInchiKey()));
					record.CH_LINK(ch_link);
				} catch (CDKException e) {
					logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Error: \""+ e.getMessage() + "\" for \"" + ch_iupac + "\".");
				}		 			
			}
		}
		recordstring=recordstring.substring(0, ch_linkPos) + sb.toString() + recordstring.substring(ch_linkPos, recordstring.length());

		return recordstring;
	}
	

	public static void main(String[] arguments) throws Exception {
		boolean doPub = false;
		boolean doName = false;
		boolean doLink = false;
		Options options = new Options();
		options.addOption("a", "all", false, "execute all operations");
		options.addOption("p", "publication", false, "format PUBLICATION tag from given DOI to follow the guidelines of ACS");
		options.addOption("n", "name", false, "fix common problems in CH$NAME tag");
		options.addOption("l", "link", false, "add links to CH$LINK tag");
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
			doLink = true;
		}
		
		if (cmd.hasOption("p")) {
			doPub = true;
		}
		
		if (cmd.hasOption("n")) {
			doName = true;
		}

		if (cmd.hasOption("l")) {
			doLink = true;
		}
		
		if (!(doPub || doName || doLink) ) {
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
		if (doLink) recordstring2=doLink(record, recordstring2);
		
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
