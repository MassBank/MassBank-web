package massbank;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import net.sf.jniinchi.INCHI_RET;

public class Record {
	private final String contributor;
	
	private Map<String, Object> data = new HashMap<>();
	private String accession_code;
	private String accession_number;
	private String record_title;
	private LocalDate date;
	private String authors;
	private String license;
	private String copyright;
	private String publication;
	private List<String> comment;
	private List<String> ch_name;
	private List<String> ch_compound_class;
	private IMolecularFormula ch_formula;
	private Double ch_exact_mass;
	private IAtomContainer ch_smiles;
	private IAtomContainer ch_iupac;
	private List<Pair<String, String>> ch_link;
	private String sp_scientific_name;
	private String sp_lineage;
	private List<Pair<String, String>> sp_link;
	private List<String> sp_sample;
	private String ac_instrument;
	private List<?> ac_instrument_type;
	private String ac_instrument_type_konkat;
	private String ac_mass_spectrometry_ms_type;
	private String ac_mass_spectrometry_ion_mode;
	private List<Pair<String, String>> ac_mass_spectrometry;
	private List<Pair<String, String>> ac_chromatography;
	private List<Pair<String, String>> ms_focused_ion;
	private List<Pair<String, String>> ms_data_processing;
	
	public Record(String contributor) {
		this.contributor	= contributor;
	}
	
	public void persist() throws SQLException {
//		this.contributor = contributor;
		DatabaseManager db  = DatabaseManager.create();
		if (db != null) {
			db.persistAccessionFile(this);
		} else {
			DevLogger.printToDBLog("Could not persist file because no connction to the database could be established" );
		}
	}
	public String CONTRIBUTOR() {
		return contributor;
	}
//	public void CONTRIBUTOR(String value) {
//		contributor=value;
//	}
	
	public String ACCESSION_CODE() {
		return accession_code;
	}
	public void ACCESSION_CODE(String value) {
		accession_code=value;
	}
	public String ACCESSION_NUMBER() {
		return accession_number;
	}
	public void ACCESSION_NUMBER(String value) {
		accession_number=value;
	}
	public String ACCESSION() {
		if (accession_code==null || accession_number==null) return null;
		return accession_code+accession_number;
	}

	
	public String RECORD_TITLE() {
		return record_title;
	}
	public void RECORD_TITLE(String value) {
		this.record_title = value;
	}
	

	public LocalDate DATE() {
		return date;
	}
	public void DATE(LocalDate value) {
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
	

	public List<String> COMMENT() {
		return comment;
	}
	public void COMMENT(List<String> value) {
		comment=value;
	}

	
	public List<String> CH_NAME() {
		return ch_name;
	}
	public void CH_NAME(List<String> value) {
		ch_name=value;
	}
	

	public List<String> CH_COMPOUND_CLASS() {
		return ch_compound_class;
	}
	public void CH_COMPOUND_CLASS(List<String> value) {
		ch_compound_class=value;
	}

	
	public String CH_FORMULA() {
		return MolecularFormulaManipulator.getString(ch_formula);
	}
	public IMolecularFormula CH_FORMULA1() {
		return ch_formula;
	}
	public void CH_FORMULA(IMolecularFormula value) {
		ch_formula=value;
	}
	

	public Double CH_EXACT_MASS() {
		return ch_exact_mass;
	}
	public void CH_EXACT_MASS(Double value) {
		ch_exact_mass=value;
	}
	

	public String CH_SMILES() throws CDKException {
		if (ch_smiles.isEmpty()) return "N/A";
		SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.Isomeric);
		return smigen.create(ch_smiles);
	}
	public IAtomContainer CH_SMILES1() {
		return ch_smiles;
	}
	public void CH_SMILES(IAtomContainer value) {
		ch_smiles=value;
	}

	
	public String CH_IUPAC() throws CDKException {
		if (ch_iupac.isEmpty()) return "N/A";
		InChIGenerator gen = InChIGeneratorFactory.getInstance()
				.getInChIGenerator(ch_iupac);
		INCHI_RET ret = gen.getReturnStatus();
		if (ret == INCHI_RET.WARNING) {
			// Structure generated, but with warning message
			System.out.println("InChI warning: " + gen.getMessage());
		} else if (ret != INCHI_RET.OKAY) {
			// Structure generation failed
			throw new IllegalStateException(
					"Structure generation failed: " + ret.toString() + " [" + gen.getMessage() + "]");
		}
		return gen.getInchi();
	}
	public IAtomContainer CH_IUPAC1() {
		return ch_smiles;
	}
	public void CH_IUPAC(IAtomContainer value) {
		ch_iupac=value;
	}

	
	public List<Pair<String, String>> CH_LINK() {
		return ch_link;
	}
	public void CH_LINK(List<Pair<String, String>> value) {
		ch_link=value;
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
		sp_link=value;
	}
	

