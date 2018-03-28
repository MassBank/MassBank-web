package massbank.admin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import massbank.Record;
import massbank.RecordParser;

public class Validator2 {
	public static String recordstringExample = 
			"ACCESSION: BSU00002\n" +
			"RECORD_TITLE: Veratramine; LC-ESI-QTOF; MS2; CE: 50 V\n" +
			"DATE: 2016.01.19 (Created 2012.12.18, modified 2013.07.16)\n" +
			"AUTHORS: Chandler, C. and Habig, J. Boise State University\n" +
			"LICENSE: CC BY-SA\n" +
			"COPYRIGHT: Chandler, C., Habig, J. and McDougal O. Boise State University\n" +
			"PUBLICATION: Iida T, Tamura T, et al, J Lipid Res. 29, 165-71 (1988). [PMID: 3367086]\n" +
			"COMMENT: Data obtained from a veratramine standard purchased from Logan Natural Products, Logan, Utah USA.\n" +
			"COMMENT: Data obtained from a veratramine standard purchased from Logan Natural Products, Logan, Utah USA.\n" +
			"CH$NAME: Veratramine\n" + 
			"CH$NAME: {(3beta,23R)-14,15,16,17-Tetradehydroveratraman-3,23-diol\n" +
//			"CH$COMPOUND_CLASS: Natural Product; Alkaloid; blah\n" + 
			"CH$COMPOUND_CLASS: N/A; Environmental Standard\n" +
			"CH$FORMULA: C27H39NO2\n" +
			"CH$EXACT_MASS: 409.29807\n" +
			"CH$SMILES: N/A\n" +
			"CH$IUPAC: N/A\n" +
			"CH$LINK: CAS 60-70-8\n" + 
			"CH$LINK: CHEBI 9951\n" + 
			"CH$LINK: CHEMSPIDER 5845\n" +
			"CH$LINK: KEGG C10829\n" + 
			"CH$LINK: KNAPSACK C00002270\n" + 
			"CH$LINK: PUBCHEM 13012\n" +
			"SP$SCIENTIFIC_NAME: Mus musculus\n" +
			"SP$LINK: NCBI-TAXONOMY 10090\n" +
			"SP$LINK: NCBI-TAXONOMY 10090\n" +
			"SP$SAMPLE: Liver extracts\n" +
			"SP$SAMPLE: Liver extracts\n" +
			"AC$INSTRUMENT: Bruker maXis ESI-QTOF\n" +
			"AC$INSTRUMENT_TYPE: LC-ESI-QTOF\n" +
			"AC$MASS_SPECTROMETRY: MS_TYPE MS2\n" + 
			"AC$MASS_SPECTROMETRY: ION_MODE POSITIVE\n" + 
			"AC$MASS_SPECTROMETRY: COLLISION_ENERGY 50 eV\n" + 
			"AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE CID\n" + 
			"AC$MASS_SPECTROMETRY: IONIZATION ESI\n" + 
			"AC$MASS_SPECTROMETRY: RESOLUTION 35000\n" + 
			"AC$CHROMATOGRAPHY: COLUMN_NAME Acclaim RSLC C18 2.2um, 2.1x100mm, Thermo\n" + 
			"AC$CHROMATOGRAPHY: FLOW_GRADIENT 99/1 at 0-1 min, 61/39 at 3 min, 0.1/99.9 at 14-16 min, 99/1 at 16.1-20 min\n" + 
			"AC$CHROMATOGRAPHY: FLOW_RATE 200 uL/min at 0-3 min, 400 uL/min at 14 min, 480 uL/min at 16-19 min, 200 uL/min at 19.1-20 min\n" +
			"AC$CHROMATOGRAPHY: NAPS_RTI 100\n" +
			"AC$CHROMATOGRAPHY: RETENTION_TIME 6.2 min\n" + 
			"AC$CHROMATOGRAPHY: SOLVENT A 90:10 water:methanol with 0.01% formic acid and 5mM ammonium formate\n" + 
			"AC$CHROMATOGRAPHY: SOLVENT B methanol with 0.01% formic acid and 5mM ammonium formate\n" +
			"MS$FOCUSED_ION: PRECURSOR_M/Z 410.3\n" +
			"MS$FOCUSED_ION: PRECURSOR_TYPE [M+H]+\n" +
			"PK$SPLASH: splash10-0002-0960000000-77302b0326a418630a84\n" +
			"PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)\n" +
			"  134.0594 C8H8NO+ 2 134.0600 -4.77\n" +
			"  135.0618 C7[13]CH8NO+ 2 135.0632 10.37\n" +
			"  140.016 C6H6NOS+ 1 140.0165 -3.22\n" +
			"PK$NUM_PEAK: 100\n" +
			"PK$PEAK: m/z int. rel.int.\n" +
			"  84.1 12461 32\n" +
			"  105.1 2208 6\n" +
			"  107.1 2394 6\n" +
			"  114.1 40390 105\n" +
			"  115.1 2816 7\n" +
			"  119.1 3122 8\n" +
			"  121.1 2233 6\n" +
			"  124.1 31739 82\n" +
			"  125.1 2905 8\n" +
			"  129.1 2850 7\n" +
			"  131.1 49572 129\n" +
			"  132.1 4865 13\n" +
			"  133.1 81554 212\n" +
			"  134.1 8725 23\n" +
			"  141.1 1570 4\n" +
			"  144.1 1940 5\n" +
			"  145.1 41441 108\n" +
			"  146.1 4421 11\n" +
			"  147.1 5354 14\n" +
			"  151.1 3257 8\n" +
			"  154.1 1568 4\n" +
			"  155.1 10003 26\n" +
			"  156.1 6505 17\n" +
			"  157.1 143020 372\n" +
			"  158.1 17481 45\n" +
			"  159.1 82905 215\n" +
			"  160.1 11221 29\n" +
			"  161.1 2347 6\n" +
			"  167.1 2176 6\n" +
			"  168.1 2768 7\n" +
			"  169.1 46331 120\n" +
			"  170.1 6508 17\n" +
			"  171.1 134721 350\n" +
			"  172.1 18235 47\n" +
			"  173.1 3731 10\n" +
			"  175.1 2015 5\n" +
			"  179.1 1858 5\n" +
			"  180.1 1535 4\n" +
			"  181.1 10257 27\n" +
			"  182.1 4717 12\n" +
			"  183.1 41396 108\n" +
			"  184.1 7101 18\n" +
			"  185.1 6914 18\n" +
			"  192.1 1409 4\n" +
			"  193.1 6170 16\n" +
			"  194.1 2234 6\n" +
			"  195.1 14163 37\n" +
			"  196.1 4474 12\n" +
			"  197.1 15633 41\n" +
			"  198.1 2557 7\n" +
			"  199.1 1506 4\n" +
			"  206.1 3117 8\n" +
			"  207.1 10875 28\n" +
			"  208.1 3128 8\n" +
			"  209.1 8959 23\n" +
			"  210.1 1778 5\n" +
			"  211.1 19727 51\n" +
			"  212.2 3417 9\n" +
			"  219.1 2485 6\n" +
			"  220.1 4278 11\n" +
			"  221.1 13267 34\n" +
			"  222.1 5170 13\n" +
			"  223.1 4101 11\n" +
			"  225.2 1653 4\n" +
			"  233.1 6156 16\n" +
			"  234.1 3148 8\n" +
			"  235.2 13261 34\n" +
			"  236.2 4846 13\n" +
			"  237.2 4596 12\n" +
			"  247.2 6331 16\n" +
			"  248.2 6908 18\n" +
			"  249.2 6605 17\n" +
			"  251.2 2136 6\n" +
			"  261.2 1339 3\n" +
			"  262.2 16113 42\n" +
			"  263.2 4055 11\n" +
			"  265.2 1366 4\n" +
			"  277.2 29461 77\n" +
			"  278.2 6409 17\n" +
			"  280.2 6501 17\n" +
			"  281.2 8268 21\n" +
			"  282.2 1857 5\n" +
			"  295.2 384391 999\n" +
			"  296.2 86672 225\n" +
			"  297.2 10146 26\n" +
			"  309.2 2642 7\n" +
			"  319.2 3763 10\n" +
			"  320.2 2207 6\n" +
			"  333.2 3370 9\n" +
			"  362.3 1851 5\n" +
			"  363.3 1716 4\n" +
			"  375.3 2380 6\n" +
			"  376.3 1392 4\n" +
			"  377.3 1755 5\n" +
			"  392.3 23807 62\n" +
			"  393.3 6937 18\n" +
			"  396.3 2914 8\n" +
			"  410.3 6059 16\n" +
			"  411.3 1871 5\n" +
			"  414.3 9233 24\n" +
			"//";	
	
