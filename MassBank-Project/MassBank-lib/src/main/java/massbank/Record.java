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
package massbank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import net.sf.jniinchi.INCHI_RET;

/**
 * This class keeps all data of a record.
 * @author rmeier
 * @version 05-05-2020
 */
public class Record {
	private static final Logger logger = LogManager.getLogger(Record.class);

	private final String contributor;
	
	private String accession;
	private boolean deprecated;
	private String deprecated_content;
	private List<String> record_title;
	private String date;
	private String authors;
	private String license;	
	private String copyright; // optional
	private String publication; // optional
	private String project; // optional
	private List<String> comment; // optional
	private List<String> ch_name;
	private List<String> ch_compound_class;
	private String ch_formula;
	private BigDecimal ch_exact_mass;
	private String ch_smiles;
	private String ch_iupac;
	private List<Pair<String, String>> ch_link; // optional
	private String sp_scientific_name; // optional
	private String sp_lineage; // optional
	private List<Pair<String, String>> sp_link; // optional
	private List<String> sp_sample; // optional
	private String ac_instrument;
	private String ac_instrument_type;
	private String ac_mass_spectrometry_ms_type;
	private String ac_mass_spectrometry_ion_mode;
	private List<Pair<String, String>> ac_mass_spectrometry; // optional
	private List<Pair<String, String>> ac_chromatography; // optional
	private List<Pair<String, String>> ms_focused_ion; // optional
	private List<Pair<String, String>> ms_data_processing; // optional
	private String pk_splash;
	private List<String> pk_annotation_header; // optional
	private final List<Pair<BigDecimal, List<String>>> pk_annotation; // optional
	private final List<Triple<BigDecimal,BigDecimal,Integer>> pk_peak;
	
	public Record(String contributor) {
		this.contributor	= contributor;
		
		// set default values for optional fields
		comment					= new ArrayList<String>();
		ch_link					= new ArrayList<Pair<String, String>>();
		sp_link					= new ArrayList<Pair<String, String>>();
		sp_sample				= new ArrayList<String>();
		ac_mass_spectrometry	= new ArrayList<Pair<String, String>>();
		ac_chromatography		= new ArrayList<Pair<String, String>>();
		ms_focused_ion			= new ArrayList<Pair<String, String>>();
		ms_data_processing		= new ArrayList<Pair<String, String>>();
		pk_annotation_header	= new ArrayList<String>();
		pk_annotation			= new ArrayList<Pair<BigDecimal, List<String>>>();
		
		// set default values for mandatory fields
		pk_peak					= new ArrayList<Triple<BigDecimal,BigDecimal,Integer>>();
	}
	
	public String CONTRIBUTOR() {
		return contributor;
	}

	
	public String ACCESSION() {
		return accession;
	}
	public void ACCESSION(String value) {
		accession = value;
	}
	
	
	public boolean DEPRECATED() {
		return deprecated;
	}
	
	public void DEPRECATED(boolean value) {
		deprecated = value;
	}
	
	// everything after the keyword "DEPRECATED: "
	public String DEPRECATED_CONTENT() {
		return deprecated_content;
	}
	
	public void DEPRECATED_CONTENT(String value) {
		deprecated_content = value;
	}
	
	public List<String> RECORD_TITLE() {
		return record_title;
	}
	public String RECORD_TITLE1() {
		return String.join("; ", record_title);
	}
	public void RECORD_TITLE(List<String> value) {
		record_title = value;
	}
	public void RECORD_TITLE1(String value) {
		record_title = new ArrayList<String>(Arrays.asList(value.split("; ")));
	}
	
	
	public String DATE() {
		return date;
	}
	public String[] DATE1() {
		// DATE: 2016.01.15
		// DATE: 2011.02.21 (Created 2007.07.07)
		// DATE: 2016.01.19 (Created 2006.12.21, modified 2011.05.06)
		return date.replace("(Created ", "").replace(", modified", "").replace(")", "").split(" ");
	}
	public void DATE(String value) {
		date=value;
	}
	
	
	public String AUTHORS() {
		return authors;
	}
	public void AUTHORS(String value) {
		authors=value;
	}
	
	
	public String LICENSE() {
		return license;
	}
	public void LICENSE(String value) {
		license=value;
	}
	
	
	public String COPYRIGHT() {
		return copyright;
	}
	public void COPYRIGHT(String value) {
		copyright= value;
	}
	
	
	public String PUBLICATION() {
		return publication;
	}
	public void PUBLICATION(String value) {
		publication=value;
	}
	
	
	public String PROJECT() {
		return project;
	}
	public void PROJECT(String value) {
		project=value;
	}


