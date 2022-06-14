package massbank.export;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import org.junit.Test;

import massbank.Record;
import massbank.cli.Validator;

public class RecordToJsonTest {

	@Test
	public void testToJson() throws IOException, URISyntaxException {

		String minimalRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("minimal_record.txt").toURI()));
		String minimalJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("minimal_record.json").toURI()));
		Record minimalRecord = Validator.validate(minimalRecordString, new HashSet<String>());
		String minimalRecordJson =  RecordToJson.convert(minimalRecord);
		assertEquals(minimalJson, minimalRecordJson);
		
		String maximalRecordString = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("maximal_record.txt").toURI()));
		String maximalJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("maximal_record.json").toURI()));
		Record maximalRecord = Validator.validate(maximalRecordString, new HashSet<String>());
		String maximalRecordJson =  RecordToJson.convert(maximalRecord);
		assertEquals(maximalJson, maximalRecordJson);
		
	}

}