	public List<String> SP_SAMPLE() {
		return sp_sample;
	}
	public void SP_SAMPLE(List<String> value) {
		sp_sample=value;
	}

	
	public String AC_INSTRUMENT() {
		return ac_instrument;
	}
	public void AC_INSTRUMENT(String value) {
		ac_instrument=value;
	}

	public String AC_INSTRUMENT_TYPE_konkat() {
		return ac_instrument_type_konkat;
	}
	public void AC_INSTRUMENT_TYPE_konkat(String value) {
		this.ac_instrument_type_konkat	= value;
	}
	public List<?> AC_INSTRUMENT_TYPE() {
		return ac_instrument_type;
	}
	public void AC_INSTRUMENT_TYPE(List<?> value) {
		ac_instrument_type=value;
		
		List<String> list	= new ArrayList<String>();
		for(int idx = 0; idx < this.ac_instrument_type.size(); idx++) {
			if(this.ac_instrument_type.get(idx) instanceof String)
				// string
				list.add((String) this.ac_instrument_type.get(idx));
			if(this.ac_instrument_type.get(idx) instanceof List)
				// list of strings
				list.addAll((List<String>) this.ac_instrument_type.get(idx));
		}
		this.ac_instrument_type_konkat	= String.join("-", list.toArray(new String[list.size()]));
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
	public void AC_MASS_SPECTROMETRY(List<Pair<String, String>> value) {
		ac_mass_spectrometry=value;
	}


	public List<Pair<String, String>> AC_CHROMATOGRAPHY() {
		return ac_chromatography;
	}
	public void AC_CHROMATOGRAPHY(List<Pair<String, String>> value) {
		ac_chromatography=value;
	}
	

	public List<Pair<String, String>> MS_FOCUSED_ION() {
		return ms_focused_ion;
	}
	public void MS_FOCUSED_ION(List<Pair<String, String>> value) {
		ms_focused_ion=value;
	}
	

	public List<Pair<String, String>> MS_DATA_PROCESSING() {
		return ms_data_processing;
	}

	public void MS_DATA_PROCESSING(List<Pair<String, String>> value) {
		ms_data_processing=value;
	}

	public String PK_SPLASH() {
		return (String) data.get("PK_SPLASH");
	}

	public void PK_SPLASH(String value) {
		data.put("PK_SPLASH", value);
	}

	@SuppressWarnings("unchecked")
	public List<String> PK_ANNOTATION_HEADER() {
		return (List<String>) data.get("PK_ANNOTATION_HEADER");
	}
	public void ADD_PK_ANNOTATION_HEADER_ITEM(String value) {
		List<String> pk_annotation_header = PK_ANNOTATION_HEADER();
		if (pk_annotation_header == null) {
			pk_annotation_header = new ArrayList<>();
			data.put("PK_ANNOTATION_HEADER", pk_annotation_header);
		}
		pk_annotation_header.add(value);
	}

	@SuppressWarnings("unchecked")
	// PK_ANNOTATION is a two-dimensional List
	public List<List<String>> PK_ANNOTATION() {
		return (List<List<String>>) data.get("PK_ANNOTATION");
	}
	public void ADD_PK_ANNOTATION_LINE() {
		List<List<String>> pk_annotation = PK_ANNOTATION();
		if (pk_annotation == null) {
			ArrayList<ArrayList<String>> new_pk_annotation = new ArrayList<ArrayList<String>>();
			data.put("PK_ANNOTATION", new_pk_annotation);
			pk_annotation = PK_ANNOTATION();
		}
		pk_annotation.add(new ArrayList<String>());
	}

	public void ADD_PK_ANNOTATION_ITEM(String value) {
		List<List<String>> pk_annotation = PK_ANNOTATION();
		if (pk_annotation == null) {
			ADD_PK_ANNOTATION_LINE();
			pk_annotation = PK_ANNOTATION();
		}
		pk_annotation.get(pk_annotation.size() - 1).add(value);
	}

	public Integer PK_NUM_PEAK() {
		return (Integer) data.get("PK_NUM_PEAK");
	}
	public void PK_NUM_PEAK(Integer value) {
		data.put("PK_NUM_PEAK", value);
	}

	@SuppressWarnings("unchecked")
	// PK_PEAK is a two-dimensional List
	public List<List<Double>> PK_PEAK() {
		return (List<List<Double>>) data.get("PK_PEAK");
	}
	public void ADD_PK_PEAK_LINE() {
		List<List<Double>> pk_peak = PK_PEAK();
		if (pk_peak == null) {
			ArrayList<ArrayList<Double>> new_pk_peak = new ArrayList<ArrayList<Double>>();
			data.put("PK_PEAK", new_pk_peak);
			pk_peak = PK_PEAK();
		}
		pk_peak.add(new ArrayList<Double>());
	}

	public void ADD_PK_PEAK_ITEM(Double value) {
		List<List<Double>> pk_peak = PK_PEAK();
		if (pk_peak == null) {
			ADD_PK_PEAK_LINE();
			pk_peak = PK_PEAK();
		}
		pk_peak.get(pk_peak.size() - 1).add(value);
	}
	

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("ACCESSION: " + ACCESSION() + "\n");
		sb.append("RECORD_TITLE: " + RECORD_TITLE() + "\n");
		sb.append("DATE: " + DATE() + "\n");
		sb.append("AUTHORS: " + AUTHORS() + "\n");
		sb.append("LICENSE: " + LICENSE() + "\n");
		if (COPYRIGHT() != null)
			sb.append("COPYRIGHT: " + COPYRIGHT() + "\n");
		if (PUBLICATION() != null)
			sb.append("PUBLICATION: " + PUBLICATION() + "\n");
		if (COMMENT() != null) {
			for (String comment : COMMENT())
				sb.append("COMMENT: " + comment + "\n");
		}
		if (CH_NAME() != null) {
			for (String ch_name : CH_NAME())
				sb.append("CH$NAME: " + ch_name + "\n");
		}
		sb.append("CH$COMPOUND_CLASS: " + CH_COMPOUND_CLASS() + "\n");
		sb.append("CH$FORMULA: " + CH_FORMULA() + "\n");
		sb.append("CH$EXACT_MASS: " + CH_EXACT_MASS() + "\n");
		try {
			sb.append("CH$SMILES: " + CH_SMILES() + "\n");
		} catch (CDKException e) {
			System.err.println(e.getMessage());
			sb.append("CH$SMILES: null\n");
		}
		try {
			sb.append("CH$IUPAC: " + CH_IUPAC() + "\n");
		} catch (CDKException e) {
			System.err.println(e.getMessage());
			sb.append("CH$IUPAC: null\n");
		}
		if (CH_LINK() != null) {
			for (Pair<String,String> link : CH_LINK())
				sb.append("CH$LINK: " + link.getKey() + " " + link.getValue() + "\n");
		}
		if (SP_SCIENTIFIC_NAME() != null)
			sb.append("SP$SCIENTIFIC_NAME: " + SP_SCIENTIFIC_NAME() + "\n");
		if (SP_LINEAGE() != null)
			sb.append("SP$LINEAGE: " + SP_LINEAGE() + "\n");
		if (SP_LINK() != null) {
			for (Pair<String,String> link : SP_LINK())
				sb.append("SP$LINK: " + link.getKey() + " " + link.getValue() + "\n");
		}
		if (SP_SAMPLE() != null) {
			for (String sample : SP_SAMPLE())
				sb.append("SP$SAMPLE: " + sample + "\n");
		}
		sb.append("AC$INSTRUMENT: " + AC_INSTRUMENT() + "\n");
		sb.append("AC$INSTRUMENT_TYPE: " + AC_INSTRUMENT_TYPE().toString() + "\n");
		sb.append("AC$MASS_SPECTROMETRY: MS_TYPE: " + AC_MASS_SPECTROMETRY_MS_TYPE() + "\n");
		sb.append("AC$MASS_SPECTROMETRY: ION_MODE: " + AC_MASS_SPECTROMETRY_ION_MODE() + "\n");
		if (AC_MASS_SPECTROMETRY() != null) {
			for (Pair<String,String> ac_mass_spectrometry : AC_MASS_SPECTROMETRY())
				sb.append("AC$MASS_SPECTROMETRY: " + ac_mass_spectrometry.getKey() + " " + ac_mass_spectrometry.getValue() + "\n");
		}
		if (AC_CHROMATOGRAPHY() != null) {
			for (Pair<String,String> ac_chromatography : AC_CHROMATOGRAPHY())
				sb.append("AC$CHROMATOGRAPHY: " + ac_chromatography.getKey() + " " + ac_chromatography.getValue() + "\n");
		}
		if (MS_FOCUSED_ION() != null) {
			for (Pair<String,String> ms_focued_ion : MS_FOCUSED_ION())
				sb.append("MS$FOCUSED_ION: " + ms_focued_ion.getKey() + " " + ms_focued_ion.getValue() + "\n");
		}
		if (MS_DATA_PROCESSING() != null) {
			for (Pair<String,String> ms_data_processing : MS_DATA_PROCESSING())
				sb.append("MS$DATA_PROCESSING: " + ms_data_processing.getKey() + " " + ms_data_processing.getValue() + "\n");
		}
		sb.append("PK$SPLASH: " + PK_SPLASH() + "\n");
		
		if (PK_ANNOTATION_HEADER() != null) {
			sb.append("PK$ANNOTATION:");
			for (String annotation_header_item : PK_ANNOTATION_HEADER())
				sb.append(" " + annotation_header_item);
			sb.append("\n");
			for (List<String> annotation_line :  PK_ANNOTATION()) {
				sb.append(" ");
				for (String annotation_item : annotation_line )
					sb.append(" " + annotation_item);
				sb.append("\n");
			}
		}

		sb.append("PK$NUM_PEAK: " + PK_NUM_PEAK() + "\n");
		sb.append("PK$PEAK: m/z int. rel.int.\n");
		for (List<Double> peak_line :  PK_PEAK()) {
			sb.append(" ");
			for (Double peak_line_item : peak_line )
				sb.append(" " + peak_line_item.toString());
			sb.append("\n");
		}
		

		/*
		 * // PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm) //
		 * 57.0701 C4H9+ 1 57.0699 4.61 // 67.0542 C5H7+ 1 67.0542 0.35 // 69.0336
		 * C4H5O+ 1 69.0335 1.14 sb.append("PK$NUM_PEAK: " + this.PK$PEAK_MZ.length +
		 * "\n"); sb.append("PK$PEAK: m/z int. rel.int." + "\n"); for(int idx = 0; idx <
		 * this.PK$PEAK_MZ.length; idx++) sb.append("  " + this.PK$PEAK_MZ[idx] + " " +
		 * this.PK$PEAK_INT[idx] + " " + this.PK$PEAK_REL[idx] + "\n");
		 */
		sb.append("//");

		return sb.toString();
	}

}
