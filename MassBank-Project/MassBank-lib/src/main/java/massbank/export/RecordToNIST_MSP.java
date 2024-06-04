package massbank.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
	public static String convert(Record record) {
		StringBuilder sb = new StringBuilder();
		
		if (record.DEPRECATED()) {
			logger.warn(record.ACCESSION() + " is deprecated. No export possible.");
			return sb.toString();
		}
		
		List<String> tmpList	= record.CH_NAME();
		sb.append("Name: ").append(tmpList.get(0)).append(System.getProperty("line.separator"));
		for(int i = 1; i < tmpList.size(); i++)
			sb.append("Synon: ").append(tmpList.get(i)).append(System.getProperty("line.separator"));
		
		sb.append("DB#: ").append(record.ACCESSION()).append(System.getProperty("line.separator"));
		if(record.CH_LINK().containsKey("INCHIKEY"))
			sb.append("InChIKey: ").append(record.CH_LINK().get("INCHIKEY")).append(System.getProperty("line.separator"));
		sb.append("InChI: ").append(record.CH_IUPAC()).append(System.getProperty("line.separator"));
		sb.append("SMILES: ").append(record.CH_SMILES()).append(System.getProperty("line.separator"));
		
		if(record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_TYPE"))
			sb.append("Precursor_type: ").append(record.MS_FOCUSED_ION_asMap().get("PRECURSOR_TYPE")).append(System.getProperty("line.separator"));
		sb.append("Spectrum_type: ").append(record.AC_MASS_SPECTROMETRY_MS_TYPE()).append(System.getProperty("line.separator"));
		if(record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_M/Z"))
			sb.append("PrecursorMZ: ").append(record.MS_FOCUSED_ION_asMap().get("PRECURSOR_M/Z")).append(System.getProperty("line.separator"));
		
		sb.append("Instrument_type: ").append(record.AC_INSTRUMENT_TYPE()).append(System.getProperty("line.separator"));
		sb.append("Instrument: ").append(record.AC_INSTRUMENT()).append(System.getProperty("line.separator"));
		sb.append("Ion_mode: ").append(record.AC_MASS_SPECTROMETRY_ION_MODE()).append(System.getProperty("line.separator"));
		
		if(record.AC_MASS_SPECTROMETRY_asMap().containsKey("COLLISION_ENERGY"))
			sb.append("Collision_energy: ").append(record.AC_MASS_SPECTROMETRY_asMap().get("COLLISION_ENERGY")).append(System.getProperty("line.separator"));
		
		sb.append("Formula: ").append(record.CH_FORMULA()).append(System.getProperty("line.separator"));
		sb.append("MW: ").append(Math.round(record.CH_EXACT_MASS().floatValue())).append(System.getProperty("line.separator"));
		sb.append("ExactMass: ").append(record.CH_EXACT_MASS()).append(System.getProperty("line.separator"));
		
		/*
https://chemdata.nist.gov/mass-spc/ftp/mass-spc/PepLib.pdf:
Comments are composed of a series of space delimited field=value pairs, where values may be embedded within double quotes. 
All field names are described in Table 3. 
There is one mandatory field, namely Parent=<m/z>, which is the precursor ion m/z required for searching.
		 */
		
		sb.append("Comments: ").append("Parent=" + ((record.MS_FOCUSED_ION_asMap().containsKey("PRECURSOR_M/Z")) ? record.MS_FOCUSED_ION_asMap().get("PRECURSOR_M/Z") : -1)).append(System.getProperty("line.separator"));
		//Comments: "accession=ET010001" "author=R. Gulde, E. Schymanski, K. Fenner, Department of Environmental Chemistry, Eawag" "license=CC BY" "copyright=Copyright (C) 2016 Eawag, Duebendorf, Switzerland" "publication=Gulde, Meier, Schymanski, Kohler, Helbling, Derrer, Rentsch & Fenner; ES&T 2016 50(6):2908-2920. DOI: 10.1021/acs.est.5b05186. Systematic Exploration of Biotransformation Reactions of Amine-containing Micropollutants in Activated Sludge" "comment=CONFIDENCE Parent Substance with Reference Standard (Level 1)" "comment=INTERNAL_ID 100" "exact mass=300.1393" "instrument=Q Exactive Orbitrap Thermo Scientific" "instrument type=LC-ESI-QFT" "ms level=MS2" "ionization=ESI" "fragmentation mode=HCD" "collision energy=15, 30, 45, 60, 70 or 90 (nominal)" "resolution=17500" "column=Atlantis T3 3um, 3x150mm, Waters with guard column" "flow gradient=95/5 at 0 min, 5/95 at 15 min, 5/95 at 20 min, 95/5 at 20.1 min, 95/5 at 25 min" "flow rate=300 uL/min" "retention time=14.6 min" "solvent a=water with 0.1% formic acid" "solvent b=methanol with 0.1% formic acid" "precursor m/z=301.1466" "precursor type=[M+H]+" "ionization mode=positive" "mass accuracy=0.007810149499385606" "mass error=-2.351999967231677E-6" "SMILES=CN1CCN(CC1)C(C1=CC=CC=C1)C1=CC=C(Cl)C=C1" "cas=82-93-9" "pubchem cid=2710" "chemspider=2609" "InChI=InChI=1S/C18H21ClN2/c1-20-11-13-21(14-12-20)18(15-5-3-2-4-6-15)16-7-9-17(19)10-8-16/h2-10,18H,11-14H2,1H3" "InChIKey=WFNAKBGANONZEQ-UHFFFAOYSA-N" "molecular formula=C18H21ClN2" "total exact mass=300.13932635199996" "SMILES=CN1CCN(CC1)C(C2=CC=CC=C2)C3=CC=C(C=C3)Cl"
		
		sb.append("Splash: ").append(record.PK_SPLASH()).append(System.getProperty("line.separator"));
		
		// mz<1 needs to be prevented, check #157
		StringBuilder peaklist = new StringBuilder();
		int numPeaks = 0;
		for(Triple<BigDecimal,BigDecimal,Integer> peak : record.PK_PEAK()) {
			if (peak.getLeft().compareTo(new BigDecimal(1)) >= 0) {
				numPeaks++;
				peaklist.append(peak.getLeft() + " " + peak.getRight()).append(System.getProperty("line.separator"));
			}
		}
		sb.append("Num Peaks: ").append(numPeaks).append(System.getProperty("line.separator"));
		sb.append(peaklist);
				
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
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
			list.add(convert(record));
			list.add("");
		}
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			for (String line : list) {
				writer.write(line);
				//writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
