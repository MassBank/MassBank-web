package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/*
An exemplary record:

ACCESSION: ET161503
RECORD_TITLE: PRI_261.1236_16.6LC-ESI-QFTMS2CE: (160-0.41mz or 15) NCER=17500[M+H]+
DATE: 2016.03.17 (Created 2015.09.25, modified 2016.02.03)
AUTHORS: R. Gulde, E. Schymanski, K. Fenner, Department of Environmental Chemistry, Eawag
LICENSE: CC BY
COPYRIGHT: Copyright (C) 2016 Eawag, Duebendorf, Switzerland
PUBLICATION: Gulde, Meier, Schymanski, Kohler, Helbling, Derrer, Rentsch & FennerES&T 2016 50(6):2908-2920. DOI: 10.1021/acs.est.5b05186. Systematic Exploration of Biotransformation Reactions of Amine-containing Micropollutants in Activated Sludge
COMMENT: CONFIDENCE Tentative identification: substance class known (Level 3)
COMMENT: INTERNAL_ID 1615
CH$NAME: PRI_261.1236_16.6
CH$COMPOUND_CLASS: N/AEnvironmental Transformation Products
CH$FORMULA: C14H16N2O3
CH$EXACT_MASS: 260.1161
CH$SMILES: NA
CH$IUPAC: NA
AC$INSTRUMENT: Q Exactive Orbitrap Thermo Scientific
AC$INSTRUMENT_TYPE: LC-ESI-QFT
AC$MASS_SPECTROMETRY: MS_TYPE MS2
AC$MASS_SPECTROMETRY: IONIZATION ESI
AC$MASS_SPECTROMETRY: ION_MODE POSITIVE
AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE HCD
AC$MASS_SPECTROMETRY: COLLISION_ENERGY 160-0.41mz or 15 (mz>350) nominal units
AC$MASS_SPECTROMETRY: RESOLUTION 17500
AC$CHROMATOGRAPHY: COLUMN_NAME Atlantis T3 3um, 3x150mm, Waters with guard column
AC$CHROMATOGRAPHY: FLOW_GRADIENT 95/5 at 0 min, 5/95 at 15 min, 5/95 at 20 min, 95/5 at 20.1 min, 95/5 at 25 min
AC$CHROMATOGRAPHY: FLOW_RATE 300 uL/min
AC$CHROMATOGRAPHY: RETENTION_TIME 16.6 min
AC$CHROMATOGRAPHY: SOLVENT A water with 0.1% formic acid
AC$CHROMATOGRAPHY: SOLVENT B methanol with 0.1% formic acid
MS$FOCUSED_ION: BASE_PEAK 65.0598
MS$FOCUSED_ION: PRECURSOR_M/Z 261.1234
MS$FOCUSED_ION: PRECURSOR_TYPE [M+H]+
MS$DATA_PROCESSING: RECALIBRATE loess on assigned fragments and MS1
MS$DATA_PROCESSING: REANALYZE Peaks with additional N2/O included
MS$DATA_PROCESSING: WHOLE RMassBank 1.99.7
PK$SPLASH: splash10-0ufr-2960000000-aa73d066778457136b51
PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
57.0701 C4H9+ 1 57.0699 4.61
67.0542 C5H7+ 1 67.0542 0.35
69.0336 C4H5O+ 1 69.0335 1.14
PK$NUM_PEAK: 22
PK$PEAK: m/z int. rel.int.
57.0701 1134.9 18
67.0542 1403.3 22
69.0336 6327.6 102
//
 */

public class AccessionData {
	
	private final ArrayList<String> tag = new ArrayList<String>();
	
	private final ArrayList<String> subtag = new ArrayList<String>();
	
	private final ArrayList<String> value = new ArrayList<String>();
	
	public String annotationHeader;
	
	public AccessionData() {}
	
	public static AccessionData getAccessionDataFromDatabase(String accessionId) {
		DatabaseManager db = DatabaseManager.create();
		if (db != null) {
			return db.getAccessionData(accessionId);
		} else {
			return null;
		}
	}
			
	public void add(String tag, String subtag, String value) {
		this.tag.add(tag);
		this.subtag.add(subtag);
		this.value.add(value);
	}
	
	public ArrayList<String[]> get(String tag) {
		ArrayList<String[]> res = new ArrayList<String[]>();
		for (int i = 0; i < this.tag.size(); i++) {
			if (this.tag.get(i).compareTo(tag) == 0) {
				String[] tmp = {this.tag.get(i),this.subtag.get(i),this.value.get(i)};
				res.add(tmp);
			}
		}
		if (res.size() == 0) {
			return null;
		}
		return res;
	}
	
