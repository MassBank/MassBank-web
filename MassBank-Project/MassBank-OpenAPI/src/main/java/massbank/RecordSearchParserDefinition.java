package massbank;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.petitparser.parser.primitive.CharacterParser;
import org.petitparser.parser.primitive.StringParser;
import org.petitparser.tools.GrammarDefinition;

public class RecordSearchParserDefinition extends GrammarDefinition {
	private static final Logger logger = LogManager.getLogger(RecordSearchParserDefinition.class);

	public RecordSearchParserDefinition() {
		def("start",
			ref("name")
			.map((List<?> value) -> {
				System.out.println(value);
				return value;
			})
			.end()
		);

		//Comparison operators in FIQL notation or alternative notation:
		//Equal to : ==
		def("eq", StringParser.of("=="));
		//Not equal to : !=
		def("ne", StringParser.of("!="));
		//Less than : =lt=
		def("lt", StringParser.of("=lt=").or(StringParser.of("<")));
		//Less than or equal to : =le=
		def("le", StringParser.of("=le=").or(StringParser.of("<=")));
		//Greater than operator : =gt=
		def("gt", StringParser.of("=gt=").or(StringParser.of(">")));
		//Greater than or equal to : =ge=
		def("ge", StringParser.of("=ge=").or(StringParser.of(">=")));
		//In : =in=
		def("in", StringParser.of("=in="));
		//Not in : =out=
		def("out", StringParser.of("=out="));


		def("input",
			StringParser.of("ACCESSION")
		);

		def("constraint",

			StringParser.of("ACCESSION")
		);


		def("comparison",
			ref("selector")
			.seq(ref("comparison-op"))
			.seq(ref("arguments"))
		);

		def("selector",
			ref("unreserved-str")
		);

		def("comparison-op",
			ref("comp-fiql")
			.or(ref("comp-alt"))
		);

		def("name",
			StringParser.of("name")
			.seq(ref("eq"))
			.seq(ref("value"))
		);

		def("value",
			ref("unreserved-str")
			.or(ref("double-quoted"))
			//.or(ref("single-quoted"))

		);
		def("unreserved-str",
			CharacterParser.noneOf("\"\'();, ").plus().flatten()
			.map((List<?> value) -> {
			    	System.out.println("unreserved-str");
				System.out.println(value);
				return value;
			})
		);
		def("double-quoted",
			CharacterParser.of('"')
			.seq(
				CharacterParser.noneOf("\\\"")
				.or(StringParser.of("\\\""))
				.or(StringParser.of("\\\\"))
				.star().flatten()
			)
			.seq(CharacterParser.of('"'))
			.map((List<?> value) -> {
			    	System.out.println("double-quoted");
				System.out.println(value);
				return value;
			})
		);

	}
}


//input          = or, EOF;
//or             = and, { "," , and };
//and            = constraint, { ";" , constraint };
//constraint     = ( group | comparison );
//group          = "(", or, ")";


//arguments      = ( "(", value, { "," , value }, ")" ) | value;
//unreserved-str = unreserved, { unreserved }
//single-quoted  = "'", { ( escaped | all-chars - ( "'" | "\" ) ) }, "'";
//double-quoted  = '"', { ( escaped | all-chars - ( '"' | "\" ) ) }, '"';
//
//reserved       = '"' | "'" | "(" | ")" | ";" | "," | "=" | "!" | "~" | "<" | ">";
//unreserved     = all-chars - reserved - " ";
//escaped        = "\", all-chars;
//all-chars      = ? all unicode characters ?;


