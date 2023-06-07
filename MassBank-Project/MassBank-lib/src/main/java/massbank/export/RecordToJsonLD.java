package massbank.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;


import massbank.Record;

/**
 * Convert Record structure to json+ld structured data.
 * 
 * @author rmeier
 * @version 07-06-2023
 */
public class RecordToJsonLD {
	private static final Logger logger = LogManager.getLogger(RecordToJsonLD.class);

	public static String convert(List<Record> records) {
		JsonArray allRecords = new JsonArray();
		for (Record r : records) {
			allRecords.addAll(r.createStructuredDataJsonArray());
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String recordJson = gson.toJson(allRecords);
		return recordJson;
	}

	/**
	 * A wrapper to convert multiple Records and write to file.
	 * 
	 * @param file    to write
	 * @param records to convert
	 * @throws CDKException
	 */
	public static void recordsToJson(File file, List<Record> records) {
		// collect data
		String recordJson = convert(records);
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(recordJson);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
