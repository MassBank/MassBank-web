package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbibtex.BibTeXDatabase;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;

import de.undercouch.citeproc.BibliographyFileReader;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.output.Bibliography;


public class AddMetaData {
	private static final Logger logger = LogManager.getLogger(AddMetaData.class);
			
	public static Record validate(String recordstring) {
		// test non standard ASCII chars and print warnings
		for (int i = 0; i < recordstring.length(); i++) {
			if (recordstring.charAt(i) > 0x7F) {
				String[] tokens = recordstring.split("\\r?\\n");
				logger.warn("non standard ASCII character found. This might be an error. Please check carefully.");
				int line = 0, col = 0, offset = 0;
				for (String token : tokens) {
					offset = offset + token.length() + 1;
					if (i < offset) {
						col = i - (offset - (token.length() + 1));
						logger.warn(tokens[line]);
						StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", tokens[line].length()));
						error_at.setCharAt(col, '^');
						logger.warn(error_at);
						break;
					}
					line++;
				}
			}
		}
		
		Record record = new Record("");
		Parser recordparser = new RecordParser(record);
		Result res = recordparser.parse(recordstring);
		if (res.isFailure()) {
			logger.error(res.getMessage());
			int position = res.getPosition();
			String[] tokens = recordstring.split("\\n");

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
		
		// look up https://www.doi.org/ and https://crosscite.org/
		// curl -LH "Accept: text/x-bibliography; style=ieee-with-url" https://doi.org/<doi>
		// curl -LH "Accept: text/x-bibliography; style=ieee-with-url" https://doi.org/10.1038/sdata.2014.29
		// curl -LH "Accept: text/x-bibliography; style=american-chemical-society"  https://data.datacite.org/10.1038/sdata.2014.29
		String formated_citation=null;
		try {
			URL obj = new URL("https://www.doi.org/"+doi);
			URLConnection conn = obj.openConnection();
			conn.setRequestProperty("Accept", "application/x-bibtex");
			
			
//			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			String line;
//			StringBuilder sb=new StringBuilder();
//			while ((line = in.readLine()) != null) 
//				sb.append(line);
//			in.close();
//			String bibtex = sb.toString();
//			
//			System.out.println(bibtex);
			
			BibTeXDatabase db = new BibTeXConverter().loadDatabase(conn.getInputStream());
			BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
			provider.addDatabase(db);		
			CSL citeproc = new CSL(provider, "MassBank");
			citeproc.setOutputFormat("text");
			
			provider.registerCitationItems(citeproc);
			
			Bibliography bib=citeproc.makeBibliography();
			String out=bib.makeString();
			System.out.println(out);
			
			
			
//			conn.setRequestProperty("Accept", "application/x-bibtex");
//			BibliographyFileReader reader = new BibliographyFileReader();
//			ItemDataProvider prov = reader.readBibliographyFile(conn.getInputStream(), BibliographyFileReader.FileFormat.BIBTEX);
//			String id = prov.getIds()[0];
//			System.out.println(id);
//			CSLItemData item = prov.retrieveItem(id);
//			String bibl = CSL.makeAdhocBibliography("MassBank", "text", item).makeString();
			
//			CSL citeproc = new CSL(prov,"MassBank");
//			citeproc.setOutputFormat("text");
//			citeproc.registerCitationItems("[Huntscha_2014]");
//			Bibliography bib = citeproc.makeBibliography();

			
//			System.out.println("hallo1");
//			System.out.println(bibl);
//			System.out.println("hallo2");
//			formated_citation=bibl;
			
			//System.out.println(citeproc.makeCitation(prov.getIds()[0]));
			//citeproc.ma
			//BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//CSLItemData item = prov.retrieveItem(prov.getIds()[0]);
			//		CSLItemData.fromJson(new JsonParser(new JsonLexer(in)).parseObject());
			//in.close();
				     
			//String bibl = CSL.makeAdhocBibliography("MassBank", "text", item).makeString();
			//System.out.println(bibl);
			
//			formated_citation = json_string.toString();
//			
//			System.out.println(json.toString());
			//formated_citation=bibl;
			
			formated_citation="hallo";
			// check for doi in new citation string
			String fetched_doi=null;
			matcher_doi = pattern_doi.matcher(formated_citation);
			if (matcher_doi.matches()) {
				fetched_doi= formated_citation.substring(matcher_doi.start(1), matcher_doi.end(1));
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

	public static void main(String[] arguments) throws Exception {
		boolean doPub = false;
		Options options = new Options();
		options.addOption("a", "all", false, "execute all operations");
		options.addOption("p", "publication", false, "format PUBLICATION tag from given DOI to follow the guidelines of ACS");
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
		}
		
		if (cmd.hasOption("p")) {
			doPub = true;
		}

		if (!doPub) {
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
		
		String recordstring = null;
		try {
			recordstring = FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8);
		}
		catch(IOException exp) {
			System.err.println( "Reading file \""+ filename + "\" failed. Reason: " + exp.getMessage() );
			System.exit(1);
		}
		
		Record record = validate(recordstring);
		if (record == null) {
			System.err.println( "Validation of  \""+ filename + "\" failed. Exiting.");
			System.exit(1);
		}
		
		String recordstring2 = recordstring;
		if (doPub) recordstring2=doPub(record, recordstring2);
		
		if (!recordstring.equals(recordstring2)) {
			try {
				FileUtils.write(new File(filename), recordstring2, StandardCharsets.UTF_8);
			}
			catch(IOException exp) {
				System.err.println( "Reading file \""+ filename + "\" failed. Reason: " + exp.getMessage() );
				System.exit(1);
			}
		}

		
		
		
		System.exit(0);
	}
}