	public List<String> COMMENT() {
		return comment;
	}
	public void COMMENT(List<String> value) {
		comment=new ArrayList<String>(value);
	}
	
	
	public List<String> CH_NAME() {
		return ch_name;
	}
	public void CH_NAME(List<String> value) {
		ch_name=new ArrayList<String>(value);
	}
	
	
	public List<String> CH_COMPOUND_CLASS() {
		return ch_compound_class;
	}
	public void CH_COMPOUND_CLASS(List<String> value) {
		ch_compound_class=new ArrayList<String>(value);
	}
	
	/**
	* Returns the molecular formula as an String.
	*/
	public String CH_FORMULA() {
		return ch_formula;
	}
	/**
	* Returns the molecular formula as an String with HTML sup tags.
	*/
	public String CH_FORMULA1() {
		IMolecularFormula m = MolecularFormulaManipulator.getMolecularFormula(ch_formula, DefaultChemObjectBuilder.getInstance());
		return MolecularFormulaManipulator.getHTML(m);
	}
	public void CH_FORMULA(String value) {
		ch_formula=value;
	}
	
	
	public BigDecimal CH_EXACT_MASS() {
		return ch_exact_mass;
	}
	public void CH_EXACT_MASS(BigDecimal value) {
		ch_exact_mass=value;
	}
	
	
	public String CH_SMILES() {
		return ch_smiles;
	}
	public IAtomContainer CH_SMILES_obj() {
		if ("N/A".equals(ch_smiles)) return new AtomContainer();
		try {
			return new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(ch_smiles);
		} catch (InvalidSmilesException e) {
			logger.error("Structure generation from SMILES failed. Error: \""+ e.getMessage() + "\" for \"" + ch_smiles + "\".");
			return new AtomContainer();
		}
	}
	public void CH_SMILES(String value) {
		ch_smiles=value;
	}
	
	
	public String CH_IUPAC() {
		return ch_iupac;
	}
	public IAtomContainer CH_IUPAC_obj() {
		if ("N/A".equals(ch_iupac)) return new AtomContainer();
		try {
			// Get InChIToStructure
			InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(ch_iupac, DefaultChemObjectBuilder.getInstance());
			INCHI_RET ret = intostruct.getReturnStatus();
			if (ret == INCHI_RET.WARNING) {
				// Structure generated, but with warning message
				logger.warn("InChI warning: \"" + intostruct.getMessage() + "\" converting \"" + ch_iupac + "\".");
			} 
			else if (ret != INCHI_RET.OKAY) {
				// Structure generation failed
				logger.error("Structure generation failed: " + ret.toString() + " [" + intostruct.getMessage() + "] for \"" + ch_iupac + "\".");
			}
			return intostruct.getAtomContainer();
		} catch (CDKException e) {
			logger.error("Structure generation from InChI failed. Error: \""+ e.getMessage() + "\" for \"" + ch_iupac + "\".");
			return new AtomContainer();
		}		 			
	}
	public void CH_IUPAC(String value) {
		ch_iupac=value;
	}

	
	public List<Pair<String, String>> CH_LINK() {
		return ch_link;
	}
	public Map<String, String> CH_LINK_asMap() {
		return listToMap(ch_link);
	}
	public void CH_LINK(List<Pair<String, String>> value) {
		ch_link=new ArrayList<Pair<String, String>>(value);
	}

	public String SP_SCIENTIFIC_NAME() {
		return sp_scientific_name;
	}
	public void SP_SCIENTIFIC_NAME(String value) {
		sp_scientific_name=value;
	}
	
	public String SP_LINEAGE() {
		return sp_lineage;
	}
	public void SP_LINEAGE(String value) {
		sp_lineage=value;
	}
 
