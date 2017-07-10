package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
public class AccessionData{
	public final String ACCESSION;
	public final String RECORD_TITLE;
	public final Date DATE;
	public final String AUTHORS;
	public final String LICENSE;
	public final String COPYRIGHT;
	public final String PUBLICATION;
	public final String[] COMMENT;
	public final String[] CH$NAME;
	public final String[] CH$COMPOUND_CLASS_NAME;
	public final String[] CH$COMPOUND_CLASS_CLASS;
	public final String CH$FORMULA;
	public final double CH$EXACT_MASS;
	public final String CH$SMILES;
	public final String CH$IUPAC;
	public final String[] CH$LINK_NAME;
	public final String[] CH$LINK_ID;
	public final String AC$INSTRUMENT;
	public final String AC$INSTRUMENT_TYPE;
	public final String[] AC$MASS_SPECTROMETRY;
	public final String[] AC$CHROMATOGRAPHY;
	public final String[] MS$FOCUSED_ION;
	public final String[] MS$DATA_PROCESSING;
	public final String PK$SPLASH;
//	public final String[] PK$ANNOTATION;
	public final int PK$NUM_PEAK;
	public final double[] PK$PEAK_MZ;
	public final double[] PK$PEAK_INT;
	public final short[] PK$PEAK_REL;
	
