package massbank;

import org.petitparser.tools.GrammarParser;

import java.util.Set;

public class RecordParser extends GrammarParser {

    public RecordParser(Set<String> config) {
        super(new RecordParserDefinition(config));
    }
}