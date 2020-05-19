package massbank.web.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.smiles.SmilesGenerator;

import massbank.DatabaseManager;
import massbank.Record;

public class RecordExporter {
	/*
.ms2 text format: peptides
-----------------
https://skyline.ms/wiki/home/software/BiblioSpec/page.view?name=BiblioSpec%20input%20and%20output%20file%20formats
This format is recongnized by proteowizard's msconvert and can be converted into other formats such as .mzXML.
In an .ms2 file there are four types of lines. 
	Lines beginning with 'H' are header lines and contain information about how the data was collected as well as comments. They appear at the beginning of the file. 
	Lines beginning with 'S' are followed by the scan number and the precursor m/z. 
	Lines beginning with 'Z' give the charge state followed by the mass of the ion at that charge state. 
	Lines beginning with 'D' contain information relevant to the preceeding charge state. BlibToMs2's output will include D-lines with the sequence and modified sequence. 
The file is arranged with these S, Z and D lines for one spectrum followed by a peak list: 
	a pair of values giving each peaks m/z and intensity. Here is an example file 

H      CreationDate    Mon Apr 12 15:12:14 2010
H       Extractor       BlibToMs2
H       Library /home/me/research/search/demo.blib
S       1       1       636.34
Z       2       1253.36
D       seq     FKNGFQTGSASK
D       modified seq    FKNGFQTGSASK
187.40  12.5
193.10  19.5
242.30  14.2
244.30  9.0
S       2       2       745.3
Z       2       1471.7
D       seq     NFLETVELQVGLK
D       modified seq    NFLETVELQVGLK
1224.60 7.9
1228.70 468.9
1230.40 658.5
1231.50 144.2

BlibBuild .ssl file:
--------------------
https://skyline.ms/wiki/home/software/BiblioSpec/page.view?name=BiblioSpec%20input%20and%20output%20file%20formats

NIST *.msp file:
----------------
https://chemdata.nist.gov/mass-spc/ftp/mass-spc/PepLib.pdf
(section 'Spectrum Fields and Format')

Name: KDLGEEHFK/2
MW: 1103.561
Comment: Spec=Consensus Pep=N-Semitryp_irreg/miss_good Fullname=F.KDLGEEHFK.G/2 Mods=0 Parent=551.781 Inst=it Mz_diff=0.544 Mz_exact=551.7805 Mz_av=552.114 Protein="sp|P02769|ALBU_BOVIN Serum albumin precursor (Allergen Bos d 6) (BSA) - Bos taurus (Bovine)." Pseq=131/1 Organism="Protein" Se=4^X12:ex=0.00037/0.0003992,td=25.85/1379,sd=0/0,hs=38.5/1.433,bs=0.00027,b2=0.00028,bd=133^O10:ex=0.0002435/0.0009314,td=74.85/3.186e+004,pr=3.235e-007/8.612e-007,bs=2.73e-005,b2=5.56e-005,bd=1.56^I1:ex=0.0339/0,dc=0.939/0,do=6.14/0,bs=0.0339,bd=0.939^C1:ex=0.032/0,td=0/0,sd=0/0,hs=555/0,bs=0.032 Sample=7/bsa_cam,2,6/bsa_cam_different_voltages,1,3/bsa_none,0,1/nist_yl_31011_sigma_t9253_bsa_cam,4,6/nist_yl_31011_sigma_t9253_bsa_time_cam,4,6/nist_yl_31611_sigma_t9253_bsa_cam,0,3/nist_yl_sgma_t9253_bsa_none,1,2 Nreps=12/27 Missing=0.1916/0.0688 Parent_med=552.3075/0.22 Max2med_orig=100.0/0.0 Dotfull=0.743/0.044 Dot_cons=0.809/0.048 Unassign_all=0.173 Unassigned=0.105 Dotbest=0.83 Flags=12,9,1 Naa=9 DUScorr=1.5/0.71/2.9 Dottheory=0.84 Pfin=4.6e+008 Probcorr=6.7 Tfratio=6e+003 Pfract=0 Unassigned_corrected=0.011
Num peaks: 124
201.2	149	"? 11/10 0.7"
209.1	238	"b2-35/-0.02 11/11 0.7"
226.3	779	"b2-18/0.18 12/12 1.7"
227.3	484	"b2-17/0.18 12/12 0.9"
228.4	62	"b2-17i/1.28 7/10 0.2"

RIKEN PRIME *.msp format II:
----------------
http://prime.psc.riken.jp/Metabolomics_Software/MS-DIAL/index.html

NAME: Apigenin M-H
PRECURSORMZ: 269.0455469
PRECURSORTYPE: [M-H]-
INSTRUMENTTYPE: DI-ESI-qTof
INSTRUMENT: DI-ESI-qTof
SMILES: OC1=CC=C(C=C1)C1=CC(=O)C2=C(O)C=C(O)C=C2O1
INCHIKEY: KZNIFHPLKGYRTM-UHFFFAOYSA-N
FORMULA: C15H10O5
RETENTIONTIME: -1
IONMODE: Negative
LINKS: CCMSLIB00000077172
Comment: 
Num Peaks: 50
117.0362	19794
118.0391	990
275.031	271
766.5048	422

NAME: Acacetin M-H
PRECURSORMZ: 283.061197
PRECURSORTYPE: [M-H]-
INSTRUMENTTYPE: DI-ESI-Ion Trap
INSTRUMENT: DI-ESI-Ion Trap
SMILES: COC1=CC=C(C=C1)C1=CC(=O)C2=C(O)C=C(O)C=C2O1
INCHIKEY: DANYIYRPLHHOCZ-UHFFFAOYSA-N
FORMULA: C16H12O5
RETENTIONTIME: -1
IONMODE: Negative
LINKS: CCMSLIB00000077212
Comment: 
Num Peaks: 17
150.955	2744
171.014	2412
221.139	2562
256.974	2958


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
	
	
	public static enum ExportFormat {
		NIST_MSP,
		RIKEN_MSP,
		MASSBANK_RECORDS;
	}
	public static void recordExport(File file, ExportFormat format, Record... records) throws CDKException{
		switch (format) {
			case NIST_MSP:{
				recordsToNIST_MSP(file, records);
				break;
			}
			case RIKEN_MSP:{
				recordsToRIKEN_MSP(file, records);
				break;
			}
			case MASSBANK_RECORDS: {
				recordsToZipFile(file, records);
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown Export-Format '" + format + "'!");
		}
	}
	
	/**
	 * wrapper for individual record export function and write
	 * @param file
	 * @param records
	 * @throws CDKException
	 */
	public static void recordsToNIST_MSP(File file, Record... records) throws CDKException{
		// collect data
		List<String> list	= new ArrayList<String>();
		for(Record record : records) {
			try {
				list.addAll(recordToNIST_MSP(record));
				list.add("");
			} catch (Exception e) {
				System.out.println("Error in file " + record.CONTRIBUTOR() + "/" + record.ACCESSION());
				e.printStackTrace();
			}
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
	/**
	 * NIST format, also used by MONA
	 * @param record
	 * @return
	 * @throws CDKException
	 */
	public static List<String> recordToNIST_MSP(Record record) throws CDKException{
		List<String> list	= new ArrayList<String>();
		
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
	 * wrapper for individual record export function and write
	 * @param file
	 * @param records
	 * @throws CDKException
	 */
	public static void recordsToRIKEN_MSP(File file, Record... records) throws CDKException{
		// collect data
		List<String> list	= new ArrayList<String>();
		for(Record record : records) {
			try {
				list.addAll(recordToRIKEN_MSP(record));
				list.add("");
			} catch (Exception e) {
				System.out.println("Error in file " + record.CONTRIBUTOR() + "/" + record.ACCESSION());
				e.printStackTrace();
			}
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
	public static List<String> recordToRIKEN_MSP(Record record) throws CDKException{
		
		/*
+	NAME: Apigenin M-H
+	PRECURSORMZ: 269.0455469
+	PRECURSORTYPE: [M-H]-
+	INSTRUMENTTYPE: DI-ESI-qTof
+	INSTRUMENT: DI-ESI-qTof
+	SMILES: OC1=CC=C(C=C1)C1=CC(=O)C2=C(O)C=C(O)C=C2O1
+	INCHIKEY: KZNIFHPLKGYRTM-UHFFFAOYSA-N
+	FORMULA: C15H10O5
+	RETENTIONTIME: -1
+	IONMODE: Negative
+	LINKS: CCMSLIB00000077172
+	Comment: 
+	Num Peaks: 50
+	117.0362	19794
	118.0391	990
	275.031	271
	766.5048	422
		 */
		
		List<String> list_links	= new ArrayList<String>();
		for(Entry<String, String> entry : record.CH_LINK_asMap().entrySet())
			list_links.add(entry.getValue() + ":" + entry.getKey());
		String links	= String.join("; ", list_links);
		
		String comment	= null;
		for(String comment2 : record.COMMENT())
			if(comment2.startsWith("CONFIDENCE"))
				comment	= comment2.substring("CONFIDENCE".length() + 1);
		if(comment == null)
			comment	= String.join("; ", record.COMMENT());
		if(comment.equals(""))
			comment	= "N/A";
		
		String smiles	= record.CH_SMILES();
		String inchi	= record.CH_IUPAC();
		String inchiKey	= (record.CH_LINK_asMap().containsKey("INCHIKEY") ? record.CH_LINK_asMap().get("INCHIKEY") : "N/A");
		if(inchiKey.equals("NA"))	inchiKey	= "N/A";
		
		if(
				(smiles == null || smiles.equals("NA") || smiles.equals("N/A") || smiles.equals("")) &&
				( inchi != null && !inchi.equals("NA") && !inchi.equals("N/A") && !inchi.equals(""))
		)
			smiles	= SmilesGenerator.isomeric().create(record.CH_IUPAC_obj());
		if(
				( inchi == null ||   inchi.equals("NA") ||   inchi.equals("N/A") ||   inchi.equals("")) &&
				(smiles != null && !smiles.equals("NA") && !smiles.equals("N/A") && !smiles.equals(""))
		)
			inchi	= InChIGeneratorFactory.getInstance().getInChIGenerator(record.CH_SMILES_obj()).getInchi();
		
		if(inchiKey.equals("N/A") && record.CH_IUPAC_obj()  != null && !record.CH_IUPAC_obj().isEmpty())
			inchiKey	= InChIGeneratorFactory.getInstance().getInChIGenerator(record.CH_IUPAC_obj()).getInchiKey();
		if(inchiKey.equals("N/A") && record.CH_SMILES_obj() != null && !record.CH_SMILES_obj().isEmpty())
			inchiKey	= InChIGeneratorFactory.getInstance().getInChIGenerator(record.CH_SMILES_obj()).getInchiKey();
		
		if(  smiles.equals("")) smiles		= "N/A";
		if(   inchi.equals("")) inchi		= "N/A";
		if(inchiKey.equals("")) inchiKey	= "N/A";
		
		List<String> list	= new ArrayList<String>();
		
//		if(  smiles.equals("N/A") && (! inchi.equals("N/A") || !inchiKey.equals("N/A")))	System.out.println("SMILES missing: " + smiles + " vs " + inchi + " + " + inchiKey);
//		if(   inchi.equals("N/A") && (!smiles.equals("N/A") || !inchiKey.equals("N/A")))	System.out.println("InChI missing: " + inchi + " vs " + smiles + " + " + inchiKey);
//		if(inchiKey.equals("N/A") && (! inchi.equals("N/A") || !  smiles.equals("N/A")))	System.out.println("InChIKey missing: " + inchiKey + " vs " + inchi + " + " + smiles);
//		
//		if(smiles.equals("N/A") || inchi.equals("N/A") || inchiKey.equals("N/A"))	return list;
//		if(!record.AC_MASS_SPECTROMETRY_MS_TYPE().equals("MS2"))	return list;
////		if(!record.AC_MASS_SPECTROMETRY_ION_MODE().equals("POSITIVE"))	return list;
//		if(!record.AC_MASS_SPECTROMETRY_ION_MODE().equals("NEGATIVE"))	return list;
		
		list.add("NAME"				+ ": " + record.CH_NAME().get(0));
		list.add("PRECURSORMZ"		+ ": " + (record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_M/Z") ? record.MS_FOCUSED_ION_asMap().get("PRECURSOR_M/Z") : ""));
		list.add("PRECURSORTYPE"	+ ": " + (record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_TYPE") ? record.MS_FOCUSED_ION_asMap().get("PRECURSOR_TYPE") : "NA"));
		list.add("INSTRUMENTTYPE"	+ ": " + record.AC_INSTRUMENT_TYPE());
		list.add("INSTRUMENT"		+ ": " + record.AC_INSTRUMENT());
		list.add("SMILES"			+ ": " + smiles);
		list.add("INCHIKEY"			+ ": " + inchiKey);
		list.add("INCHI"			+ ": " + inchi);
		list.add("FORMULA"			+ ": " + record.CH_FORMULA());
		list.add("RETENTIONTIME"	+ ": " + (record.AC_CHROMATOGRAPHY_asMap().containsKey("RETENTION_TIME") ? record.MS_FOCUSED_ION_asMap().get("RETENTION_TIME") : "NA"));
		list.add("IONMODE"			+ ": " + record.AC_MASS_SPECTROMETRY_ION_MODE());
		list.add("LINKS"			+ ": " + links);
		list.add("Comment"			+ ": " + comment);
		list.add("Num Peaks"		+ ": " + record.PK_NUM_PEAK());
		for(Triple<BigDecimal,BigDecimal,Integer> peak : record.PK_PEAK())
			list.add(peak.getLeft() + "\t" + peak.getRight());
		
		return list;
	}
	
	public static void recordsToZipFile(File file, Record... records){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ZipOutputStream zos = new ZipOutputStream(fos);
			
			for(Record record : records) {
				try {
					String fileName	= record.ACCESSION() + ".txt";
					
					ZipEntry zipEntry = new ZipEntry(fileName);
					zos.putNextEntry(zipEntry);
					zos.write(record.toString().getBytes());
					zos.closeEntry();
				} catch (Exception e) {
					System.out.println("Error in file " + record.CONTRIBUTOR() + "/" + record.ACCESSION());
					e.printStackTrace();
				}
			}
			
			zos.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exportWholeMassBank(ExportFormat exportFormat, File file) throws SQLException, ConfigurationException, CDKException {
		// #################################################################
		// get data
		System.out.println("Creating DB connection");
		DatabaseManager dbMan	= new DatabaseManager("MassBank");
		System.out.println("Fetching accession codes");
		String[] accessions	= dbMan.getAccessions();
		System.out.println("Fetching " + accessions.length + " records");
		Record[] records	= new Record[accessions.length];
		for(int i = 0; i < accessions.length; i++)
			records[i]	= dbMan.getAccessionData(accessions[i]);
		System.out.println("Closing DB connection");
		dbMan.closeConnection();
		
		// #################################################################
		// export data
		System.out.println("Exporting records to file " + file.getAbsolutePath());
		recordExport(file, exportFormat, records);
		System.out.println("Finished");
	}
	public static void main(String[] args) throws SQLException, ConfigurationException, CDKException {
		if(false) {
			DatabaseManager dbMan	= new DatabaseManager("MassBank");
			Record record	= dbMan.getAccessionData("AU100601");
			dbMan.closeConnection();
			//Record record	= new DatabaseManager("MassBank").getAccessionData("UA006601");
			
			System.out.println(record.toString());
			System.out.println();
			
			List<String> export	= recordToNIST_MSP(record);
			System.out.println(String.join("\n", export));
			
			
			File file	= new File("/home/htreutle/Downloads/tmp/Test.zip");
			recordExport(file, ExportFormat.MASSBANK_RECORDS, record);
			
			File file2	= new File("/home/htreutle/Downloads/tmp/Test.txt");
			recordExport(file2, ExportFormat.NIST_MSP, record);
		}
		
		File file	= new File("/home/htreutle/Downloads/tmp/190516_MassBank.msp");
		exportWholeMassBank(ExportFormat.RIKEN_MSP, file);
	}
}