	public void print() {
		new RecordFormat();
		boolean multiline = false;
		boolean headerPrinted = false;
		for (String s : RecordFormat.TAGS) {
			headerPrinted = false;
			ArrayList<String[]> lines = new ArrayList<String[]>();
			lines = this.get(s);
			if (lines != null) {
				for (String[] line : lines) {
					if (RecordFormat.TAGS.contains(line[0] + RecordFormat.TAG_SEPARATOR + line[1]) && s.compareTo(line[0] + RecordFormat.TAG_SEPARATOR + line[1]) != 0)
						continue;
					if (line[2] != null) {
						if (!RecordFormat.allowsMultipleLines(s) ) {
							multiline = false;
						}
						if (multiline) {
							System.out.print(RecordFormat.MULTILINE_PREFIX);
							System.out.print(line[2]);
							System.out.print("\n");
						}
						if (RecordFormat.allowsMultipleLines(s) ) {
							multiline = true;
							if (!headerPrinted) {
								if (s.compareTo("PK$PEAK") == 0) {
									System.out.print(s);
									System.out.print(RecordFormat.TAG_SEPARATOR);
									System.out.print("m/z int. rel.int.");
									System.out.print("\n");
									headerPrinted = true;
								}
								if (s.compareTo("PK$ANNOTATION") == 0) {
									System.out.print(s);
									System.out.print(RecordFormat.TAG_SEPARATOR);
									System.out.print(this.annotationHeader);
									System.out.print("\n");
									headerPrinted = true;
								}
							}
						}
						if (!multiline) {
							System.out.print(line[0]);
							System.out.print(RecordFormat.TAG_SEPARATOR);
							if (line[1] != null) {
								System.out.print(line[1]);
								System.out.print(RecordFormat.SUBTAG_SEPARATOR);
							}
							System.out.print(line[2]);
							System.out.print("\n");
						}
					}
				}
			}
		}
		System.out.print("//");
//		boolean multiLine = false;
//		String preTag = null;
//		String actTag = null; 
//		for (int i = 0; i < tag.size(); i++) {
//			actTag = tag.get(i);
//			if (new RecordFormat().allowsMultipleLines(tag.get(i))) {
//				multiLine = true;
//			} else {
//				multiLine = false;
//			}
//			if (!multiLine || preTag.compareTo(actTag) != 0) {
//				System.out.print(tag.get(i) + RecordFormat.TAG_SEPARATOR);
//				if (subtag.get(i) != null)
//					System.out.print(subtag.get(i) + RecordFormat.SUBTAG_SEPARATOR);
//			}
//			if (multiLine && preTag.compareTo(actTag) == 0)
//				System.out.print(RecordFormat.MULTILINE_PREFIX);
//			System.out.print(value.get(i) + "\n");
//			preTag = actTag;
//		}
//		System.out.print("//");
	}
	
	public static void main (String[] args) throws IOException {
//		File file = new File("/Users/laptop/Documents/UFZ/MassBank-web/MassBank-Project/MassBank/target/test-classes/YYY00001.txt");
//		AccessionData acc = AccessionData.getAccessionDataFromFile(file);
//		acc.print();
		AccessionData acc = AccessionData.getAccessionDataFromDatabase("AU580906");
		acc.print();
		
//		System.setOut(new PrintStream(new FileOutputStream("/Users/laptop/Desktop/output.txt")));
//		Long start = 0L;
//		Long end = 0L;
//		Path dir = FileSystems.getDefault().getPath("/Users/laptop/Desktop/UFZ/MBRecords/newFolder/");
//		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
//		PrintStream ps = new PrintStream(new FileOutputStream("/Users/laptop/Desktop/times.txt",true));
//		ArrayList<AccessionFile> array = new ArrayList<AccessionFile>();
//		ArrayList<Thread> threadPool = new ArrayList<Thread>();
//		for (Path path : stream) {
//			if (path.toFile().isDirectory()) {
//				DirectoryStream<Path> stream2 = Files.newDirectoryStream(path);
//				for (Path path2 : stream2) {
//					String file = path2.getFileName().toString().substring(0, 7);
//					start = System.nanoTime();
//					AccessionData acc = AccessionData.getAccessionDataFromDatabase(file);
////					new DatabaseManager();
//					end = System.nanoTime();
//					System.out.println(end-start);
//				}
//				stream2.close();
//				break;
//			}
//		}
	}
	
}

