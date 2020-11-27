package massbank.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
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
 * @version 21-08-2020
 */
public class RecordToRIKEN_MSP {
	private static final Logger logger = LogManager.getLogger(RecordToRIKEN_MSP.class);
	
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
				
		list.add("NAME"				+ ": " + record.CH_NAME().get(0));
		Map<String, String> MS_FOCUSED_ION = record.MS_FOCUSED_ION_asMap();
		list.add("PRECURSORMZ"	    + ": " + (MS_FOCUSED_ION.containsKey("PRECURSOR_M/Z") ? MS_FOCUSED_ION.get("PRECURSOR_M/Z") : ""));
		list.add("ADDUCTIONNAME"	+ ": " + (MS_FOCUSED_ION.containsKey("PRECURSOR_TYPE") ? MS_FOCUSED_ION.get("PRECURSOR_TYPE") : "NA"));
		list.add("INSTRUMENTTYPE"	+ ": " + record.AC_INSTRUMENT_TYPE());
		list.add("INSTRUMENT"		+ ": " + record.AC_INSTRUMENT());
		list.add("SMILES"			+ ": " + record.CH_SMILES());
		Map<String, String> CH_LINK = record.CH_LINK_asMap();
		list.add("INCHIKEY"			+ ": " + (CH_LINK.containsKey("INCHIKEY") ? CH_LINK.get("INCHIKEY") : "N/A"));
		list.add("INCHI"			+ ": " + record.CH_IUPAC());
		list.add("FORMULA"			+ ": " + record.CH_FORMULA());
		list.add("RETENTIONTIME"	+ ": " + (record.AC_CHROMATOGRAPHY_asMap().containsKey("RETENTION_TIME") ? record.MS_FOCUSED_ION_asMap().get("RETENTION_TIME") : "0"));
		list.add("IONMODE"			+ ": " + record.AC_MASS_SPECTROMETRY_ION_MODE());
		
		List<String> list_links	= new ArrayList<String>();
		for(Entry<String, String> entry : record.CH_LINK_asMap().entrySet())
			list_links.add(entry.getKey() + ":" + entry.getValue());
		String links	= String.join("; ", list_links);
		list.add("LINKS"			+ ": " + links);
		
		List<String> recordComment = record.COMMENT();
		for (int i = 0; i < recordComment.size(); i++) {
			if(recordComment.get(i).startsWith("CONFIDENCE")) recordComment.set(i,recordComment.get(i).substring("CONFIDENCE".length()).trim());
        }
		
		String accession = "DB#="+record.ACCESSION()+"; origin=MassBank";
		System.out.println(accession);
		String comment = String.join("; ", recordComment);
		
		if(comment.equals("")) comment = accession;
		else comment = accession +"; " + comment;
		list.add("Comment"			+ ": " + comment);
		list.add("Num Peaks"		+ ": " + record.PK_NUM_PEAK());
		for(Triple<BigDecimal,BigDecimal,Integer> peak : record.PK_PEAK())
			list.add(peak.getLeft().toPlainString() + "\t" + peak.getMiddle().toPlainString());
		
		return list;
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
		
//		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//		//Loop over the elements in the string array and write each line.
//		for (String line : array) {
//			writer.write(line);
//			writer.newLine();
//		}
//		writer.close();
		
	}

}
