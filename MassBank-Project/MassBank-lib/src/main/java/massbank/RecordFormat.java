package massbank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

public class RecordFormat {

	public static final String TAG_SEPARATOR = ": ";
	
	public static final String SUBTAG_SEPARATOR = " ";
	
	public static final String MULTILINE_PREFIX = "  ";
	
	public static final String EOF_MARKER = "//";
	
	public static final ArrayList<String> TAGS = new ArrayList<String>();
	
	public static final ArrayList<Boolean> SUBTAGS = new ArrayList<Boolean>();
	
	public static final ArrayList<Boolean> MANDATORY = new ArrayList<Boolean>();
	
	public static final ArrayList<Boolean> ITERATIVE = new ArrayList<Boolean>();
	
	public static final ArrayList<Boolean> MULTILINE = new ArrayList<Boolean>();
	
	public static final Pattern TAG_REGEX = Pattern.compile("^[A-Z]+(_?[A-Z]+)*(_|\\$)?[A-Z]+(_?[A-Z]+)*$");
	
	public static final Pattern SUBTAG_REGEX = Pattern.compile("^[A-Z]+(_?[A-Z,\\/])*$");
	
	public static final Pattern VALUE_REGEX = Pattern.compile("(^(\\S+[ ]?)+$)|(^$)");
	
	public static boolean initialized = false;
	
	public RecordFormat() {
		if (!RecordFormat.initialized) {
			// name, subtags, mandatory, iterative, mulitline
			add("ACCESSION", false, true, false, false);
			add("RECORD_TITLE", false, true, false, false);
			add("DATE", false, true, false, false);
			add("AUTHORS", false, true, false, false);
			add("LICENSE", false, true, false, false);
			add("COPYRIGHT", false, false, false, false);
			add("PUBLICATION", false, false, false, false);
			add("COMMENT", false, false, true, false);
			add("CH$NAME", false, true, true, false);
			add("CH$COMPOUND_CLASS", false, true, true, false);
			add("CH$FORMULA", false, true, false, false);
			add("CH$EXACT_MASS", false, true, false, false);
			add("CH$SMILES", false, true, false, false);
			add("CH$IUPAC", false, true, false, false);
			add("CH$CDK_DEPICT_SMILES", false, false, false, false);
			add("CH$CDK_DEPICT_GENERIC_SMILES", false, false, false, false);
			add("CH$CDK_DEPICT_STRUCTURE_SMILES", false, false, false, false);
			add("CH$LINK", true, false, true, false);
			add("SP$SCIENTIFIC_NAME", false, false, false, false);
			add("SP$LINEAGE", false, false, false, false);
			add("SP$LINK", false, false, true, false);
			add("SP$SAMPLE", false, false, true, false);
			add("AC$INSTRUMENT", false, true, false, false);
			add("AC$INSTRUMENT_TYPE", false, true, false, false);
			add("AC$MASS_SPECTROMETRY: MS_TYPE", false, true, false, false);
			add("AC$MASS_SPECTROMETRY: ION_MODE", false, true, false, false);
			add("AC$MASS_SPECTROMETRY", true, false, false, false);
//			add("AC$CHROMATOGRAPHY: SOLVENT", false, false, true, false);
			add("AC$CHROMATOGRAPHY", true, false, true, false);
			add("MS$FOCUSED_ION", true, false, false, false);
			add("MS$DATA_PROCESSING", true, false, false, false);
			add("PK$SPLASH", false, true, false, false);
			add("PK$ANNOTATION", false, false, false, true);
			add("PK$NUM_PEAK", false, true, false, false);
			add("PK$PEAK", false, true, false, true);
		}
		RecordFormat.initialized = true;
	}
	
	private static void add(String tag, boolean allowsSubtags, boolean isMandatory, boolean isIterative, boolean isMultiline) {
		TAGS.add(tag);
		SUBTAGS.add(allowsSubtags);
		MANDATORY.add(isMandatory);
		ITERATIVE.add(isIterative);
		MULTILINE.add(isMultiline);	
	}
	
	public static boolean allowsSubtags(String tag) {
		int idx = TAGS.indexOf(tag);
		if (idx != -1) {
			return SUBTAGS.get(idx);
		} else {
			return false;
		}
	}
	
	public static boolean allowsMultipleLines(String tag) {
		int idx = TAGS.indexOf(tag);
		if (idx != -1) {
			return MULTILINE.get(idx);
		} else {
			return false;
		}
	}
	
	public static boolean isMandatory(String tag) {
		int idx = TAGS.indexOf(tag);
		if (idx != -1) {
			return MANDATORY.get(idx);
		} else {
			return false;
		}
	}
	
	public static boolean isIterative(String tag) {
		int idx = TAGS.indexOf(tag);
		if (idx != -1) {
			return ITERATIVE.get(idx);
		} else {
			return false;
		}
	}
		
}
