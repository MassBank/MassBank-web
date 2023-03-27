package massbank.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;
import massbank.Record;

/*
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
*/


/**
 * This class creates RIKEN PRIME msp from the given Record.
 * @author rmeier, htreutle
 * @version 24-02-2023
 */
public class RecordToRIKEN_MSP {
	private static final Logger logger = LogManager.getLogger(RecordToRIKEN_MSP.class);
	
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
		
		sb.append("NAME : ").append(record.CH_NAME().get(0)).append(System.getProperty("line.separator"));
		Map<String, String> MS_FOCUSED_ION = record.MS_FOCUSED_ION_asMap();
		sb.append("PRECURSORMZ: ").append((MS_FOCUSED_ION.containsKey("PRECURSOR_M/Z") ? MS_FOCUSED_ION.get("PRECURSOR_M/Z") : "")).append(System.getProperty("line.separator"));
		sb.append("PRECURSORTYPE: ").append((MS_FOCUSED_ION.containsKey("PRECURSOR_TYPE") ? MS_FOCUSED_ION.get("PRECURSOR_TYPE") : "NA")).append(System.getProperty("line.separator"));
		sb.append("FORMULA: ").append(record.CH_FORMULA()).append(System.getProperty("line.separator"));
		if (record.CH_LINK().containsKey("ChemOnt")) {
			sb.append("Ontology: ").append(record.CH_LINK().get("ChemOnt")).append(System.getProperty("line.separator"));
		}
		sb.append("INCHIKEY: ").append(record.CH_LINK().containsKey("INCHIKEY") ? record.CH_LINK().get("INCHIKEY") : "N/A").append(System.getProperty("line.separator"));
		sb.append("INCHI: ").append(record.CH_IUPAC()).append(System.getProperty("line.separator"));
		sb.append("SMILES: ").append(record.CH_SMILES()).append(System.getProperty("line.separator"));
		sb.append("RETENTIONTIME: ").append(record.AC_CHROMATOGRAPHY_asMap().containsKey("RETENTION_TIME") ? record.AC_CHROMATOGRAPHY_asMap().get("RETENTION_TIME") : "0").append(System.getProperty("line.separator"));
		sb.append("INSTRUMENTTYPE: ").append(record.AC_INSTRUMENT_TYPE()).append(System.getProperty("line.separator"));
		sb.append("INSTRUMENT: ").append(record.AC_INSTRUMENT()).append(System.getProperty("line.separator"));
		if (record.AC_MASS_SPECTROMETRY_ION_MODE().equals("NEGATIVE")) {
			sb.append("IONMODE: Negative").append(System.getProperty("line.separator"));
		} else if (record.AC_MASS_SPECTROMETRY_ION_MODE().equals("POSITIVE")) {
			sb.append("IONMODE: Positive").append(System.getProperty("line.separator"));
		}
				
		List<String> links	= new ArrayList<String>();
		
		record.CH_LINK().forEach((key,value) -> {
			if (!key.equals("ChemOnt")) links.add(key + ":" + value);	    
		});
		sb.append("LINKS: ").append(String.join("; ", links)).append(System.getProperty("line.separator"));
		
		List<String> recordComment = record.COMMENT();
		for (int i = 0; i < recordComment.size(); i++) {
			if(recordComment.get(i).startsWith("CONFIDENCE")) recordComment.set(i,"Annotation " + recordComment.get(i).substring("CONFIDENCE".length()).trim());
        }
		recordComment.add(0, "DB#="+record.ACCESSION()+"; origin=MassBank");
				
		sb.append("Comment: ").append(String.join("; ", recordComment)).append(System.getProperty("line.separator"));
		sb.append("Splash: ").append(record.PK_SPLASH()).append(System.getProperty("line.separator"));
		
		sb.append("Num Peaks"		+ ": " + record.PK_NUM_PEAK()).append(System.getProperty("line.separator"));
		for(Triple<BigDecimal,BigDecimal,Integer> peak : record.PK_PEAK())
			sb.append(peak.getLeft().toPlainString() + "\t" + peak.getMiddle().toPlainString()).append(System.getProperty("line.separator"));
		
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
	}
	
	/**
	 * A wrapper to convert multiple Records and write to file.
	 * @param file to write
	 * @param records to convert
	 * @throws CDKException
	 */
	public static void recordsToRIKEN_MSP(File file, List<Record> records) {
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
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