	public AccessionData(
			String ACCESSION,
			String RECORD_TITLE,
			Date DATE,
			String AUTHORS,
			String LICENSE,
			String COPYRIGHT,
			String PUBLICATION,
			String[] COMMENT,
			String[] CH$NAME,
			String[] CH$COMPOUND_CLASS_NAME,
			String[] CH$COMPOUND_CLASS_CLASS,
			String CH$FORMULA,
			double CH$EXACT_MASS,
			String CH$SMILES,
			String CH$IUPAC,
			String[] CH$LINK_NAME,
			String[] CH$LINK_ID,
			String AC$INSTRUMENT,
			String AC$INSTRUMENT_TYPE,
			String[] AC$MASS_SPECTROMETRY,
			String[] AC$CHROMATOGRAPHY,
			String[] MS$FOCUSED_ION,
			String[] MS$DATA_PROCESSING,
			String PK$SPLASH,
//			String[] PK$ANNOTATION,
			int PK$NUM_PEAK,
			double[] PK$PEAK_MZ,
			double[] PK$PEAK_INT,
			short[] PK$PEAK_REL
	){
		this.ACCESSION            = ACCESSION;
		this.RECORD_TITLE         = RECORD_TITLE;
		this.DATE                 = DATE;
		this.AUTHORS              = AUTHORS;
		this.LICENSE              = LICENSE;
		this.COPYRIGHT            = COPYRIGHT;
		this.PUBLICATION          = PUBLICATION;
		this.COMMENT              = COMMENT;
		this.CH$NAME              = CH$NAME;
		this.CH$COMPOUND_CLASS_NAME  = CH$COMPOUND_CLASS_NAME;
		this.CH$COMPOUND_CLASS_CLASS = CH$COMPOUND_CLASS_CLASS;
		this.CH$FORMULA           = CH$FORMULA;
		this.CH$EXACT_MASS        = CH$EXACT_MASS;
		this.CH$SMILES            = CH$SMILES;
		this.CH$IUPAC             = CH$IUPAC;
		this.CH$LINK_NAME		  = CH$LINK_NAME;
		this.CH$LINK_ID			  = CH$LINK_ID;
		this.AC$INSTRUMENT        = AC$INSTRUMENT;
		this.AC$INSTRUMENT_TYPE   = AC$INSTRUMENT_TYPE;
		this.AC$MASS_SPECTROMETRY = AC$MASS_SPECTROMETRY;
		this.AC$CHROMATOGRAPHY    = AC$CHROMATOGRAPHY;
		this.MS$FOCUSED_ION       = MS$FOCUSED_ION;
		this.MS$DATA_PROCESSING   = MS$DATA_PROCESSING;
		this.PK$SPLASH            = PK$SPLASH;
//		this.PK$ANNOTATION        = PK$ANNOTATION;
		this.PK$NUM_PEAK          = PK$NUM_PEAK;
		this.PK$PEAK_MZ           = PK$PEAK_MZ;
		this.PK$PEAK_INT          = PK$PEAK_INT;
		this.PK$PEAK_REL          = PK$PEAK_REL;
	}
	public String toString() {
		StringBuilder sb	= new StringBuilder();
		
		sb.append("ACCESSION: " + this.ACCESSION + "\n");
		sb.append("RECORD_TITLE: " + this.RECORD_TITLE + "\n");
		sb.append("DATE: " + this.DATE + "\n");
		sb.append("AUTHORS: " + this.AUTHORS + "\n");
		sb.append("LICENSE: " + this.LICENSE + "\n");
		sb.append("COPYRIGHT: " + this.COPYRIGHT + "\n");
		sb.append("PUBLICATION: " + this.PUBLICATION + "\n");
		for(String COMMENT : this.COMMENT)
			sb.append("COMMENT: " + COMMENT + "\n");
		for(String CH$NAME : this.CH$NAME)
			sb.append("CH$NAME: " + CH$NAME + "\n");
		for(int idx = 0; idx < this.CH$COMPOUND_CLASS_NAME.length; idx++)
			sb.append("CH$COMPOUND_CLASS: " + CH$COMPOUND_CLASS_CLASS[idx] + " " + CH$COMPOUND_CLASS_NAME[idx] + "\n");
		sb.append("CH$FORMULA: " + this.CH$FORMULA + "\n");
		sb.append("CH$EXACT_MASS: " + this.CH$EXACT_MASS + "\n");
		sb.append("CH$SMILES: " + this.CH$SMILES + "\n");
		sb.append("CH$IUPAC: " + this.CH$IUPAC + "\n");
		for(int idx = 0; idx < this.CH$LINK_ID.length; idx++)
			sb.append("CH$LINK: " + CH$LINK_NAME[idx] + " " + CH$LINK_ID[idx] + "\n");
		sb.append("AC$INSTRUMENT: " + this.AC$INSTRUMENT + "\n");
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
			sb.append("  " + this.PK$PEAK_MZ[idx] + " " + this.PK$PEAK_INT[idx] + " " + this.PK$PEAK_REL[idx] + "\n");
		sb.append("//");
		
		return sb.toString();
	}
	private static int getMaximumStringLength(String[] sa){
		int maximumMzLength		= 0;
		for(int idx = 0; idx < sa.length; idx++){
			int mzLength	= (sa [idx] + "").length();
			maximumMzLength		= mzLength	> maximumMzLength	? mzLength	: maximumMzLength;
		}
		return maximumMzLength;
	}
	private static String[] pad(String[] PK$PEAK){
		int maximumLength	= AccessionData.getMaximumStringLength(PK$PEAK);
		String padding	= StringUtils.repeat(' ', maximumLength);
		
		String[] PK$PEAK2	= new String[PK$PEAK.length];
		for(int idx = 0; idx < PK$PEAK.length; idx++)
			PK$PEAK2 [idx]	= padding.substring(PK$PEAK[idx].length()) + PK$PEAK[idx];
		
		return PK$PEAK2;
	}
	private static String[] formatPK$PEAK(double[] PK$PEAK, boolean pad){
		String[] PK$PEAK2		= new String[PK$PEAK.length];
		DecimalFormat df = new DecimalFormat("0.0");
		df.setMaximumFractionDigits(8);
		for(int idx = 0; idx < PK$PEAK.length; idx++)
			PK$PEAK2 [idx]	= df.format(PK$PEAK [idx]);
		
		if(pad)	PK$PEAK2	= pad(PK$PEAK2);
		
		return PK$PEAK2;
	}
	public String[] formatPK$PEAK_MZ(boolean pad){
		return formatPK$PEAK(this.PK$PEAK_MZ,  pad);
	}
	public String[] formatPK$PEAK_INT(boolean pad){
		return formatPK$PEAK(this.PK$PEAK_INT, pad);
	}
	public String[] formatPK$PEAK_REL(boolean pad){
		String[] PK$PEAK_REL		= new String[this.PK$PEAK_REL.length];
		for(int idx = 0; idx < this.PK$PEAK_REL.length; idx++)
			PK$PEAK_REL[idx]	= this.PK$PEAK_REL[idx] + "";
		
		return pad(PK$PEAK_REL);
	}
	
