package massbank;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
 * This class adds meta information automatically where feasible and makes some automatic fixes. Supported functions are:<p>
 * {@link doPub}<p>
 * {@link doName}<p>
 * {@link doLink}
 * 
 * @author rmeier
 * @version 27-06-2019
 */
public class AddMetaData {
	private static final Logger logger = LogManager.getLogger(AddMetaData.class);
	private static final String CHEMSPIDER_API_KEY = "";
	
	
	/**
	 * Try to fetch the PubChem CID for a given InChI-key using PUG REST. This function gets the first CID which has some SID associated.
	 * CIDs without SIDs are marked as "Non-Live".
	 */
	public static String getPubchemCID(String INCHIKEY) throws JSONException, MalformedURLException, IOException {
		JSONObject jo = new JSONObject(IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + INCHIKEY + "/sids/JSON"), Charset.forName("UTF-8")));
		logger.trace(jo.toString());
		JSONArray Information = jo.getJSONObject("InformationList").getJSONArray("Information");
		int len = Information.length();
		for (int i=0;i<len;i++) {
			if (Information.getJSONObject(i).has("SID")) {
				return Integer.toString(Information.getJSONObject(i).getInt("CID"));
			}
		}
		throw new JSONException("No CID found wich has some SIDs associated.");
	}
	
	/**
	 * Try to fetch the COMPTOX id for a given InChI-key.
	 */
	public static String getComptoxID(String INCHIKEY) throws JSONException, MalformedURLException, IOException {
		return new JSONObject(IOUtils.toString(new URL("https://actorws.epa.gov/actorws/chemIdentifier/v01/resolve.json?identifier=" + INCHIKEY),
				Charset.forName("UTF-8"))).getJSONObject("DataRow").getString("dtxsid");
	}
	
	/**
	 * Try to fetch the CHEMSPIDER id for a given InChI-key.
	 */
	public static String getChemspiderID(String INCHIKEY) throws MalformedURLException, IOException, JSONException {
		HttpURLConnection connection = (HttpURLConnection) new URL("https://api.rsc.org/compounds/v1/filter/inchikey").openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "");
		connection.setRequestProperty("apikey", CHEMSPIDER_API_KEY);
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
		writer.write("{\"inchikey\": \"" + INCHIKEY + "\" }");
		writer.close();
		String queryID = new JSONObject(IOUtils.toString(connection.getInputStream(), Charset.forName("UTF-8"))).getString("queryId");
		connection = (HttpURLConnection) new URL("https://api.rsc.org/compounds/v1/filter/" + queryID + "/results").openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "");
		connection.setRequestProperty("apikey", CHEMSPIDER_API_KEY);
		return Integer.toString(new JSONObject(IOUtils.toString(connection.getInputStream(), Charset.forName("UTF-8"))).getJSONArray("results").getInt(0));
	}
	
	/**
	 * Automatically format a PUBLICATION tag according to ACS rules if a DOI could be identified.
	 */
	public static String doPub(Record record, String recordstring) {
		String publication = record.PUBLICATION();
		if (publication == null) return recordstring;
		
		String regex_doi = "10\\.\\d{3,9}\\/[\\-\\._;\\(\\)\\/<>:a-zA-Z0-9]+[a-zA-Z0-9]";
		Pattern pattern_doi = Pattern.compile(".*" + "(" + regex_doi + ")" + ".*");
		Matcher matcher_doi = pattern_doi.matcher(publication);

		String doi=null;
		if (matcher_doi.matches()) {
			doi = publication.substring(matcher_doi.start(1), matcher_doi.end(1));
		}
		if (doi == null) return recordstring;
		
		// look up https://www.doi.org/
		// https://crosscite.org/
		// curl -LH "Accept: application/x-bibtex" https://doi.org/<doi>
		String formated_citation=null;
		try {
			// URL encode the doi
			String EncDoi=URLEncoder.encode(doi, StandardCharsets.UTF_8.toString());
			URLConnection conn = new URL("https://www.doi.org/"+EncDoi).openConnection();
			conn.setRequestProperty("Accept", "application/x-bibtex");
			BibTeXItemDataProvider p = new BibTeXItemDataProvider();
			// feed the bibtex string into CSL
			p.addDatabase(new BibTeXConverter().loadDatabase(conn.getInputStream()));
			CSL citeproc = new CSL(p, "MassBank");
			citeproc.setOutputFormat("text");
			citeproc.registerCitationItems(p.getIds());
			formated_citation=citeproc.makeBibliography().makeString().replace("\n", "");
			citeproc.close();
			// call twice because of bug https://github.com/michel-kraemer/citeproc-java/issues/53
			// formated_citation=citeproc.makeBibliography().makeString().replace("\n", "");
			// remove some formating characters
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
		
		
		// add InChI-Key if missing
		boolean hasInchiKey=false;
		String INCHIKEY = null;
		for (Pair<String, String> link : record.CH_LINK()) {
			if ("INCHIKEY".equals(link.getKey())) {
				hasInchiKey=true;
				INCHIKEY=link.getValue();
			}
		}
		if (!hasInchiKey) {
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
					else {
						List<Pair<String, String>>ch_link = record.CH_LINK();
						ch_link.add(Pair.of("INCHIKEY",inchiGen.getInchiKey()));
						record.CH_LINK(ch_link);
						INCHIKEY=inchiGen.getInchiKey();
						hasInchiKey=true;
					}					
				} catch (CDKException e) {
					logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Error: \""+ e.getMessage() + "\" for \"" + ch_iupac + "\".");
				}		 			
			}
		}
		
		// add database identifier
		if (hasInchiKey) {
			
			// add PUBCHEM cid if missing
			List<String> PUBCHEM = new ArrayList<String>();
			for (Pair<String, String> link : record.CH_LINK()) {
				if ("PUBCHEM".equals(link.getKey())) {
					PUBCHEM.add(link.getValue());
					break;
				}
			}
			if (PUBCHEM.isEmpty()) {
				try {
					String PUBCHEMCID=getPubchemCID(INCHIKEY);
					List<Pair<String, String>>ch_link = record.CH_LINK();
					ch_link.add(Pair.of("PUBCHEM", "CID:"+PUBCHEMCID));
					record.CH_LINK(ch_link);
				} catch (JSONException | IOException e) {
					logger.warn("Could not fetch PUBCHEM cid.");
					logger.trace(e.getMessage());
				}
			}
			else {
				try {
					String PUBCHEMCID=getPubchemCID(INCHIKEY);;
					if (!("CID:"+PUBCHEMCID).equals(PUBCHEM.get(0))) {
						logger.error("Wrong PUBCHEM database identifier in record file.");
					}
				} catch (JSONException | IOException e) {
					logger.warn("Could not fetch PUBCHEM id for comparision.");
					logger.trace(e.getMessage());
				}
			}
			
			// add COMPTOX if missing
			List<String> COMPTOX = new ArrayList<String>();
			for (Pair<String, String> link : record.CH_LINK()) {
				if ("COMPTOX".equals(link.getKey())) {
					COMPTOX.add(link.getValue());
					break;
				}
			}
			if (COMPTOX.isEmpty()) {
				try {
					String COMPTOXID=getComptoxID(INCHIKEY);
					List<Pair<String, String>>ch_link = record.CH_LINK();
					ch_link.add(Pair.of("COMPTOX", COMPTOXID));
					record.CH_LINK(ch_link);
				} catch (JSONException | IOException e) {
					logger.warn("Could not fetch COMPTOX id.");
					logger.trace(e.getMessage());
				}
			}
			else {
				try {
					String COMPTOXID=getComptoxID(INCHIKEY);
					if (!COMPTOXID.equals(COMPTOX.get(0))) {
						logger.error("Wrong COMPTOX database identifier in record file.");
					}
				} catch (JSONException | IOException e) {
					logger.warn("Could not fetch COMPTOX id for comparision.");
					logger.trace(e.getMessage());
				}
			}
			
			// add Chemspider
			List<String> CHEMSPIDER = new ArrayList<String>();
			for (Pair<String, String> link : record.CH_LINK()) {
				if ("CHEMSPIDER".equals(link.getKey())) {
					CHEMSPIDER.add(link.getValue());
					break;
				}
			}
			if (CHEMSPIDER.isEmpty()) {
				try {
					String CHEMSPIDERID=getChemspiderID(INCHIKEY);
					List<Pair<String, String>>ch_link = record.CH_LINK();
					ch_link.add(Pair.of("CHEMSPIDER", CHEMSPIDERID));
					record.CH_LINK(ch_link);
				} catch (JSONException | IOException e) {
					logger.warn("Could not fetch CHEMSPIDER id.");
					logger.trace(e.getMessage());
				}
			}
			else {
				try {
					String CHEMSPIDERID=getChemspiderID(INCHIKEY);
					if (!CHEMSPIDERID.equals(CHEMSPIDER.get(0))) {
						logger.error("Wrong CHEMSPIDER database identifier in record file.");
					}
				} catch (JSONException | IOException e) {
					logger.warn("Could not fetch CHEMSPIDER id for comparision.");
					logger.trace(e.getMessage());
				}
			}
		}
		
		StringBuilder sb=new StringBuilder();
		for (Pair<String, String> link : record.CH_LINK()) {
			sb.append("CH$LINK: " + link.getKey() + " " + link.getValue() + "\n");
		}
		recordstring=recordstring.substring(0, ch_linkPos) + sb.toString() + recordstring.substring(ch_linkPos, recordstring.length());

		return recordstring;
	}
	
	/**
	 * Automatically adjust MS$FOCUSED_ION: ION_TYPE and MS$FOCUSED_ION: PRECURSOR_TYPE
	 */
	public static String doFocusedIon(Record record, String recordstring) {
		// if record is MS2 and MS$FOCUSED_ION: ION_TYPE == MS$FOCUSED_ION: PRECURSOR_TYPE
		// remove MS$FOCUSED_ION: ION_TYPE
		if (!record.AC_MASS_SPECTROMETRY_MS_TYPE().equals("MS2")) return recordstring;
		
		Map<String, String> ms_focused_ion = new HashMap<String, String>();
		for (Pair<String, String> pair : record.MS_FOCUSED_ION()) {
			ms_focused_ion.put(pair.getKey(), pair.getValue());			
		}
		if (ms_focused_ion.get("ION_TYPE") == null) return recordstring;
		
		if (ms_focused_ion.get("ION_TYPE").equals(ms_focused_ion.get("PRECURSOR_TYPE"))) {
			recordstring=recordstring.replaceAll("MS\\$FOCUSED_ION: ION_TYPE .*\n", "");
		}
		
		return recordstring;
	}

	public static void main(String[] arguments) throws Exception {
		Options options = new Options();
		options.addOption("a", "all", false, "execute all operations");
		options.addOption("p", "publication", false, "format PUBLICATION tag from given DOI to follow the guidelines of ACS");
		options.addOption("n", "name", false, "fix common problems in CH$NAME tag");
		options.addOption("l", "link", false, "add links to CH$LINK tag");
		options.addOption("r", "rewrite", false, "read and rewrite the file.");
		options.addOption("ms_focused_ion", false, "Inspect MS$FOCUSED_ION");
		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse( options, arguments);
		}
		catch(ParseException exp) {
	        // oops, something went wrong
	        System.err.println( "Parsing command line failed. Reason: " + exp.getMessage() );
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
		
		if (cmd.getOptions().length == 0) {
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
		else if (record.DEPRECATED()) {
			System.exit(0);
		}
		
		String recordstring2 = recordstring;
		if (cmd.hasOption("p") || cmd.hasOption("a")) recordstring2=doPub(record, recordstring2);
		if (cmd.hasOption("n") || cmd.hasOption("a")) recordstring2=doName(record, recordstring2);
		if (cmd.hasOption("l") || cmd.hasOption("a")) recordstring2=doLink(record, recordstring2);
		if (cmd.hasOption("ms_focused_ion") || cmd.hasOption("a")) recordstring2=doFocusedIon(record, recordstring2);
		
		if (cmd.hasOption("r")) recordstring2=record.toString();
		
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
