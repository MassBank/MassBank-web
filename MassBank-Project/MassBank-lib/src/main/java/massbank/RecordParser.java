package massbank;

import java.util.Set;

import org.petitparser.tools.GrammarParser;

/**
 * Record parser. To parse an Record consider the following code:
 *
 * <pre>
 * Parser record = new RecordParser();
 * Object result = record.parse(your_record_content);
 * System.out.println(result.value);
 * </pre>
 */
public class RecordParser extends GrammarParser {
  public RecordParser(Record callback, Set<String> config) {
    super(new RecordParserDefinition(callback, config));
  }
}
