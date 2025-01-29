package massbank.cli;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import massbank.ProjectPropertiesLoader;
import massbank.RecordParser;
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
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import io.github.dan2097.jnainchi.InchiStatus;
import massbank.Record;

import java.util.stream.Collectors;

import static massbank.cli.Validator.parseRecord;

/**
 * This class adds meta information automatically where feasible and makes some automatic fixes. Supported functions are:<p>
 *
 * @author rmeier
 * @version 29-01-2025
 * @version 29-01-2025
 */
public class AddMetaData {
	private static final Logger logger = LogManager.getLogger(AddMetaData.class);
	private static final String CHEMSPIDER_API_KEY = "";
	
	
	/**
	 * Try to fetch the COMPTOX id for a given InChI-key.
	 */
	public static String getComptoxID(String INCHIKEY) throws JsonSyntaxException, MalformedURLException, IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.fromJson(IOUtils.toString(new URL("https://actorws.epa.gov/actorws/chemIdentifier/v01/resolve.json?identifier=" + INCHIKEY),
            StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("DataRow").getAsJsonPrimitive("dtxsid").getAsString();
	}
	
	/**
	 * Try to fetch the CHEMSPIDER id for a given InChI-key.
	 */
	public static String getChemspiderID(String INCHIKEY) throws JsonSyntaxException, MalformedURLException, IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		HttpURLConnection connection = (HttpURLConnection) new URL("https://api.rsc.org/compounds/v1/filter/inchikey").openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "");
		connection.setRequestProperty("apikey", CHEMSPIDER_API_KEY);
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
		writer.write("{\"inchikey\": \"" + INCHIKEY + "\" }");
		writer.close();
		String queryID = gson.fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), JsonObject.class).getAsJsonPrimitive("queryId").getAsString();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "");
		connection.setRequestProperty("apikey", CHEMSPIDER_API_KEY);
		return gson.fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), JsonObject.class).getAsJsonArray("results").get(0).getAsString();
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
			String EncDoi=URLEncoder.encode(doi, StandardCharsets.UTF_8);
			URLConnection conn = new URL("https://www.doi.org/"+EncDoi).openConnection();
			conn.setRequestProperty("Accept", "application/x-bibtex");
			BibTeXItemDataProvider p = new BibTeXItemDataProvider();
			// feed the bibtex string into CSL
			p.addDatabase(new BibTeXConverter().loadDatabase(conn.getInputStream()));
			CSL citeproc = new CSL(p, "MassBank");
			citeproc.setOutputFormat("text");
			citeproc.registerCitationItems(p.getIds());
			formated_citation=citeproc.makeBibliography().makeString().replace("\n", "");
			//citeproc.close();
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
		if (!duplicates.isEmpty()) {
			for (String d : duplicates) {
				// find first occurrence 
				String fullDup = "CH$NAME: " + d + "\n";
				int index = recordstring.indexOf(fullDup)+fullDup.length();
				String begining = recordstring.substring(0, index);
				String end = recordstring.substring(index).replace(fullDup, "");
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
	public static String doLink(Record record) {
		// add InChI-Key if missing
		boolean hasInchiKey=record.CH_LINK().containsKey("INCHIKEY");
		String INCHIKEY = record.CH_LINK().get("INCHIKEY");

		
		if (!hasInchiKey) {
			String ch_iupac = record.CH_IUPAC();
			if (!"N/A".equals(ch_iupac)) {
				try {
					// Get InChIToStructure
					InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(ch_iupac, SilentChemObjectBuilder.getInstance());
					InchiStatus ret = intostruct.getStatus();
					if (ret == InchiStatus.WARNING) {
						// Structure generated, but with warning message
						logger.warn("InChI warning: " + intostruct.getMessage());
						logger.warn(record.ACCESSION());
					} 
					else if (ret == InchiStatus.ERROR) {
						// Structure generation failed
						logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed: " + intostruct.getMessage() + " for " + ch_iupac + ".");
					}
					// Structure generation succeeded
					IAtomContainer m = intostruct.getAtomContainer();
					// prepare an InChIGenerator
					InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(m);
					ret = inchiGen.getStatus();
					if (ret == InchiStatus.WARNING) {
						// InChI generated, but with warning message
						logger.warn("InChI warning: " + inchiGen.getMessage());
						logger.warn(record.ACCESSION());
					} else if (ret == InchiStatus.ERROR) {
						// InChI generation failed
						logger.error("Can not create InChiKey from InChI string in \"CH$IUPAC\" field. Error: " + inchiGen.getMessage() + " for " + ch_iupac + ".");
					}
					else {
						LinkedHashMap<String, String> ch_link = record.CH_LINK();
						ch_link.put("INCHIKEY",inchiGen.getInchiKey());
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
			// add COMPTOX if missing
			if (!record.CH_LINK().containsKey("COMPTOX")) {
				try {
					String COMPTOXID=getComptoxID(INCHIKEY);
					LinkedHashMap<String, String> ch_link = record.CH_LINK();
					ch_link.put("COMPTOX", COMPTOXID);
					record.CH_LINK(ch_link);
				} catch (JsonSyntaxException | IOException e) {
					logger.warn("Could not fetch COMPTOX id.");
					logger.trace(e.getMessage());
				}
			}
			else {
				try {
					String COMPTOXID=getComptoxID(INCHIKEY);
					if (!COMPTOXID.equals(record.CH_LINK().get("COMPTOX"))) {
						logger.error("Wrong COMPTOX database identifier in record file.");
					}
				} catch (JsonSyntaxException | IOException e) {
					logger.warn("Could not fetch COMPTOX id for comparision.");
					logger.trace(e.getMessage());
				}
			}
			
			// add Chemspider if missing
			if (!record.CH_LINK().containsKey("CHEMSPIDER")) {
				try {
					String CHEMSPIDERID=getChemspiderID(INCHIKEY);
					LinkedHashMap<String, String> ch_link = record.CH_LINK();
					ch_link.put("CHEMSPIDER", CHEMSPIDERID);
					record.CH_LINK(ch_link);
				} catch (JsonSyntaxException | IOException e) {
					logger.warn("Could not fetch CHEMSPIDER id.");
					logger.trace(e.getMessage());
				}
			}
			else {
				try {
					String CHEMSPIDERID=getChemspiderID(INCHIKEY);
					if (!CHEMSPIDERID.equals(record.CH_LINK().get("CHEMSPIDER"))) {
						logger.error("Wrong CHEMSPIDER database identifier in record file.");
					}
				} catch (JsonSyntaxException | IOException e) {
					logger.warn("Could not fetch CHEMSPIDER id for comparision.");
					logger.trace(e.getMessage());
				}
			}
		}
		
		return record.toString();
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
	
	/**
	 * Automatically add CH$LINK: INCHIKEY
	 * 	if no CH$IUPAC entry is available - do nothing
	 *  if CH$IUPAC entry is available and no CH$LINK: INCHIKEY - add InChIKey
	 *  if CH$IUPAC entry is available and CH$LINK: INCHIKEY - correct InChIKey
	 */
	public static String doAddInchikey(Record record) {
		String ch_iupac = record.CH_IUPAC();
		
		if ("N/A".equals(ch_iupac)) return record.toString();
		
		try {
			// Get InChIToStructure
			InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(ch_iupac, SilentChemObjectBuilder.getInstance());
			InchiStatus ret = intostruct.getStatus();
			if (ret == InchiStatus.WARNING) {
				// Structure generated, but with warning message
				logger.warn("InChI warning: " + intostruct.getMessage());
				logger.warn(record.ACCESSION());
			} 
			else if (ret == InchiStatus.ERROR) {
				// Structure generation failed
				logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed: " + intostruct.getMessage() + " for " + ch_iupac + ".");
				return record.toString();
			}
			// Structure generation succeeded
			IAtomContainer m = intostruct.getAtomContainer();
			// prepare an InChIGenerator
			InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(m);
			ret = inchiGen.getStatus();
			if (ret == InchiStatus.WARNING) {
				// InChI generated, but with warning message
				logger.warn("InChI warning: " + inchiGen.getMessage());
				logger.warn(record.ACCESSION());
			} else if (ret == InchiStatus.ERROR) {
				// InChI generation failed
				logger.error("Can not create InChiKey from InChI string in \"CH$IUPAC\" field. Error: " + inchiGen.getMessage() + " for " + ch_iupac + ".");
				return record.toString();
			}
			
			String INCHIKEY = inchiGen.getInchiKey();
			if (!record.CH_LINK().containsKey("INCHIKEY")) {
				LinkedHashMap<String, String> ch_link = record.CH_LINK();
				ch_link.put("INCHIKEY", INCHIKEY);
				//sort
				ch_link=ch_link.entrySet().stream()
						.sorted(Entry.comparingByKey())
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, LinkedHashMap::new));
				record.CH_LINK(ch_link);
			}
			else {
				if (!INCHIKEY.equals(record.CH_LINK().get("INCHIKEY"))) {
					logger.error("Wrong INCHIKEY identifier in record file.");
				}
			}
		} catch (CDKException e) {
			logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Error: \""+ e.getMessage() + "\" for \"" + ch_iupac + "\".");
		}		 			
		return record.toString();
	}
	
	public static String doAddPubchemCID(Record record) {
		String ch_iupac = record.CH_IUPAC();
		
		if ("N/A".equals(ch_iupac)) return record.toString();
		
		try {
			// Get InChIToStructure
			InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(ch_iupac, SilentChemObjectBuilder.getInstance());
			InchiStatus ret = intostruct.getStatus();
			if (ret == InchiStatus.WARNING) {
				// Structure generated, but with warning message
				logger.warn("InChI warning: " + intostruct.getMessage());
				logger.warn(record.ACCESSION());
			} 
			else if (ret == InchiStatus.ERROR) {
				// Structure generation failed
				logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed: " + intostruct.getMessage() + " for " + ch_iupac + ".");
				return record.toString();
			}
			// Structure generation succeeded
			IAtomContainer m = intostruct.getAtomContainer();
			// prepare an InChIGenerator
			InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(m);
			ret = inchiGen.getStatus();
			if (ret == InchiStatus.WARNING) {
				// InChI generated, but with warning message
				logger.warn("InChI warning: " + inchiGen.getMessage());
				logger.warn(record.ACCESSION());
			} else if (ret == InchiStatus.ERROR) {
				// InChI generation failed
				logger.error("Can not create InChiKey from InChI string in \"CH$IUPAC\" field. Error: " + inchiGen.getMessage() + " for " + ch_iupac + ".");
				return record.toString();
			}
			
			String INCHIKEY = inchiGen.getInchiKey();
			if (!record.CH_LINK().containsKey("INCHIKEY")) {
				LinkedHashMap<String, String> ch_link = record.CH_LINK();
				ch_link.put("INCHIKEY", INCHIKEY);
				//sort
				ch_link=ch_link.entrySet().stream()
						.sorted(Entry.comparingByKey())
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, LinkedHashMap::new));
				record.CH_LINK(ch_link);
			}
			else {
				if (!INCHIKEY.equals(record.CH_LINK().get("INCHIKEY"))) {
					logger.error("Wrong INCHIKEY identifier in record file.");
				}
			}
		} catch (CDKException e) {
			logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Error: \""+ e.getMessage() + "\" for \"" + ch_iupac + "\".");
		}		 			
		return record.toString();
	}
	
	public static String doSetSMILESfromInChi(Record record) {
		IAtomContainer fromCH_IUPAC = new AtomContainer();
		try {
			InChIToStructure intoStruct = InChIGeneratorFactory.getInstance().getInChIToStructure(record.CH_IUPAC(), SilentChemObjectBuilder.getInstance());
			InchiStatus ret = intoStruct.getStatus();
			if (ret == InchiStatus.WARNING) {
				// Structure generated, but with warning message
				logger.warn("InChI warning: " + intoStruct.getMessage());
			} 
			else if (ret == InchiStatus.ERROR) {
				logger.error("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed.\nError:\n" + intoStruct.getMessage() + ".");
			}
			fromCH_IUPAC = intoStruct.getAtomContainer();
		} catch (CDKException e) {
			logger.error("Can not parse InChI string in \"CH$IUPAC\" field.\nError from CDK:\n"+ e.getMessage());
		}
		
		try {
			InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(fromCH_IUPAC);
			InchiStatus ret = inchiGen.getStatus();
			if (ret == InchiStatus.WARNING) {
				// Structure generated, but with warning message
				logger.warn("InChI warning: " + inchiGen.getMessage());
			} 
			else if (ret == InchiStatus.ERROR) {
				// InChI generation failed
				logger.error("Can not create InChIKey from SMILES string in \"CH$SMILES\" field. InChI generation failed: " + ret.toString() + " [" + inchiGen.getMessage() + "].");
			}
			String InChiKeyFromCH_SMILES = inchiGen.getInchiKey();
			System.out.println(InChiKeyFromCH_SMILES);
		} catch (CDKException e) {
			logger.error("Can not create InChIKey from SMILES string in \"CH$SMILES\" field.\nError from CDK:\n"+ e.getMessage());
		}

		IAtomContainer inchi = record.CH_IUPAC_obj();
		SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.Absolute);
		String smi;
		try {
			smi = smigen.create(inchi);
			System.out.println(smi);
			record.CH_SMILES(smi);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return record.toString();
	}
	

	public static void main(String[] arguments) throws Exception {
		// load version and print
		Properties properties = ProjectPropertiesLoader.loadProperties();
		System.out.println("AddMetaData version: " + properties.getProperty("version"));

		// parse command line
		CommandLine cmd = parseCommandLine(arguments);
		AtomicBoolean doAddPubchemCid = new AtomicBoolean(cmd.hasOption("add-pubchemcid"));
		AtomicBoolean doSetSMILESfromInChi = new AtomicBoolean(cmd.hasOption("smiles-from-inchi"));

		List<Path> recordFiles = Validator.findRecordFiles(cmd.getArgList());
		if (recordFiles.isEmpty()) {
			logger.error("No files found for validation.");
			System.exit(1);
		}

		RecordParser recordparser = new RecordParser(new HashSet<>());
		recordFiles.parallelStream()
			.map(Validator::readFile)
			.filter(Objects::nonNull)
			.forEach(recordString -> {
                logger.info("Working on {}.", recordString.getKey());
				Record record = parseRecord(recordString, recordparser);
                if (record == null || record.DEPRECATED()) return;

				String recordStringAfterMod = recordString.getValue();
				//if (cmd.hasOption("p") || cmd.hasOption("a")) recordstring2=doPub(record, recordstring2);
				//if (cmd.hasOption("n") || cmd.hasOption("a")) recordstring2=doName(record, recordstring2);
				//if (cmd.hasOption("l") || cmd.hasOption("a")) recordstring2=doLink(record);
				//if (cmd.hasOption("ms_focused_ion") || cmd.hasOption("a")) recordstring2=doFocusedIon(record, recordstring2);

				//if (cmd.hasOption("add-inchikey")) {
				//	recordstring2=doAddInchikey(record);
				//}


				if (doAddPubchemCid.get()) {
					recordStringAfterMod=doAddPubchemCID(record);
				}
				if (doSetSMILESfromInChi.get()) {
					recordStringAfterMod=doSetSMILESfromInChi(record);
				}

				if (!recordString.getValue().equals(recordStringAfterMod)) {
					Record recordAfterMod = parseRecord(new SimpleEntry<>(recordString.getKey(), recordStringAfterMod), recordparser);

					if (recordAfterMod == null) {
						System.err.println( "Validation of new created record file failed. Do not write.");
					} else {
						try {
							FileUtils.write(recordString.getKey().toFile(), recordStringAfterMod, StandardCharsets.UTF_8);
						}
						catch(IOException exp) {
							System.err.println( "Writing file \""+ recordString.getKey() + "\" failed. Reason: " + exp.getMessage() );
							System.exit(1);
						}
					}
				}
			});
	}

	private static CommandLine parseCommandLine(String[] arguments) {
		// parse command line
		Options options = new Options();
		options.addOption("a", "all", false, "execute all operations");
		options.addOption("p", "publication", false, "format PUBLICATION tag from given DOI to follow the guidelines of ACS");
		options.addOption("n", "name", false, "fix common problems in CH$NAME tag");
		options.addOption("l", "link", false, "add links to CH$LINK tag");
		options.addOption("r", "rewrite", false, "read and rewrite the file.");
		options.addOption("ms_focused_ion", false, "Inspect MS$FOCUSED_ION");
		options.addOption(null, "add-inchikey", false, "Add or fix InChIKey from the value in CH$IUPAC");
		options.addOption(null, "add-pubchemcid", false, "Add or fix PubChem CID from InChIKey and flag Problems.");
		options.addOption(null, "smiles-from-inchi", false, "Set SMILES from existing InChi.");
		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse( options, arguments);
		}
		catch(ParseException e) {
			// oops, something went wrong
			System.err.println( "Parsing command line failed. Reason: " + e.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("AddMetaData [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
			System.exit(1);
		}
		if (cmd.getArgList().isEmpty()) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("AddMetaData [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
			System.exit(1);
		}


		return cmd;
	}
}
