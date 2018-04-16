package massbank;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
	
	private String accession;
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
	private String ac_instrument_type;
	private String ac_mass_spectrometry_ms_type;
	private String ac_mass_spectrometry_ion_mode;
	private List<Pair<String, String>> ac_mass_spectrometry;
	private List<Pair<String, String>> ac_chromatography;
	private List<Pair<String, String>> ms_focused_ion;
	private List<Pair<String, String>> ms_data_processing;
	private String pk_splash;
	private List<String> pk_annotation_header;
	private final List<List<String>> pk_annotation;
	private int pk_num_peak;
	private final List<List<Double>> pk_peak;
	
	public Record(String contributor) {
		this.contributor	= contributor;
		
		// set default values for optional fields
		copyright				= null;
		publication				= null;
		comment					= new ArrayList<String>();
		ch_link					= new ArrayList<Pair<String, String>>();
		sp_scientific_name		= null;
		sp_lineage				= null;
		sp_link					= new ArrayList<Pair<String, String>>();
		sp_sample				= new ArrayList<String>();
		ac_mass_spectrometry	= new ArrayList<Pair<String, String>>();
		ac_chromatography		= new ArrayList<Pair<String, String>>();
		ms_focused_ion			= new ArrayList<Pair<String, String>>();
		ms_data_processing		= new ArrayList<Pair<String, String>>();
		pk_annotation			= new ArrayList<List<String>>();
		
		// set default values for mandatory fields
		pk_num_peak				= -1;
		pk_peak					= new ArrayList<List<Double>>();
	}
	
	public void persist() throws SQLException {
		DatabaseManager db  = DatabaseManager.create();
		if (db != null) {
			db.persistAccessionFile(this);
		} else {
			System.out.println("Could not persist file because no connction to the database could be established" );
		}
	}
	public String CONTRIBUTOR() {
		return contributor;
	}
	
	public String ACCESSION() {
		return accession;
	}
	public void ACCESSION(String value) {
		accession	= value;
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
		InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(ch_iupac);
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
		return pk_splash;
	}
	public void PK_SPLASH(String value) {
		pk_splash=value;
	}

	public List<String> PK_ANNOTATION_HEADER() {
		return pk_annotation_header;
	}
	public void PK_ANNOTATION_HEADER(List<String> value) {
		pk_annotation_header=value;
	}

	// PK_ANNOTATION is a two-dimensional List
	public List<List<String>> PK_ANNOTATION() {
		return pk_annotation;
	}
	public void PK_ANNOTATION_ADD_LINE(List<String> value) {
		pk_annotation.add(value);
	}

	public Integer PK_NUM_PEAK() {
		return pk_num_peak;
	}
	public void PK_NUM_PEAK(Integer value) {
		pk_num_peak	= value;
	}

	// PK_PEAK is a two-dimensional List
	public List<List<Double>> PK_PEAK() {
		return pk_peak;
	}
	public void PK_PEAK_ADD_LINE(List<Double> value) {
		pk_peak.add(value);
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
		
		sb.append("CH$COMPOUND_CLASS: " + CH_COMPOUND_CLASS().get(0));
		for (String ch_compound_class : CH_COMPOUND_CLASS().subList(1, CH_COMPOUND_CLASS().size())) {
			sb.append("; " + ch_compound_class );
		}
		sb.append("\n");
				
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
		sb.append("AC$INSTRUMENT_TYPE: " + AC_INSTRUMENT_TYPE() + "\n");
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
		
		sb.append("//");

		return sb.toString();
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
}