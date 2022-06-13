package massbank.export;

import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import massbank.Record;

/**
 * Convert Record structure to json.
 * 
 * @author rmeier
 * @version 09-06-2022
 */
public class RecordToJson {
	private static final Logger logger = LogManager.getLogger(RecordToJson.class);

	static class CollectionAdapter implements JsonSerializer<Collection<?>> {
		@Override
		public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == null || src.isEmpty())
				return null;

			JsonArray array = new JsonArray();

			for (Object child : src) {
				JsonElement element = context.serialize(child);
				array.add(element);
			}

			return array;
		}
	}

	public static String convert(Record record) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//.registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter()).create();
		String recordJson = gson.toJson(record);
		return recordJson;
	}
	
	public static String convert(List<Record> records) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//.registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter()).create();
		String recordJson = gson.toJson(records);
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

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
				//.registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter()).create();

		String recordJson = gson.toJson(records.get(0));
		System.out.println(recordJson);

		// collect data
		/*
		 * List<String> list = new ArrayList<String>(); for(Record record : records) {
		 * list.add(convert(record)); list.add(""); }
		 * 
		 * BufferedWriter writer; try { writer = new BufferedWriter(new
		 * FileWriter(file)); for (String line : list) { writer.write(line);
		 * //writer.newLine(); } writer.close(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

}
