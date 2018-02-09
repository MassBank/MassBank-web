package massbank;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.time.LocalDate;
import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.letter;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.petitparser.context.Context;
import org.petitparser.context.Result;
import org.petitparser.context.Token;
import org.petitparser.parser.primitive.CharacterParser;
import org.petitparser.parser.primitive.StringParser;
import org.petitparser.tools.GrammarDefinition;
import net.sf.jniinchi.INCHI_RET;


public class RecordParserDefinition extends GrammarDefinition {

	@SuppressWarnings("unchecked")
	public RecordParserDefinition(Record callback) {
		def("start",
			ref("accession")
			.seq(ref("record_title"))
			.seq(ref("date"))
			.seq(ref("authors"))
			.seq(ref("license"))
			.seq(ref("copyright").optional())
			.seq(ref("publication").optional())
			.seq(ref("comment").optional())
			.seq(ref("ch_name"))
			.seq(ref("ch_compound_class"))
			.seq(ref("ch_formula"))
			.seq(ref("ch_exact_mass"))
			.seq(ref("ch_smiles"))
			.seq(ref("ch_iupac"))
			.seq(ref("ch_link").optional())
			.seq(ref("sp_scientific_name").optional())
			.seq(ref("sp_lineage").optional())
			.seq(ref("sp_link").optional())
			.seq(ref("sp_sample").optional())
			.seq(ref("ac_instrument"))
			.seq(ref("ac_instrument_type"))
			.seq(ref("ac_mass_spectrometry_ms_type"))
			.seq(ref("ac_mass_spectrometry_ion_mode"))
			.seq(ref("ac_mass_spectrometry").optional())
			.seq(ref("ac_chromatography").optional())
			.seq(ref("ms_focused_ion").optional())
			.seq(ref("ms_data_processing").optional())
			.seq(ref("pk_splash"))
		);/*
			.seq(ref("endtag"))
			.map((List<?> value) -> {
				value.remove(value.size()-1);
				return value;
			})
			.end()
		);*/
		
		// 1.1 Syntax Rules
		// Single line information is either one of the followings:
		// Tag : space Value ( ; space Value)
		// Tag : space subtag space Value ( ; space Value)
		// Multiple line information
		// First line is Tag: space
		// Following lines are space space Value
		// Last line of a MassBank Record is // .
		def("tagsep", StringParser.of(": "));
		def("valuesep", StringParser.of("; "));
		def("endtag", StringParser.of("//"));

		// 2.1 Record Specific Information
		// 2.1.1 ACCESSION
		// Identifier of the MassBank Record. Mandatory
		// Example
		// ACCESSION: ZMS00006
		// 8-character fix-length string.
		// Prefix two or three alphabetical capital characters.
		def("accession", 
			StringParser.of("ACCESSION")
			.seq(ref("tagsep"))
			// parse accession and put it in the record
			.seq(
				letter().times(2).seq(digit().times(6))
				.or(letter().times(3).seq(digit().times(5)))
				.flatten()
				.map((String value) -> {
					callback.ACCESSION(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.1.2 RECORD_TITLE (CH$NAME ; AC$INSTRUMENT_TYPE ; AC$MASS_SPECTROMETRY: MS_TYPE)
		// Brief Description of MassBank Record. Mandatory
		// Example: RECORD_TITLE: (-)-Nicotine; ESI-QQ; MS2; CE 40 V; [M+H]+
		// It consists of the values of CH$NAME; AC$INSTRUMENT_TYPE; AC$MASS_SPECTROMETRY: MS_TYPE;.
		def("record_title",
			StringParser.of("RECORD_TITLE")
			.seq(ref("tagsep"))
			.seq(
				ref("ch_name_value")
				.seq(ref("valuesep"))
				.pick(0)
			)
			.seq(
				ref("ac_instrument_type_value")
				.seq(ref("valuesep"))
				.pick(0)
			)
			.seq(
				ref("ac_mass_spectrometry_ms_type_value")
			)
			// TODO curation required because ionisation energy is not in format doc
			.seq(
				ref("valuesep")
				.seq(
					CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.pick(1)
				.optional()
			)
			.seq(Token.NEWLINE_PARSER)
			.map((List<?> value) -> {
				//System.out.println(value);
				if (value.get(value.size()-2) == null) callback.RECORD_TITLE(value.subList(2, value.size()-2).toString());
				else callback.RECORD_TITLE(value.subList(2, value.size()-1).toString());
				return value;						
			})
		);
		
		// 2.1.3 DATE
		// Date of the Creation or the Last Modification of MassBank Record. Mandatory
		// Example
		// DATE: 2011.02.21 (Created 2007.07.07)
		// DATE: 2016.01.15
		// DATE: 2016.01.19 (Created 2006.12.21, modified 2011.05.06)
		def("date_value",
			CharacterParser.digit().times(4).flatten().trim(CharacterParser.of('.')).map((String value) -> Integer.parseInt(value))
			.seq(
				CharacterParser.digit().times(2)
				.flatten().trim(CharacterParser.of('.'))
				.map((String value) -> Integer.parseInt(value))
			)
			.seq(
				CharacterParser.digit().times(2)
				.flatten()
				.map((String value) -> Integer.parseInt(value))
			)
			.map((List<Integer> value) -> {
				return LocalDate.of(value.get(0),value.get(1),value.get(2));		
			})
		);
		def("date",
			StringParser.of("DATE")
   			.seq(ref("tagsep"))
   			.seq(ref("date_value"))
    		.seq(
    			ref("date_value")
    			.trim(StringParser.of(" (Created "),StringParser.of(", modified "))
    			.seq(ref("date_value").trim(CharacterParser.none(), CharacterParser.of(')')))
    			.or(ref("date_value").trim(StringParser.of(" (Created "),CharacterParser.of(')')))
    			.optional()
    		)
    		.seq(Token.NEWLINE_PARSER)
			.map((List<?> value) -> {
				//System.out.println(value);
				callback.DATE((LocalDate) value.get(2));
				return value;						
			})
		);
		
		// 2.1.4 AUTHORS
		// Authors and Affiliations of MassBank Record. Mandatory
		// Example
		// AUTHORS: Akimoto N, Grad Sch Pharm Sci, Kyoto Univ and Maoka T, Res Inst Prod Dev.
		// Only single-byte characters are allowed.  For example, ö is not allowed.
		def("authors",
			StringParser.of("AUTHORS")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.AUTHORS(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.1.5 LICENSE
		// License of MassBank Record. Mandatory
		// Example
		// LICENSE: CC BY
		// TODO fix format doc
		def("license",
			StringParser.of("LICENSE")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.LICENSE(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
			
		// 2.1.6 COPYRIGHT
		// Copyright of MassBank Record. Optional
		// Example
		// COPYRIGHT: Keio University
		def("copyright",
			StringParser.of("COPYRIGHT")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.COPYRIGHT(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.1.7 PUBLICATION
		// Reference of the Mass Spectral Data. Optional
		// Example
		// PUBLICATION: Iida T, Tamura T, et al, J Lipid Res. 29, 165-71 (1988). [PMID: 3367086]
		// Citation with PubMed ID is recommended.
		def("publication",
			StringParser.of("PUBLICATION")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.PUBLICATION(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.1.8 COMMENT
		// Comments.   Optional and Iterative 
		// In MassBank, COMMENT fields are often used to show the relations of the present record with other MassBank
		// records and with data files. In these cases, the terms in brackets [ and ] are reserved for the comments
		// specific to the following five examples.
		// Example 1
		// COMMENT: This record is a MS3 spectrum. Link to the MS2 spectrum is added in the following comment field.
		// COMMENT: [MS2] KO008089
		// Example 2
		// COMMENT: This record was generated by merging the following three MassBank records.
		// COMMENT: [Merging] KO006229 Tiglate; ESI-QTOF; MS2; CE:10 V [M-H]-.
		// COMMENT: [Merging] KO006230 Tiglate; ESI-QTOF; MS2; CE:20 V [M-H]-.
		// COMMENT: [Merging] KO006231 Tiglate; ESI-QTOF; MS2; CE:30 V [M-H]-.
		// Example 3
		// COMMENT: This record was merged into a MassBank record, KOX00012, with other records.
		// COMMENT: [Merged] KOX00012
		// Example 4
		// COMMENT: Analytical conditions of LC-MS were described in separate files.
		// COMMENT: [Mass spectrometry] ms1.txt
		// COMMENT: [Chromatography] lc1.txt.
		// Example 5
		// COMMENT: Profile spectrum of this record is given as a JPEG file.
		// COMMENT: [Profile] CA000185.jpg
		def("comment",
			StringParser.of("COMMENT")
			.seq(ref("tagsep"))
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER)
			.plus()
			.map((List<?> value) -> {
				//System.out.println(value);
				List<String> comments = new ArrayList<String>();
				for (Object v : value) comments.add(((List<String>) v).get(2));
				callback.COMMENT(comments);
				return value;
			})
		);
		
		
		// 2.2.1 CH$NAME
		// Name of the Chemical Compound Analyzed. Mandatory and Iterative
		// Example
		// CH$NAME: D-Tartaric acid
		// CH$NAME: (2S,3S)-Tartaric acid
		// No prosthetic molecule of adducts (HCl, H2SO3, H2O, etc), conjugate ions (Chloride, etc) , and 
		// protecting groups (TMS, etc.) is included.
		// Chemical names which are listed in the compound list are recommended.  Synonyms could be added.
		// If chemical compound is a stereoisomer, stereochemistry should be indicated.
		// TODO no ';' in CH$NAME
		def("ch_name_value",
			CharacterParser.word().or(CharacterParser.anyOf("-+, ()[]{}/.:$^'`_*?<>"))
			.plusLazy(Token.NEWLINE_PARSER.or(CharacterParser.of(';')))
			.flatten()
		);
		def("ch_name", 
			StringParser.of("CH$NAME")
			.seq(ref("tagsep"))
			.seq(ref("ch_name_value"))
			.seq(Token.NEWLINE_PARSER)
			.plus()
			.map((List<?> value) -> {
				//System.out.println(value);
				List<String> ch_name = new ArrayList<String>();
				for (Object v : value) ch_name.add(((List<String>) v).get(2));
				callback.CH_NAME(ch_name);
				return value;						
			})
		);

		
		// 2.2.2 CH$COMPOUND_CLASS
		// Category of Chemical Compound. Mandatory
		// Example
		// CH$COMPOUND_CLASS: Natural Product; Carotenoid; Terpenoid; Lipid
		// Either Natural Product or Non-Natural Product should be precedes the other class names .
		def("ch_compound_class",
			StringParser.of("CH$COMPOUND_CLASS")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.CH_COMPOUND_CLASS(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value.toString());
//				return value;						
//			})
		);
		
		// 2.2.3 CH$FORMULA
		// Molecular Formula of Chemical Compound. Mandatory
		// Example
		// CH$FORMULA: C9H10ClNO3
		// It follows the Hill's System.
		// No prosthetic molecule is included (see 2.2.1 CH$NAME).
		// Molecular formulae of derivatives by chemical modification with TMS, etc. should be given in the MS$FOCUSED_ION: DERIVATIVE_FORM (2.5.1) field.
		def("ch_formula",
			StringParser.of("CH$FORMULA")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					IMolecularFormula m = MolecularFormulaManipulator.getMolecularFormula((String) value, DefaultChemObjectBuilder.getInstance());
					callback.CH_FORMULA(m);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value.toString());
//				return value;						
//			})
		);
		
		
		// 2.2.4 CH$EXACT_MASS
		// Monoisotopic Mass of Chemical Compound. Mandatory
		// Example
		// CH$EXACT_MASS: 430.38108
		// A value with 5 digits after the decimal point is recommended.
		def("ch_exact_mass",
			StringParser.of("CH$EXACT_MASS")
			.seq(ref("tagsep"))
			.seq(
				digit().plus()
				.seq(
		        	CharacterParser.of('.')
		        	.seq(digit().plus()).optional()
	        	)
		        .flatten()
		        .map((String value) -> {
	        		Double d = Double.parseDouble(value);
	        		callback.CH_EXACT_MASS(d);
	        		return value;
	        	})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value.toString());
//				return value;						
//			})
		);
		
		// 2.2.5 CH$SMILES *
		// SMILES String. Mandatory
		// Example
		// CH$SMILES: NCC(O)=O
		// Isomeric SMILES but not a canonical one.
		def("ch_smiles",
			StringParser.of("CH$SMILES")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				// call a Continuation Parser to validate content of SMILES string
				.callCC((Function<Context, Result> continuation, Context context) -> {
					Result r = continuation.apply(context);
					if (r.isSuccess()) {
						try {
							if (r.get().equals("N/A")) callback.CH_SMILES(new AtomContainer());
							else callback.CH_SMILES(new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(r.get()));
						} catch (InvalidSmilesException e) { 
							return r=context.failure("Can not parse SMILES string in \"CH$SMILES\" field.\nError: "+ e.getMessage());		 				
						}		 			
					}
					return r; 
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value.toString());
//				return value;						
//			})
		);

		// 2.2.6 CH$IUPAC *
		// IUPAC International Chemical Identifier (InChI Code). Mandatory
		// Example
		// CH$IUPAC: InChI=1S/C2H5NO2/c3-1-2(4)5/h1,3H2,(H,4,5)
		// Not IUPAC name.
		def("ch_iupac",
			StringParser.of("CH$IUPAC")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				// call a Continuation Parser to validate content of InChi string
				.callCC((Function<Context, Result> continuation, Context context) -> {
					Result r = continuation.apply(context);
					if (r.isSuccess()) {
						try {
							if (r.get().equals("N/A")) callback.CH_IUPAC(new AtomContainer());
							else {
								// Get InChIToStructure
								InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(r.get(), DefaultChemObjectBuilder.getInstance());
								INCHI_RET ret = intostruct.getReturnStatus();
								if (ret == INCHI_RET.WARNING) {
									// Structure generated, but with warning message
									System.out.println("InChI warning: " + intostruct.getMessage());
								} 
								else if (ret != INCHI_RET.OKAY) {
									// Structure generation failed
									return context.failure("Can not parse INCHI string in \"CH$IUPAC\" field. Structure generation failed: " + ret.toString() + " [" + intostruct.getMessage() + "]");
								}
								IAtomContainer m = intostruct.getAtomContainer();
								callback.CH_IUPAC(m);
							}
						} catch (CDKException e) { 
							return context.failure("\"Can not parse INCHI string in \"CH$IUPAC\" field.");		 				
						}		 			
					}
					return r; 
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value.toString());
//				return value;						
//			})
		);

		// TODO no record implements CH$CDK_DEPICT
		// 2.2.7 CH$CDK_DEPICT
		
		// 2.2.8 CH$LINK: subtag  identifier
		// Identifier and Link of Chemical Compound to External Databases.
		// Optional and Iterative
		// Example
		// CH$LINK: CAS 56-40-6
		// CH$LINK: COMPTOX DTXSID50274017
		// CH$LINK: INCHIKEY UFFBMTHBGFGIHF-UHFFFAOYSA-N
		// CH$LINK: KEGG C00037
		// CH$LINK: PUBCHEM SID: 11916 CID:182232
		// CH$LINK fields should be arranged by the alphabetical order of database names.
		def("ch_link",
			StringParser.of("CH$LINK")
			.seq(ref("tagsep"))
			.seq(StringParser.of("CAS").trim())
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER).optional()
			.seq(StringParser.of("CH$LINK")
					.seq(ref("tagsep"))
					.seq(StringParser.of("CAYMAN").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("CHEBI").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("CHEMPDB").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("CHEMSPIDER").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("COMPTOX").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("HMDB").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
					.seq(ref("tagsep"))
					.seq(StringParser.of("INCHIKEY").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
					.seq(ref("tagsep"))
					.seq(StringParser.of("KAPPAVIEW").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("KEGG").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("KNAPSACK").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("LIPIDBANK").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("LIPIDMAPS").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
					.seq(ref("tagsep"))
					.seq(StringParser.of("NIKKAJI").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("CH$LINK")
				.seq(ref("tagsep"))
				.seq(StringParser.of("PUBCHEM").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)				
			.map((List<?> value) -> {
				// System.out.println(value);
				List<String> link = new ArrayList<String>();
				for (Object v : value) {
					if (v == null) continue;
					link.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
				}
				callback.CH_LINK(link);
				return value;
			})
		);

		// 2.3.1 SP$SCIENTIFIC_NAME
		// Scientific Name of Biological Species, from Which Sample was Prepared.  Optional
		// Example
		// SP$SCIENTIFIC_NAME: Mus musculus
		def("sp_scientific_name",
			StringParser.of("SP$SCIENTIFIC_NAME")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
		        .map((String value) -> {
		       		callback.SP_SCIENTIFIC_NAME(value);
		       		return value;
		       	})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.3.2 SP$LINEAGE
		// Evolutionary lineage of the species, from which the sample was prepared. Optional
		// Example: SP$LINEAGE: cellular organisms; Eukaryota; Fungi/Metazoa group; Metazoa; Eumetazoa; Bilateria; Coelomata; Deuterostomia; Chordata; Craniata; Vertebrata; Gnathostomata; Teleostomi; Euteleostomi; Sarcopterygii; Tetrapoda; Amniota; Mammalia; Theria; Eutheria; Euarchontoglires; Glires; Rodentia; Sciurognathi; Muroidea; Muridae; Murinae; Mus
		def("sp_lineage",
			StringParser.of("SP$LINEAGE")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
		        .map((String value) -> {
		       		callback.SP_LINEAGE(value);
		       		return value;
		       	})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);

		// 2.3.3 SP$LINK subtag identifier
		// Identifier of Biological Species in External Databases.  Optional and iterative
		// Example
		// SP$LINK: NCBI-TAXONOMY 10090
		def("sp_link",
			StringParser.of("SP$LINK")
			.seq(ref("tagsep"))
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER)
			.plus()
			.map((List<?> value) -> {
				//System.out.println(value);
				List<String> links = new ArrayList<String>();
				for (Object v : value) links.add(((List<String>) v).get(2));
				callback.SP_LINK(links);
				return value;
			})
		);
		
		// 2.3.4 SP$SAMPLE
		// Tissue or Cell, from which Sample was Prepared. Optional and iterative
		// Example
		// SP$SAMPLE: Liver extracts
		def("sp_sample",
			StringParser.of("SP$SAMPLE")
			.seq(ref("tagsep"))
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER)
			.plus()
			.map((List<?> value) -> {
				//System.out.println(value);
				List<String> sample = new ArrayList<String>();
				for (Object v : value) sample.add(((List<String>) v).get(2));
				callback.SP_SAMPLE(sample);
				return value;
			})
		);		

		
		// 2.4.1 AC$INSTRUMENT
		// Commercial Name and Model of Chromatographic Separation Instrument,
		// if any were coupled, and Mass Spectrometer and Manufacturer. Mandatory
		// Example: AC$INSTRUMENT: LC-10ADVPmicro HPLC, Shimadzu; LTQ Orbitrap, Thermo Electron.
		// Cross-reference to mzOntology: Instrument model [MS:1000031] All the instruments
		// are given together in a single line. This record is not iterative.
		def("ac_instrument",
			StringParser.of("AC$INSTRUMENT")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.AC_INSTRUMENT(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.4.2 AC$INSTRUMENT_TYPE
		// General Type of Instrument. Mandatory
		// Example
		// AC$INSTRUMENT_TYPE: LC-ESI-QTOF
		// Format is:
		// (Separation tool type-)Ionization method-Ion analyzer type(Ion analyzer type).
		// Separation tool types are CE, GC, LC.
		// Ionization methods are APCI, APPI, EI, ESI, FAB, MALDI.
		// Ion analyzer types are B, E, FT, IT, Q, TOF.
		// In tandem mass analyzers, no “–“ is inserted between ion analyzers.
		// FT includes FTICR and other type analyzers using FT, such as Orbitrap(R).
		// IT comprises quadrupole ion trap analyzers such as 3D ion trap and linear ion trap.
		// Other examples of AC$INSTRUMENT_TYPE data are as follows.
		// ESI-QQ
		// ESI-QTOF
		// GC-EI-EB
		// LC-ESI-ITFT
		// Cross-reference to mzOntology: Ionization methods [MS:1000008]; APCI [MS:1000070]; APPI [MS:1000382]; EI [MS:1000389]; ESI [MS:1000073]; B [MS:1000080]; IT [MS:1000264], Q [MS:1000081], TOF [MS:1000084].
		def("ac_instrument_type_sep",
			StringParser.of("CE")
			.or(StringParser.of("GC"))
			.or(StringParser.of("LC"))
			.seq(CharacterParser.of('-'))
			.pick(0)
		);
		def("ac_instrument_type_ionisation",
			StringParser.of("APCI")
			.or(StringParser.of("APPI"))
			.or(StringParser.of("EI"))
			.or(StringParser.of("ESI"))
			.or(StringParser.of("FAB"))
			.or(StringParser.of("MALDI"))
			.or(StringParser.of("FD"))
			.or(StringParser.of("CI"))
			.or(StringParser.of("FI"))
			.seq(CharacterParser.of('-'))
			.pick(0)
		);
		def("ac_instrument_type_analyzer",
			StringParser.of("B")
			.or(StringParser.of("E"))
			.or(StringParser.of("FT"))
			.or(StringParser.of("IT"))
			.or(StringParser.of("Q"))
			.or(StringParser.of("TOF"))
		);
		def("ac_instrument_type_value", 
			ref("ac_instrument_type_sep")
			.optional()
			.seq(ref("ac_instrument_type_ionisation"))
			.seq(ref("ac_instrument_type_analyzer").plus())
		);
		def("ac_instrument_type", 
			StringParser.of("AC$INSTRUMENT_TYPE")
			.seq(ref("tagsep"))
			.seq(
				ref("ac_instrument_type_value")
				.map((List<?> value) -> {
					callback.AC_INSTRUMENT_TYPE((List<?>) value);
					return value;						
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);

		// 2.4.3 AC$MASS_SPECTROMETRY: MS_TYPE
		// Data Type.   Mandatory
		// Example
		// AC$MASS_SPECTROMETRY: MS_TYPE MS2
		// Either of MS, MS2, MS3, MS4, , , .
		// Brief definition of terms used in MS_TYPE
		// MS2  is 1st generation product ion spectrum(of MS)
		// MS3  is 2nd generation product ion spectrum(of MS)
		// MS2  is the precursor ion spectrum of MS3
		// IUPAC Recommendations 2006 (http://old.iupac.org/reports/provisional/abstract06/murray_prs.pdf)
		def("ac_mass_spectrometry_ms_type_value",
			StringParser.of("MS4")
			.or(StringParser.of("MS3"))
			.or(StringParser.of("MS2"))
			.or(StringParser.of("MS"))
		);
		def("ac_mass_spectrometry_ms_type", 
			StringParser.of("AC$MASS_SPECTROMETRY")
			.seq(ref("tagsep"))
			.seq(StringParser.of("MS_TYPE "))
			.seq(
				ref("ac_mass_spectrometry_ms_type_value")
				.map((String value) -> {
					callback.AC_MASS_SPECTROMETRY_MS_TYPE(value);
					return value;						
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.4.4 AC$MASS_SPECTROMETRY: ION_MODE
		// Polarity of Ion Detection. Mandatory
		// Example: AC$MASS_SPECTROMETRY: ION_MODE POSITIVE
		// Either of POSITIVE or NEGATIVE is allowed. Cross-reference to mzOntology: POSITIVE [MS:1000030] 
		// or NEGATIVE [MS:1000129]; Ion mode [MS:1000465]
		def("ac_mass_spectrometry_ion_mode_value",
			StringParser.of("POSITIVE")
			.or(StringParser.of("NEGATIVE"))
		);
		def("ac_mass_spectrometry_ion_mode", 
			StringParser.of("AC$MASS_SPECTROMETRY")
			.seq(ref("tagsep"))
			.seq(StringParser.of("ION_MODE "))
			.seq(
				ref("ac_mass_spectrometry_ion_mode_value")
				.map((String value) -> {
					callback.AC_MASS_SPECTROMETRY_ION_MODE(value);
					return value;						
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//			System.out.println(value);
//			return value;						
//			})
		);		
		
		// 2.4.5 AC$MASS_SPECTROMETRY: subtag Description
		// Other Experimental Methods and Conditions of Mass Spectrometry. Optional
		// Description is a list of numerical values with/without unit or a sentence. 
		// AC$MASS_SPECTROMETRY fields should be arranged by the alphabetical order of subtag names.
		// 2.4.5 Subtag: COLLISION_ENERGY
		// Collision Energy for Dissociation.
		// Example 1: AC$MASS_SPECTROMETRY: COLLISION_ENERGY 20 kV 
		// Example 2: AC$MASS_SPECTROMETRY: COLLISION_ENERGY Ramp 10-50 kV
		// 2.4.5 Subtag: COLLISION_GAS
		// Name of Collision Gas.
		// Example: AC$MASS_SPECTROMETRY: COLLISION_GAS N2
		// Cross-reference to mzOntology: Collision gas [MS:1000419]
		// 2.4.5 Subtag: DATE
		// Date of Analysis.
		// 2.4.5 Subtag: DESOLVATION_GAS_FLOW
		// Flow Rate of Desolvation Gas.
		// Example: AC$MASS_SPECTROMETRY: DESOLVATION_GAS_FLOW 600.0 l/h
		// 2.4.5 Subtag: DESOLVATION_TEMPERATURE
		// Temperature of Desolvation Gas.
		// Example: AC$MASS_SPECTROMETRY: DESOLVATION_TEMPERATURE 400 C
		// 2.4.5 Subtag: IONIZATION_ENERGY
		// Energy of Ionization.
		// Example: AC$MASS_SPECTROMETRY: IONIZATION_ENERGY 70 eV
		// 2.4.5 Subtag: LASER
		// Desorption /Ionization Conditions in MALDI.
		// Example: AC$MASS_SPECTROMETRY: LASER 337 nm nitrogen laser, 20 Hz, 10 nsec
		// 2.4.5 Subtag: MATRIX
		// Matrix Used in MALDI and FAB.
		// Example: AC$MASS_SPECTROMETRY: MATRIX 1-2 uL m-NBA
		// 2.4.5. Subtag : MASS_ACCURACY
		// Relative Mass Accuracy.
		// Example: AC$MASS_SPECTROMETRY: MASS_ACCURACY 50 ppm over a range of about m/z 100-1000
		// 2.4.5 Subtag: REAGENT_GAS
		// Name of Reagent Gas.
		// Example: AC$MASS_SPECTROMETRY: REAGENT_GAS ammonia
		// 2.4.5 Subtag: SCANNING
		// Scan Cycle and Range.
		// Example: AC$MASS_SPECTROMETRY: SCANNING 0.2 sec/scan (m/z 50-500)
		def("ac_mass_spectrometry", 
			StringParser.of("AC$MASS_SPECTROMETRY")
			.seq(ref("tagsep"))
			.seq(StringParser.of("COLLISION_ENERGY").trim())
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER).optional()
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("COLLISION_GAS").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("DATE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("DESOLVATION_GAS_FLOW").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("DESOLVATION_TEMPERATURE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("FRAGMENTATION_MODE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
					.seq(ref("tagsep"))
					.seq(StringParser.of("ION_SPRAY_VOLTAGE").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("IONIZATION").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("IONIZATION_ENERGY").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("LASER").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("MATRIX").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("MASS_ACCURACY").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("REAGENT_GAS").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("RESOLUTION").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$MASS_SPECTROMETRY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("SCANNING").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.map((List<?> value) -> {
				// System.out.println(value);
				List<String> subtag = new ArrayList<String>();
				for (Object v : value) {
					if (v == null) continue;
					subtag.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
				}
				callback.AC_MASS_SPECTROMETRY(subtag);
				return value;
			})
		);	
			
		// 2.4.6 AC$CHROMATOGRAPHY: subtag Description
		// Experimental Method and Conditions of Chromatographic Separation. Optional
		// AC$CHROMATOGRAPHY fields should be arranged by the alphabetical order of subtag names.
		// 2.4.6 Subtag: CAPILLARY_VOLTAGE
		// Voltage Applied to Capillary Electrophoresis or Voltage Applied to the Interface of LC-MS.
		// Example: AC$CHROMATOGRAPHY: CAPILLARY_VOLTAGE 4 kV
		// 2.4.6 Subtag: COLUMN_NAME
		// Commercial Name of Chromatography Column and Manufacture.
		// Example of LC: AC$CHROMATOGRAPHY: COLUMN_NAME Acquity UPLC BEH C18 2.1 by 50 mm (Waters, Milford, MA, USA) Example of CE: AC$CHROMATOGRAPHY: COLUMN_NAME Fused silica capillary id=50 um L=100 cm (HMT, Tsuruoka, Japan)
		// 2.4.6 Subtag: COLUMN_TEMPERATURE
		// Column Temperature.
		// Example: AC$CHROMATOGRAPHY: COLUMN_TEMPERATURE 40 C
		// 2.4.6 Subtag: FLOW_GRADIENT
		// Gradient of Elusion Solutions.
		// Example: AC$CHROMATOGRAPHY: FLOW_GRADIENT 0/100 at 0 min, 15/85 at 5 min, 21/79 at 20 min, 90/10 at 24 min, 95/5 at 26 min, 0/100, 30 min
		// 2.4.6 Subtag: FLOW_RATE
		// Flow Rate of Migration Phase.
		// Example: AC$CHROMATOGRAPHY: FLOW_RATE 0.25 ml/min
		// 2.4.6 Subtag: RETENTION_TIME
		// Retention Time on Chromatography.
		// Example: AC$CHROMATOGRAPHY: RETENTION_TIME 40.3 min
		// Cross-reference to mzOntology: Retention time [MS:1000016]
		// 2.4.6 Subtag: SOLVENT
		// Chemical Composition of Buffer Solution. Iterative
		// Example:
		// AC$CHROMATOGRAPHY: SOLVENT A acetonitrile-methanol-water (19:19:2) with 0.1% acetic acid
		// AC$CHROMATOGRAPHY: SOLVENT B 2-propanol with 0.1% acetic acid and 0.1% ammonium hydroxide (28%)
		// 2.4.6 Subtag: NAPS_RTI
		// N-alkylpyrinium-3-sulfonate based retention time index.
		// Reference: http://nparc.cisti-icist.nrc-cnrc.gc.ca/eng/view/object/?id=b4db3589-ae0b-497e-af03-264785d7922f
		// Example: AC$CHROMATOGRAPHY: NAPS_RTI 100	
		def("ac_chromatography", 
			StringParser.of("AC$CHROMATOGRAPHY")
			.seq(ref("tagsep"))
			.seq(StringParser.of("CAPILLARY_VOLTAGE").trim())
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER).optional()
			
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("COLUMN_NAME").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("COLUMN_PRESSURE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("COLUMN_TEMPERATURE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("FLOW_GRADIENT").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("FLOW_RATE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("RETENTION_TIME").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("SOLVENT").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).plus().optional()
			)
			.seq(StringParser.of("AC$CHROMATOGRAPHY")
				.seq(ref("tagsep"))
				.seq(StringParser.of("NAPS_RTI").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.map((List<?> value) -> {
				//System.out.println(value);
				List<String> subtag = new ArrayList<String>();
				// the first 6 lines go directly to subtag if existing
				for (Object v : value.subList(0,6)) {
					if (v == null) continue;
					subtag.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
				}
				// 7th element of parse tree might be AC$CHROMATOGRAPHY: SOLVENT with multiple lines
				// need to be unnested
				if (value.get(7) != null) {
					for (Object v : (List<?>) value.get(7)) {
						subtag.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
					}
				}
				for (Object v : value.subList(8,value.size())) {
					if (v == null) continue;
					subtag.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
				}
				callback.AC_MASS_SPECTROMETRY(subtag);
				return value;
			})			
		);
		
		// 2.5.1 MS$FOCUSED_ION: subtag Description
		// Information of Precursor or Molecular Ion. Optional
		// MS$FOCUSED_ION fields should be arranged by the alphabetical order of subtag names.
		// 2.5.1 Subtag: BASE_PEAK
		// m/z of Base Peak.
		// Example: MS$FOCUSED_ION: BASE_PEAK 73
		// 2.5.1 Subtag: DERIVATIVE_FORM
		// Molecular Formula of Derivative for GC-MS.
		// Example
		// MS$FOCUSED_ION: DERIVATIVE_FORM C19H42O5Si4
		// MS$FOCUSED_ION: DERIVATIVE_FORM C{9+3*n}H{16+8*n}NO5Si{n}
		// 2.5.1 Subtag: DERIVATIVE_MASS
		// Exact Mass of Derivative for GC-MS.
		// Example: MS$FOCUSED_ION: DERIVATIVE_MASS 462.21093
		// 2.5.1 Subtag: DERIVATIVE_TYPE
		// Type of Derivative for GC-MS.
		// Example: MS$FOCUSED_ION: DERIVATIVE_TYPE 4 TMS
		// 2.5.1 Subtag: ION_TYPE
		// Type of Focused Ion.
		// Example: MS$FOCUSED_ION: ION_TYPE [M+H]+
		// Types currently used in MassBank are [M]+, [M]+*, [M+H]+, [2M+H]+, [M+Na]+, [M-H+Na]+, [2M+Na]+, [M+2Na-H]+, [(M+NH3)+H]+, [M+H-H2O]+, [M+H-C6H10O4]+, [M+H-C6H10O5]+, [M]-, [M-H]-, [M-2H]-, [M-2H+H2O]-, [M-H+OH]-, [2M-H]-, [M+HCOO-]-, [(M+CH3COOH)-H]-, [2M-H-CO2]- and [2M-H-C6H10O5]-.
		// 2.5.1 Subtag: PRECURSOR_M/Z
		// m/z of Precursor Ion in MSn spectrum.
		// Example: MS$FOCUSED_ION: PRECURSOR_M/Z 289.07123
		// Calculated exact mass is preferred to the measured accurate mass of the precursor ion. Cross-reference to mzOntology: precursor m/z [MS:1000504]
		// 2.5.1 Subtag: PRECURSOR_TYPE
		// Type of Precursor Ion in MSn.
		// Example: MS$FOCUSED_ION: PRECURSOR_TYPE [M-H]-
		// Types currently used in MassBank are [M]+, [M]+*, [M+H]+, [2M+H]+, [M+Na]+,
		// [M-H+Na]+, [2M+Na]+, [M+2Na-H]+, [(M+NH3)+H]+, [M+H-H2O]+, [M+H-C6H10O4]+,
		// [M+H-C6H10O5]+, [M]-, [M-H]-, [M-2H]-, [M-2H+H2O]-, [M-H+OH]-, [2M-H]-, [M+HCOO-]-,
		// [(M+CH3COOH)-H]-, [2M-H-CO2]- and [2M-H-C6H10O5]-. Cross-reference to mzOntology: Precursor type [MS: 1000792]
		def("precursor_type",
			StringParser.of("[M]+*")
			.or(StringParser.of("[M]+"))
			.or(StringParser.of("[M+H]+"))
			.or(StringParser.of("[2M+H]+"))
			.or(StringParser.of("[M+Na]+"))
			.or(StringParser.of("[M-H+Na]+"))
			.or(StringParser.of("[2M+Na]+"))
			.or(StringParser.of("[M+2Na-H]+"))
			.or(StringParser.of("[(M+NH3)+H]+"))
			.or(StringParser.of("[M+H-H2O]+"))
			.or(StringParser.of("[M+H-C6H10O4]+"))
			.or(StringParser.of("[M+H-C6H10O5]+"))
			.or(StringParser.of("[M]-"))
			.or(StringParser.of("[M-H]-"))
			.or(StringParser.of("[M-2H]-"))
			.or(StringParser.of("[M-2H+H2O]-"))
			.or(StringParser.of("[M-H+OH]-"))
			.or(StringParser.of("[2M-H]-"))
			.or(StringParser.of("[M+HCOO-]-"))
			.or(StringParser.of("[(M+CH3COOH)-H]-"))
			.or(StringParser.of("[2M-H-CO2]-"))
			.or(StringParser.of("[2M-H-C6H10O5]-"))
		);
		
		def("ms_focused_ion", 
			StringParser.of("MS$FOCUSED_ION")
			.seq(ref("tagsep"))
			.seq(StringParser.of("BASE_PEAK").trim())
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER).optional()
			.seq(StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("DERIVATIVE_FORM").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("DERIVATIVE_MASS").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("DERIVATIVE_TYPE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("ION_TYPE "))
				.seq(ref("precursor_type"))
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("PRECURSOR_M/Z").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("PRECURSOR_TYPE").trim())
				.seq(ref("precursor_type"))
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.map((List<?> value) -> {
				// System.out.println(value);
				List<String> subtag = new ArrayList<String>();
				for (Object v : value) {
					if (v == null) continue;
					subtag.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
				}
				callback.MS_FOCUSED_ION(subtag);
				return value;
			})
		);
		
		// 2.5.3 MS$DATA_PROCESSING: subtag
		// Data Processing Method of Peak Detection. Optional
		// MS$DATA_PROCESSING fields should be arranged by the alphabetical order of subtag names. Cross-reference to mzOntology: Data processing [MS:1000543]
		// 2.5.3 Subtag: FIND_PEAK
		// Peak Detection.
		// Example: MS$DATA_PROCESSING: FIND_PEAK convexity search; threshold = 9.1
		// 2.5.3 Subtag: WHOLE
		// Whole Process in Single Method / Software.
		// Example: MS$DATA_PROCESSING: WHOLE Analyst 1.4.2
		def("ms_data_processing", 
			StringParser.of("MS$DATA_PROCESSING")
			.seq(ref("tagsep"))
			.seq(StringParser.of("FIND_PEAK").trim())
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER).optional()
			.seq(StringParser.of("MS$DATA_PROCESSING")
					.seq(ref("tagsep"))
					.seq(StringParser.of("IGNORE").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$DATA_PROCESSING")
					.seq(ref("tagsep"))
					.seq(StringParser.of("REANALYZE").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$DATA_PROCESSING")
					.seq(ref("tagsep"))
					.seq(StringParser.of("RECALIBRATE").trim())
					.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
					.seq(Token.NEWLINE_PARSER).optional()
			)
			.seq(StringParser.of("MS$DATA_PROCESSING")
				.seq(ref("tagsep"))
				.seq(StringParser.of("WHOLE").trim())
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.seq(Token.NEWLINE_PARSER).optional()
			)
			.map((List<?> value) -> {
				// System.out.println(value);
				List<String> subtag = new ArrayList<String>();
				for (Object v : value) {
					if (v == null) continue;
					subtag.add(((List<String>) v).get(2) + " " + ((List<String>) v).get(3));
				}
				callback.MS_DATA_PROCESSING(subtag);
				return value;
			})
		);
		
		// 2.6.1 PK$SPLASH
		// Hashed Identifier of Mass Spectra. Mandatory and Single Line Information
		// Example: PK$SPLASH: splash10-z200000000-87bb3c76b8e5f33dd07f
		def("pk_splash",
			StringParser.of("PK$SPLASH")
			.seq(ref("tagsep"))
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.PK_SPLASH(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value.toString());
//				return value;						
//			})
		);
		
		
	}

}