//public class AccessionData{
//	public final String ACCESSION;
//	public final String RECORD_TITLE;
//	public final String DATE;
//	public final String AUTHORS;
//	public final String LICENSE;
//	public final String COPYRIGHT;
//	public final String PUBLICATION;
//	public final String[] COMMENT;
//	public final String[] CH$NAME;
////	public final String[] CH$COMPOUND_CLASS_NAME;
////	public final String[] CH$COMPOUND_CLASS_CLASS;
//	public final String[] CH$COMPOUND_CLASS;
//	public final String CH$FORMULA;
//	public final double CH$EXACT_MASS;
//	public final String CH$SMILES;
//	public final String CH$IUPAC;
//	public final String CH$CDK_DEPICT_SMILES;
//	public final String CH$CDK_DEPICT_GENERIC_SMILES;
//	public final String CH$CDK_DEPICT_STRUCTURE_SMILES;
//	public final String[] CH$LINK_NAME;
//	public final String[] CH$LINK_ID;
//	public final String SP$SCIENTIFIC_NAME;
//	public final String SP$LINEAGE;
//	public final String[] SP$LINK;
//	public final String[] SP$SAMPLE;
//	public final String AC$INSTRUMENT;
//	public final String AC$INSTRUMENT_TYPE;
//	public final String AC$MASS_SPECTROMETRY_MS_TYPE;
//	public final String AC$MASS_SPECTROMETRY_ION_MODE;
//	public final String[] AC$MASS_SPECTROMETRY_SUBTAG;
//	public final String[] AC$MASS_SPECTROMETRY_VALUE;
//	public final String[] AC$CHROMATOGRAPHY_SUBTAG;
//	public final String[] AC$CHROMATOGRAPHY_VALUE;
//	public final String[] MS$FOCUSED_ION_SUBTAG;
//	public final String[] MS$FOCUSED_ION_VALUE;
//	public final String[] MS$DATA_PROCESSING_SUBTAG;
//	public final String[] MS$DATA_PROCESSING_VALUE;
//	public final String PK$SPLASH;
////	public final String[] PK$ANNOTATION;
//	public final int PK$NUM_PEAK;
//	public final double[] PK$PEAK_MZ;
//	public final double[] PK$PEAK_INTENSITY;
//	public final short[] PK$PEAK_RELATIVE;
//	public final String[] PK$ANNOTATION_TENTATIVE_FORMULA;
//	public final short[] PK$ANNOTATION_FORMULA_COUNT;
//	public final float[] PK$ANNOTATION_THEORETICAL_MASS;
//	public final float[] PK$ANNTOATION_ERROR_PPM;
//	
//	public AccessionData(
//			String ACCESSION,
//			String RECORD_TITLE,
//			String DATE,
//			String AUTHORS,
//			String LICENSE,
//			String COPYRIGHT,
//			String PUBLICATION,
//			String[] COMMENT,
//			String[] CH$NAME,
//			//String[] CH$COMPOUND_CLASS_NAME,
//			//String[] CH$COMPOUND_CLASS_CLASS,
//			String[] CH$COMPOUND_CLASS,
//			String CH$FORMULA,
//			double CH$EXACT_MASS,
//			String CH$SMILES,
//			String CH$IUPAC,
//			String CH$CDK_DEPICT_SMILES,
//			String CH$CDK_DEPICT_GENERIC_SMILES,
//			String CH$CDK_DEPICT_STRUCTURE_SMILES,
//			String[] CH$LINK_NAME,
//			String[] CH$LINK_ID,
//			String SP$SCIENTIFIC_NAME,
//			String SP$LINEAGE,
//			String[] SP$LINK,
//			String[] SP$SAMPLE,
//			String AC$INSTRUMENT,
//			String AC$INSTRUMENT_TYPE,
//			String AC$MASS_SPECTROMETRY_MS_TYPE,
//			String AC$MASS_SPECTROMETRY_ION_MODE,
//			String[] AC$MASS_SPECTROMETRY_SUBTAG,
//			String[] AC$MASS_SPECTROMETRY_VALUE,
//			String[] AC$CHROMATOGRAPHY_SUBTAG,
//			String[] AC$CHROMATOGRAPHY_VALUE,
//			String[] MS$FOCUSED_ION_SUBTAG,
//			String[] MS$FOCUSED_ION_VALUE,
//			String[] MS$DATA_PROCESSING_SUBTAG,
//			String[] MS$DATA_PROCESSING_VALUE,
//			String PK$SPLASH,
//			//String[] PK$ANNOTATION,
//			int PK$NUM_PEAK,
//			double[] PK$PEAK_MZ,
//			double[] PK$PEAK_INTENSITY,
//			short[] PK$PEAK_RELATIVE,
//			String[] PK$ANNOTATION_TENTATIVE_FORMULA,
//			short[] PK$ANNOTATION_FORMULA_COUNT,
//			float[] PK$ANNOTATION_THEORETICAL_MASS,
//			float[] PK$ANNTOATION_ERROR_PPM
//	){
//		this.ACCESSION = ACCESSION;
//		this.RECORD_TITLE = RECORD_TITLE;
//		this.DATE = DATE;
//		this.AUTHORS = AUTHORS;
//		this.LICENSE = LICENSE;
//		this.COPYRIGHT = COPYRIGHT;
//		this.PUBLICATION = PUBLICATION;
//		this.COMMENT = COMMENT;
//		this.CH$NAME = CH$NAME;
//		//this.CH$COMPOUND_CLASS_NAME = CH$COMPOUND_CLASS_NAME;
//		//this.CH$COMPOUND_CLASS_CLASS = CH$COMPOUND_CLASS_CLASS;
//		this.CH$COMPOUND_CLASS = CH$COMPOUND_CLASS;
//		this.CH$FORMULA = CH$FORMULA;
//		this.CH$EXACT_MASS = CH$EXACT_MASS;
//		this.CH$SMILES = CH$SMILES;
//		this.CH$IUPAC = CH$IUPAC;
//		this.CH$CDK_DEPICT_SMILES = CH$CDK_DEPICT_SMILES;
//		this.CH$CDK_DEPICT_GENERIC_SMILES = CH$CDK_DEPICT_GENERIC_SMILES;
//		this.CH$CDK_DEPICT_STRUCTURE_SMILES = CH$CDK_DEPICT_STRUCTURE_SMILES;
//		this.CH$LINK_NAME = CH$LINK_NAME;
//		this.CH$LINK_ID = CH$LINK_ID;
//		this.SP$SCIENTIFIC_NAME = SP$SCIENTIFIC_NAME;
//		this.SP$LINEAGE = SP$LINEAGE;
//		this.SP$LINK = SP$LINK;
//		this.SP$SAMPLE = SP$SAMPLE;
//		this.AC$INSTRUMENT = AC$INSTRUMENT;
//		this.AC$INSTRUMENT_TYPE = AC$INSTRUMENT_TYPE;
//		this.AC$MASS_SPECTROMETRY_MS_TYPE = AC$MASS_SPECTROMETRY_MS_TYPE;
//		this.AC$MASS_SPECTROMETRY_ION_MODE = AC$MASS_SPECTROMETRY_ION_MODE;
//		this.AC$MASS_SPECTROMETRY_SUBTAG = AC$MASS_SPECTROMETRY_SUBTAG;
//		this.AC$MASS_SPECTROMETRY_VALUE = AC$MASS_SPECTROMETRY_VALUE;
//		this.AC$CHROMATOGRAPHY_SUBTAG = AC$CHROMATOGRAPHY_SUBTAG;
//		this.AC$CHROMATOGRAPHY_VALUE = AC$CHROMATOGRAPHY_VALUE;
//		this.MS$FOCUSED_ION_SUBTAG = MS$FOCUSED_ION_SUBTAG;
//		this.MS$FOCUSED_ION_VALUE = MS$FOCUSED_ION_VALUE;
//		this.MS$DATA_PROCESSING_SUBTAG = MS$DATA_PROCESSING_SUBTAG;
//		this.MS$DATA_PROCESSING_VALUE = MS$DATA_PROCESSING_VALUE;
//		this.PK$SPLASH = PK$SPLASH;
//		//this.PK$ANNOTATION = PK$ANNOTATION;
//		this.PK$NUM_PEAK = PK$NUM_PEAK;
//		this.PK$PEAK_MZ = PK$PEAK_MZ;
//		this.PK$PEAK_INTENSITY = PK$PEAK_INTENSITY;
//		this.PK$PEAK_RELATIVE = PK$PEAK_RELATIVE;
//		this.PK$ANNOTATION_TENTATIVE_FORMULA = PK$ANNOTATION_TENTATIVE_FORMULA;
//		this.PK$ANNOTATION_FORMULA_COUNT = PK$ANNOTATION_FORMULA_COUNT;
//		this.PK$ANNOTATION_THEORETICAL_MASS = PK$ANNOTATION_THEORETICAL_MASS;
//		this.PK$ANNTOATION_ERROR_PPM = PK$ANNTOATION_ERROR_PPM;
//	}
//	public String toString() {
//		StringBuilder sb	= new StringBuilder();
//		
//		sb.append("ACCESSION: " + this.ACCESSION + "\n");
//		sb.append("RECORD_TITLE: " + this.RECORD_TITLE + "\n");
//		sb.append("DATE: " + this.DATE + "\n");
//		sb.append("AUTHORS: " + this.AUTHORS + "\n");
//		sb.append("LICENSE: " + this.LICENSE + "\n");
//		sb.append("COPYRIGHT: " + this.COPYRIGHT + "\n");
//		sb.append("PUBLICATION: " + this.PUBLICATION + "\n");
//		for(String COMMENT : this.COMMENT)
//			sb.append("COMMENT: " + COMMENT + "\n");
//		for(String CH$NAME : this.CH$NAME)
//			sb.append("CH$NAME: " + CH$NAME + "\n");
////		for(int idx = 0; idx < this.CH$COMPOUND_CLASS_NAME.length; idx++)
////			sb.append("CH$COMPOUND_CLASS: " + CH$COMPOUND_CLASS_CLASS[idx] + " " + CH$COMPOUND_CLASS_NAME[idx] + "\n");
//		for(String CH$COMPOUND_CLASS : this.CH$COMPOUND_CLASS)
//			sb.append("CH$COMPOUND_CLASS: " + CH$COMPOUND_CLASS + "\n");
//		sb.append("CH$FORMULA: " + this.CH$FORMULA + "\n");
//		sb.append("CH$EXACT_MASS: " + this.CH$EXACT_MASS + "\n");
//		sb.append("CH$SMILES: " + this.CH$SMILES + "\n");
//		sb.append("CH$IUPAC: " + this.CH$IUPAC + "\n");
////		sb.append("CH$CDK_DEPICT_SMILES" + CH$CDK_DEPICT_SMILES + "\n");
////		sb.append("CH$CDK_DEPICT_GENERIC_SMILES" + CH$CDK_DEPICT_GENERIC_SMILES + "\n");
////		sb.append("CH$CDK_DEPICT_STRUCTURE_SMILES" + CH$CDK_DEPICT_STRUCTURE_SMILES + "\n");
//		for(int idx = 0; idx < this.CH$LINK_ID.length; idx++)
//			sb.append("CH$LINK: " + this.CH$LINK_NAME[idx] + " " + this.CH$LINK_ID[idx] + "\n");
//		sb.append("SP$SCIENTIFIC_NAME: " + this.SP$SCIENTIFIC_NAME + "\n");
//		sb.append("SP$LINEAGE: " + this.SP$LINEAGE + "\n");
//		for(String SP$LINK : this.SP$LINK)
//			sb.append("SP$LINK: " + SP$LINK + "\n");
//		for(String SP$SAMPLE : this.SP$SAMPLE)
//			sb.append("SP$SAMPLE: " + SP$SAMPLE + "\n");
//		sb.append("AC$INSTRUMENT: " + this.AC$INSTRUMENT + "\n");
//		sb.append("AC$INSTRUMENT_TYPE: " + this.AC$INSTRUMENT_TYPE + "\n");
//		sb.append("AC$MASS_SPECTROMETRY: MS_TPYE " + this.AC$MASS_SPECTROMETRY_MS_TYPE + "\n");
//		sb.append("AC$MASS_SPECTROMETRY: ION_MODE " + this.AC$MASS_SPECTROMETRY_ION_MODE + "\n");
////		for(String AC$MASS_SPECTROMETRY : this.AC$MASS_SPECTROMETRY)
////			sb.append("AC$MASS_SPECTROMETRY: " + AC$MASS_SPECTROMETRY + "\n");
//		for(int idx=0; idx < this.AC$MASS_SPECTROMETRY_SUBTAG.length; idx++)
//			sb.append("AC$MASS_SPECTROMETRY: " + this.AC$MASS_SPECTROMETRY_SUBTAG[idx] + " " + this.AC$MASS_SPECTROMETRY_VALUE[idx] +"\n");
////		for(String AC$CHROMATOGRAPHY : this.AC$CHROMATOGRAPHY)
////			sb.append("AC$CHROMATOGRAPHY: " + AC$CHROMATOGRAPHY + "\n");
//		for(int idx=0; idx < this.AC$CHROMATOGRAPHY_SUBTAG.length; idx++)
//			sb.append("AC$CHROMATOGRAPHY: " + this.AC$CHROMATOGRAPHY_SUBTAG[idx] + " " + this.AC$CHROMATOGRAPHY_VALUE[idx] + "\n");
////		for(String MS$FOCUSED_ION : this.MS$FOCUSED_ION)
////			sb.append("MS$FOCUSED_ION: " + MS$FOCUSED_ION + "\n");
//		for(int idx=0; idx < this.MS$FOCUSED_ION_SUBTAG.length; idx++)
//			sb.append("MS$FOCUSED_ION: " + this.MS$FOCUSED_ION_SUBTAG[idx] + " " + this.MS$FOCUSED_ION_VALUE[idx] + "\n");
////		for(String MS$DATA_PROCESSING : this.MS$DATA_PROCESSING)
////			sb.append("MS$DATA_PROCESSING: " + MS$DATA_PROCESSING + "\n");
//		for (int idx=0; idx < this.MS$DATA_PROCESSING_SUBTAG.length; idx++)
//			sb.append("MS$DATA_PROCESSING: " + this.MS$DATA_PROCESSING_SUBTAG[idx] + " " + this.MS$DATA_PROCESSING_VALUE[idx] + "\n");
//		sb.append("PK$SPLASH: " + this.PK$SPLASH + "\n");
////		PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
////		  57.0701 C4H9+ 1 57.0699 4.61
////		  67.0542 C5H7+ 1 67.0542 0.35
////		  69.0336 C4H5O+ 1 69.0335 1.14
////		sb.append("PK$NUM_PEAK: " + this.PK$PEAK_MZ.length + "\n");
//		sb.append("PK$NUM_PEAK: " + this.PK$NUM_PEAK + "\n");
//		sb.append("PK$PEAK: m/z int. rel.int." + "\n");
//		for(int idx = 0; idx < this.PK$PEAK_MZ.length; idx++)
//			sb.append("  " + this.PK$PEAK_MZ[idx] + " " + this.PK$PEAK_INTENSITY[idx] + " " + this.PK$PEAK_RELATIVE[idx] + "\n");
//		for(int idx = 0; idx < this.PK$ANNOTATION_FORMULA_COUNT.length; idx++) {
//			if (idx == 0) {
//				sb.append("PK$ANNOTATION: m/z formula annotation exact_mass error(ppm)");
//			}
//			sb.append("  " + this.PK$PEAK_MZ[idx] + " " + this.PK$ANNOTATION_TENTATIVE_FORMULA[idx] + " " + this.PK$ANNOTATION_FORMULA_COUNT[idx] + " " + this.PK$ANNOTATION_THEORETICAL_MASS[idx] + " " + this.PK$ANNTOATION_ERROR_PPM[idx] + "\n");
//		}
//		sb.append("//");
//		
//		return sb.toString();
//	}
//	private static int getMaximumStringLength(String[] sa){
//		int maximumMzLength		= 0;
//		for(int idx = 0; idx < sa.length; idx++){
//			int mzLength	= (sa [idx] + "").length();
//			maximumMzLength		= mzLength	> maximumMzLength	? mzLength	: maximumMzLength;
//		}
//		return maximumMzLength;
//	}
//	private static String[] pad(String[] PK$PEAK){
//		int maximumLength	= AccessionData.getMaximumStringLength(PK$PEAK);
//		String padding	= StringUtils.repeat(' ', maximumLength);
//		
//		String[] PK$PEAK2	= new String[PK$PEAK.length];
//		for(int idx = 0; idx < PK$PEAK.length; idx++)
//			PK$PEAK2 [idx]	= padding.substring(PK$PEAK[idx].length()) + PK$PEAK[idx];
//		
//		return PK$PEAK2;
//	}
//	private static String[] formatPK$PEAK(double[] PK$PEAK, boolean pad){
//		String[] PK$PEAK2		= new String[PK$PEAK.length];
//		DecimalFormat df = new DecimalFormat("0.0");
//		df.setMaximumFractionDigits(8);
//		for(int idx = 0; idx < PK$PEAK.length; idx++)
//			PK$PEAK2 [idx]	= df.format(PK$PEAK [idx]);
//		
//		if(pad)	PK$PEAK2	= pad(PK$PEAK2);
//		
//		return PK$PEAK2;
//	}
//	public String[] formatPK$PEAK_MZ(boolean pad){
//		return formatPK$PEAK(this.PK$PEAK_MZ,  pad);
//	}
//	public String[] formatPK$PEAK_INT(boolean pad){
//		return formatPK$PEAK(this.PK$PEAK_INTENSITY, pad);
//	}
//	public String[] formatPK$PEAK_REL(boolean pad){
//		String[] PK$PEAK_REL		= new String[this.PK$PEAK_RELATIVE.length];
//		for(int idx = 0; idx < this.PK$PEAK_RELATIVE.length; idx++)
//			PK$PEAK_REL[idx]	= this.PK$PEAK_RELATIVE[idx] + "";
//		return pad(PK$PEAK_REL);
//	}
//	
//	public static String getDatabaseOfAccession(String accession){
//		String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
//		GetConfig conf = new GetConfig(baseUrl);
//		String[] databaseNames	= conf.getDbName();
//		
//		String databaseName	= null;
//		for(String databaseName2 : databaseNames){
//			if(AccessionData.existsFile(databaseName2, accession)){
//				databaseName	= databaseName2;
//				break;
//			}
//		}
//		
//		return databaseName;
//	}
//	public static boolean existsFile(String databaseName, String accession){
//		File file	= AccessionData.getFile(databaseName, accession);
//		return file.exists();
//	}
//	public static File getFile(String databaseName, String accession){
//		// http://localhost/MassBank/DB/annotation/MassBank/XXX00001.txt
//		File file	= new File(MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH) + databaseName + File.separator + accession + ".txt");
//		return file;
//	}
//	public static AccessionData getAccessionDataFromFile(String databaseName, String accession){
//		if(!AccessionData.existsFile(databaseName, accession))
//			return null;
//		File file	= AccessionData.getFile(databaseName, accession);
//		return AccessionData.getAccessionDataFromFile(file);
//	}
//	public static AccessionData getAccessionDataFromFile(File file){
//		// initialization of fields
//		String ACCESSION = null;
//		String RECORD_TITLE = null;
//		String DATE = null;
//		String AUTHORS = null;
//		String LICENSE = null;
//		String COPYRIGHT = null;
//		String PUBLICATION = null;
//		List<String> COMMENT = new ArrayList<String>();
//		List<String> CH$NAME = new ArrayList<String>();
//		//List<String> CH$COMPOUND_CLASS_NAME = new ArrayList<String>();
//		//List<String> CH$COMPOUND_CLASS_CLASS = new ArrayList<String>();
//		List<String> CH$COMPOUND_CLASS = new ArrayList<String>();
//		String CH$FORMULA = null;
//		double CH$EXACT_MASS = -1;
//		String CH$SMILES = null;
//		String CH$IUPAC = null;
//		String CH$CDK_DEPICT_SMILES = null;
//		String CH$CDK_DEPICT_GENERIC_SMILES = null;
//		String CH$CDK_DEPICT_STRUCTURE_SMILES = null;
//		List<String> CH$LINK_NAME = new ArrayList<String>();
//		List<String> CH$LINK_ID = new ArrayList<String>();
//		String SP$SCIENTIFIC_NAME = null;
//		String SP$LINEAGE = null;
//		List<String> SP$LINK = new ArrayList<String>();
//		List<String> SP$SAMPLE = new ArrayList<String>();
//		String AC$INSTRUMENT = null;
//		String AC$INSTRUMENT_TYPE = null;
//		String AC$MASS_SPECTROMETRY_MS_TYPE = null;
//		String AC$MASS_SPECTROMETRY_ION_MODE = null;
//		List<String> AC$MASS_SPECTROMETRY_SUBTAG = new ArrayList<String>();
//		List<String> AC$MASS_SPECTROMETRY_VALUE = new ArrayList<String>();
//		List<String> AC$CHROMATOGRAPHY_SUBTAG = new ArrayList<String>();
//		List<String> AC$CHROMATOGRAPHY_VALUE = new ArrayList<String>();
//		List<String> MS$FOCUSED_ION_SUBTAG = new ArrayList<String>();
//		List<String> MS$FOCUSED_ION_VALUE = new ArrayList<String>();
//		List<String> MS$DATA_PROCESSING_SUBTAG = new ArrayList<String>();
//		List<String> MS$DATA_PROCESSING_VALUE = new ArrayList<String>();
//		String PK$SPLASH = null;
//		//String[] PK$ANNOTATION,
//		int PK$NUM_PEAK = -1;
//		List<Double> PK$PEAK_MZ = new ArrayList<Double>();
//		List<Double> PK$PEAK_INTENSITY = new ArrayList<Double>();
//		List<Short> PK$PEAK_RELATIVE = new ArrayList<Short>();
//		List<String> PK$ANNOTATION_TENTATIVE_FORMULA = new ArrayList<String>();
//		List<Short> PK$ANNOTATION_FORMULA_COUNT = new ArrayList<Short>();
//		List<Float> PK$ANNOTATION_THEORETICAL_MASS = new ArrayList<Float>();
//		List<Float> PK$ANNTOATION_ERROR_PPM = new ArrayList<Float>();		
//		
//		// fetch information
//		String[] fileLines	= getFileContent(file);
//		if(fileLines == null)
//			return null;
//		
//		final String sumFormulaRegEx	= "([A-Za-z]{1,3}\\d*(\\[\\d+\\])?)+";
//		final String decimalRegEx		= "\\d+(\\.\\d+)?";
//		final String integerRegEx		= "\\d+";
//		
//		try{
//			for(String fileLine : fileLines){
//				
//				if(fileLine.startsWith("ACCESSION: ")){
//					ACCESSION		= fileLine.substring("ACCESSION: ".length());
//				} else if(fileLine.startsWith("RECORD_TITLE: ")){
//					RECORD_TITLE	= fileLine.substring("RECORD_TITLE: ".length());
//				} else if(fileLine.startsWith("DATE: ")){
////					SimpleDateFormat parser = new SimpleDateFormat("yyyy.MM.dd");
////					DATE	= new Date(parser.parse(fileLine.substring("DATE: ".length())).getTime());
//					DATE = fileLine.substring("DATE: ".length());
//				} else if(fileLine.startsWith("AUTHORS: ")){
//					AUTHORS			= fileLine.substring("AUTHORS: ".length());
//				} else if(fileLine.startsWith("LICENSE: ")){
//					LICENSE	= fileLine.substring("LICENSE: ".length());
//				} else if(fileLine.startsWith("COPYRIGHT: ")){
//					COPYRIGHT	= fileLine.substring("COPYRIGHT: ".length());
//				} else if(fileLine.startsWith("PUBLICATION: ")){
//					PUBLICATION	= fileLine.substring("PUBLICATION: ".length());
//				} else if(fileLine.startsWith("COMMENT: ")){
//					COMMENT.add(fileLine.substring("COMMENT: ".length()));
//				} else if(fileLine.startsWith("CH$NAME: ")){
//					CH$NAME.add(fileLine.substring("CH$NAME: ".length()));
//				} else if(fileLine.startsWith("CH$COMPOUND_CLASS: ")){
////					String val	= fileLine.substring("CH$COMPOUND_CLASS: ".length());
////					int idx	= val.indexOf("; ");
////					if(idx == -1){
////						CH$COMPOUND_CLASS_CLASS.add("NA");
////						CH$COMPOUND_CLASS_NAME .add(val);
////					} else {
////						CH$COMPOUND_CLASS_CLASS.add(val.substring(0, idx));
////						CH$COMPOUND_CLASS_NAME .add(val.substring(idx + "; ".length()));
////					}
//					CH$COMPOUND_CLASS.add(fileLine.substring("CH$COMPOUND_CLASS: ".length()));
//				} else if(fileLine.startsWith("CH$FORMULA: ")){
//					CH$FORMULA	= fileLine.substring("CH$FORMULA: ".length());
//				} else if(fileLine.startsWith("CH$EXACT_MASS: ")){
//					CH$EXACT_MASS	= Double.parseDouble(fileLine.substring("CH$EXACT_MASS: ".length()));
//				} else if(fileLine.startsWith("CH$SMILES: ")){
//					CH$SMILES	= fileLine.substring("CH$SMILES: ".length());
//				} else if(fileLine.startsWith("CH$IUPAC: ")){
//					CH$IUPAC	= fileLine.substring("CH$IUPAC: ".length());
//				} else if(fileLine.startsWith("CH$CDK_DEPICT_SMILES: ")) {
//					CH$CDK_DEPICT_SMILES = fileLine.substring("CH$CDK_DEPICT_SMILES: ".length());
//				} else if(fileLine.startsWith("CH$CDK_DEPICT_GENERIC_SMILES: ")) {
//					CH$CDK_DEPICT_SMILES = fileLine.substring("CH$CDK_DEPICT_GENERIC_SMILES: ".length());
//				} else if(fileLine.startsWith("CH$CDK_DEPICT_STRUCTURE_SMILES: ")) {
//					CH$CDK_DEPICT_SMILES = fileLine.substring("CH$CDK_DEPICT_STRUCTURE_SMILES: ".length());
//				} else if(fileLine.startsWith("CH$LINK: ")){
//					String[] tmp	= fileLine.substring("CH$LINK: ".length()).split(" ");
//					CH$LINK_NAME.add(tmp[0]);
//					CH$LINK_ID.add(tmp[1]);
//				} else if(fileLine.startsWith("SP$SCIENTIFIC_NAME: ")){
//					SP$SCIENTIFIC_NAME = fileLine.substring("SP$SCIENTIFIC_NAME: ".length());
//				} else if(fileLine.startsWith("SP$LINEAGE: ")) {
//					SP$LINEAGE = fileLine.substring("SP$LINEAGE: ".length());
//				} else if(fileLine.startsWith("SP$LINK: ")) {
//					SP$LINK.add(fileLine.substring("SP$LINK: ".length()));
//				} else if(fileLine.startsWith("SP$SAMPLE: ")) {
//					SP$SAMPLE.add(fileLine.substring("SP$SAMPLE: ".length()));
//				} else if(fileLine.startsWith("AC$INSTRUMENT: ")){
//					AC$INSTRUMENT	= fileLine.substring("AC$INSTRUMENT: ".length());
//				} else if(fileLine.startsWith("AC$INSTRUMENT_TYPE: ")){
//					AC$INSTRUMENT_TYPE	= fileLine.substring("AC$INSTRUMENT_TYPE: ".length());
//				} else if(fileLine.startsWith("AC$MASS_SPECTROMETRY: ION_MODE ")) {
//					AC$MASS_SPECTROMETRY_ION_MODE = fileLine.substring("AC$MASS_SPECTROMETRY: ION_MODE ".length());
//				} else if(fileLine.startsWith("AC$MASS_SPECTROMETRY: MS_TYPE ")) {
//					AC$MASS_SPECTROMETRY_MS_TYPE = fileLine.substring("AC$MASS_SPECTROMETRY: MS_TYPE ".length());
//				} else if(fileLine.startsWith("AC$MASS_SPECTROMETRY: ") && 
//						!fileLine.startsWith("AC$MASS_SPECTROMETRY: ION_MODE ") && 
//						!fileLine.startsWith("AC$MASS_SPECTROMETRY: MS_TYPE ")){
//					int tmp = fileLine.substring("AC$MASS_SPECTROMETRY: ".length()).indexOf(" ");
//					AC$MASS_SPECTROMETRY_SUBTAG.add(fileLine.substring("AC$MASS_SPECTROMETRY: ".length()).substring(0, tmp));
//					AC$MASS_SPECTROMETRY_VALUE.add(fileLine.substring("AC$MASS_SPECTROMETRY: ".length()).substring(tmp+1)); 
//				} else if(fileLine.startsWith("AC$CHROMATOGRAPHY: ")){
//					int tmp = fileLine.substring("AC$CHROMATOGRAPHY: ".length()).indexOf(" ");
//					AC$CHROMATOGRAPHY_SUBTAG.add(fileLine.substring("AC$CHROMATOGRAPHY: ".length()).substring(0, tmp));
//					AC$CHROMATOGRAPHY_VALUE.add(fileLine.substring("AC$CHROMATOGRAPHY: ".length()).substring(tmp+1));
//				} else if(fileLine.startsWith("MS$FOCUSED_ION: ")){
//					int tmp = fileLine.substring("MS$FOCUSED_ION: ".length()).indexOf(" ");
//					MS$FOCUSED_ION_SUBTAG.add(fileLine.substring("MS$FOCUSED_ION: ".length()).substring(0, tmp));
//					MS$FOCUSED_ION_VALUE.add(fileLine.substring("MS$FOCUSED_ION: ".length()).substring(tmp+1));
//				} else if(fileLine.startsWith("MS$DATA_PROCESSING: ")){
//					int tmp = fileLine.substring("MS$DATA_PROCESSING: ".length()).indexOf(" ");
//					MS$DATA_PROCESSING_SUBTAG.add(fileLine.substring("MS$DATA_PROCESSING: ".length()).substring(0, tmp));
//					MS$DATA_PROCESSING_VALUE.add(fileLine.substring("MS$DATA_PROCESSING: ".length()).substring(tmp+1));
//				} else if(fileLine.startsWith("PK$SPLASH: ")){
//					PK$SPLASH	= fileLine.substring("PK$SPLASH: ".length());
//				} else if(fileLine.startsWith("PK$ANNOTATION")){
//					// PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
//				} else if(
//						fileLine.matches(" *" + decimalRegEx + " +" + sumFormulaRegEx + "[+-] +" + integerRegEx + " +" + decimalRegEx + " +-?" + decimalRegEx + " *") ||
//						fileLine.matches(" *" + decimalRegEx + " +" + decimalRegEx + " +" + sumFormulaRegEx + "/-" + sumFormulaRegEx + " *") ||
//						fileLine.matches(" *" + decimalRegEx + " +" + decimalRegEx + " +" + sumFormulaRegEx + "/none *") ||
//						fileLine.matches(" *" + decimalRegEx + " +" + decimalRegEx + " +" + sumFormulaRegEx + " *")
//				){
//					String[] tmp	= fileLine.trim().split(" ");
//					PK$ANNOTATION_TENTATIVE_FORMULA.add(tmp[0]);
//					PK$ANNOTATION_FORMULA_COUNT.add(Short.parseShort(tmp[1]));
//					PK$ANNOTATION_THEORETICAL_MASS.add(Float.parseFloat(tmp[2]));
//					PK$ANNTOATION_ERROR_PPM.add(Float.parseFloat(tmp[3]));
//				} else if(fileLine.startsWith("PK$NUM_PEAK: ")){
//					PK$NUM_PEAK	= Integer.parseInt(fileLine.substring("PK$NUM_PEAK: ".length()));
//				} else if(fileLine.startsWith("PK$PEAK: ")){
//					// PK$PEAK: m/z int. rel.int.
//				} else if(fileLine.matches(" *-?" + decimalRegEx + " " + decimalRegEx + " " + decimalRegEx + "")){
//					String[] tmp	= fileLine.trim().split(" ");
//					PK$PEAK_MZ .add(Double.parseDouble(tmp[0]));
//					PK$PEAK_INTENSITY.add(Double.parseDouble(tmp[1]));
//					PK$PEAK_RELATIVE.add( Short.parseShort( tmp[2]));
//				} else if(fileLine.startsWith("//")){
//					// end of record
//				} else {
//					System.out.println("Warning: could not parse line in file '" + file.getName() + "': '" + fileLine + "'");
//					return null;
//				}
//				
//				/*
//???
//DATE: 2016.03.17 (Created 2015.09.25, modified 2016.02.03)
//
//not parsed:
//PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
//57.0701 C4H9+ 1 57.0699 4.61
//67.0542 C5H7+ 1 67.0542 0.35
//69.0336 C4H5O+ 1 69.0335 1.14
//
////
//				 */
//			}
//		} catch(NumberFormatException e){
//			e.printStackTrace();
////		} catch (ParseException e) {
////			e.printStackTrace();
//		}
//		
//		// box information
//		return new AccessionData(
//				ACCESSION,
//				RECORD_TITLE,
//				DATE,
//				AUTHORS,
//				LICENSE,
//				COPYRIGHT,
//				PUBLICATION,
//				COMMENT.toArray(new String[0]),
//				CH$NAME.toArray(new String[0]),
//				//CH$COMPOUND_CLASS_NAME,
//				//CH$COMPOUND_CLASS_CLASS,
//				CH$COMPOUND_CLASS.toArray(new String[0]),
//				CH$FORMULA,
//				CH$EXACT_MASS,
//				CH$SMILES,
//				CH$IUPAC,
//				CH$CDK_DEPICT_SMILES,
//				CH$CDK_DEPICT_GENERIC_SMILES,
//				CH$CDK_DEPICT_STRUCTURE_SMILES,
//				CH$LINK_NAME.toArray(new String[0]),
//				CH$LINK_ID.toArray(new String[0]),
//				SP$SCIENTIFIC_NAME,
//				SP$LINEAGE,
//				SP$LINK.toArray(new String[0]),
//				SP$SAMPLE.toArray(new String[0]),
//				AC$INSTRUMENT,
//				AC$INSTRUMENT_TYPE,
//				AC$MASS_SPECTROMETRY_MS_TYPE,
//				AC$MASS_SPECTROMETRY_ION_MODE,
//				AC$MASS_SPECTROMETRY_SUBTAG.toArray(new String[0]),
//				AC$MASS_SPECTROMETRY_VALUE.toArray(new String[0]),
//				AC$CHROMATOGRAPHY_SUBTAG.toArray(new String[0]),
//				AC$CHROMATOGRAPHY_VALUE.toArray(new String[0]),
//				MS$FOCUSED_ION_SUBTAG.toArray(new String[0]),
//				MS$FOCUSED_ION_VALUE.toArray(new String[0]),
//				MS$DATA_PROCESSING_SUBTAG.toArray(new String[0]),
//				MS$DATA_PROCESSING_VALUE.toArray(new String[0]),
//				PK$SPLASH,
//				//String[],PK$ANNOTATION,	
//				PK$NUM_PEAK,
//				ArrayUtils.toPrimitive(PK$PEAK_MZ.toArray(new Double[0])),
//				ArrayUtils.toPrimitive(PK$PEAK_INTENSITY.toArray(new Double[0])),
//				ArrayUtils.toPrimitive(PK$PEAK_RELATIVE.toArray(new Short[0])),
//				PK$ANNOTATION_TENTATIVE_FORMULA.toArray(new String[0]),
//				ArrayUtils.toPrimitive(PK$ANNOTATION_FORMULA_COUNT.toArray(new Short[0])),
//				ArrayUtils.toPrimitive(PK$ANNOTATION_THEORETICAL_MASS.toArray(new Float[0])),
//				ArrayUtils.toPrimitive(PK$ANNTOATION_ERROR_PPM.toArray(new Float[0]))
//		);
//	}
//	private static String[] getFileContent(File file){
//		List<String> list	= new ArrayList<String>();
//		BufferedReader br = null;
//		try {
//			// read
//			br = new BufferedReader(new FileReader(file));
//			String currentLine;
//			while ((currentLine = br.readLine()) != null)
//				list.add(currentLine);
//
//		} catch (IOException e) {
//			// read error
//			e.printStackTrace();
//		} finally {
//			// close file
//			try {
//				if (br != null)
//					br.close();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//
//		}
//		return list.toArray(new String[0]);
//	}
//	public static AccessionData getAccessionDataFromDatabase(String accessionId, String databaseName){
//		DatabaseManager dbManager	= new DatabaseManager(databaseName);
//		AccessionData accessionData	= dbManager.getAccessionData(accessionId);
//		dbManager.closeConnection();
//		
//		return accessionData;
//	}
// }