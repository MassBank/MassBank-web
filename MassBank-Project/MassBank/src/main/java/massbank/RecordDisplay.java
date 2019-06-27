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
 * package massbank;
 * 
 ******************************************************************************/
package massbank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;

@WebServlet("/RecordDisplay2")
public class RecordDisplay extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(RecordDisplay.class);
	
	private static String createPeakListForSpectrumViewer(Record record) {
        // convert a list of lists [[mz, int, rel.int], [...], ...]
        // to String "mz,rel.int@mz,rel.int@..."
		List<String> peaks = new ArrayList<>();
		for (List<Double> peak : record.PK_PEAK()) {
			peaks.add(peak.get(0)+","+peak.get(2));
		}
		return String.join("@", peaks);
	}
	
	private static String createRecordString(Record record) {
		StringBuilder sb = new StringBuilder();
		sb.append("<hr>\n");
		sb.append("<b>ACCESSION:</b> " + record.ACCESSION() + "<br>\n");
		sb.append("<b>RECORD_TITLE:</b> " + record.RECORD_TITLE1() + "<br>\n");
		sb.append("<b>DATE:</b> " + record.DATE() + "<br>\n");
		sb.append("<b>AUTHORS:</b> " + record.AUTHORS() + "<br>\n");
		sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/licenses/\" target=\"_blank\">" + record.LICENSE() + "</a><br>\n");
		if (record.COPYRIGHT() != null)
			sb.append("<b>COPYRIGHT:</b> " + record.COPYRIGHT() + "<br>\n");
		if (record.PUBLICATION() != null) {
			String pub=record.PUBLICATION();
			String regex_doi = "10\\.\\d{3,9}\\/[\\-\\._;\\(\\)\\/:a-zA-Z0-9]+[a-zA-Z0-9]";
			String regex_pmid = "PMID:[ ]?\\d{8,8}";
			Pattern pattern_doi = Pattern.compile(".*" + "(" + regex_doi+ ")" + ".*");
			Pattern pattern_pmid = Pattern.compile(".*" + "(" + regex_pmid	+ ")" + ".*");
			Matcher matcher_doi = pattern_doi.matcher(pub);
		    Matcher matcher_pmid = pattern_pmid.matcher(pub);
		    if(matcher_doi.matches()){
				//link doi
				String doi=pub.substring(matcher_doi.start(1), matcher_doi.end(1));
				pub.replaceAll(doi, "<a href=\"https:\\/\\/doi.org/" + doi + "\" target=\"_blank\">" + doi + "</a>");
			} else if (matcher_pmid.matches()) {
				String PMID = pub.substring(matcher_pmid.start(1), matcher_pmid.end(1));
		    	String id = PMID.substring("PMID:".length()).trim();
		    	pub = pub.replaceAll(PMID, "<a href=\"http:\\/\\/www.ncbi.nlm.nih.gov/pubmed/" + id + "?dopt=Citation\" target=\"_blank\">" + PMID + "</a>");
			}
			sb.append("<b>PUBLICATION:</b> " + pub + "<br>\n");
		}
		for (String comment : record.COMMENT())
			sb.append("<b>COMMENT:</b> " + comment + "<br>\n");
		sb.append("<hr>\n");
		for (String ch_name : record.CH_NAME())
			sb.append("<b>CH$NAME:</b> " + ch_name + "<br>\n");
		sb.append("<b>CH$COMPOUND_CLASS:</b> " + String.join("; ", record.CH_COMPOUND_CLASS()) + "<br>\n");
		sb.append("<b>CH$FORMULA:</b> <a href=\"http://www.chemspider.com/Search.aspx?q=" + record.CH_FORMULA() + "\" target=\"_blank\">" + record.CH_FORMULA1() + "</a><br>\n");
		sb.append("<b>CH$EXACT_MASS:</b> " + record.CH_EXACT_MASS() + "<br>\n");
		
		
		
		
				
		
		sb.append("CH$SMILES: " + record.CH_SMILES() + "<br>\n");
		sb.append("CH$IUPAC: " + record.CH_IUPAC() + "<br>\n");
		
		for (Pair<String,String> link : record.CH_LINK()) {
			sb.append("CH$LINK: " + link.getKey() + " " + link.getValue() + "<br>\n");
		}
		if (record.SP_SCIENTIFIC_NAME() != null)
			sb.append("SP$SCIENTIFIC_NAME: " + record.SP_SCIENTIFIC_NAME() + "<br>\n");
		if (record.SP_LINEAGE() != null)
			sb.append("SP$LINEAGE: " + record.SP_LINEAGE() + "<br>");
		for (Pair<String,String> link : record.SP_LINK())
			sb.append("SP$LINK: " + link.getKey() + " " + link.getValue() + "<br>");
		for (String sample : record.SP_SAMPLE())
				sb.append("SP$SAMPLE: " + sample + "<br>");

		
		sb.append("<hr>");
		sb.append("AC$INSTRUMENT: " + record.AC_INSTRUMENT() + "<br>");
		sb.append("AC$INSTRUMENT_TYPE: " + record.AC_INSTRUMENT_TYPE() + "<br>");
		sb.append("AC$MASS_SPECTROMETRY: MS_TYPE: " + record.AC_MASS_SPECTROMETRY_MS_TYPE() + "<br>");
		sb.append("AC$MASS_SPECTROMETRY: ION_MODE: " + record.AC_MASS_SPECTROMETRY_ION_MODE() + "<br>");
		for (Pair<String,String> ac_mass_spectrometry : record.AC_MASS_SPECTROMETRY())
			sb.append("AC$MASS_SPECTROMETRY: " + ac_mass_spectrometry.getKey() + " " + ac_mass_spectrometry.getValue() + "<br>");
		for (Pair<String,String> ac_chromatography : record.AC_CHROMATOGRAPHY())
			sb.append("AC$CHROMATOGRAPHY: " + ac_chromatography.getKey() + " " + ac_chromatography.getValue() + "<br>");
		
		
		if (!record.MS_FOCUSED_ION().isEmpty() || !record.MS_DATA_PROCESSING().isEmpty()) sb.append("<hr>");
		for (Pair<String,String> ms_focued_ion : record.MS_FOCUSED_ION())
			sb.append("MS$FOCUSED_ION: " + ms_focued_ion.getKey() + " " + ms_focued_ion.getValue() + "<br>");
		for (Pair<String,String> ms_data_processing : record.MS_DATA_PROCESSING())
				sb.append("MS$DATA_PROCESSING: " + ms_data_processing.getKey() + " " + ms_data_processing.getValue() + "<br>");
		
		sb.append("<hr>");
		sb.append("PK$SPLASH: " + record.PK_SPLASH() + "<br>");
		if (!record.PK_ANNOTATION_HEADER().isEmpty()) {
			sb.append("PK$ANNOTATION:");
			for (String annotation_header_item : record.PK_ANNOTATION_HEADER())
				sb.append(" " + annotation_header_item);
			sb.append("<br>");
			for (List<String> annotation_line :  record.PK_ANNOTATION()) {
				sb.append("&nbsp");
				for (String annotation_item : annotation_line )
					sb.append("&nbsp" + annotation_item);
				sb.append("<br>");
			}
		}

		sb.append("PK$NUM_PEAK: " + record.PK_NUM_PEAK() + "<br>");
		sb.append("PK$PEAK: m/z int. rel.int.<br>");
		for (List<Double> peak_line :  record.PK_PEAK()) {
			sb.append("&nbsp");
			for (Double peak_line_item : peak_line )
				sb.append("&nbsp" + peak_line_item.toString());
			sb.append("<br>");
		}
		
		sb.append("//");

		return sb.toString();
	}
	
	private static String createStructuredData(Record record) {
		StringBuilder sb = new StringBuilder();
		sb.append("<script type=\"application/ld+json\">\n");
		sb.append("[\n");
		sb.append("{\n");
		sb.append("\"identifier\": \""+record.ACCESSION()+"\",\n");
		sb.append("\"url\": \"https://massbank.eu/RecordDisplay?id="+record.ACCESSION()+"\",\n");
		sb.append("\"name\": \""+record.RECORD_TITLE().get(0)+"\",\n");
		if (record.CH_NAME().size() == 1)  sb.append("\"alternateName\": \""+ record.CH_NAME().get(0) +"\",\n");
		else if (record.CH_NAME().size() >= 1) sb.append("\"alternateName\": [\""+ String.join("\", \"", record.CH_NAME()) +"\"],\n");

		
		
		sb.append("\"molecularFormula\": \""+record.CH_FORMULA()+"\",\n");
		sb.append("\"monoisotopicMolecularWeight\": \""+record.CH_EXACT_MASS()+"\",\n");
		sb.append("\"inChI\": \""+record.CH_IUPAC()+"\",\n");
		sb.append("\"smiles\": \""+record.CH_SMILES()+"\",\n");
		sb.append("\"@context\": \"http://schema.org\",\n");
		sb.append("\"@type\": \"MolecularEntity\"\n");
		sb.append("},\n");
		sb.append("{\n");
		
		
		
		
		sb.append("\"identifier\": \""+record.ACCESSION()+"\",\n");
		sb.append("\"url\": \"https://massbank.eu/RecordDisplay?id="+record.ACCESSION()+"\",\n");
		sb.append("\"headline\": \""+record.RECORD_TITLE1()+"\",\n");
		sb.append("\"name\": \""+record.RECORD_TITLE().get(0)+"\",\n");
		String[] tokens	= record.DATE1();
		sb.append("\"datePublished\": \""+tokens[0].replace(".","-")+"\",\n");
		if(tokens.length >= 2) { sb.append("\"dateCreated\": \""+tokens[1].replace(".","-")+"\",\n"); }
		if(tokens.length == 3) { sb.append("\"dateModified\": \""+tokens[2].replace(".","-")+"\",\n"); }
		sb.append("\"license\": \"https://creativecommons.org/licenses\",\n");
		sb.append("\"citation\": \""+record.PUBLICATION()+"\",\n");
		if (record.COMMENT().size() == 1)  sb.append("\"comment\": \""+ record.COMMENT().get(0) +"\",\n");
		else if (record.COMMENT().size() >= 1) sb.append("\"comment\": [\""+ String.join("\", \"", record.COMMENT()) +"\"],\n");
		if (record.CH_NAME().size() == 1)  sb.append("\"alternateName\": \""+ record.CH_NAME().get(0) +"\",\n");
		else if (record.CH_NAME().size() >= 1) sb.append("\"alternateName\": [\""+ String.join("\", \"", record.CH_NAME()) +"\"],\n");
		

		
		sb.append("\"@context\": \"http://schema.org\",\n");
		sb.append("\"@type\": \"Dataset\"\n");
		sb.append("}\n");
		sb.append("]\n");
		sb.append("</script>");
		return sb.toString();
	}	
	