	public List<Pair<String, String>> SP_LINK() {
		return sp_link;
	}
	public void SP_LINK(List<Pair<String, String>>  value) {
		sp_link=new ArrayList<Pair<String, String>>(value);
	}

	public List<String> SP_SAMPLE() {
		return sp_sample;
	}
	public void SP_SAMPLE(List<String> value) {
		sp_sample=new ArrayList<String>(value);
	}
	
	public String AC_INSTRUMENT() {
		return ac_instrument;
	}
	public void AC_INSTRUMENT(String value) {
		ac_instrument=value;
	}

	public String AC_INSTRUMENT_TYPE() {
		return ac_instrument_type;
	}
	public void AC_INSTRUMENT_TYPE(String value) {
		this.ac_instrument_type	= value;
	}
	
	public String AC_MASS_SPECTROMETRY_MS_TYPE() {
		return ac_mass_spectrometry_ms_type;
	}
	public void AC_MASS_SPECTROMETRY_MS_TYPE(String value) {
		ac_mass_spectrometry_ms_type=value;
	}
	
	public String AC_MASS_SPECTROMETRY_ION_MODE() {
		return ac_mass_spectrometry_ion_mode;
	}
	public void AC_MASS_SPECTROMETRY_ION_MODE(String value) {
		ac_mass_spectrometry_ion_mode=value;
	}
	
	public List<Pair<String, String>> AC_MASS_SPECTROMETRY() {
		return ac_mass_spectrometry;
	}
	public Map<String, String> AC_MASS_SPECTROMETRY_asMap() {
		return listToMap(ac_mass_spectrometry);
	}
	public void AC_MASS_SPECTROMETRY(List<Pair<String, String>> value) {
		ac_mass_spectrometry=new ArrayList<Pair<String, String>>(value);
	}

	public List<Pair<String, String>> AC_CHROMATOGRAPHY() {
		return ac_chromatography;
	}
	public Map<String, String> AC_CHROMATOGRAPHY_asMap() {
		return listToMap(ac_chromatography);
	}
	public void AC_CHROMATOGRAPHY(List<Pair<String, String>> value) {
		ac_chromatography=new ArrayList<Pair<String, String>>(value);
	}
	
	public List<Pair<String, String>> MS_FOCUSED_ION() {
		return ms_focused_ion;
	}
	public Map<String, String> MS_FOCUSED_ION_asMap() {
		return listToMap(ms_focused_ion);
	}
	public void MS_FOCUSED_ION(List<Pair<String, String>> value) {
		ms_focused_ion=new ArrayList<Pair<String, String>>(value);
	}
	
	public List<Pair<String, String>> MS_DATA_PROCESSING() {
		return ms_data_processing;
	}
	public void MS_DATA_PROCESSING(List<Pair<String, String>> value) {
		ms_data_processing=new ArrayList<Pair<String, String>>(value);
	}

	public String PK_SPLASH() {
		return pk_splash;
	}
	public void PK_SPLASH(String value) {
		pk_splash=value;
	}

	public List<String> PK_ANNOTATION_HEADER() {
		return pk_annotation_header;
	}
	public void PK_ANNOTATION_HEADER(List<String> value) {
		pk_annotation_header=new ArrayList<String>(value);
	}

	// PK_ANNOTATION is a two-dimensional List
	public List<Pair<BigDecimal, List<String>>> PK_ANNOTATION() {
		return pk_annotation;
	}
	public void PK_ANNOTATION_ADD_LINE(Pair<BigDecimal, List<String>> annotation) {
		pk_annotation.add(annotation);
	}

	public int PK_NUM_PEAK() {
		return pk_peak.size();
	}

