package massbank.export;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import massbank.Record;
import massbank.cli.Validator;

public class RecordToJsonTest {

	@Test
	public void testToJsonMinimalRecord() throws IOException, URISyntaxException {
		String minimalRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("MSBNK-test-TST00001.txt").toURI()));
		String minimalJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("minimal_record.json").toURI()));
		Record minimalRecord = Validator.validate(minimalRecordString, new HashSet<String>());
		String minimalRecordJson =  RecordToJson.convert(minimalRecord);
		Assertions.assertEquals(minimalJson, minimalRecordJson);
	}
		
	@Test
	public void testToJsonMaximalRecord() throws IOException, URISyntaxException {
		String maximalRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("MSBNK-test-TST00002.txt").toURI()));
		String maximalJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("maximal_record.json").toURI()));
		Record maximalRecord = Validator.validate(maximalRecordString, new HashSet<String>());
		String maximalRecordJson =  RecordToJson.convert(maximalRecord);
		Assertions.assertEquals(maximalJson, maximalRecordJson);
	}
	
	@Test
	public void testToJsonDeprecatedRecord() throws IOException, URISyntaxException {
		String deprecatedRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("MSBNK-test-TST00003.txt").toURI()));
		String deprecatedJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("deprecated_record.json").toURI()));
		Record deprecatedRecord = Validator.validate(deprecatedRecordString, new HashSet<String>());
		String deprecatedRecordJson =  RecordToJson.convert(deprecatedRecord);
		Assertions.assertEquals(deprecatedJson, deprecatedRecordJson);
	}
	
	@Test
	public void testToJsonCombinedRecord() throws IOException, URISyntaxException {
		String minimalRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("MSBNK-test-TST00001.txt").toURI()));
		Record minimalRecord = Validator.validate(minimalRecordString, new HashSet<String>());
		String maximalRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("MSBNK-test-TST00002.txt").toURI()));
		Record maximalRecord = Validator.validate(maximalRecordString, new HashSet<String>());
		String deprecatedRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("MSBNK-test-TST00003.txt").toURI()));
		Record deprecatedRecord = Validator.validate(deprecatedRecordString, new HashSet<String>());
		
				
		String combinedJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("combined_record.json").toURI()));
		List<Record> records = new ArrayList<Record>();
		records.add(minimalRecord); records.add(maximalRecord); records.add(deprecatedRecord);
		String combinedRecordJson =  RecordToJson.convert(records);
		Assertions.assertEquals(combinedJson, combinedRecordJson);
		
	}

}