//	case "CH$COMPOUND_CLASS":
//	sb.append(tag + ": " + value + "\n");
//	compoundClass	= value;
//	break;

//		@id  https://massbank.eu/MassBank/RecordDisplay.jsp?id=WA001202&dsn=Waters
//		measurementTechnique LC-ESI-Q

//			"biologicalRole": [
//				{
//					"@type": "DefinedTerm",
//					"@id": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C66892",
//					"inDefinedTermSet":
//						{
//							"@type":"DefinedTermSet",
//							"@id":"http://data.bioontology.org/ontologies/NCIT/submissions/69/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb",
//							"name": "National Cancer Institute Thesaurus"
//						},
//					"termCode": "C66892",
//					"name": "natural product",
//					"url": "http://bioportal.bioontology.org/ontologies/NCIT?p=classes&conceptid=http%3A%2F%2Fncicb.nci.nih.gov%2Fxml%2Fowl%2FEVS%2FThesaurus.owl%23C66892"
//				}
//		  ]
//		}
		

//		File file	= FileUtil.getFile(databaseName, accession);//new File(Config.get().DataRootPath() + databaseName + File.separator + accession + ".txt");
//		List<String> list	= FileUtil.readFromFile(file);
//		
//		// process record
//		// TODO property="schema:fileFormat"
//		// TODO property="schema:isAccessibleForFree"
//		// TODO property="schema:keywords"
//		// TODO property="schema:publisher"
//		final String delimiter	= ": ";
//		String inchi			= null;
//		String smiles			= null;
//		String recordTitle		= null;
//		
//		String msType			= null;
//		String name				= null;
//		String compoundClass	= null;
//		String inchiKey			= null;
//		String splash			= null;
//		String instrumentType	= null;
//		String ionization		= null;
//		String ionMode			= null;
//		String fragmentation	= null;
//		String collisionEnergy	= null;
//		String resolution		= null;
//		
//		// CH, AC, MS, PK
//		boolean ch	= false;
//		boolean ac	= false;
//		boolean ms	= false;
//		boolean pk	= false;
//		int PK_PEAK_idx	= -1;
//		
//		for(int lineIdx = 0; lineIdx < list.size(); lineIdx++){
//			String line	= list.get(lineIdx);
//			int delimiterIndex	= line.indexOf(delimiter);
//			if(delimiterIndex == -1){
//				sb.append(line + "\n");
//				continue;
//			}
//			
//			String tag		= line.substring(0, delimiterIndex);
//			String value	= line.substring(delimiterIndex + delimiter.length());
//			
//			// horizontal ruler
//			if(tag.startsWith("CH$") && (!ch))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	ch	= true;	}
//			if(tag.startsWith("AC$") && (!ac))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	ac	= true;	}
//			if(tag.startsWith("MS$") && (!ms))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	ms	= true;	}
//			if(tag.startsWith("PK$") && (!pk))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	pk	= true;	}
//			
//			switch(tag){

