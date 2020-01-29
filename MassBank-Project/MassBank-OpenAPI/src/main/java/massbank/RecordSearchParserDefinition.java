package massbank;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.petitparser.context.Context;
import org.petitparser.context.Result;
import org.petitparser.parser.primitive.StringParser;
import org.petitparser.tools.GrammarDefinition;

public class RecordSearchParserDefinition extends GrammarDefinition {
	private static final Logger logger = LogManager.getLogger(RecordSearchParserDefinition.class);
	
	public RecordSearchParserDefinition() {
		def("start",
				ref("accession")
				.end()
		);
		
		def("accession", 
				StringParser.of("ACCESSION")
		);
	}
}


//input          = or, EOF;
//or             = and, { "," , and };
//and            = constraint, { ";" , constraint };
//constraint     = ( group | comparison );
//group          = "(", or, ")";
//comparison     = selector, comparison-op, arguments;
//selector       = unreserved-str;
//comparison-op  = comp-fiql | comp-alt;
//comp-fiql      = ( ( "=", { ALPHA } ) | "!" ), "=";
//comp-alt       = ( ">" | "<" ), [ "=" ];
//arguments      = ( "(", value, { "," , value }, ")" ) | value;
//value          = unreserved-str | double-quoted | single-quoted;
//
//unreserved-str = unreserved, { unreserved }
//single-quoted  = "'", { ( escaped | all-chars - ( "'" | "\" ) ) }, "'";
//double-quoted  = '"', { ( escaped | all-chars - ( '"' | "\" ) ) }, '"';
//
//reserved       = '"' | "'" | "(" | ")" | ";" | "," | "=" | "!" | "~" | "<" | ">";
//unreserved     = all-chars - reserved - " ";
//escaped        = "\", all-chars;
//all-chars      = ? all unicode characters ?;


