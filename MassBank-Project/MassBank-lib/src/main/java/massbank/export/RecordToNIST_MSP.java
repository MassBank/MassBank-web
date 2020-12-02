package massbank.export;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;
import massbank.Record;

/*
MONA *.msp format III:
----------------
http://mona.fiehnlab.ucdavis.edu/downloads

Name: CLC_301.1468_14.3
Synon: Chlorcyclizine
Synon: 1-[(4-chlorophenyl)-phenylmethyl]-4-methylpiperazine
SYNON: $:00in-source
DB#: ET010001
InChIKey: WFNAKBGANONZEQ-UHFFFAOYSA-N
Precursor_type: [M+H]+
Spectrum_type: MS2
PrecursorMZ: 301.1466
Instrument_type: LC-ESI-QFT
Instrument: Q Exactive Orbitrap Thermo Scientific
Ion_mode: P
Collision_energy: 15, 30, 45, 60, 70 or 90 (nominal)
InChIKey: WFNAKBGANONZEQ-UHFFFAOYSA-N
Formula: C18H21ClN2
MW: 300
ExactMass: 300.13932635199996
Comments: "accession=ET010001" "author=R. Gulde, E. Schymanski, K. Fenner, Department of Environmental Chemistry, Eawag" "license=CC BY" "copyright=Copyright (C) 2016 Eawag, Duebendorf, Switzerland" "publication=Gulde, Meier, Schymanski, Kohler, Helbling, Derrer, Rentsch & Fenner; ES&T 2016 50(6):2908-2920. DOI: 10.1021/acs.est.5b05186. Systematic Exploration of Biotransformation Reactions of Amine-containing Micropollutants in Activated Sludge" "comment=CONFIDENCE Parent Substance with Reference Standard (Level 1)" "comment=INTERNAL_ID 100" "exact mass=300.1393" "instrument=Q Exactive Orbitrap Thermo Scientific" "instrument type=LC-ESI-QFT" "ms level=MS2" "ionization=ESI" "fragmentation mode=HCD" "collision energy=15, 30, 45, 60, 70 or 90 (nominal)" "resolution=17500" "column=Atlantis T3 3um, 3x150mm, Waters with guard column" "flow gradient=95/5 at 0 min, 5/95 at 15 min, 5/95 at 20 min, 95/5 at 20.1 min, 95/5 at 25 min" "flow rate=300 uL/min" "retention time=14.6 min" "solvent a=water with 0.1% formic acid" "solvent b=methanol with 0.1% formic acid" "precursor m/z=301.1466" "precursor type=[M+H]+" "ionization mode=positive" "mass accuracy=0.007810149499385606" "mass error=-2.351999967231677E-6" "SMILES=CN1CCN(CC1)C(C1=CC=CC=C1)C1=CC=C(Cl)C=C1" "cas=82-93-9" "pubchem cid=2710" "chemspider=2609" "InChI=InChI=1S/C18H21ClN2/c1-20-11-13-21(14-12-20)18(15-5-3-2-4-6-15)16-7-9-17(19)10-8-16/h2-10,18H,11-14H2,1H3" "InChIKey=WFNAKBGANONZEQ-UHFFFAOYSA-N" "molecular formula=C18H21ClN2" "total exact mass=300.13932635199996" "SMILES=CN1CCN(CC1)C(C2=CC=CC=C2)C3=CC=C(C=C3)Cl"
Num Peaks: 5
99.0915 0.440287
165.0694 1.883217
166.0777 3.318937
201.0466 100.000000

Name: CLC_301.1468_14.3
Synon: Chlorcyclizine
Synon: 1-[(4-chlorophenyl)-phenylmethyl]-4-methylpiperazine
SYNON: $:00in-source
DB#: ET010002
InChIKey: WFNAKBGANONZEQ-UHFFFAOYSA-N
Precursor_type: [M+H]+
Spectrum_type: MS2
PrecursorMZ: 301.1466
Instrument_type: LC-ESI-QFT
Instrument: Q Exactive Orbitrap Thermo Scientific
Ion_mode: P
Collision_energy: 15, 30, 45, 60, 70 or 90 (nominal)
InChIKey: WFNAKBGANONZEQ-UHFFFAOYSA-N
Formula: C18H21ClN2
MW: 300
ExactMass: 300.13932635199996
Comments: "accession=ET010002" "author=R. Gulde, E. Schymanski, K. Fenner, Department of Environmental Chemistry, Eawag" "license=CC BY" "copyright=Copyright (C) 2016 Eawag, Duebendorf, Switzerland" "publication=Gulde, Meier, Schymanski, Kohler, Helbling, Derrer, Rentsch & Fenner; ES&T 2016 50(6):2908-2920. DOI: 10.1021/acs.est.5b05186. Systematic Exploration of Biotransformation Reactions of Amine-containing Micropollutants in Activated Sludge" "comment=CONFIDENCE Parent Substance with Reference Standard (Level 1)" "comment=INTERNAL_ID 100" "exact mass=300.1393" "instrument=Q Exactive Orbitrap Thermo Scientific" "instrument type=LC-ESI-QFT" "ms level=MS2" "ionization=ESI" "fragmentation mode=HCD" "collision energy=15, 30, 45, 60, 70 or 90 (nominal)" "resolution=17500" "column=Atlantis T3 3um, 3x150mm, Waters with guard column" "flow gradient=95/5 at 0 min, 5/95 at 15 min, 5/95 at 20 min, 95/5 at 20.1 min, 95/5 at 25 min" "flow rate=300 uL/min" "retention time=14.6 min" "solvent a=water with 0.1% formic acid" "solvent b=methanol with 0.1% formic acid" "precursor m/z=301.1466" "precursor type=[M+H]+" "ionization mode=positive" "mass accuracy=0.007810149499385606" "mass error=-2.351999967231677E-6" "SMILES=CN1CCN(CC1)C(C1=CC=CC=C1)C1=CC=C(Cl)C=C1" "cas=82-93-9" "pubchem cid=2710" "chemspider=2609" "InChI=InChI=1S/C18H21ClN2/c1-20-11-13-21(14-12-20)18(15-5-3-2-4-6-15)16-7-9-17(19)10-8-16/h2-10,18H,11-14H2,1H3" "InChIKey=WFNAKBGANONZEQ-UHFFFAOYSA-N" "molecular formula=C18H21ClN2" "total exact mass=300.13932635199996" "SMILES=CN1CCN(CC1)C(C2=CC=CC=C2)C3=CC=C(C=C3)Cl"
Num Peaks: 7
99.0916 0.527554
165.07 14.
183.0805 3.771867
201.0466 100.000000
*/