//			case "AUTHORS":
//				//sb.append(tag + ": " + value + "\n");
//				String[] authorTokens	= value.split(", ");
//				
//				// check which tokens are authors
//				int lastAuthorIdx	= -1;
//				for(int i = 0; i < authorTokens.length; i++){
//					// check if string is author like 'Akimoto AV'
//					boolean isAuthor	= authorTokens[i].matches("\\w+ \\w+");
//					if(isAuthor){
//						// 2nd word is initials?
//						String[] initials	= authorTokens[i].split(" ")[1].split("");
//						for(int j = 0; j < initials.length; j++)
//							if(!Character.isUpperCase(initials[j].toCharArray()[0])){
//								isAuthor	= false;
//								break;
//							}
//					}
//					if(isAuthor){
//						lastAuthorIdx	= i;
//					}
//				}
//				
//				// create affiliation
//				int numberOfAuthors	= lastAuthorIdx + 1;
//				String affiliation	= String.join(", ", Arrays.copyOfRange(authorTokens, numberOfAuthors, authorTokens.length));
//				
//				// create authors
//				String[] authors	= new String[numberOfAuthors];
//				for(int i = 0; i < numberOfAuthors; i++)
//					authors[i]	= 
//							"<span property=\"schema:author\" typeof=\"schema:Person\">" +
//								"<span property=\"schema:name\">" + authorTokens[i] + "</span>" +
//								//((affiliation.length() > 0) ?"<span property=\"schema:affiliation\" style=\"visibility:hidden\">" + affiliation + "</span>" : "") +
//								((affiliation.length() > 0) ?"<span property=\"schema:affiliation\" style=\"display:none\">" + affiliation + "</span>" : "") +
//							"</span>";
//				
//				// paste
//				sb.append(tag + ": ");
//				sb.append(String.join(", ", authors));
//				if(affiliation.length() > 0)
//					sb.append(", " + affiliation);
//				sb.append("\n");
//				break;