	public static String getDatabaseOfAccession(String accession){
		String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
		GetConfig conf = new GetConfig(baseUrl);
		String[] databaseNames	= conf.getDbName();
		
		String databaseName	= null;
		for(String databaseName2 : databaseNames){
			if(AccessionData.existsFile(databaseName2, accession)){
				databaseName	= databaseName2;
				break;
			}
		}
		
		return databaseName;
	}
	public static boolean existsFile(String databaseName, String accession){
		File file	= AccessionData.getFile(databaseName, accession);
		return file.exists();
	}
	public static File getFile(String databaseName, String accession){
		// http://localhost/MassBank/DB/annotation/MassBank/XXX00001.txt
		File file	= new File(MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH) + databaseName + File.separator + accession + ".txt");
		return file;
	}
	public static AccessionData getAccessionDataFromFile(String databaseName, String accession){
		if(!AccessionData.existsFile(databaseName, accession))
			return null;
		File file	= AccessionData.getFile(databaseName, accession);
		return AccessionData.getAccessionDataFromFile(file);
	}
	public static AccessionData getAccessionDataFromFile(File file){
		// initialization of fields
		String ACCESSION		= null;
		String RECORD_TITLE		= null;
		Date DATE				= null;
		String AUTHORS			= null;
		String LICENSE			= null;
		String COPYRIGHT		= null;
		String PUBLICATION		= null;
		List<String> COMMENT	= new ArrayList<String>();
		List<String> CH$NAME	= new ArrayList<String>();
		List<String> CH$COMPOUND_CLASS_NAME		= new ArrayList<String>();
		List<String> CH$COMPOUND_CLASS_CLASS	= new ArrayList<String>();
		String CH$FORMULA		= null;
		double CH$EXACT_MASS	= -1;
		String CH$SMILES		= null;
		String CH$IUPAC			= null;
		List<String> CH$LINK_NAME	= new ArrayList<String>();
		List<String> CH$LINK_ID		= new ArrayList<String>();
		String AC$INSTRUMENT		= null;
		String AC$INSTRUMENT_TYPE	= null;
		List<String> AC$MASS_SPECTROMETRY	= new ArrayList<String>();
		List<String> AC$CHROMATOGRAPHY		= new ArrayList<String>();
		List<String> MS$FOCUSED_ION			= new ArrayList<String>();
		List<String> MS$DATA_PROCESSING		= new ArrayList<String>();
		String PK$SPLASH			= null;
//		List<String> PK$ANNOTATION	= new ArrayList<String>();
		int PK$NUM_PEAK				= -1;
		List<Double> PK$PEAK_MZ		= new ArrayList<Double>();
		List<Double> PK$PEAK_INT	= new ArrayList<Double>();
		List<Short> PK$PEAK_REL		= new ArrayList<Short>();
		
		// fetch information
		String[] fileLines	= getFileContent(file);
		if(fileLines == null)
			return null;
		
		try{
			for(String fileLine : fileLines){
				
				if(fileLine.startsWith("ACCESSION: ")){
					ACCESSION		= fileLine.substring("ACCESSION: ".length());
				} else if(fileLine.startsWith("RECORD_TITLE: ")){
					RECORD_TITLE	= fileLine.substring("RECORD_TITLE: ".length());
				} else if(fileLine.startsWith("DATE: ")){
					SimpleDateFormat parser = new SimpleDateFormat("yyyy.MM.dd");
					DATE	= new Date(parser.parse(fileLine.substring("DATE: ".length())).getTime());
				} else if(fileLine.startsWith("AUTHORS: ")){
					AUTHORS			= fileLine.substring("AUTHORS: ".length());
				} else if(fileLine.startsWith("LICENSE: ")){
					LICENSE	= fileLine.substring("LICENSE: ".length());
				} else if(fileLine.startsWith("COPYRIGHT: ")){
					COPYRIGHT	= fileLine.substring("COPYRIGHT: ".length());
				} else if(fileLine.startsWith("PUBLICATION: ")){
					PUBLICATION	= fileLine.substring("PUBLICATION: ".length());
				} else if(fileLine.startsWith("COMMENT: ")){
					COMMENT.add(fileLine.substring("COMMENT: ".length()));
				} else if(fileLine.startsWith("CH$NAME: ")){
					CH$NAME.add(fileLine.substring("CH$NAME: ".length()));
				} else if(fileLine.startsWith("CH$COMPOUND_CLASS: ")){
					String val	= fileLine.substring("CH$COMPOUND_CLASS: ".length());
					int idx	= val.indexOf("; ");
					if(idx == -1){
						CH$COMPOUND_CLASS_CLASS.add("NA");
						CH$COMPOUND_CLASS_NAME .add(val);
					} else {
						CH$COMPOUND_CLASS_CLASS.add(val.substring(0, idx));
						CH$COMPOUND_CLASS_NAME .add(val.substring(idx + "; ".length()));
					}
				} else if(fileLine.startsWith("CH$FORMULA: ")){
					CH$FORMULA	= fileLine.substring("CH$FORMULA: ".length());
				} else if(fileLine.startsWith("CH$EXACT_MASS: ")){
					CH$EXACT_MASS	= Double.parseDouble(fileLine.substring("CH$EXACT_MASS: ".length()));
				} else if(fileLine.startsWith("CH$SMILES: ")){
					CH$SMILES	= fileLine.substring("CH$SMILES: ".length());
				} else if(fileLine.startsWith("CH$IUPAC: ")){
					CH$IUPAC	= fileLine.substring("CH$IUPAC: ".length());
				} else if(fileLine.startsWith("CH$LINK: ")){
					String[] tmp	= fileLine.substring("CH$LINK: ".length()).split(" ");
					CH$LINK_ID.add(tmp[0]);
					CH$LINK_NAME.add(tmp[1]);
				} else if(fileLine.startsWith("AC$INSTRUMENT: ")){
					AC$INSTRUMENT	= fileLine.substring("AC$INSTRUMENT: ".length());
				} else if(fileLine.startsWith("AC$INSTRUMENT_TYPE: ")){
					AC$INSTRUMENT_TYPE	= fileLine.substring("AC$INSTRUMENT_TYPE: ".length());
				} else if(fileLine.startsWith("AC$MASS_SPECTROMETRY: ")){
					AC$MASS_SPECTROMETRY.add(fileLine.substring("AC$MASS_SPECTROMETRY: ".length()));
				} else if(fileLine.startsWith("AC$CHROMATOGRAPHY: ")){
					AC$CHROMATOGRAPHY.add(fileLine.substring("AC$CHROMATOGRAPHY: ".length()));
				} else if(fileLine.startsWith("MS$FOCUSED_ION: ")){
					MS$FOCUSED_ION.add(fileLine.substring("MS$FOCUSED_ION: ".length()));
				} else if(fileLine.startsWith("MS$DATA_PROCESSING: ")){
					MS$DATA_PROCESSING.add(fileLine.substring("MS$DATA_PROCESSING: ".length()));
				} else if(fileLine.startsWith("PK$SPLASH: ")){
					PK$SPLASH	= fileLine.substring("PK$SPLASH: ".length());
//				} else if(fileLine.startsWith("PK$ANNOTATION")){
//					PK$ANNOTATION	= fileLine.substring("PK$ANNOTATION".length());
//				} else if(fileLine.startsWith("")){
//					PK$ANNOTATION	= fileLine.substring("".length());
				} else if(fileLine.startsWith("PK$NUM_PEAK: ")){
					PK$NUM_PEAK	= Integer.parseInt(fileLine.substring("PK$NUM_PEAK: ".length()));
//				} else if(fileLine.startsWith("PK$PEAK: ")){
//					PK$PEAK	= fileLine.substring("PK$PEAK: ".length());
				} else if(fileLine.matches(" *\\d+(\\.\\d+)? \\d+(\\.\\d+)? \\d+(\\.\\d+)?")){
					String[] tmp	= fileLine.trim().split(" ");
					PK$PEAK_MZ .add(Double.parseDouble(tmp[0]));
					PK$PEAK_INT.add(Double.parseDouble(tmp[1]));
					PK$PEAK_REL.add( Short.parseShort( tmp[2]));
				} else {
					System.out.println("Warning: could not parse line in file '" + file.getName() + "': '" + fileLine + "'");
				}
				
				/*
???
DATE: 2016.03.17 (Created 2015.09.25, modified 2016.02.03)

not parsed:
PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
57.0701 C4H9+ 1 57.0699 4.61
67.0542 C5H7+ 1 67.0542 0.35
69.0336 C4H5O+ 1 69.0335 1.14

//
				 */
			}
		} catch(NumberFormatException e){
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// box information
		return new AccessionData(
				ACCESSION, 
				RECORD_TITLE, 
				DATE, 
				AUTHORS, 
				LICENSE, 
				COPYRIGHT, 
				PUBLICATION, 
				COMMENT.toArray(new String[0]), 
				CH$NAME.toArray(new String[0]), 
				CH$COMPOUND_CLASS_NAME.toArray(new String[0]), 
				CH$COMPOUND_CLASS_CLASS.toArray(new String[0]), 
				CH$FORMULA, 
				CH$EXACT_MASS, 
				CH$SMILES, 
				CH$IUPAC, 
				CH$LINK_NAME.toArray(new String[0]), 
				CH$LINK_ID.toArray(new String[0]), 
				AC$INSTRUMENT, 
				AC$INSTRUMENT_TYPE, 
				AC$MASS_SPECTROMETRY.toArray(new String[0]), 
				AC$CHROMATOGRAPHY.toArray(new String[0]), 
				MS$FOCUSED_ION.toArray(new String[0]), 
				MS$DATA_PROCESSING.toArray(new String[0]), 
				PK$SPLASH, 
//				PK$ANNOTATION.toArray(new String[0]), 
				PK$NUM_PEAK, 
				ArrayUtils.toPrimitive(PK$PEAK_MZ.toArray(new Double[0])),
				ArrayUtils.toPrimitive(PK$PEAK_INT.toArray(new Double[0])),
				ArrayUtils.toPrimitive(PK$PEAK_REL.toArray(new Short[0]))
		);
	}
	private static String[] getFileContent(File file){
		List<String> list	= new ArrayList<String>();
		BufferedReader br = null;
		try {
			// read
			br = new BufferedReader(new FileReader(file));
			String currentLine;
			while ((currentLine = br.readLine()) != null)
				list.add(currentLine);

		} catch (IOException e) {
			// read error
			e.printStackTrace();
		} finally {
			// close file
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		return list.toArray(new String[0]);
	}
}