	public static Record validate(String recordstring, String contributor) {
		// test non standard ASCII chars and print warnings
		for (int i = 0; i < recordstring.length(); i++) {
			if (recordstring.charAt(i) > 0x7F) {
				String[] tokens = recordstring.split("\\r?\\n");
				System.out.println("Warning: non standard ASCII charactet found. This might be an error. Please check carefully.");
				int line = 0, col = 0, offset = 0;
				for (String token : tokens) {
					offset = offset + token.length() + 1;
					if (i < offset) {
						col = i - (offset - (token.length() + 1));
						System.out.println(tokens[line]);
						StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", tokens[line].length()));
						error_at.setCharAt(col, '^');
						System.out.println(error_at);
						break;
					}
					line++;
				}
			}
		}
		
		Record record = new Record(contributor);
		Parser recordparser = new RecordParser(record);
		Result res = recordparser.parse(recordstring);
		if (res.isFailure()) {
			System.err.println();
			System.err.println(res.getMessage());
			int position = res.getPosition();
			String[] tokens = recordstring.split("\\n");

			int line = 0, col = 0, offset = 0;
			for (String token : tokens) {
				offset = offset + token.length() + 1;
				if (position < offset) {
					col = position - (offset - (token.length() + 1));
					System.err.println(tokens[line]);
					StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", col));
					error_at.append('^');
					System.err.println(error_at);
//					record = new Record();
					break;
				}
				line++;
			}
			return null;
		} else
			return record;
	}

	public static void main(String[] arguments) throws Exception {
		boolean haserror = false;
		if (arguments.length==0) {
			Record record = validate(recordstringExample, "Boise State University");
			if (record == null) System.err.println("Error.");
			else System.out.println(record.toString());
		}
		else {
			for (String filename : arguments) {
				//System.out.print("Validating " + filename + " ... ");
//				String contributor	= (new File(filename)).getParentFile().getName();
				recordstringExample = FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8);
//				Record record = validate(recordstringExample, contributor);
				Record record = validate(recordstringExample, "");
				if (record == null) {
					System.err.println("Error in " + filename);
					haserror = true;
				}
				else {
					System.out.println("ok");
					System.out.println(record.toString());
				}
			}
		}
		if (haserror) System.exit(1);
	}
}