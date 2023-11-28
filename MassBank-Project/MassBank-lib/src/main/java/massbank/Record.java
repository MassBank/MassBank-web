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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.dan2097.jnainchi.InchiStatus;

/**
 * This class keeps all data of a record.
 * @author rmeier
 * @version 01-12-2022
 */
public class Record {
	private static final Logger logger = LogManager.getLogger(Record.class);

	private String contributor;
	private String ACCESSION;
	private Instant timestamp;
	private boolean deprecated;
	private String deprecated_content;
	private List<String> RECORD_TITLE;
	private String DATE;
	private String AUTHORS;
	private String LICENSE;	
	private String COPYRIGHT; // optional
	private String PUBLICATION; // optional
	private String PROJECT; // optional
	private List<String> COMMENT = new ArrayList<String>(); // optional
	private List<String> CH$NAME;
	private List<String> CH$COMPOUND_CLASS; // optional
	private String CH$FORMULA;
	private BigDecimal CH$EXACT_MASS;
	private String CH$SMILES;
	private String CH$IUPAC;
	private LinkedHashMap<String, String> CH$LINK; // optional
	private String SP$SCIENTIFIC_NAME; // optional
	private String SP$LINEAGE; // optional
	private LinkedHashMap<String, String> SP$LINK; // optional
	private List<String> SP$SAMPLE; // optional
	private String AC$INSTRUMENT;
	private String AC$INSTRUMENT_TYPE;
	private String AC$MASS_SPECTROMETRY_MS_TYPE;
	private String AC$MASS_SPECTROMETRY_ION_MODE;
	private List<Pair<String, String>> AC$MASS_SPECTROMETRY; // optional
	private List<Pair<String, String>> AC$CHROMATOGRAPHY; // optional
	private List<Pair<String, String>> MS$FOCUSED_ION; // optional
	private List<Pair<String, String>> MS$DATA_PROCESSING; // optional
	private String PK$SPLASH;
	private List<String> PK$ANNOTATION_HEADER; // optional
	private List<Pair<BigDecimal, List<String>>> PK$ANNOTATION; // optional
	private List<Triple<BigDecimal,BigDecimal,Integer>> PK$PEAK;
	
	public Record() {
		contributor = new String();
		ACCESSION = new String();
		deprecated = false;
		deprecated_content = new String();
		RECORD_TITLE = new ArrayList<String>();
		DATE = new String();
		AUTHORS = new String();
		LICENSE = new String();
		COPYRIGHT = new String(); // optional
		PUBLICATION = new String(); // optional
		PROJECT = new String(); // optional
		COMMENT = new ArrayList<String>(); // optional
		CH$NAME = new ArrayList<String>();
		CH$COMPOUND_CLASS = new ArrayList<String>();
		CH$FORMULA = new String();
		CH$EXACT_MASS = new BigDecimal(0);
		CH$SMILES = new String();
		CH$IUPAC = new String();
		CH$LINK = new LinkedHashMap<String, String>(); // optional
		SP$SCIENTIFIC_NAME = new String(); // optional
		SP$LINEAGE = new String(); // optional
		SP$LINK = new LinkedHashMap<String, String>(); // optional
		SP$SAMPLE = new ArrayList<String>(); // optional
		AC$INSTRUMENT = new String();
		AC$INSTRUMENT_TYPE = new String();
		AC$MASS_SPECTROMETRY_MS_TYPE = new String();
		AC$MASS_SPECTROMETRY_ION_MODE = new String();
		AC$MASS_SPECTROMETRY = new ArrayList<Pair<String, String>>(); // optional
		AC$CHROMATOGRAPHY = new ArrayList<Pair<String, String>>(); // optional
		MS$FOCUSED_ION = new ArrayList<Pair<String, String>>(); // optional
		MS$DATA_PROCESSING = new ArrayList<Pair<String, String>>(); // optional
		PK$SPLASH = new String();
		PK$ANNOTATION_HEADER = new ArrayList<String>(); // optional
		PK$ANNOTATION = new ArrayList<Pair<BigDecimal, List<String>>>(); // optional
		PK$PEAK = new ArrayList<Triple<BigDecimal,BigDecimal,Integer>>();
	}
	