	// PK_PEAK is a List with Triple values M/Z, intensity, rel. intensity
	public List<Triple<BigDecimal,BigDecimal,Integer>> PK_PEAK() {
		return pk_peak;
	}
	public void PK_PEAK_ADD_LINE(Triple<BigDecimal,BigDecimal,Integer> peak) {
		pk_peak.add(peak);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("ACCESSION: " + ACCESSION() + "\n");
		if (DEPRECATED()) {
			sb.append("DEPRECATED: ");
			sb.append(DEPRECATED_CONTENT());
			return sb.toString();
		}
		sb.append("RECORD_TITLE: " + RECORD_TITLE1() + "\n");
		sb.append("DATE: " + DATE() + "\n");
		sb.append("AUTHORS: " + AUTHORS() + "\n");
		sb.append("LICENSE: " + LICENSE() + "\n");
		if (COPYRIGHT() != null)
			sb.append("COPYRIGHT: " + COPYRIGHT() + "\n");
		if (PUBLICATION() != null)
			sb.append("PUBLICATION: " + PUBLICATION() + "\n");
		if (PROJECT() != null)
			sb.append("PROJECT: " + PROJECT() + "\n");
		for (String comment : COMMENT())
			sb.append("COMMENT: " + comment + "\n");
		
		for (String ch_name : CH_NAME())
			sb.append("CH$NAME: " + ch_name + "\n");
		sb.append("CH$COMPOUND_CLASS: " + String.join("; ", CH_COMPOUND_CLASS()) + "\n");
		sb.append("CH$FORMULA: " + CH_FORMULA() + "\n");
		sb.append("CH$EXACT_MASS: " + CH_EXACT_MASS() + "\n");
		sb.append("CH$SMILES: " + CH_SMILES() + "\n");
		sb.append("CH$IUPAC: " + CH_IUPAC() + "\n");
		for (Pair<String,String> link : CH_LINK())
			sb.append("CH$LINK: " + link.getKey() + " " + link.getValue() + "\n");
		
		if (SP_SCIENTIFIC_NAME() != null)
			sb.append("SP$SCIENTIFIC_NAME: " + SP_SCIENTIFIC_NAME() + "\n");
		if (SP_LINEAGE() != null)
			sb.append("SP$LINEAGE: " + SP_LINEAGE() + "\n");
		for (Pair<String,String> link : SP_LINK())
			sb.append("SP$LINK: " + link.getKey() + " " + link.getValue() + "\n");
		for (String sample : SP_SAMPLE())
			sb.append("SP$SAMPLE: " + sample + "\n");
		
		sb.append("AC$INSTRUMENT: " + AC_INSTRUMENT() + "\n");
		sb.append("AC$INSTRUMENT_TYPE: " + AC_INSTRUMENT_TYPE() + "\n");
		sb.append("AC$MASS_SPECTROMETRY: MS_TYPE " + AC_MASS_SPECTROMETRY_MS_TYPE() + "\n");
		sb.append("AC$MASS_SPECTROMETRY: ION_MODE " + AC_MASS_SPECTROMETRY_ION_MODE() + "\n");
		for (Pair<String,String> ac_mass_spectrometry : AC_MASS_SPECTROMETRY())
			sb.append("AC$MASS_SPECTROMETRY: " + ac_mass_spectrometry.getKey() + " " + ac_mass_spectrometry.getValue() + "\n");
		for (Pair<String,String> ac_chromatography : AC_CHROMATOGRAPHY())
			sb.append("AC$CHROMATOGRAPHY: " + ac_chromatography.getKey() + " " + ac_chromatography.getValue() + "\n");
		for (Pair<String,String> ms_focued_ion : MS_FOCUSED_ION())
			sb.append("MS$FOCUSED_ION: " + ms_focued_ion.getKey() + " " + ms_focued_ion.getValue() + "\n");
		for (Pair<String,String> ms_data_processing : MS_DATA_PROCESSING())
			sb.append("MS$DATA_PROCESSING: " + ms_data_processing.getKey() + " " + ms_data_processing.getValue() + "\n");

		sb.append("PK$SPLASH: " + PK_SPLASH() + "\n");
		if (!PK_ANNOTATION_HEADER().isEmpty()) {
			sb.append("PK$ANNOTATION:");
			for (String annotation_header_item : PK_ANNOTATION_HEADER())
				sb.append(" " + annotation_header_item);
			sb.append("\n");
			for (Pair<BigDecimal, List<String>> annotation_line :  PK_ANNOTATION()) {
				sb.append("  " + annotation_line.getLeft() + " " + String.join(" ", annotation_line.getRight()) + "\n");
			}
		}

		sb.append("PK$NUM_PEAK: " + PK_NUM_PEAK() + "\n");
		sb.append("PK$PEAK: m/z int. rel.int.\n");
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			String intensity1 = peak.getMiddle().toPlainString();
			String intensity2 = peak.getMiddle().toString();
			String intensity = (intensity1.length() <  intensity2.length() ) ? intensity1 : intensity2;
			sb.append("  " + peak.getLeft() + " " + intensity + " " + peak.getRight() + "\n");
		}
		sb.append("//\n");

