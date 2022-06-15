package massbank;

import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;

public class RecordParserDefinitionTest {

	Record record = new Record();
	private final RecordParserDefinition recordParser = new RecordParserDefinition(record, new HashSet<String>());

	private Result assertValid(String source, String production) {
		Parser parser = recordParser.build(production).end();
		Result result = parser.parse(source);
		Assertions.assertTrue(result.isSuccess());
		return result;
	}
	
	private Result assertInvalid(String source, String production) {
		Parser parser = recordParser.build(production).end();
		Result result = parser.parse(source);
		Assertions.assertTrue(result.isFailure());
		return result;
	}

	@Test
	public void testACCESSION() {
		assertValid("ACCESSION: MSBNK-Aa10_zZ-Aa10_zZ\n", "accession");
		assertInvalid("ACCESSION: MSBNK-Aa:10_zZ-Aa10_zZ\n", "accession");
	}

}