	public String CONTRIBUTOR() {
		return contributor;
	}

	
	public String ACCESSION() {
		return ACCESSION;
	}
	public void ACCESSION(String value) {
		String[] splitedAccession = value.split("-");
		contributor=splitedAccession[1];
		ACCESSION = value;
	}
	
	
	public Instant getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Instant value) {
		timestamp = value;
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
		return RECORD_TITLE;
	}
	public String RECORD_TITLE1() {
		return String.join("; ", RECORD_TITLE);
	}
	public void RECORD_TITLE(List<String> value) {
		RECORD_TITLE = value;
	}
	public void RECORD_TITLE1(String value) {
		RECORD_TITLE = new ArrayList<String>(Arrays.asList(value.split("; ")));
	}
	
	
	public String DATE() {
		return DATE;
	}
	public String[] DATE1() {
		// DATE: 2016.01.15
		// DATE: 2011.02.21 (Created 2007.07.07)
		// DATE: 2016.01.19 (Created 2006.12.21, modified 2011.05.06)
		return DATE.replace("(Created ", "").replace(", modified", "").replace(")", "").split(" ");
	}
	public void DATE(String value) {
		DATE=value;
	}
	
	
	public String AUTHORS() {
		return AUTHORS;
	}
	public void AUTHORS(String value) {
		AUTHORS=value;
	}
	
	
	public String LICENSE() {
		return LICENSE;
	}
	public void LICENSE(String value) {
		LICENSE=value;
	}
	
	
	public String COPYRIGHT() {
		return COPYRIGHT;
	}
	public void COPYRIGHT(String value) {
		COPYRIGHT= value;
	}
	
	
	public String PUBLICATION() {
		return PUBLICATION;
	}
	public void PUBLICATION(String value) {
		PUBLICATION=value;
	}
	
	
	public String PROJECT() {
		return PROJECT;
	}
	public void PROJECT(String value) {
		PROJECT=value;
	}


	public List<String> COMMENT() {
		return COMMENT;
	}
	public void COMMENT(List<String> value) {
		COMMENT=new ArrayList<String>(value);
	}
	
	
	public List<String> CH_NAME() {
		return CH$NAME;
	}
	public void CH_NAME(List<String> value) {
		CH$NAME=new ArrayList<String>(value);
	}
	
	
	public List<String> CH_COMPOUND_CLASS() {
		return CH$COMPOUND_CLASS;
	}
	public void CH_COMPOUND_CLASS(List<String> value) {
		CH$COMPOUND_CLASS=new ArrayList<String>(value);
	}
	
	/**
	* Returns the molecular formula as an String.
	*/
	public String CH_FORMULA() {
		return CH$FORMULA;
	}
	/**
	* Returns the molecular formula as an String with HTML sup tags.
	*/
	public String CH_FORMULA1() {
		IMolecularFormula m = MolecularFormulaManipulator.getMolecularFormula(CH$FORMULA, SilentChemObjectBuilder.getInstance());
		return MolecularFormulaManipulator.getHTML(m);
	}
	public void CH_FORMULA(String value) {
		CH$FORMULA=value;
	}
	
	
	public BigDecimal CH_EXACT_MASS() {
		return CH$EXACT_MASS;
	}
	public void CH_EXACT_MASS(BigDecimal value) {
		CH$EXACT_MASS=value;
	}
	
	
	public String CH_SMILES() {
		return CH$SMILES;
	}
	public IAtomContainer CH_SMILES_obj() {
		if ("N/A".equals(CH$SMILES)) return SilentChemObjectBuilder.getInstance().newAtomContainer();
		try {
			return new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(CH$SMILES);
		} catch (InvalidSmilesException e) {
			logger.error("Structure generation from SMILES failed. Error: \""+ e.getMessage() + "\" for \"" + CH$SMILES + "\".");
			return SilentChemObjectBuilder.getInstance().newAtomContainer();
		}
	}
	public void CH_SMILES(String value) {
		CH$SMILES=value;
	}
	
	
	public String CH_IUPAC() {
		return CH$IUPAC;
	}
	public IAtomContainer CH_IUPAC_obj() {
		if ("N/A".equals(CH$IUPAC)) return SilentChemObjectBuilder.getInstance().newAtomContainer();
		try {
			// Get InChIToStructure
			InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(CH$IUPAC, SilentChemObjectBuilder.getInstance());
			InchiStatus ret = intostruct.getStatus();
			if (ret == InchiStatus.WARNING) {
				// Structure generated, but with warning message
				logger.warn("InChI warning: \"" + intostruct.getMessage() + "\" converting \"" + CH$IUPAC + "\".");
			} 
			else if (ret == InchiStatus.ERROR) {
				// Structure generation failed
				logger.error("Structure generation failed: " + intostruct.getMessage() + " converting \"" + CH$IUPAC + "\".");
				return  SilentChemObjectBuilder.getInstance().newAtomContainer();
			}
			return intostruct.getAtomContainer();
		} catch (CDKException e) {
			logger.error("Structure generation from InChI failed. Error: \""+ e.getMessage() + "\" for \"" + CH$IUPAC + "\".");
			return  SilentChemObjectBuilder.getInstance().newAtomContainer();
		}		 			
	}
	public void CH_IUPAC(String value) {
		CH$IUPAC=value;
	}
		
	public LinkedHashMap<String, String> CH_LINK() {
		return CH$LINK;
	}
	public void CH_LINK(LinkedHashMap<String, String> value) {
		CH$LINK=value;
	}

	public String SP_SCIENTIFIC_NAME() {
		return SP$SCIENTIFIC_NAME;
	}
	public void SP_SCIENTIFIC_NAME(String value) {
		SP$SCIENTIFIC_NAME=value;
	}
	
	public String SP_LINEAGE() {
		return SP$LINEAGE;
	}
	public void SP_LINEAGE(String value) {
		SP$LINEAGE=value;
	}
 

	public LinkedHashMap<String, String> SP_LINK() {
		return SP$LINK;
	}
	public void SP_LINK(LinkedHashMap<String, String> value) {
		SP$LINK=value;
	}

	public List<String> SP_SAMPLE() {
		return SP$SAMPLE;
	}
	public void SP_SAMPLE(List<String> value) {
		SP$SAMPLE=new ArrayList<String>(value);
	}
	
	public String AC_INSTRUMENT() {
		return AC$INSTRUMENT;
	}
	public void AC_INSTRUMENT(String value) {
		AC$INSTRUMENT=value;
	}

	public String AC_INSTRUMENT_TYPE() {
		return AC$INSTRUMENT_TYPE;
	}
	public void AC_INSTRUMENT_TYPE(String value) {
		this.AC$INSTRUMENT_TYPE	= value;
	}
	
	public String AC_MASS_SPECTROMETRY_MS_TYPE() {
		return AC$MASS_SPECTROMETRY_MS_TYPE;
	}
	public void AC_MASS_SPECTROMETRY_MS_TYPE(String value) {
		AC$MASS_SPECTROMETRY_MS_TYPE=value;
	}
	
	public String AC_MASS_SPECTROMETRY_ION_MODE() {
		return AC$MASS_SPECTROMETRY_ION_MODE;
	}
	public void AC_MASS_SPECTROMETRY_ION_MODE(String value) {
		AC$MASS_SPECTROMETRY_ION_MODE=value;
	}
	
	public List<Pair<String, String>> AC_MASS_SPECTROMETRY() {
		return AC$MASS_SPECTROMETRY;
	}
	public Map<String, String> AC_MASS_SPECTROMETRY_asMap() {
		return listToMap(AC$MASS_SPECTROMETRY);
	}
	public void AC_MASS_SPECTROMETRY(List<Pair<String, String>> value) {
		AC$MASS_SPECTROMETRY=new ArrayList<Pair<String, String>>(value);
	}

	public List<Pair<String, String>> AC_CHROMATOGRAPHY() {
		return AC$CHROMATOGRAPHY;
	}
	public Map<String, String> AC_CHROMATOGRAPHY_asMap() {
		return listToMap(AC$CHROMATOGRAPHY);
	}
	public void AC_CHROMATOGRAPHY(List<Pair<String, String>> value) {
		AC$CHROMATOGRAPHY=new ArrayList<Pair<String, String>>(value);
	}
	
	public List<Pair<String, String>> MS_FOCUSED_ION() {
		return MS$FOCUSED_ION;
	}
	public Map<String, String> MS_FOCUSED_ION_asMap() {
		return listToMap(MS$FOCUSED_ION);
	}
	public void MS_FOCUSED_ION(List<Pair<String, String>> value) {
		MS$FOCUSED_ION=new ArrayList<Pair<String, String>>(value);
	}
	
	public List<Pair<String, String>> MS_DATA_PROCESSING() {
		return MS$DATA_PROCESSING;
	}
	public void MS_DATA_PROCESSING(List<Pair<String, String>> value) {
		MS$DATA_PROCESSING=new ArrayList<Pair<String, String>>(value);
	}

	public String PK_SPLASH() {
		return PK$SPLASH;
	}
	public void PK_SPLASH(String value) {
		PK$SPLASH=value;
	}

	public List<String> PK_ANNOTATION_HEADER() {
		return PK$ANNOTATION_HEADER;
	}
	public void PK_ANNOTATION_HEADER(List<String> value) {
		PK$ANNOTATION_HEADER=new ArrayList<String>(value);
	}

	// PK_ANNOTATION is a two-dimensional List
	public List<Pair<BigDecimal, List<String>>> PK_ANNOTATION() {
		return PK$ANNOTATION;
	}
	public void PK_ANNOTATION_ADD_LINE(Pair<BigDecimal, List<String>> annotation) {
		PK$ANNOTATION.add(annotation);
	}

	public int PK_NUM_PEAK() {
		return PK$PEAK.size();
	}

	// PK_PEAK is a List with Triple values M/Z, intensity, rel. intensity
	public List<Triple<BigDecimal,BigDecimal,Integer>> PK_PEAK() {
		return PK$PEAK;
	}
	public void PK_PEAK_ADD_LINE(Triple<BigDecimal,BigDecimal,Integer> peak) {
		PK$PEAK.add(peak);
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
		if (!"".equals(COPYRIGHT()))
			sb.append("COPYRIGHT: " + COPYRIGHT() + "\n");
		if (!"".equals(PUBLICATION()))
			sb.append("PUBLICATION: " + PUBLICATION() + "\n");
		if (!"".equals(PROJECT()))
			sb.append("PROJECT: " + PROJECT() + "\n");
		for (String comment : COMMENT())
			sb.append("COMMENT: " + comment + "\n");
		
		for (String ch_name : CH_NAME())
			sb.append("CH$NAME: " + ch_name + "\n");
		if (!CH_COMPOUND_CLASS().isEmpty()) {
			sb.append("CH$COMPOUND_CLASS: " + String.join("; ", CH_COMPOUND_CLASS()) + "\n");
		}
		sb.append("CH$FORMULA: " + CH_FORMULA() + "\n");
		sb.append("CH$EXACT_MASS: " + CH_EXACT_MASS() + "\n");
		sb.append("CH$SMILES: " + CH_SMILES() + "\n");
		sb.append("CH$IUPAC: " + CH_IUPAC() + "\n");
		CH_LINK().forEach((key,value) -> {
			sb.append("CH$LINK: " + key + " " + value + "\n");		    
		});
		
		if (!"".equals(SP_SCIENTIFIC_NAME()))
			sb.append("SP$SCIENTIFIC_NAME: " + SP_SCIENTIFIC_NAME() + "\n");
		if (!"".equals(SP_LINEAGE()))
			sb.append("SP$LINEAGE: " + SP_LINEAGE() + "\n");
		SP_LINK().forEach((key,value) -> {
			sb.append("SP$LINK: " + key + " " + value + "\n");		    
		});
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
		if (LICENSE().equals("CC0")) {
			sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/publicdomain/zero/1.0/\" target=\"_blank\">CC0</a><br>\n");
		} else if (LICENSE().equals("CC BY")) {
			sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/licenses/by/4.0/\" target=\"_blank\">CC BY</a><br>\n");
		} else if (LICENSE().equals("CC BY-SA")) {
			sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/licenses/by-sa/4.0/\" target=\"_blank\">CC BY-SA</a><br>\n");
		} else if (LICENSE().equals("CC BY-NC")) {
			sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/licenses/by-nc/4.0/\" target=\"_blank\">CC BY-NC</a><br>\n");
		} else if (LICENSE().equals("CC BY-NC-SA")) {
			sb.append("<b>LICENSE:</b> <a href=\"https://creativecommons.org/licenses/by-nc-sa/4.0/\" target=\"_blank\">CC BY-NC-SA</a><br>\n");
		} else if (LICENSE().equals("dl-de/by-2-0")) {
			sb.append("<b>LICENSE:</b> <a href=\"https://www.govdata.de/dl-de/by-2-0\" target=\"_blank\">dl-de/by-2-0</a><br>\n");
		} else {
			sb.append("<b>LICENSE:</b> "+ LICENSE() + "<br>\n");
		}
		if (!"".equals(COPYRIGHT()))
			sb.append("<b>COPYRIGHT:</b> " + COPYRIGHT() + "<br>\n");
		if (!"".equals(PUBLICATION())) {
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
		if (!"".equals(PROJECT()))
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
		CH_LINK().forEach((key,value) -> {
			switch(key){
				case "CAS":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.google.com/search?q=&quot;" + value + "&quot;\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "CAYMAN":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.caymanchem.com/product/" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "CHEBI":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "CHEMSPIDER":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.chemspider.com/" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "COMPTOX":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://comptox.epa.gov/dashboard/dsstoxdb/results?search=" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "HMDB":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"http://www.hmdb.ca/metabolites/" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "INCHIKEY":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.google.com/search?q=&quot;" + value + "&quot;\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "KAPPAVIEW":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"http://kpv.kazusa.or.jp/kpv4/compoundInformation/view.action?id=" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "KEGG":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.genome.jp/dbget-bin/www_bget?cpd:" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "KNAPSACK":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"http://www.knapsackfamily.com/knapsack_jsp/information.jsp?sname=C_ID&word=" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;

				case "LIPIDBANK":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"http://lipidbank.jp/cgi-bin/detail.cgi?id=" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "LIPIDMAPS":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://www.lipidmaps.org/data/LMSDRecord.php?LMID=" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "NIKKAJI":
					sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://jglobal.jst.go.jp/en/redirect?Nikkaji_No=" + value + "\" target=\"_blank\">" + value + "</a><br>\n");
					break;
				case "PUBCHEM":{
					if(value.startsWith("CID:")) sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://pubchem.ncbi.nlm.nih.gov/compound/" + value.substring("CID:".length()) + "\" target=\"_blank\">" + value + "</a><br>\n");
					else if(value.startsWith("SID:")) sb.append("<b>CH$LINK:</b> " + key + " <a href=\"https://pubchem.ncbi.nlm.nih.gov/substance/" + value.substring("SID:".length()) + "\" target=\"_blank\">" + value + "</a><br>\n");
					else sb.append("<b>CH$LINK:</b> " + key + " " + value + "<br>\n");
					break;
				}
				default:
					sb.append("<b>CH$LINK:</b> " + key + " " + value + "<br>\n");
			}
		});
		
		if (!"".equals(SP_SCIENTIFIC_NAME()))
			sb.append("<b>SP$SCIENTIFIC_NAME:</b> " + SP_SCIENTIFIC_NAME() + "<br>\n");
		if (!"".equals(SP_LINEAGE()))
			sb.append("<b>SP$LINEAGE:</b> " + SP_LINEAGE() + "<br>\n");
		SP_LINK().forEach((key,value) -> {
			sb.append("<b>SP$LINK:</b> " + key + " " + value + "<br>\n");
		});
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
			for (Pair<BigDecimal, List<String>> annotation_line :  PK$ANNOTATION) {
				sb.append("&nbsp;&nbsp;" + annotation_line.getLeft() + "&nbsp;" + String.join("&nbsp;", annotation_line.getRight()) + "<br>\n");
  		}
		}
		sb.append("<b>PK$NUM_PEAK:</b> " + PK_NUM_PEAK() + "<br>\n");
		sb.append("<b>PK$PEAK:</b> m/z int. rel.int.<br>\n");
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			sb.append("&nbsp;&nbsp;" + peak.getLeft() + "&nbsp;" + peak.getMiddle() + "&nbsp;" + peak.getRight() + "<br>\n");
		}
		
		sb.append("//");

		return sb.toString();
	}
	