/**
 * This class creates RIKEN PRIME msp from the given Record.
 * @author rmeier, htreutle
 * @version 21-08-2020
 */
public class RecordToNIST_MSP {
	private static final Logger logger = LogManager.getLogger(RecordToNIST_MSP.class);
	
	/**
	 * A plain converter Record to String with RIKEN PRIME msp.
	 * @param record to convert
	 */
	public static List<String> convert(Record record) {
		System.out.println(record.ACCESSION());
		List<String> list	= new ArrayList<String>();
		if (record.DEPRECATED()) {
			logger.warn(record.ACCESSION() + " is deprecated. No export possible.");
			return list;
		}
		
		List<String> tmpList	= record.CH_NAME();
		list.add("Name" + ": " + tmpList.get(0));
		for(int i = 1; i < tmpList.size(); i++)
			list.add("Synon" + ": " + tmpList.get(i));
		
		list.add("DB#" + ": " + record.ACCESSION());
		if(record.CH_LINK_asMap().containsKey("INCHIKEY"))
			list.add("InChIKey" + ": " + record.CH_LINK_asMap().get("INCHIKEY"));
		list.add("InChI" + ": " + record.CH_IUPAC());
		list.add("SMILES" + ": " + record.CH_SMILES());
		
		if(record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_TYPE"))
			list.add("Precursor_type" + ": " + record.MS_FOCUSED_ION_asMap().get("PRECURSOR_TYPE"));
		list.add("Spectrum_type" + ": " + record.AC_MASS_SPECTROMETRY_MS_TYPE());
		if(record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_M/Z"))
			list.add("PrecursorMZ" + ": " + record.MS_FOCUSED_ION_asMap().get("PRECURSOR_M/Z"));
		
		list.add("Instrument_type" + ": " + record.AC_INSTRUMENT_TYPE());
		list.add("Instrument" + ": " + record.AC_INSTRUMENT());
		list.add("Ion_mode" + ": " + record.AC_MASS_SPECTROMETRY_ION_MODE());
		
		if(record.AC_MASS_SPECTROMETRY_asMap().containsKey("COLLISION_ENERGY"))
			list.add("Collision_energy" + ": " + record.AC_MASS_SPECTROMETRY_asMap().get("COLLISION_ENERGY"));
		
		list.add("Formula" + ": " + record.CH_FORMULA());
		list.add("MW" + ": " + Math.round(record.CH_EXACT_MASS().floatValue()));
		list.add("ExactMass" + ": " + record.CH_EXACT_MASS());
		
		// remaining stuff:
		/*
#################################################################
## legend
+	exported
-	not exported
/	partially exported

#################################################################
## fields
-	private final String contributor;

+	private String accession;
-	private String record_title;
+	private String record_title_name;
/	private String record_title_condition;
		+ Instrument, MS_TYPE, collision energy
		- RESOLUTION (-> AC$MASS_SPECTROMETRY)
-	private LocalDate date;
-	private String authors;
-	private String license;	
-	private String copyright;
-	private String publication;
-	private List<String> comment;
+	private List<String> ch_name;
-	private List<String> ch_compound_class;
+	private IMolecularFormula ch_formula;
+	private double ch_exact_mass;
+	private IAtomContainer ch_smiles;
+	private IAtomContainer ch_iupac;
/	private List<Pair<String, String>> ch_link;
		+ INCHIKEY
		- Rest
-	private String sp_scientific_name;
-	private String sp_lineage;
-	private List<Pair<String, String>> sp_link;
-	private List<String> sp_sample;
+	private String ac_instrument;
+	private String ac_instrument_type;
+	private String ac_mass_spectrometry_ms_type;
+	private String ac_mass_spectrometry_ion_mode;
/	private List<Pair<String, String>> ac_mass_spectrometry;
		+ COLLISION_ENERGY
		- Rest
-	private List<Pair<String, String>> ac_chromatography;
/	private List<Pair<String, String>> ms_focused_ion;
		+ PRECURSOR_TYPE, PRECURSOR_M/Z
		- Rest
-	private List<Pair<String, String>> ms_data_processing;
+	private String pk_splash;
-	private List<String> pk_annotation_header;
-	private final List<List<String>> pk_annotation;
+	private int pk_num_peak;
+	private final List<List<Double>> pk_peak;
		 */
		
		/*
https://chemdata.nist.gov/mass-spc/ftp/mass-spc/PepLib.pdf:
Comments are composed of a series of space delimited field=value pairs, where values may be embedded within double quotes. 
All field names are described in Table 3. 
There is one mandatory field, namely Parent=<m/z>, which is the precursor ion m/z required for searching.
		 */
		
		list.add("Comments" + ": " + 
				"Parent=" + ((record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_M/Z")) ? record.MS_FOCUSED_ION_asMap().get("PRECURSOR_M/Z") : -1)
		);
		//Comments: "accession=ET010001" "author=R. Gulde, E. Schymanski, K. Fenner, Department of Environmental Chemistry, Eawag" "license=CC BY" "copyright=Copyright (C) 2016 Eawag, Duebendorf, Switzerland" "publication=Gulde, Meier, Schymanski, Kohler, Helbling, Derrer, Rentsch & Fenner; ES&T 2016 50(6):2908-2920. DOI: 10.1021/acs.est.5b05186. Systematic Exploration of Biotransformation Reactions of Amine-containing Micropollutants in Activated Sludge" "comment=CONFIDENCE Parent Substance with Reference Standard (Level 1)" "comment=INTERNAL_ID 100" "exact mass=300.1393" "instrument=Q Exactive Orbitrap Thermo Scientific" "instrument type=LC-ESI-QFT" "ms level=MS2" "ionization=ESI" "fragmentation mode=HCD" "collision energy=15, 30, 45, 60, 70 or 90 (nominal)" "resolution=17500" "column=Atlantis T3 3um, 3x150mm, Waters with guard column" "flow gradient=95/5 at 0 min, 5/95 at 15 min, 5/95 at 20 min, 95/5 at 20.1 min, 95/5 at 25 min" "flow rate=300 uL/min" "retention time=14.6 min" "solvent a=water with 0.1% formic acid" "solvent b=methanol with 0.1% formic acid" "precursor m/z=301.1466" "precursor type=[M+H]+" "ionization mode=positive" "mass accuracy=0.007810149499385606" "mass error=-2.351999967231677E-6" "SMILES=CN1CCN(CC1)C(C1=CC=CC=C1)C1=CC=C(Cl)C=C1" "cas=82-93-9" "pubchem cid=2710" "chemspider=2609" "InChI=InChI=1S/C18H21ClN2/c1-20-11-13-21(14-12-20)18(15-5-3-2-4-6-15)16-7-9-17(19)10-8-16/h2-10,18H,11-14H2,1H3" "InChIKey=WFNAKBGANONZEQ-UHFFFAOYSA-N" "molecular formula=C18H21ClN2" "total exact mass=300.13932635199996" "SMILES=CN1CCN(CC1)C(C2=CC=CC=C2)C3=CC=C(C=C3)Cl"
		
		list.add("Splash" + ": " + record.PK_SPLASH());
		list.add("Num Peaks" + ": " + record.PK_NUM_PEAK());
		
		for(Triple<BigDecimal,BigDecimal,Integer> peak : record.PK_PEAK()){
//			StringBuilder peakS	= new StringBuilder();
//			peakS.append(peak.get(0));
//			for(int i = 1; i < peak.size(); i++)
//				peakS.append(" " + peak.get(i));
//			list.add(peakS.toString());
			list.add(peak.getLeft() + " " + peak.getRight());
		}
		
		return list;
	}
	
	/**
	 * A wrapper to convert multiple Records and write to file.
	 * @param file to write
	 * @param records to convert
	 * @throws CDKException
	 */
	public static void recordsToNIST_MSP(File file, List<Record> records) {
		// collect data
		List<String> list	= new ArrayList<String>();
		for(Record record : records) {
			list.addAll(convert(record));
			list.add("");
		}
		try {
			FileUtils.writeStringToFile(
					file, 
					String.join("\n", list.toArray(new String[list.size()])), 
					Charset.forName("UTF-8")
			);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