//			case "CH$SMILES":
//				sb.append(tag + ": " + value + "\n");
//				smiles	= value;
//				break;
//			case "CH$IUPAC":
//				sb.append(tag + ": " + value + "\n");
//				inchi	= value;
//				break;
//			case "CH$CDK_DEPICT_SMILES":
//			case "CH$CDK_DEPICT_GENERIC_SMILES":
//			case "CH$CDK_DEPICT_STRUCTURE_SMILES":
//				ClickablePreviewImageData clickablePreviewImageData2	= StructureToSvgStringGenerator.createClickablePreviewImage(
//						tag, null, value,
//						tmpFileFolder, tmpUrlFolder,
//						80, 200, 436
//				);
//				if(clickablePreviewImageData2 != null)
//					sb.append(tag + ": " + clickablePreviewImageData2.getMediumClickablePreviewLink("CH$CDK_DEPICT_SMILES", value));
//				else
//					sb.append(tag + ": " + value + "\n");
//				break;
//			case "CH$LINK":
//				String delimiter2	= " ";
//				int delimiterIndex2	= value.indexOf(delimiter2);
//				
//				if(delimiterIndex2 == -1){
//					sb.append(tag + ": " + value + "\n");
//					break;
//				}
//				
//				String CH$LINK_NAME	= value.substring(0, delimiterIndex2);
//				String CH$LINK_ID	= value.substring(delimiterIndex2 + delimiter2.length());
//				
//				if(CH$LINK_NAME == "INCHIKEY")	inchiKey	= CH$LINK_ID;
//				
//				switch(CH$LINK_NAME){
//					case "CAS":								CH$LINK_ID	= "<a href=\"https://www.google.com/search?q=&quot;" + CH$LINK_ID + "&quot;"									+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "CAYMAN":                      	CH$LINK_ID	= "<a href=\"https://www.caymanchem.com/app/template/Product.vm/catalog/" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "CHEBI":                       	CH$LINK_ID	= "<a href=\"https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:" + CH$LINK_ID								+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "CHEMPDB":                     	CH$LINK_ID	= "<a href=\"https://www.ebi.ac.uk/msd-srv/chempdb/cgi-bin/cgi.pl?FUNCTION=getByCode&amp;CODE=" + CH$LINK_ID	+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "CHEMSPIDER":                  	CH$LINK_ID	= "<a href=\"https://www.chemspider.com/" + CH$LINK_ID															+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "COMPTOX": 	                	CH$LINK_ID	= "<a href=\"https://comptox.epa.gov/dashboard/dsstoxdb/results?search=" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "FLAVONOIDVIEWER":             	CH$LINK_ID	= "<a href=\"http://www.metabolome.jp/software/FlavonoidViewer/"												+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "HMDB":                        	CH$LINK_ID	= "<a href=\"http://www.hmdb.ca/metabolites/" + CH$LINK_ID														+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "INCHIKEY":                    	CH$LINK_ID	= "<a href=\"https://www.google.com/search?q=&quot;" + CH$LINK_ID + "&quot;"									+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "KAPPAVIEW":                   	CH$LINK_ID	= "<a href=\"http://kpv.kazusa.or.jp/kpv4/compoundInformation/view.action?id=" + CH$LINK_ID						+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "KEGG":                        	CH$LINK_ID	= "<a href=\"http://www.genome.jp/dbget-bin/www_bget?cpd:" + CH$LINK_ID											+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "KNAPSACK":                    	CH$LINK_ID	= "<a href=\"http://kanaya.naist.jp/knapsack_jsp/info.jsp?sname=C_ID&word=" + CH$LINK_ID						+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "LIPIDBANK":                   	CH$LINK_ID	= "<a href=\"http://lipidbank.jp/cgi-bin/detail.cgi?id=" + CH$LINK_ID											+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "LIPIDMAPS":                   	CH$LINK_ID	= "<a href=\"http://www.lipidmaps.org/data/get_lm_lipids_dbgif.php?LM_ID=" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "NIKKAJI":                     	CH$LINK_ID	= "<a href=\"https://jglobal.jst.go.jp/en/redirect?Nikkaji_No=" + CH$LINK_ID + "&CONTENT=syosai"		+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "OligosaccharideDataBase":     	CH$LINK_ID	= "<a href=\"http://www.fukuyama-u.ac.jp/life/bio/biochem/" + CH$LINK_ID + ".html" + CH$LINK_ID					+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "OligosaccharideDataBase2D":   	CH$LINK_ID	= "<a href=\"http://www.fukuyama-u.ac.jp/life/bio/biochem/" + CH$LINK_ID + ".html"								+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "NCBI-TAXONOMY":               	CH$LINK_ID	= "<a href=\"https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
//					case "PUBCHEM":{
//						if(CH$LINK_ID.startsWith("CID:"))	CH$LINK_ID	= "<a href=\"https://pubchem.ncbi.nlm.nih.gov/compound/" + CH$LINK_ID.substring("CID:".length())				+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>";
//						if(CH$LINK_ID.startsWith("SID:"))	CH$LINK_ID	= "<a href=\"https://pubchem.ncbi.nlm.nih.gov/substance/" + CH$LINK_ID.substring("SID:".length())				+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>";
//						break;
//					}
//				}
//				
//				sb.append(tag + ": " + CH$LINK_NAME + delimiter2 + CH$LINK_ID + "\n");
//				break;
//			// AC$INSTRUMENT
//			case "AC$INSTRUMENT_TYPE":
//				// TODO property="schema:measurementTechnique"
//				sb.append(tag + ": <span property=\"schema:measurementTechnique\">" + value + "</span>\n");
//				instrumentType	= value;
//				break;
//			case "AC$MASS_SPECTROMETRY":
//				sb.append(tag + ": " + value + "\n");
//				
//				String[] tokens2	= value.split(" ");
//				String subTag	= tokens2[0];
//				switch(subTag){
//					case "MS_TYPE":
//						msType			= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
//						break;
//					case "IONIZATION":
//						ionization		= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
//						break;
//					case "ION_MODE":
//						ionMode			= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
//						break;
//					case "FRAGMENTATION_MODE":
//						fragmentation	= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
//						break;
//					case "COLLISION_ENERGY":
//						collisionEnergy	= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
//						break;
//					case "RESOLUTION":
//						resolution		= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
//						break;
//				}
//				
//				break;
//			// AC$CHROMATOGRAPHY
//			// MS$FOCUSED_ION
//			// MS$DATA_PROCESSING
//			case "PK$SPLASH":
//				sb.append(tag + ": " + "<a href=\"http://www.google.com/search?q=" + value + "\" target=\"_blank\">" + value + "</a>" + "\n"); // https://www.google.com/search?q=&quot;%s&quot;
//				splash	= value;
//				break;
//			// PK$NUM_PEAK
//			case "PK$PEAK":{
//				PK_PEAK_idx	= lineIdx;
//				sb.append(line + "\n");
//				break;
//			}
//			default:
//				sb.append(line + "\n");
//				break;
//			}
//		}
//		
//		// get peaks for specktackle
//		StringBuilder sbPeaks	= new StringBuilder();
//		for(int lineIdx = PK_PEAK_idx + 1; lineIdx < list.size() - 1; lineIdx++){
//			String[] tokens	= list.get(lineIdx).trim().split(" ");
//			sbPeaks.append(tokens[0] + "," + tokens[2] + "@");
//		}
//		String peaks	= sbPeaks.toString();
//		
//		if(recordTitle == null)
//			recordTitle	= "NA";
//		
//		String shortName	= recordTitle.split(";")[0].trim();
//		
//		ClickablePreviewImageData clickablePreviewImageData	= StructureToSvgStringGenerator.createClickablePreviewImage(
//				accession, inchi, smiles, tmpFileFolder, tmpUrlFolder,
//				80, 200, 436
//		);
//		String svgMedium	= null;
//		if(clickablePreviewImageData != null)
//			svgMedium	= clickablePreviewImageData.getMediumClickableImage();
//		
//		// meta data
//		String[] compoundClasses	= compoundClass.split("; ");
//		String compoundClass2	= compoundClasses[0];
//		if(compoundClass2.equals("NA") && compoundClasses.length > 1)	compoundClass2	= compoundClasses[1];
//		
//		String description	= 
//				"This MassBank Record with Accession " + accession + 
//				" contains the " + msType + " mass spectrum" + 
//				" of '" + name + "'" +
//				(inchiKey			!= null		? " with the InChIKey '" + inchiKey + "'"					: "") + 
//				(!compoundClass2.equals("N/A")	? " with the compound class '" + compoundClass2 + "'"	: "") + 
//				"." +
//				" The mass spectrum was acquired on a " + instrumentType + 
//				//" with " + (ionization != null	? ionization											: "") + 
//				" with " + ionMode + " ionisation" +
//				(fragmentation		!= null		? " using " + fragmentation + " fragmentation"			: "") +
//				(collisionEnergy	!= null		? " with the collision energy '" + collisionEnergy + "'": "") + 
//				(collisionEnergy	!= null		? " at a resolution of " + resolution					: "") + 
//				(splash				!= null		? " and has the SPLASH '" + splash + "'"				: "") +
//				".";
//		
//		// record
//		String recordString	= sb.toString();
//	%>
//	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
//	<html lang="en">
//		<head>
//			<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
//			<meta name="author" content="MassBank" />
//			<meta name="coverage" content="worldwide" />
//			<meta name="Targeted Geographic Area" content="worldwide" />
//			<meta name="rating" content="general" />
//			<meta name="copyright" content="Copyright (c) 2006 MassBank Project and (c) 2011 NORMAN Association" />
//			<meta name="description" content="<%=description%>">
//			<meta name="keywords" content="<%=accession%>, <%=shortName%>, <%=inchiKey%>, mass spectrum, MassBank record, mass spectrometry, mass spectral library">
//			<meta name="revisit_after" content="30 days">
//			<meta name="hreflang" content="en">
//			<meta name="variableMeasured" content="m/z">
//			<meta http-equiv="Content-Style-Type" content="text/css">
//			<meta http-equiv="Content-Script-Type" content="text/javascript">
//			<link rel="stylesheet" type="text/css" href="css/Common.css">
//			<script type="text/javascript" src="script/Common.js"></script>
//			<!-- SpeckTackle dependencies-->
//			<script type="text/javascript" src="script/d3.v3.min.js"></script>
//			<!-- SpeckTackle library-->
//			<script type="text/javascript" src="script/st.min.js" charset="utf-8"></script>
//			<!-- SpeckTackle style sheet-->
//			<link rel="stylesheet" href="css/st.css" type="text/css" />
//			<!-- SpeckTackle MassBank loading script-->
//			<script type="text/javascript" src="script/massbank_specktackle.js"></script>
//			<title><%=shortName%> Mass Spectrum</title>
//		</head>
//		<body style="font-family:Times;" typeof="schema:WebPage">
//		<main context="http://schema.org" property="schema:about" resource="https://massbank.eu/MassBank/RecordDisplay.jsp?id=<%=accession%>&dsn=<%=databaseName%>" typeof="schema:Dataset" >
//			<table border="0" cellpadding="0" cellspacing="0" width="100%">
//				<tr>
//					<td>
//						<h1>MassBank Record: <%=accession%> </h1>
//					</td>
//				</tr>
//			</table>
//			<iframe src="menu.jsp" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
//			<hr size="1">
//			<br>
//			<font size="+1" style="background-color:LightCyan">&nbsp;<%=recordTitle%>&nbsp;</font>
//			<hr size="1">
//			<table>
//				<tr>
//					<td valign="top">
//						<font style="font-size:10pt;" color="dimgray">Mass Spectrum</font>
//						<br>
//						<div id="spectrum_canvas" peaks="<%=peaks%>" style="height: 200px; width: 750px; background-color: white"></div>
//					</td>
//					<td valign="top">
//						<font style="font-size:10pt;" color="dimgray">Chemical Structure</font><br>
//						<% // display svg
//						if(clickablePreviewImageData != null){
//							// paste small image to web site %>
//							<%=svgMedium%><%
//						} else {
//							// no structure there or svg generation failed%>
//							<img src="image/not_available_s.gif" width="200" height="200" style="margin:0px;">
//						<%}%></td>
//				</tr>
//			</table>
//	<hr size="1">
//	<pre style="font-family:Courier New;font-size:10pt">
//	<%=recordString%>
//	</pre>
//			<hr size=1>
//			<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
//		</main>
//		</body>
//	</html>
		
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// preprocess request
		Record record = null;
		try {
			// get parameters
			// http://localhost/MassBank/jsp/RecordDisplay.jsp?id=XXX00001
			// String accession = "XXX00001";
			String accession = null;
			
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				String val = (String) request.getParameter( key );
				switch(key){
					case "id": accession = val; break;
					default: logger.warn("unused argument " + key + "=" + val);
				}
			}
			
			// error handling
			if("".equals(accession)) accession = null;
			if(accession == null){
				String errormsg ="missing argument 'id'.";
				logger.error(errormsg);
				String redirectUrl	= "/NoRecordPage" + "?error=" + errormsg;
				response.sendRedirect(request.getContextPath()+redirectUrl);
				return;
			}

			// load record for display
			DatabaseManager dbMan	= new DatabaseManager("MassBank");
			record	= dbMan.getAccessionData(accession);
			dbMan.closeConnection();
			if(record == null) {
				String errormsg	= "retrieval of '" + accession + "' from database failed";
				logger.error(errormsg);
				String redirectUrl	= "/NoRecordPage" + "?id=" + accession + "&error=" + errormsg;
				response.sendRedirect(request.getContextPath()+redirectUrl);
				return;
			}
			
			if (record.DEPRECATED()) {
				logger.trace("Show deprecated record " + accession + ".");
				String shortname = "DEPRECATED RECORD " + accession;
				request.setAttribute("accession", accession);
				request.setAttribute("short_name", shortname);
				request.setAttribute("isDeprecated", true);
				request.setAttribute("record_title", accession + " has been deprecated.");	
				request.setAttribute("recordstring", "<pre>\nACCESSION: "+ accession + "\nDEPRECATED: "+ record.DEPRECATED_CONTENT() + "\n<pre>");
				
			} else {
				logger.trace("Show record "+accession+".");
				String shortname = record.RECORD_TITLE().get(0)+ " Mass Spectrum";
				// find InChIKey in CH_LINK
				String inchikey = null;
				for (Pair<String,String> link : record.CH_LINK()) {
					if ("INCHIKEY".equals(link.getKey())) {
						inchikey=link.getValue();
					}
				}
				String keywords =
					accession + ", " 
					+ shortname +", "
					+ (inchikey != null ? inchikey + ", " : "")
				    + "mass spectrum, MassBank record, mass spectrometry, mass spectral library";
				String description	= 
					"This MassBank Record with Accession " + accession + 
					" contains the " + record.AC_MASS_SPECTROMETRY_MS_TYPE() + " mass spectrum" + 
					" of '" + record.RECORD_TITLE().get(0) + "'" +
					(inchikey != null ? " with the InChIKey '" + inchikey + "'" : "") + 
					//(!compoundClass2.equals("N/A")	? " with the compound class '" + compoundClass2 + "'"	: "") + 
					//"." +
					//" The mass spectrum was acquired on a " + instrumentType + 
					//" with " + (ionization != null	? ionization											: "") + 
					//" with " + ionMode + " ionisation" +
					//(fragmentation		!= null		? " using " + fragmentation + " fragmentation"			: "") +
					//(collisionEnergy	!= null		? " with the collision energy '" + collisionEnergy + "'": "") + 
					//(collisionEnergy	!= null		? " at a resolution of " + resolution					: "") + 
					//(splash				!= null		? " and has the SPLASH '" + splash + "'"				: "") +
					".";
				String recordstring = createRecordString(record);
				String structureddata = createStructuredData(record);
				IAtomContainer mol = record.CH_IUPAC_obj(); 
				String svg = new DepictionGenerator().withAtomColors().depict(mol).toSvgStr(Depiction.UNITS_PX);
				
				request.setAttribute("accession", accession);
				request.setAttribute("short_name", shortname);
		        request.setAttribute("keywords", keywords);
		        request.setAttribute("record_title", record.RECORD_TITLE1());	        		
		        request.setAttribute("peaks", createPeakListForSpectrumViewer(record));
				request.setAttribute("description", description);
				request.setAttribute("recordstring", recordstring);
		        request.setAttribute("structureddata", structureddata);
		        request.setAttribute("svg", svg);
			}
	        request.getRequestDispatcher("/RecordDisplay2.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Cannot load record", e);
        }
     }

}