//	[
//	{
//	"identifier": "LQB00001",
//	"url": "https://massbank.eu/MassBank/RecordDisplay?id=LQB00001",
//	"name": "Cer[AP] t34:0",
//	"alternateName": "Cer[AP] t34:0",
//	"inchikey": "RHIXBFQKTNYVCX-UHFFFAOYSA-N",
//	"description": "This MassBank record with Accession LQB00001 contains the MS2 mass spectrum of 'Cer[AP] t34:0'.",
//	"molecularFormula": "C34H69NO5",
//	"monoisotopicMolecularWeight": "571.928",
//	"inChI": "InChI=1S/C34H69NO5/c1-3-5-7-9-11-13-15-17-19-21-23-25-27-31(37)33(39)30(29-36)35-34(40)32(38)28-26-24-22-20-18-16-14-12-10-8-6-4-2/h30-33,36-39H,3-29H2,1-2H3,(H,35,40)",
//	"smiles": "CCCCCCCCCCCCCCC(O)C(O)C(CO)NC(=O)C(O)CCCCCCCCCCCCCC",
//	"@context": "http://schema.org",
//	"@type": "MolecularEntity"
//	},
//	{
//	"identifier": "LQB00001",
//	"url": "https://massbank.eu/MassBank/RecordDisplay?id=LQB00001",
//	"headline": "Cer[AP] t34:0; LC-ESI-QTOF; MS2",
//	"name": "Cer[AP] t34:0",
//	"description": "This MassBank record with Accession LQB00001 contains the MS2 mass spectrum of 'Cer[AP] t34:0'.",
//	"datePublished": "2016-10-03",
//	"license": "https://creativecommons.org/licenses",
//	"citation": "null",
//	"comment": "Found in mouse small intestine; TwoDicalId=238; MgfFile=160907_Small_Intestine_normal_Neg_01_never; MgfId=1081",
//	"alternateName": "Cer[AP] t34:0",
//	"@context": "http://schema.org",
//	"@type": "Dataset"
//	}
//	]
	
