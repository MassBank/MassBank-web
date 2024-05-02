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
		assertValid("ACCESSION: MSBNK-Aa10_zZ-A10_Z\n", "accession");
		assertInvalid("ACCESSION: MSBNK-Aa:10_zZ-A10_Z\n", "accession");
	}
	
	@Test
	public void testCVterm() {
		assertValid("[MS,,,]", "cvterm");
		assertValid("[MS ,,,]", "cvterm");
		assertValid("[ MS ,,,]", "cvterm");
		assertValid("[,,,]", "cvterm");
		assertValid("[ ,,,]", "cvterm");
		assertValid("[MS, MS:1001477, SpectraST,]", "cvterm");
		assertValid("[MOD, MOD:00648, \"N,O-diacetylated L-serine\",]", "cvterm");
		assertValid("[MS, MS:1003294, electron activated dissociation,]", "cvterm_validated");
//		assertInvalid("[MS, MMMS:1003294, electron activated dissociation,]", "cvterm_validated");
//		assertInvalid("[MS, MS:1003294, collision-induced dissociation,]", "cvterm_validated");
	}

}
