package massbank;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;

public class RecordParserDefinitionTest {

	Record record = new Record();
	private final RecordParserDefinition recordParser = new RecordParserDefinition(record, new HashSet<String>());

	private Result assertValid(String source, String production) {
		Parser parser = recordParser.build(production).end();
		Result result = parser.parse(source);
		assertTrue(result.isSuccess());
		return result;
	}
	
	private Result assertInvalid(String source, String production) {
		Parser parser = recordParser.build(production).end();
		Result result = parser.parse(source);
		assertTrue(result.isFailure());
		return result;
	}

	@Test
	public void testACCESSION() {
		assertValid("ACCESSION: MSBNK-Aa10_zZ-Aa10_zZ\n", "accession");
		assertInvalid("ACCESSION: MSBNK-Aa:10_zZ-Aa10_zZ\n", "accession");
	}

}