//	Thanks for the contribution of markup within MassBank. As discussed in PR 274 there are some refinements that should be made.
//
//	Add DataCatalog and Dataset markup to the landing page https://massbank.eu/MassBank/
//	Use DataRecord instead of Dataset on MassBank Record pages such as LQB00001
//
//	    Replace the value in the @type property so that it is DataRecord instead of Dataset
//	    Include the comment text with the schema:description property
//
//	Include the comment text with the schema:description property
//
//	    Include the chemical image with the schema:image property
//
//	You should ensure that there are different identifiers used for the DataRecord (currently Dataset) and the MolecularEntity.
//
//	Once you've made these refinements, we'll be able to add you to the DataRecord, Dataset, and DataCatalog list of live deploys.

	//https://github.com/BioSchemas/specifications/issues/198
	
	public JsonArray createStructuredDataJsonArray() {
		if (DEPRECATED()) {
			return new JsonArray();
		}
		String InChiKey = CH_LINK().get("INCHIKEY");
		String description = "This MassBank record with Accession " + ACCESSION() 
			+ " contains the " + AC_MASS_SPECTROMETRY_MS_TYPE() + " mass spectrum of " + RECORD_TITLE().get(0)
			+ ((InChiKey==null) ? "." : " with the InChIkey " + InChiKey + ".");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// dataset
		JsonObject dataset = new JsonObject();
		dataset.addProperty("@context", "https://schema.org");
		dataset.addProperty("@type", "Dataset");
		dataset.add("http://purl.org/dc/terms/conformsTo",
				gson.fromJson("{ \"@type\": \"CreativeWork\", \"@id\": \"https://bioschemas.org/profiles/Dataset/1.0-RELEASE\" }", JsonObject.class));
		dataset.addProperty("@id", "https://massbank.eu/MassBank/RecordDisplay?id="+ACCESSION()+"#Dataset");
		dataset.addProperty("description", description);
		dataset.addProperty("identifier", ACCESSION());
		dataset.addProperty("name", RECORD_TITLE1());
		
		JsonArray keywords = new JsonArray();
		keywords.add(gson.fromJson(
				"{ \"@type\": \"DefinedTerm\","
				+ "\"name\": \"Mass spectrometry data\","
				+ "\"url\": \"http://edamontology.org/data_2536\","
				+ "\"termCode\": \"data_2536\","
				+ "\"inDefinedTermSet\": {"
				+ "\"@type\": \"DefinedTermSet\",\n"
				+ "\"name\": \"Bioinformatics operations, data types, formats, identifiers and topics\",\n"
				+ "\"url\": \"http://edamontology.org\"\n"
				+ "} }", JsonObject.class));
		dataset.add("keywords", keywords);
		
		if (LICENSE().equals("CC0")) {
			dataset.addProperty("license", "ttps://creativecommons.org/publicdomain/zero/1.0/");
		} else if (LICENSE().equals("CC BY")) {
			dataset.addProperty("license", "https://creativecommons.org/licenses/by/4.0/");
		} else if (LICENSE().equals("CC BY-SA")) {
			dataset.addProperty("license", "https://creativecommons.org/licenses/by-sa/4.0");
		}  else if (LICENSE().equals("CC BY-NC")) {
			dataset.addProperty("license", "https://creativecommons.org/licenses/by-nc/4.0");
		} else if (LICENSE().equals("CC BY-NC-SA")) {
			dataset.addProperty("license", "https://creativecommons.org/licenses/by-nc-sa/4.0");
		} else if (LICENSE().equals("dl-de/by-2-0")) {
			dataset.addProperty("license", "https://www.govdata.de/dl-de/by-2-0");
		}
		
		dataset.addProperty("url", "https://massbank.eu/MassBank/RecordDisplay?id="+ACCESSION());
		dataset.addProperty("datePublished", DATE1()[0].replace(".","-"));
		dataset.addProperty("citation", PUBLICATION());
		
		JsonArray measurementTechnique = new JsonArray();
		measurementTechnique.add(gson.fromJson(
				"{\"@type\": \"DefinedTerm\","
				+ "\"name\": \"liquid chromatography-mass spectrometry\","
				+ "\"url\": \"http://purl.obolibrary.org/obo/CHMO_0000524\","
				+ "\"termCode\": \"CHMO_0000524\","
				+ "\"inDefinedTermSet\": {"
				+ "\"@type\": \"DefinedTermSet\","
				+ "\"name\": \"Chemical Methods Ontology\","
				+ "\"url\": \"http://purl.obolibrary.org/obo/chmo.owl\""
				+ "} }", JsonObject.class));
		dataset.add("measurementTechnique", measurementTechnique);
		
		dataset.add("includedinDataCatalog", gson.fromJson(
				"{\"@type\": \"DataCatalog\","
				+ "\"name\": \"MassBank\","
				+ "\"url\": \"https://massbank.eu\""
				+ "}", JsonObject.class));
		
		JsonObject chemicalSubstance = new JsonObject();
		chemicalSubstance.addProperty("@context", "https://schema.org");
		chemicalSubstance.addProperty("@type", "ChemicalSubstance");
		chemicalSubstance.add("http://purl.org/dc/terms/conformsTo",
				gson.fromJson("{ \"@type\": \"CreativeWork\", \"@id\": \"https://bioschemas.org/profiles/ChemicalSubstance/0.4-RELEASE\" }", JsonObject.class));
		chemicalSubstance.addProperty("@id", "https://massbank.eu/MassBank/RecordDisplay?id="+ACCESSION()+"#ChemicalSubstance");
		chemicalSubstance.addProperty("identifier", ACCESSION());
		chemicalSubstance.addProperty("name", RECORD_TITLE().get(0));
		chemicalSubstance.addProperty("url", "https://massbank.eu/MassBank/RecordDisplay?id="+ACCESSION());
		chemicalSubstance.addProperty("chemicalComposition", CH_FORMULA());
		if (CH_NAME().size() == 1)  chemicalSubstance.addProperty("alternateName", CH_NAME().get(0));
		else if (CH_NAME().size() >= 1) chemicalSubstance.add("alternateName", gson.toJsonTree(CH_NAME()));
		
		JsonArray molecularEntitys = new JsonArray();
		
		// create a loop in case of multiple MolecularEntity
		JsonObject molecularEntity = new JsonObject();
		molecularEntity.addProperty("@context", "https://schema.org");
		molecularEntity.addProperty("@type", "MolecularEntity");
		molecularEntity.add("http://purl.org/dc/terms/conformsTo",
				gson.fromJson("{ \"@type\": \"CreativeWork\", \"@id\": \"https://bioschemas.org/profiles/MolecularEntity/0.5-RELEASE\" }", JsonObject.class));
		molecularEntity.addProperty("@id", "https://massbank.eu/MassBank/RecordDisplay?id=" + ACCESSION()
				+ "#" + (InChiKey!=null ? InChiKey : "MolecularEntity"));
		molecularEntity.addProperty("identifier", ACCESSION());
		molecularEntity.addProperty("name", RECORD_TITLE().get(0));
		molecularEntity.addProperty("url", "https://massbank.eu/MassBank/RecordDisplay?id="+ACCESSION());
		if (!CH_IUPAC().equals("N/A")) molecularEntity.addProperty("inChI", CH_IUPAC());
		if (!CH_SMILES().equals("N/A")) molecularEntity.addProperty("smiles", CH_SMILES());
		molecularEntity.addProperty("molecularFormula", CH_FORMULA());
		molecularEntity.addProperty("monoisotopicMolecularWeight", CH_EXACT_MASS());
		if (InChiKey!=null) molecularEntity.addProperty("inChIKey", InChiKey);
		
		molecularEntitys.add(molecularEntity);
		chemicalSubstance.add("hasBioChemEntityPart", molecularEntitys);
		
		// put MolecularEntity and Dataset together
		JsonArray structuredData = new JsonArray();
		structuredData.add(dataset);
		structuredData.add(chemicalSubstance);
		return structuredData;

	}
	
	public String createStructuredData() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(createStructuredDataJsonArray());
	}
	

	public String createPeakListForSpectrumViewer() {
        // convert a list of lists [[mz, int, rel.int], [...], ...]
        // to String "mz,rel.int@mz,rel.int@..."
		List<String> peaks = new ArrayList<>();
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			peaks.add(peak.getLeft()+","+peak.getRight());
		}
		return String.join("@", peaks);
	}
	
	public JsonObject createPeakListData() {
		JsonObject result = new JsonObject();
		JsonArray peaklist = new JsonArray();
		for (Triple<BigDecimal,BigDecimal,Integer> peak : PK_PEAK()) {
			JsonObject jsonPeak = new JsonObject();
			jsonPeak.addProperty("intensity",peak.getRight());
			jsonPeak.addProperty("mz", peak.getLeft());
			peaklist.add(jsonPeak);
		}
		result.add("peaks", peaklist);
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
