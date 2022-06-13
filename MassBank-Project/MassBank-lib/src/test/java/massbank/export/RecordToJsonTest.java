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

		String minimalRecord = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("minimal_record.txt").toURI()));
		String minimalJson = Files
				.readString(Paths.get(getClass().getClassLoader().getResource("minimal_record.json").toURI()));
		Record record = Validator.validate(minimalRecord, new HashSet<String>());
		String recordJson =  RecordToJson.convert(record);
		//recordJson=recordJson.strip();
		//minimalJson=minimalJson.strip();
		System.out.println(minimalJson);
		System.out.println(recordJson);
		
		assertEquals(minimalJson, recordJson);
	}

}