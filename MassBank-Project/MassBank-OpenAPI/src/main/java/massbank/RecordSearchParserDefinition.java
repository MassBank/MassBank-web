package massbank;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.petitparser.context.Context;
import org.petitparser.context.Result;
import org.petitparser.tools.GrammarDefinition;

public class RecordSearchParserDefinition extends GrammarDefinition {
	private static final Logger logger = LogManager.getLogger(RecordSearchParserDefinition.class);
	
	public RecordSearchParserDefinition() {
		def("start",
				ref("accession")
				.end()
		);
	}
}
