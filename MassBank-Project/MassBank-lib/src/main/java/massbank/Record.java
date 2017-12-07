package massbank;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<String,Object> data = new HashMap<>();
	
	public Record() {
	}
	
	public String ACCESSION() {
		return (String) data.get("ACCESSION");
	}
	public void ACCESSION(String value) {
		data.put("ACCESSION", value);
	}
	
	public String RECORD_TITLE() {
		return (String) data.get("RECORD_TITLE");
	}
	public void RECORD_TITLE(String value) {
		data.put("RECORD_TITLE", value);
	}
	
	public LocalDate DATE() {
		return (LocalDate) data.get("DATE");
	}
	public void DATE(LocalDate value) {
		data.put("DATE", value);
	}
	
	public String AUTHORS() {
		return (String) data.get("AUTHORS");
	}
	public void AUTHORS(String value) {
		data.put("AUTHORS", value);
	}
	
	public String LICENSE() {
		return (String) data.get("LICENSE");
	}
	public void LICENSE(String value) {
		data.put("LICENSE", value);
	}
	
	public String COPYRIGHT() {
		return (String) data.get("COPYRIGHT");
	}
	public void COPYRIGHT(String value) {
		data.put("COPYRIGHT", value);
	}
	
	public String PUBLICATION() {
		return (String) data.get("PUBLICATION");
	}
	public void PUBLICATION(String value) {
		data.put("PUBLICATION", value);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> COMMENT() {
		return (List<String>) data.get("COMMENT");
	}
	public void COMMENT(List<String> value) {
		data.put("COMMENT", value);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> CH_NAME() {
		return (List<String>) data.get("CH_NAME");
	}
	public void CH_NAME(List<String> value) {
		data.put("CH_NAME", value);
	}
	
	public String CH_COMPOUND_CLASS() {
		return (String) data.get("CH_COMPOUND_CLASS");
	}
	public void CH_COMPOUND_CLASS(String value) {
		data.put("CH_COMPOUND_CLASS", value);
	}
	
	public String CH_FORMULA() {
		return MolecularFormulaManipulator.getString((IMolecularFormula) data.get("CH_FORMULA"));
	}
	public void CH_FORMULA(IMolecularFormula value) {
		data.put("CH_FORMULA", value);
	}
	
	public Double CH_EXACT_MASS() {
		return (Double) data.get("CH_EXACT_MASS");
	}
	public void CH_EXACT_MASS(Double value) {
		data.put("CH_EXACT_MASS", value);
	}
	
	public String CH_SMILES() throws CDKException {
		if (((IAtomContainer) data.get("CH_SMILES")).isEmpty()) return "N/A";
		SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.Isomeric);
		return smigen.create((IAtomContainer) data.get("CH_SMILES"));
	}
	public void CH_SMILES(IAtomContainer value) {
		data.put("CH_SMILES", value);
	}
	
	public String CH_IUPAC() throws CDKException {
		if (((IAtomContainer) data.get("CH_IUPAC")).isEmpty()) return "N/A";
		InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator((IAtomContainer) data.get("CH_IUPAC"));
		INCHI_RET ret = gen.getReturnStatus();
		if (ret == INCHI_RET.WARNING) {
			// Structure generated, but with warning message
			System.out.println("InChI warning: " + gen.getMessage());
		} 
		else if (ret != INCHI_RET.OKAY) {
			// Structure generation failed
			throw new IllegalStateException("Structure generation failed: " + ret.toString() + " [" + gen.getMessage() + "]");
		}
		return gen.getInchi();
	}
	public void CH_IUPAC(IAtomContainer value) {
		data.put("CH_IUPAC", value);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<String> CH_LINK() {
		return (List<String>) data.get("CH_LINK");
	}
	public void CH_LINK(List<String> value) {
		data.put("CH_LINK", value);
	}
	
	public String SP_SCIENTIFIC_NAME() {
		return (String) data.get("SP_SCIENTIFIC_NAME");
	}
	public void SP_SCIENTIFIC_NAME(String value) {
		data.put("SP_SCIENTIFIC_NAME", value);
	}
	
	public String SP_LINEAGE() {
		return (String) data.get("SP_LINEAGE");
	}
	public void SP_LINEAGE(String value) {
		data.put("SP_LINEAGE", value);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> SP_LINK() {
		return (List<String>) data.get("SP_LINK");
	}
	public void SP_LINK(List<String> value) {
		data.put("SP_LINK", value);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> SP_SAMPLE() {
		return (List<String>) data.get("SP_SAMPLE");
	}
	public void SP_SAMPLE(List<String> value) {
		data.put("SP_SAMPLE", value);
		
	}
	
	public String AC_INSTRUMENT() {
		return (String) data.get("AC_INSTRUMENT");
	}
	public void AC_INSTRUMENT(String value) {
		data.put("AC_INSTRUMENT", value);
	}
	
	public List<?> AC_INSTRUMENT_TYPE() {
		return (List<?>) data.get("AC_INSTRUMENT_TYPE");
	}
	public void AC_INSTRUMENT_TYPE(List<?> value) {
		data.put("AC_INSTRUMENT_TYPE", value);
	}
	
	public String AC_MASS_SPECTROMETRY_MS_TYPE() {
		return (String) data.get("AC_MASS_SPECTROMETRY_MS_TYPE");
	}
	public void AC_MASS_SPECTROMETRY_MS_TYPE(String value) {
		data.put("AC_MASS_SPECTROMETRY_MS_TYPE", value);
	}
	
	public String toString() {
		StringBuilder sb	= new StringBuilder();
		
		sb.append("ACCESSION: " + ACCESSION() + "\n");
		sb.append("RECORD_TITLE: " + RECORD_TITLE() + "\n");
		sb.append("DATE: " + DATE() + "\n");
		sb.append("AUTHORS: " + AUTHORS() + "\n");
		sb.append("LICENSE: " + LICENSE() + "\n");
		if (COPYRIGHT() != null) sb.append("COPYRIGHT: " + COPYRIGHT() + "\n");
		if (PUBLICATION() != null) sb.append("PUBLICATION: " + PUBLICATION() + "\n");
		if (COMMENT() != null) {
			for(String comment : COMMENT())
				sb.append("COMMENT: " + comment + "\n");
		}
		if (CH_NAME() != null) {
			for(String ch_name : CH_NAME())
				sb.append("CH_NAME: " + ch_name + "\n");
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
			for(String link : CH_LINK())
				sb.append("CH$LINK: " + link + "\n");
		}
		if (SP_SCIENTIFIC_NAME() != null) sb.append("SP$SCIENTIFIC_NAME: " + SP_SCIENTIFIC_NAME() + "\n");
		if (SP_LINEAGE() != null) sb.append("SP$LINEAGE: " + SP_LINEAGE() + "\n");
		if (SP_LINK() != null) {
			for(String link : SP_LINK())
				sb.append("SP$LINK: " + link + "\n");
		}
		if (SP_SAMPLE() != null) {
			for(String sample : SP_SAMPLE())
				sb.append("SP$SAMPLE: " + sample + "\n");
		}
		sb.append("AC$INSTRUMENT: " + AC_INSTRUMENT() + "\n");
		sb.append("AC$INSTRUMENT_TYPE: " + AC_INSTRUMENT_TYPE().toString() + "\n");
		sb.append("AC$MASS_SPECTROMETRY: MS_TYPE: " + AC_MASS_SPECTROMETRY_MS_TYPE() + "\n");

		/*
		sb.append("AC$INSTRUMENT_TYPE: " + this.AC$INSTRUMENT_TYPE + "\n");
		for(String AC$MASS_SPECTROMETRY : this.AC$MASS_SPECTROMETRY)
			sb.append("AC$MASS_SPECTROMETRY: " + AC$MASS_SPECTROMETRY + "\n");
		for(String AC$CHROMATOGRAPHY : this.AC$CHROMATOGRAPHY)
			sb.append("AC$CHROMATOGRAPHY: " + AC$CHROMATOGRAPHY + "\n");
		for(String MS$FOCUSED_ION : this.MS$FOCUSED_ION)
			sb.append("MS$FOCUSED_ION: " + MS$FOCUSED_ION + "\n");
		for(String MS$DATA_PROCESSING : this.MS$DATA_PROCESSING)
			sb.append("MS$DATA_PROCESSING: " + MS$DATA_PROCESSING + "\n");
		sb.append("PK$SPLASH: " + this.PK$SPLASH + "\n");
//		PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
//		  57.0701 C4H9+ 1 57.0699 4.61
//		  67.0542 C5H7+ 1 67.0542 0.35
//		  69.0336 C4H5O+ 1 69.0335 1.14
		sb.append("PK$NUM_PEAK: " + this.PK$PEAK_MZ.length + "\n");
		sb.append("PK$PEAK: m/z int. rel.int." + "\n");
		for(int idx = 0; idx < this.PK$PEAK_MZ.length; idx++)
			sb.append("  " + this.PK$PEAK_MZ[idx] + " " + this.PK$PEAK_INT[idx] + " " + this.PK$PEAK_REL[idx] + "\n"); */
		sb.append("//");
		
		return sb.toString();
}
	
    
 }

