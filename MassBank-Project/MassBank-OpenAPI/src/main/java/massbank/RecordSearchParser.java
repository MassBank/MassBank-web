package massbank;

import org.petitparser.tools.GrammarParser;

public class RecordSearchParser extends GrammarParser {
	public RecordSearchParser() {
		    super(new RecordSearchParserDefinition());
		  }
}