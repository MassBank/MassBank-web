package massbank.export;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import massbank.Record;

/**
 * Convert Record structure to json.
 * @author rmeier
 * @version 09-06-2022
 */
public class RecordToJson {
	private static final Logger logger = LogManager.getLogger(RecordToJson.class);
	
	public static class RecordJsonSerializer {
		String ACCESSION;
		List<String> RECORD_TITLE;
		String DATE; 
		
//		boolean deprecated;
//		String deprecated_content;
//		
//		String date;
//		String authors;
//		String license;	
//		String copyright; // optional
//		String publication; // optional
//		String project; // optional
//		List<String> comment; // optional
//		List<String> ch_name;
//		List<String> ch_compound_class;
//		String ch_formula;
//		BigDecimal ch_exact_mass;
//		String ch_smiles;
//		String ch_iupac;
//		LinkedHashMap<String, String> ch_link; // optional
//		String sp_scientific_name; // optional
//		String sp_lineage; // optional
//		List<Pair<String, String>> sp_link; // optional
//		List<String> sp_sample; // optional
//		String ac_instrument;
//		String ac_instrument_type;
//		String ac_mass_spectrometry_ms_type;
//		String ac_mass_spectrometry_ion_mode;
//		List<Pair<String, String>> ac_mass_spectrometry; // optional
//		List<Pair<String, String>> ac_chromatography; // optional
//		List<Pair<String, String>> ms_focused_ion; // optional
//		List<Pair<String, String>> ms_data_processing; // optional
//		String pk_splash;
//		List<String> pk_annotation_header; // optional
//		final List<Pair<BigDecimal, List<String>>> pk_annotation; // optional
//		final List<Triple<BigDecimal,BigDecimal,Integer>> pk_peak;
		
		
	    RecordJsonSerializer(Record record)
	    {
	    	ACCESSION=record.ACCESSION();
	    	RECORD_TITLE=record.RECORD_TITLE();
	    	DATE=record.DATE();
	    }
	}
	
	/**
	 * A wrapper to convert multiple Records and write to file.
	 * @param file to write
	 * @param records to convert
	 * @throws CDKException
	 */
	public static void recordsToJson(File file, List<Record> records) {		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String recordJson = gson.toJson(new RecordJsonSerializer(records.get(0)));  
		System.out.println(recordJson);
		
		// collect data
/*		List<String> list	= new ArrayList<String>();
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
		}*/
	}

}