		return sb.toString();
	}
	
	public String createRecordString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<b>ACCESSION:</b> " + ACCESSION() + "<br>\n");
		sb.append("<b>RECORD_TITLE:</b> " + RECORD_TITLE1() + "<br>\n");
		sb.append("<b>DATE:</b> " + DATE() + "<br>\n");
		sb.append("<b>AUTHORS:</b> " + AUTHORS() + "<br>\n");
		sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/licenses/\" target=\"_blank\">" + LICENSE() + "</a><br>\n");
		if (COPYRIGHT() != null) 
			sb.append("<b>COPYRIGHT:</b> " + COPYRIGHT() + "<br>\n");
		if (PUBLICATION() != null) {
			String pub=PUBLICATION();
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
		if (PROJECT() != null)
			sb.append("<b>PROJECT:</b> " + PROJECT() + "<br>\n");
		for (String comment : COMMENT())
			sb.append("<b>COMMENT:</b> " + comment + "<br>\n");
		sb.append("<hr>\n");
		
		for (String ch_name : CH_NAME())
			sb.append("<b>CH$NAME:</b> " + ch_name + "<br>\n");
		sb.append("<b>CH$COMPOUND_CLASS:</b> " + String.join("; ", CH_COMPOUND_CLASS()) + "<br>\n");
		sb.append("<b>CH$FORMULA:</b> <a href=\"http://www.chemspider.com/Search.aspx?q=" + CH_FORMULA() + "\" target=\"_blank\">" + CH_FORMULA1() + "</a><br>\n");
		sb.append("<b>CH$EXACT_MASS:</b> " + CH_EXACT_MASS() + "<br>\n");
		sb.append("<b>CH$SMILES:</b> " + CH_SMILES() + "<br>\n");
		sb.append("<b>CH$IUPAC:</b> " + CH_IUPAC() + "<br>\n");
		for (Pair<String,String> link : CH_LINK()) {
			switch(link.getKey()){
				case "CAS":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.google.com/search?q=&quot;" + link.getValue() + "&quot;\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "CAYMAN":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.caymanchem.com/product/" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "CHEBI":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "CHEMSPIDER":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.chemspider.com/" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "COMPTOX":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://comptox.epa.gov/dashboard/dsstoxdb/results?search=" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "HMDB":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"http://www.hmdb.ca/metabolites/" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "INCHIKEY":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.google.com/search?q=&quot;" + link.getValue() + "&quot;\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "KAPPAVIEW":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"http://kpv.kazusa.or.jp/kpv4/compoundInformation/view.action?id=" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "KEGG":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.genome.jp/dbget-bin/www_bget?cpd:" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "KNAPSACK":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"http://www.knapsackfamily.com/knapsack_jsp/information.jsp?sname=C_ID&word=" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;

				case "LIPIDBANK":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"http://lipidbank.jp/cgi-bin/detail.cgi?id=" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "LIPIDMAPS":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://www.lipidmaps.org/data/LMSDRecord.php?LMID=" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "NIKKAJI":
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://jglobal.jst.go.jp/en/redirect?Nikkaji_No=" + link.getValue() + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					break;
				case "PUBCHEM":{
					if(link.getValue().startsWith("CID:")) sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://pubchem.ncbi.nlm.nih.gov/compound/" + link.getValue().substring("CID:".length()) + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					else if(link.getValue().startsWith("SID:")) sb.append("<b>CH$LINK:</b> " + link.getKey() + " <a href=\"https://pubchem.ncbi.nlm.nih.gov/substance/" + link.getValue().substring("SID:".length()) + "\" target=\"_blank\">" + link.getValue() + "</a><br>\n");
					else sb.append("<b>CH$LINK:</b> " + link.getKey() + " " + link.getValue() + "<br>\n");
					break;
				}
				default:
					sb.append("<b>CH$LINK:</b> " + link.getKey() + " " + link.getValue() + "<br>\n");
			}
		}
		
		if (SP_SCIENTIFIC_NAME() != null)
			sb.append("<b>SP$SCIENTIFIC_NAME:</b> " + SP_SCIENTIFIC_NAME() + "<br>\n");
		if (SP_LINEAGE() != null)
			sb.append("<b>SP$LINEAGE:</b> " + SP_LINEAGE() + "<br>\n");
		for (Pair<String,String> link : SP_LINK())
			sb.append("<b>SP$LINK:</b> " + link.getKey() + " " + link.getValue() + "<br>\n");
		for (String sample : SP_SAMPLE())
				sb.append("<b>SP$SAMPLE:</b> " + sample + "<br>\n");
		sb.append("<hr>\n");
		
		sb.append("<b>AC$INSTRUMENT:</b> " + AC_INSTRUMENT() + "<br>\n");
		sb.append("<b>AC$INSTRUMENT_TYPE:</b> " + AC_INSTRUMENT_TYPE() + "<br>\n");
		sb.append("<b>AC$MASS_SPECTROMETRY:</b> MS_TYPE " + AC_MASS_SPECTROMETRY_MS_TYPE() + "<br>\n");
		sb.append("<b>AC$MASS_SPECTROMETRY:</b> ION_MODE " + AC_MASS_SPECTROMETRY_ION_MODE() + "<br>\n");
		for (Pair<String,String> ac_mass_spectrometry : AC_MASS_SPECTROMETRY())
			sb.append("<b>AC$MASS_SPECTROMETRY:</b> " + ac_mass_spectrometry.getKey() + " " + ac_mass_spectrometry.getValue() + "<br>\n");
		for (Pair<String,String> ac_chromatography : AC_CHROMATOGRAPHY())
			sb.append("<b>AC$CHROMATOGRAPHY:</b> " + ac_chromatography.getKey() + " " + ac_chromatography.getValue() + "<br>\n");
		sb.append("<hr>\n");
		
		for (Pair<String,String> ms_focued_ion : MS_FOCUSED_ION())
			sb.append("<b>MS$FOCUSED_ION:</b> " + ms_focued_ion.getKey() + " " + ms_focued_ion.getValue() + "<br>\n");
		for (Pair<String,String> ms_data_processing : MS_DATA_PROCESSING())
				sb.append("<b>MS$DATA_PROCESSING:</b> " + ms_data_processing.getKey() + " " + ms_data_processing.getValue() + "<br>\n");
		if (!MS_FOCUSED_ION().isEmpty() || !MS_DATA_PROCESSING().isEmpty()) sb.append("<hr>\n");
		
		sb.append("<b>PK$SPLASH:</b> <a href=\"http://www.google.com/search?q=" + PK_SPLASH() + "\" target=\"_blank\">" + PK_SPLASH() + "</a><br>\n");
		if (!PK_ANNOTATION_HEADER().isEmpty()) {
			sb.append("<b>PK$ANNOTATION:</b>");
			for (String annotation_header_item : PK_ANNOTATION_HEADER())
				sb.append(" " + annotation_header_item);
			sb.append("<br>\n");
			for (Pair<BigDecimal, List<String>> annotation_line :  pk_annotation) {
				sb.append("&nbsp&nbsp" + annotation_line.getLeft() + "&nbsp" + String.join("&nbsp", annotation_line.getRight()) + "<br>\n");
			}
		}
		sb.append("<b>PK$NUM_PEAK:</b> " + PK_NUM_PEAK() + "<br>\n");
		sb.append("<b>PK$PEAK:</b> m/z int. rel.int.<br>\n");
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			sb.append("&nbsp&nbsp" + peak.getLeft() + "&nbsp" + peak.getMiddle() + "&nbsp" + peak.getRight() + "<br>\n");
		}
		
		sb.append("//");

		return sb.toString();
	}
	
	public String createStructuredData() {
		StringBuilder sb = new StringBuilder();
		sb.append("<script type=\"application/ld+json\">\n");
		sb.append("[\n");
		sb.append("{\n");
		sb.append("\"identifier\": \""+ACCESSION()+"\",\n");
		sb.append("\"url\": \"https://massbank.eu/RecordDisplay?id="+ACCESSION()+"\",\n");
		sb.append("\"name\": \""+RECORD_TITLE().get(0)+"\",\n");
		if (CH_NAME().size() == 1)  sb.append("\"alternateName\": \""+ CH_NAME().get(0) +"\",\n");
		else if (CH_NAME().size() >= 1) sb.append("\"alternateName\": [\""+ String.join("\", \"", CH_NAME()) +"\"],\n");
		
		sb.append("\"molecularFormula\": \""+CH_FORMULA()+"\",\n");
		sb.append("\"monoisotopicMolecularWeight\": \""+CH_EXACT_MASS()+"\",\n");
		sb.append("\"inChI\": \""+CH_IUPAC()+"\",\n");
		sb.append("\"smiles\": \""+CH_SMILES()+"\",\n");
		sb.append("\"@context\": \"http://schema.org\",\n");
		sb.append("\"@type\": \"MolecularEntity\"\n");
		sb.append("},\n");
		sb.append("{\n");
		
		sb.append("\"identifier\": \""+ACCESSION()+"\",\n");
		sb.append("\"url\": \"https://massbank.eu/RecordDisplay?id="+ACCESSION()+"\",\n");
		sb.append("\"headline\": \""+RECORD_TITLE1()+"\",\n");
		sb.append("\"name\": \""+RECORD_TITLE().get(0)+"\",\n");
		String[] tokens	= DATE1();
		sb.append("\"datePublished\": \""+tokens[0].replace(".","-")+"\",\n");
		if(tokens.length >= 2) { sb.append("\"dateCreated\": \""+tokens[1].replace(".","-")+"\",\n"); }
		if(tokens.length == 3) { sb.append("\"dateModified\": \""+tokens[2].replace(".","-")+"\",\n"); }
		sb.append("\"license\": \"https://creativecommons.org/licenses\",\n");
		sb.append("\"citation\": \""+PUBLICATION()+"\",\n");
		if (COMMENT().size() == 1)  sb.append("\"comment\": \""+ COMMENT().get(0) +"\",\n");
		else if (COMMENT().size() >= 1) sb.append("\"comment\": [\""+ String.join("\", \"", COMMENT()) +"\"],\n");
		if (CH_NAME().size() == 1)  sb.append("\"alternateName\": \""+ CH_NAME().get(0) +"\",\n");
		else if (CH_NAME().size() >= 1) sb.append("\"alternateName\": [\""+ String.join("\", \"", CH_NAME()) +"\"],\n");
		
		sb.append("\"@context\": \"http://schema.org\",\n");
		sb.append("\"@type\": \"Dataset\"\n");
		sb.append("}\n");
		sb.append("]\n");
		sb.append("</script>");
		return sb.toString();
	}
	
	public String createPeakListForSpectrumViewer() {
        // convert a list of lists [[mz, int, rel.int], [...], ...]
        // to String "mz,rel.int@mz,rel.int@..."
		List<String> peaks = new ArrayList<>();
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			peaks.add(peak.getRight()+","+peak.getLeft());
		}
		return String.join("@", peaks);
	}
	
	public JSONObject createPeakListData() {
		JSONObject result = new JSONObject();
		JSONArray peaklist = new JSONArray();
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			peaklist.put(new JSONObject().put("intensity", peak.getRight()).put("mz", peak.getLeft()));
		}
		result.put("peaks", peaklist);
		return result;
	}
	
	public static class Structure{
		public final String CH_SMILES;
		public final String CH_IUPAC;
		public Structure(String CH_SMILES, String CH_IUPAC) {
			this.CH_SMILES	= CH_SMILES;
			this.CH_IUPAC	= CH_IUPAC;
		}
	}
	
	public static class Contributor{
		public final String ACRONYM;
		public final String SHORT_NAME;
		public final String FULL_NAME;
		public Contributor(String ACRONYM, String SHORT_NAME, String FULL_NAME) {
			this.ACRONYM	= ACRONYM;
			this.SHORT_NAME	= SHORT_NAME;
			this.FULL_NAME	= FULL_NAME;
		}
	}
	
	private static Map<String, String> listToMap(List<Pair<String, String>> list) {
		Map<String, String> map	= new HashMap<String, String>();
		for (Pair<String, String> pair : list) map.put(pair.getKey(), pair.getValue());
		return map;		
	}
}