package massbank;

import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.letter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
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

import edu.ucdavis.fiehnlab.spectra.hash.core.Spectrum;
import edu.ucdavis.fiehnlab.spectra.hash.core.Splash;
import edu.ucdavis.fiehnlab.spectra.hash.core.SplashFactory;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.Ion;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectrumImpl;
import net.sf.jniinchi.INCHI_RET;

public class RecordParserDefinition extends GrammarDefinition {
	private static final Logger logger = LogManager.getLogger(RecordParserDefinition.class);

	public RecordParserDefinition(Record callback, boolean strict) {
		def("start",
			ref("accession")
			.seq(ref("deprecated_record")
				.or(
					ref("record_title")
					.seq(ref("date"))
					.seq(ref("authors"))
					.seq(ref("license"))
					.seq(ref("copyright").optional())
					.seq(ref("project").optional())
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
					.seq(ref("pk_annotation").optional())
					.seq(ref("pk_num_peak"))
					.seq(ref("pk_peak"))
					.seq(ref("endtag"))
//					.map((List<?> value) -> {
//						System.out.println(value);
//						return value;						
//					})
					// check semantic here
					.callCC((Function<Context, Result> continuation, Context context) -> {
						return checkSemantic(continuation, context, callback, strict);
					})
				)
			)
			.end()
		);
		
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
		def("endtag", StringParser.of("//").seq(Token.NEWLINE_PARSER));
		def("multiline_start", StringParser.of("  "));
		
		def("uint_primitive", digit().plus().flatten());

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
			.seq(
				letter().times(2)
				.seq(
					digit().times(6)
				)
				.or(
					letter().times(3)
					.seq(
						digit().times(5)
					)
				)
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
		
		def("deprecated_record",
			StringParser.of("DEPRECATED")
			.seq(ref("tagsep"))
			.map((List<?> value) -> {
				callback.DEPRECATED(true);
				return value;						
			})
			.seq(CharacterParser.any().star()
				.flatten()
				.map((String value) -> {
					callback.DEPRECATED_CONTENT(value);
					return value;
				})
			)
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
			).pick(2)
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
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.pick(1)
				.optional()
			)
			.map((List<String> value) -> {
				if (value.get(value.size()-1) == null) value.remove(value.size()-1);
				callback.RECORD_TITLE(value);
				return value;
			})
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;
//			})
		);
		
		// 2.1.3 DATE
		// Date of the Creation or the Last Modification of MassBank Record. Mandatory
		// Example
		// DATE: 2016.01.15
		// DATE: 2011.02.21 (Created 2007.07.07)
		// DATE: 2016.01.19 (Created 2006.12.21, modified 2011.05.06)
		def("date_value",
			CharacterParser.digit().times(4)
			.seq(CharacterParser.of('.'))
			.seq(CharacterParser.digit().times(2))
			.seq(CharacterParser.of('.'))
			.seq(CharacterParser.digit().times(2))
			.flatten()
			.callCC((Function<Context, Result> continuation, Context context) -> {
				Result r = continuation.apply(context);
				if (r.isSuccess()) {
					try {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
						LocalDate.parse(r.get(), formatter);
					} catch (Exception e) { 
						return context.failure("Can not parse date:\n" + e.getMessage());		 				
					}
				}
				return r;
			})
		);
		def("date",
			StringParser.of("DATE")
   			.seq(ref("tagsep"))
   			.seq(
   				ref("date_value")
   				.seq(
   					StringParser.of(" (Created ")
   					.seq(ref("date_value"))
   					.seq(StringParser.of(", modified "))
   					.seq(ref("date_value"))
   					.seq(CharacterParser.of(')'))
   					.or(
   						StringParser.of(" (Created ")
   						.seq(ref("date_value"))
   						.seq(CharacterParser.of(')'))
   					).optional()
   				).flatten()
   				.map((String value) -> {
   					callback.DATE(value);
   					return value;						
   				})
  			)
    		.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
		
		// 2.1.4 AUTHORS
		// Authors and Affiliations of MassBank Record. Mandatory
		// Example
		// AUTHORS: Akimoto N, Grad Sch Pharm Sci, Kyoto Univ and Maoka T, Res Inst Prod Dev.
		// Only single-byte characters are allowed.  For example, ö is not allowed.
		def("authors",
			StringParser.of("AUTHORS")
			.seq(ref("tagsep"))
			.seq(Token.NEWLINE_PARSER.not())
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
		def("license",
			StringParser.of("LICENSE")
			.seq(ref("tagsep"))
			.seq(Token.NEWLINE_PARSER.not())
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
			.seq(Token.NEWLINE_PARSER.not())
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
			.seq(Token.NEWLINE_PARSER.not())
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
		
		// 2.1.8 PROJECT
		// A project tag of a project related to the record. Optional Project tags currently used are listed in the “Project Tag” 
		// column of the MassBank List of contributors, prefixes and projects.
		// Example
		// PROJECT: NATOXAQ Natural Toxins and Drinking Water Quality - From Source to Tap
		// PROJECT: SOLUTIONS for present and future emerging pollutants in land and water resources management
		def("project",
			StringParser.of("PROJECT")
			.seq(ref("tagsep"))
			.seq(Token.NEWLINE_PARSER.not())
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				.map((String value) -> {
					callback.PROJECT(value);
					return value;
				})
			)
			.seq(Token.NEWLINE_PARSER)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);

		
		// 2.1.9 COMMENT
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
			.seq(Token.NEWLINE_PARSER.not())
			.seq(CharacterParser.any().repeatLazy(Token.NEWLINE_PARSER, 1, 600).flatten())
			.seq(Token.NEWLINE_PARSER).pick(3)
			.plus()
			.map((List<String> value) -> {
				callback.COMMENT(value);
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
		def("ch_name_value",
			CharacterParser.word().or(CharacterParser.anyOf("-+, ()[]{}/.:$^'`_*?<>#|;"))
			.plusLazy(ref("valuesep").or(Token.NEWLINE_PARSER))
			.flatten()
		);
		def("ch_name", 
			StringParser.of("CH$NAME")
			.seq(ref("tagsep"))
			.seq(
				ref("ch_name_value")
			)
			.seq(Token.NEWLINE_PARSER).pick(2)
			.plus()
			.map((List<String> value) -> {
				//System.out.println(value);
				callback.CH_NAME(value);
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
				CharacterParser.word().or(CharacterParser.anyOf("-+,()[]{}/.:$^'`_*?<> ")).plus().flatten()
				.seq(ref("valuesep"))
				.pick(0).star()
			).pick(2)
			.seq(CharacterParser.word().or(CharacterParser.anyOf("-+,()[]{}/.:$^'`_*?<> ")).plus().flatten())
			.seq(Token.NEWLINE_PARSER)
			.map((List<?> value) -> {
				//System.out.println(value.toString());
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) value.get(0);
				list.add((String) value.get(1));
				callback.CH_COMPOUND_CLASS(list);
				return value;						
			})
		);
		
		
		// 2.2.3 CH$FORMULA
		def("element", StringParser.of("Zr")
			.or(StringParser.of("Zn"))
			.or(StringParser.of("Yb"))
			.or(StringParser.of("Y"))
			.or(StringParser.of("Xe"))
			.or(StringParser.of("W"))
			.or(StringParser.of("V"))
			.or(StringParser.of("U"))
			.or(StringParser.of("Ts"))
			.or(StringParser.of("Tm"))
			.or(StringParser.of("Tl"))
			.or(StringParser.of("Ti"))
			.or(StringParser.of("Th"))
			.or(StringParser.of("Te"))
			.or(StringParser.of("Tc"))
			.or(StringParser.of("Tb"))
			.or(StringParser.of("Ta"))
			.or(StringParser.of("Sr"))
			.or(StringParser.of("Sn"))
			.or(StringParser.of("Sm"))
			.or(StringParser.of("Si"))
			.or(StringParser.of("Sg"))
			.or(StringParser.of("Se"))
			.or(StringParser.of("Sc"))
			.or(StringParser.of("Sb"))
			.or(StringParser.of("S"))
			.or(StringParser.of("Ru"))
			.or(StringParser.of("Rn"))
			.or(StringParser.of("Rh"))
			.or(StringParser.of("Rg"))
			.or(StringParser.of("Rf"))
			.or(StringParser.of("Re"))
			.or(StringParser.of("Rb"))
			.or(StringParser.of("Ra"))
			.or(StringParser.of("Pu"))
			.or(StringParser.of("Pt"))
			.or(StringParser.of("Pr"))
			.or(StringParser.of("Po"))
			.or(StringParser.of("Pm"))
			.or(StringParser.of("Pd"))
			.or(StringParser.of("Pb"))
			.or(StringParser.of("Pa"))
			.or(StringParser.of("P"))
			.or(StringParser.of("Os"))
			.or(StringParser.of("Og"))
			.or(StringParser.of("O"))
			.or(StringParser.of("Np"))
			.or(StringParser.of("No"))
			.or(StringParser.of("Ni"))
			.or(StringParser.of("Nh"))
			.or(StringParser.of("Ne"))
			.or(StringParser.of("Nd"))
			.or(StringParser.of("Nb"))
			.or(StringParser.of("Na"))
			.or(StringParser.of("N"))
			.or(StringParser.of("Mt"))
			.or(StringParser.of("Mo"))
			.or(StringParser.of("Mn"))
			.or(StringParser.of("Mg"))
			.or(StringParser.of("Md"))
			.or(StringParser.of("Mc"))
			.or(StringParser.of("Lv"))
			.or(StringParser.of("Lu"))
			.or(StringParser.of("Lr"))
			.or(StringParser.of("Li"))
			.or(StringParser.of("La"))
			.or(StringParser.of("Kr"))
			.or(StringParser.of("K"))
			.or(StringParser.of("Ir"))
			.or(StringParser.of("In"))
			.or(StringParser.of("I"))
			.or(StringParser.of("Hs"))
			.or(StringParser.of("Ho"))
			.or(StringParser.of("Hg"))
			.or(StringParser.of("Hf"))
			.or(StringParser.of("He"))
			.or(StringParser.of("H"))
			.or(StringParser.of("Ge"))
			.or(StringParser.of("Gd"))
			.or(StringParser.of("Ga"))
			.or(StringParser.of("Fr"))
			.or(StringParser.of("Fm"))
			.or(StringParser.of("Fl"))
			.or(StringParser.of("Fe"))
			.or(StringParser.of("F"))
			.or(StringParser.of("Eu"))
			.or(StringParser.of("Es"))
			.or(StringParser.of("Er"))
			.or(StringParser.of("Dy"))
			.or(StringParser.of("Ds"))
			.or(StringParser.of("Db"))
			.or(StringParser.of("Cu"))
			.or(StringParser.of("Cs"))
			.or(StringParser.of("Cr"))
			.or(StringParser.of("Co"))
			.or(StringParser.of("Cn"))
			.or(StringParser.of("Cm"))
			.or(StringParser.of("Cl"))
			.or(StringParser.of("Cf"))
			.or(StringParser.of("Ce"))
			.or(StringParser.of("Cd"))
			.or(StringParser.of("Ca"))
			.or(StringParser.of("C"))
			.or(StringParser.of("Br"))
			.or(StringParser.of("Bk"))
			.or(StringParser.of("Bi"))
			.or(StringParser.of("Bh"))
			.or(StringParser.of("Be"))
			.or(StringParser.of("Ba"))
			.or(StringParser.of("B"))
			.or(StringParser.of("Au"))
			.or(StringParser.of("At"))
			.or(StringParser.of("As"))
			.or(StringParser.of("Ar"))
			.or(StringParser.of("Am"))
			.or(StringParser.of("Al"))
			.or(StringParser.of("Ag"))
			.or(StringParser.of("Ac"))
		);
		def("element_count", 
			ref("element")
			.seq(ref("uint_primitive").optional())
		);
		def("molecular_formula", 
				ref("element_count").plus().flatten()
		);
		
		def("ch_formula",
			StringParser.of("CH$FORMULA")
			.seq(ref("tagsep"))
			.seq(
				StringParser.of("N/A")
				.or(ref("molecular_formula"))
				.or(
					CharacterParser.of('[')
					.seq(ref("molecular_formula"))
					.seq(CharacterParser.of(']'))
					.seq(
						CharacterParser.anyOf("+-").plus()
						.or(
							ref("uint_primitive")
							.seq(CharacterParser.anyOf("+-"))
						)
					)
				).flatten()
				.map((String value) -> {
					callback.CH_FORMULA(value);
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
		def("number",
			digit().plus()
			.seq(
		       	CharacterParser.of('.')
		       	.seq(digit().plus()).optional()
			)
		    .seq(
		    	CharacterParser.anyOf("eE")
		    	.seq(digit().plus()).optional()
		    ).flatten()	
		);
		
		def("ch_exact_mass",
			StringParser.of("CH$EXACT_MASS")
			.seq(ref("tagsep"))
			.seq(
				ref("number")
				.map((String value) -> {
	        		double d = Double.parseDouble(value);
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
				CharacterParser.any().repeatLazy(Token.NEWLINE_PARSER, 1, 1200)
				.flatten()
				// call a Continuation Parser to validate content of SMILES string
				.callCC((Function<Context, Result> continuation, Context context) -> {
					Result r = continuation.apply(context);
					if (r.isSuccess()) {
						if (!"N/A".equals(r.get())) {
							// validate SMILES
							try {
								new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(r.get());
							} catch (InvalidSmilesException e) { 
								return context.failure("Can not parse SMILES string in \"CH$SMILES\" field.\nError: "+ e.getMessage() + " for " + r.get());		 				
							}
						}
					}
					return r;
				})
				.map((String value) -> {
					callback.CH_SMILES(value);
					return value;						
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
			.seq(Token.NEWLINE_PARSER.not())
			.seq(
				CharacterParser.any().plusLazy(Token.NEWLINE_PARSER)
				.flatten()
				// call a Continuation Parser to validate content of InChi string
				.callCC((Function<Context, Result> continuation, Context context) -> {
					Result r = continuation.apply(context);
					if (r.isSuccess()) {
						if (!"N/A".equals(r.get())) {
							// validate InChI
							try {
								// Get InChIToStructure
								InChIToStructure intostruct = InChIGeneratorFactory.getInstance().getInChIToStructure(r.get(), DefaultChemObjectBuilder.getInstance());
								INCHI_RET ret = intostruct.getReturnStatus();
								if (ret == INCHI_RET.WARNING) {
									// Structure generated, but with warning message
									logger.warn("InChI warning: " + intostruct.getMessage());
									logger.warn(callback.ACCESSION());
								} 
								else if (ret != INCHI_RET.OKAY) {
									// Structure generation failed
									return context.failure("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed: " + ret.toString() + " [" + intostruct.getMessage() + "] for " + r.get() + ".");
								}
							} catch (CDKException e) {
								return context.failure("Can not parse InChI string in \"CH$IUPAC\" field. Error: \""+ e.getMessage() + "\" for \"" + r.get() + "\".");		 				
							}		 			
						}
					}
					return r;
				})
				.map((String value) -> {
					callback.CH_IUPAC(value);
					return value;						
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
		def("ch_link_subtag",
			StringParser.of("CAS ")
			.or(StringParser.of("CAYMAN "))
			.or(StringParser.of("CHEBI "))
			.or(StringParser.of("CHEMBL "))
			.or(StringParser.of("CHEMPDB "))
			.or(StringParser.of("CHEMSPIDER "))
			.or(StringParser.of("COMPTOX "))
			.or(StringParser.of("HMDB "))
			.or(
				StringParser.of("INCHIKEY ")
				// validate INCHIKEY against InChI
				.callCC((Function<Context, Result> continuation, Context context) -> {
					Result r = continuation.apply(context);
					if (r.isSuccess()) {
						// get InCHI
						String InChI = callback.CH_IUPAC();
						if ("N/A".equals(InChI)) {
							logger.error("Found INCHIKEY while \"CH$IUPAC: N/A\" is set.");
							return context.failure("Found INCHIKEY while CH$IUPAC is missing.");
						}
					}
					return r; 
				})
			)
			.or(StringParser.of("KAPPAVIEW "))
			.or(StringParser.of("KEGG "))
			.or(StringParser.of("KNAPSACK "))
			.or(StringParser.of("LIPIDBANK "))
			.or(StringParser.of("LIPIDMAPS "))
			.or(StringParser.of("NIKKAJI "))
			.or(StringParser.of("PUBCHEM "))
			.or(StringParser.of("ZINC "))
		);
		def("ch_link",
			StringParser.of("CH$LINK")
			.seq(ref("tagsep"))
			.seq(ref("ch_link_subtag"))
			.seq(Token.NEWLINE_PARSER.not()).pick(2)
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.map((List<String> value) -> {
				return Pair.of(value.get(0).trim(), value.get(1));
			})
			.seq(Token.NEWLINE_PARSER).pick(0)
			.plus()		
			.map((List<Pair<String,String>> value) -> {
//				System.out.println(value);
				callback.CH_LINK(value);
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
			.seq(Token.NEWLINE_PARSER.not())
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
			.seq(Token.NEWLINE_PARSER.not())
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
			.seq(Token.NEWLINE_PARSER.not())
			.seq(CharacterParser.any().plusLazy(CharacterParser.whitespace()).flatten())
			.seq(CharacterParser.whitespace()).pick(3)
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.map((List<String> value) -> {
				return Pair.of(value.get(0).trim(), value.get(1));
			})
			.seq(Token.NEWLINE_PARSER).pick(0)
			.plus()
			.map((List<Pair<String,String>> value) -> {
				//System.out.println(value);
				callback.SP_LINK(value);
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
			.seq(Token.NEWLINE_PARSER.not())
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.seq(Token.NEWLINE_PARSER).pick(3)
			.plus()
			.map((List<String> value) -> {
				//System.out.println(value);
				callback.SP_SAMPLE(value);
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
			.seq(Token.NEWLINE_PARSER.not())
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
		// Mandatory
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
			.or(StringParser.of("SI"))
			.seq(CharacterParser.of('-'))
			.pick(0)
		);
		def("ac_instrument_type_analyzer",
			StringParser.of("B")
			.or(StringParser.of("E"))
			.or(StringParser.of("FT"))
			.or(StringParser.of("IT"))
			.or(StringParser.of("Q"))
			.or(StringParser.of("TOF")).plus().flatten()
		);
		def("ac_instrument_type_value", 
			ref("ac_instrument_type_sep")
			.optional()
			.seq(ref("ac_instrument_type_ionisation"))
			.seq(ref("ac_instrument_type_analyzer"))
			.map((List<String> value) -> {
				if (value.get(0)==null) value.remove(0);
				return String.join("-", value);
			})
		);
		def("ac_instrument_type", 
			StringParser.of("AC$INSTRUMENT_TYPE")
			.seq(ref("tagsep"))
			.seq(
				ref("ac_instrument_type_value")
				.map((String value) -> {
					callback.AC_INSTRUMENT_TYPE(value);
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
		def("ac_mass_spectrometry_subtag",
			StringParser.of("ACTIVATION_PARAMETER ")
			.or(StringParser.of("ACTIVATION_TIME "))
			.or(StringParser.of("ATOM_GUN_CURRENT "))
			.or(StringParser.of("AUTOMATIC_GAIN_CONTROL "))
			.or(StringParser.of("BOMBARDMENT "))
			.or(StringParser.of("CAPILLARY_TEMPERATURE "))
			.or(StringParser.of("CAPILLARY_VOLTAGE "))
			.or(StringParser.of("CDL_SIDE_OCTOPOLES_BIAS_VOLTAGE "))
			.or(StringParser.of("CDL_TEMPERATURE "))
			.or(StringParser.of("COLLISION_ENERGY "))
			.or(StringParser.of("COLLISION_GAS "))
			.or(StringParser.of("DATAFORMAT "))
			.or(StringParser.of("DATE "))
			.or(StringParser.of("DESOLVATION_GAS_FLOW "))
			.or(StringParser.of("DESOLVATION_TEMPERATURE "))
			.or(StringParser.of("DRY_GAS_FLOW "))
			.or(StringParser.of("DRY_GAS_TEMP "))
			.or(StringParser.of("FRAGMENTATION_METHOD "))
			.or(StringParser.of("FRAGMENTATION_MODE "))
			.or(StringParser.of("FRAGMENT_VOLTAGE "))
			.or(StringParser.of("GAS_PRESSURE "))
			.or(StringParser.of("HELIUM_FLOW "))
			.or(StringParser.of("INTERFACE_VOLTAGE "))
			.or(StringParser.of("IONIZATION "))
			.or(StringParser.of("IONIZATION_ENERGY "))
			.or(StringParser.of("IONIZATION_POTENTIAL "))
			.or(StringParser.of("IONIZATION_VOLTAGE "))
			.or(StringParser.of("ION_GUIDE_PEAK_VOLTAGE "))
			.or(StringParser.of("ION_GUIDE_VOLTAGE "))
			.or(StringParser.of("ION_SOURCE_TEMPERATURE "))
			.or(StringParser.of("ION_SPRAY_VOLTAGE "))
			.or(StringParser.of("ISOLATION_WIDTH "))
			.or(StringParser.of("IT_SIDE_OCTOPOLES_BIAS_VOLTAGE "))
			.or(StringParser.of("LASER "))
			.or(StringParser.of("LENS_VOLTAGE "))
			.or(StringParser.of("MASS_ACCURACY "))
			.or(StringParser.of("MASS_RANGE_M/Z "))
			.or(StringParser.of("MATRIX "))
			.or(StringParser.of("MASS_ACCURACY "))
			.or(StringParser.of("NEBULIZER "))
			.or(StringParser.of("NEBULIZING_GAS "))
			.or(StringParser.of("NEEDLE_VOLTAGE "))
			.or(StringParser.of("OCTPOLE_VOLTAGE "))
			.or(StringParser.of("ORIFICE_TEMP "))
			.or(StringParser.of("ORIFICE_TEMPERATURE "))
			.or(StringParser.of("ORIFICE_VOLTAGE "))
			.or(StringParser.of("PEAK_WIDTH "))
			.or(StringParser.of("PROBE_TIP "))
			.or(StringParser.of("REAGENT_GAS "))
			.or(StringParser.of("RESOLUTION "))
			.or(StringParser.of("RESOLUTION_SETTING "))
			.or(StringParser.of("RING_VOLTAGE "))
			.or(StringParser.of("SAMPLE_DRIPPING "))
			.or(StringParser.of("SCANNING "))
			.or(StringParser.of("SCANNING_CYCLE "))
			.or(StringParser.of("SCANNING_RANGE "))
			.or(StringParser.of("SCAN_RANGE_M/Z "))
			.or(StringParser.of("SKIMMER_VOLTAGE "))
			.or(StringParser.of("SOURCE_TEMPERATURE "))
			.or(StringParser.of("SPRAY_CURRENT "))
			.or(StringParser.of("SPRAY_VOLTAGE "))
			.or(StringParser.of("TUBE_LENS_VOLTAGE "))
		);
		def("ac_mass_spectrometry",
			StringParser.of("AC$MASS_SPECTROMETRY")
			.seq(ref("tagsep"))
			.seq(
				ref("ac_mass_spectrometry_subtag")
				.or(
					CharacterParser.letter().or(CharacterParser.digit()).or(CharacterParser.of('_')).or(CharacterParser.of('/'))
					.plus().flatten()
					.map((String value) -> {
						logger.warn("Usage of free subtag \""+value+"\" in AC$MASS_SPECTROMETRY is not recomended.");
						return value;
					})
					.seq(CharacterParser.whitespace()).flatten()
				)
			)
			.seq(Token.NEWLINE_PARSER.not()).pick(2)
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.map((List<String> value) -> {
				return Pair.of(value.get(0).trim(), value.get(1));
			})
			.seq(Token.NEWLINE_PARSER).pick(0)
			.plus()		
			.map((List<Pair<String,String>> value) -> {
				//System.out.println(value);
				callback.AC_MASS_SPECTROMETRY(value);
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
		def("ac_chromatography_subtag",
			StringParser.of("ANALYTICAL_TIME ")
			.or(StringParser.of("CAPILLARY_VOLTAGE "))
			.or(StringParser.of("COLUMN_NAME "))
			.or(StringParser.of("COLUMN_PRESSURE "))
			.or(StringParser.of("COLUMN_TEMPERATURE "))
			.or(StringParser.of("FLOW_GRADIENT "))
			.or(StringParser.of("FLOW_RATE "))
			.or(StringParser.of("INJECTION_TEMPERATURE "))
			.or(StringParser.of("INTERNAL_STANDARD "))
			.or(StringParser.of("INTERNAL_STANDARD_MT "))
			.or(StringParser.of("NAPS_RTI "))
			.or(StringParser.of("MIGRATION_TIME "))
			.or(StringParser.of("OVEN_TEMPERATURE "))
			.or(StringParser.of("PRECONDITIONING "))
			.or(StringParser.of("RETENTION_INDEX "))
			.or(StringParser.of("RETENTION_TIME "))
			.or(StringParser.of("RUNNING_BUFFER "))
			.or(StringParser.of("RUNNING_VOLTAGE "))
			.or(StringParser.of("SAMPLE_INJECTION "))
			.or(StringParser.of("SAMPLING_CONE "))
			.or(StringParser.of("SHEATH_LIQUID "))
			.or(StringParser.of("SOLVENT "))
			.or(StringParser.of("TIME_PROGRAM "))
			.or(StringParser.of("TRANSFARLINE_TEMPERATURE "))
			.or(StringParser.of("WASHING_BUFFER "))
		);
		def("ac_chromatography",
			StringParser.of("AC$CHROMATOGRAPHY")
			.seq(ref("tagsep"))
			.seq(ref("ac_chromatography_subtag")
				.or(
					CharacterParser.letter().or(CharacterParser.digit()).or(CharacterParser.of('_'))
					.plus().flatten()
					.map((String value) -> {
						logger.warn("Usage of free subtag \""+value+"\" in AC$CHROMATOGRAPHY is not recomended.");
						return value;
					})
					.seq(CharacterParser.whitespace()).flatten()
				)
			)
			.seq(Token.NEWLINE_PARSER.not()).pick(2)
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.map((List<String> value) -> {
				return Pair.of(value.get(0).trim(), value.get(1));
			})
			.seq(Token.NEWLINE_PARSER).pick(0)
			.plus()
			.map((List<Pair<String,String>> value) -> {
				//System.out.println(value);
				callback.AC_CHROMATOGRAPHY(value);
				return value;
			})
		);


		// 2.5.1 MS$FOCUSED_ION: subtag Description
		// 2.5.1 Subtag: BASE_PEAK
		// 2.5.1 Subtag: DERIVATIVE_FORM
		// 2.5.1 Subtag: DERIVATIVE_MASS
		// 2.5.1 Subtag: DERIVATIVE_TYPE
		// 2.5.1 Subtag: ION_TYPE
		// 2.5.1 Subtag: PRECURSOR_M/Z
		
		// 2.5.1 Subtag: PRECURSOR_TYPE
		def("adduct_token", 
				CharacterParser.anyOf("+-")
				.seq(ref("uint_primitive").optional())
				.seq(
					ref("molecular_formula")
					.or(StringParser.of("ACN"))
					.or(StringParser.of("FA"))
				)
//				.map((List<?> value) -> {
//					System.out.println(value);
//					return value;						
//				})
		);
		def("adduct", 
				CharacterParser.of('[')
				.seq(ref("uint_primitive").optional())
				.seq(CharacterParser.of('M'))
				.seq(ref("adduct_token").star())
				.seq(CharacterParser.of(']'))
				.seq(
					CharacterParser.anyOf("+-").plus()
					.or(
						ref("uint_primitive")
						.seq(CharacterParser.anyOf("+-"))
					)
				)
				.seq(CharacterParser.of('*').optional())
//				.map((List<?> value) -> {
//					System.out.println(value);
//					return value;						
//				})
		);
		
		def("precursor_type",
				ref("adduct")
				.seq(
					CharacterParser.of('/')
					.seq(ref("adduct")).star()
				)						
				.flatten()
//				.map((String value) -> {
//					System.out.println(value);
//					return value;						
//				})
		);
		
		def("ion_type",
			StringParser.of("[M]+*")
			.or(StringParser.of("[M]++"))
			.or(StringParser.of("[M]+"))
			.or(StringParser.of("[M+H]+,[M-H2O+H]+"))
			.or(StringParser.of("[M+H]+"))
			.or(StringParser.of("[M+2H]++"))
			.or(StringParser.of("[2M+H]+"))
			.or(StringParser.of("[M+Li]+*"))
			.or(StringParser.of("[M-H+Li]+*"))
			.or(StringParser.of("[M+Na]+*"))
			.or(StringParser.of("[M-H+Na]+*"))
			.or(StringParser.of("[M+Na]+"))
			.or(StringParser.of("[M+K]+"))
			.or(StringParser.of("[M+K]+"))
			.or(StringParser.of("[M-H2O+H]+,[M-2H2O+H]+"))
			.or(StringParser.of("[M-H2O+H]+"))
			.or(StringParser.of("[M+15]+"))
			.or(StringParser.of("[M-H+Na]+"))
			.or(StringParser.of("[2M+Na]+"))
			.or(StringParser.of("[M+2Na-H]+"))
			.or(StringParser.of("[M+NH3+H]+"))
			.or(StringParser.of("[M+NH4]+"))
			.or(StringParser.of("[M+H-H2O]+"))
			.or(StringParser.of("[M-2H2O+H]+,[M-H2O+H]+"))
			.or(StringParser.of("[M-2H2O+H]+"))
			.or(StringParser.of("[M+H-C6H10O4]+"))
			.or(StringParser.of("[M+H-C6H10O5]+"))
			.or(StringParser.of("[M+H-C12H20O9]+"))
			.or(StringParser.of("[M-H]+"))
			.or(StringParser.of("[M+CH3]+"))
			.or(StringParser.of("[M-OH]+"))
			
			.or(StringParser.of("[M-3]+,[M-H2O+H]+"))
			
			.or(StringParser.of("[M]-"))
			.or(StringParser.of("[M-H]-/[M-Ser]-"))
			.or(StringParser.of("[M-H]-"))
			.or(StringParser.of("[M-2H]--"))
			.or(StringParser.of("[M-2H]-"))
			.or(StringParser.of("[M+K-2H]-"))
			.or(StringParser.of("[M-2H+H2O]-"))
			.or(StringParser.of("[M-H+OH]-"))
			.or(StringParser.of("[M-CH3]-"))
			.or(StringParser.of("[2M-H]-"))
			.or(StringParser.of("[M+HCOO]-"))
			.or(StringParser.of("[M-C2H3O]-"))
			.or(StringParser.of("[M-C3H7O2]-"))
			.or(StringParser.of("[M-H-C6H10O5]-"))
			.or(StringParser.of("[M-H-CO2]-"))
			.or(StringParser.of("[M+CH3COOH-H]-"))
			.or(StringParser.of("[M+CH3COO]-/[M-CH3]-"))
			.or(StringParser.of("[M+CH3COO]-"))
			.or(StringParser.of("[2M-H-CO2]-"))
			.or(StringParser.of("[2M-H-C6H10O5]-"))
			.or(StringParser.of("[M-H-CO2-2HF]-"))
		);
		def ("ms_focused_ion_subtag",
			StringParser.of("BASE_PEAK ")
			.or(StringParser.of("DERIVATIVE_FORM "))
			.or(StringParser.of("DERIVATIVE_MASS "))
			.or(StringParser.of("DERIVATIVE_TYPE "))
			.or(StringParser.of("FULL_SCAN_FRAGMENT_ION_PEAK "))
			.or(StringParser.of("PRECURSOR_M/Z "))
			.or(StringParser.of("PRECURSOR_INTENSITY "))
		);
		
		def("ms_focused_ion",
			StringParser.of("MS$FOCUSED_ION")
			.seq(ref("tagsep"))
			.seq(StringParser.of("ION_TYPE ")).pick(2)
			.seq(ref("ion_type"))
			.map((List<String> value) -> {
				return Pair.of(value.get(0).trim(), value.get(1));
			})
			.seq(Token.NEWLINE_PARSER).pick(0)
			.or(
				StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(StringParser.of("PRECURSOR_TYPE ")).pick(2)
				.seq(ref("precursor_type"))
				.map((List<String> value) -> {
					return Pair.of(value.get(0).trim(), value.get(1));
				})
				.seq(Token.NEWLINE_PARSER).pick(0)
			)
			.or(
				StringParser.of("MS$FOCUSED_ION")
				.seq(ref("tagsep"))
				.seq(ref("ms_focused_ion_subtag"))
				.seq(Token.NEWLINE_PARSER.not()).pick(2)
				.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
				.map((List<String> value) -> {
					return Pair.of(value.get(0).trim(), value.get(1));
				})
				.seq(Token.NEWLINE_PARSER).pick(0)
			)
			.plus()
			.map((List<Pair<String,String>> value) -> {
				//System.out.println(value);
				callback.MS_FOCUSED_ION(value);
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
		def ("ms_data_processing_subtag",
			StringParser.of("CHARGE_DECONVOLUTION ")
			.or(StringParser.of("DEPROFILE "))
			.or(StringParser.of("FIND_PEAK "))
			.or(StringParser.of("IGNORE "))
			.or(StringParser.of("INTENSITY CUTOFF "))
			.or(StringParser.of("REANALYZE "))
			.or(StringParser.of("RECALIBRATE "))
			.or(StringParser.of("RELATIVE_M/Z "))
			.or(StringParser.of("REMOVE_PEAK "))
			.or(StringParser.of("WHOLE "))
		);
		def("ms_data_processing", 
			StringParser.of("MS$DATA_PROCESSING")
			.seq(ref("tagsep"))
			.seq(ref("ms_data_processing_subtag"))
			.seq(Token.NEWLINE_PARSER.not()).pick(2)
			.seq(CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten())
			.map((List<String> value) -> {
				return Pair.of(value.get(0).trim(), value.get(1));
			})
			.seq(Token.NEWLINE_PARSER).pick(0)
			.plus()
			
			.map((List<Pair<String,String>> value) -> {
				//System.out.println(value);
				callback.MS_DATA_PROCESSING(value);
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
//				System.out.println(value);
//				return value;						
//			})
		);
		
		
		// 2.6.2 PK$ANNOTATION
		// Chemical Annotation of Peaks with Molecular Formula. Optional and Multiple Line Information
		def("pk_annotation",
			StringParser.of("PK$ANNOTATION")
			.seq(ref("tagsep"))
			.seq(StringParser.of("m/z")
				.flatten()
				.trim(CharacterParser.of(' '))	
			)
			.seq(
				CharacterParser.word().or(CharacterParser.anyOf("-+,()[]{}\\/.:$^'`_*?<>="))
				.plus()
				.flatten()
				.trim(CharacterParser.of(' '))
				.plus()
				.map((List<String> value) -> {
					//System.out.println(value);
					value.add(0,"m/z");
					callback.PK_ANNOTATION_HEADER(value);
					return value;						
				})
				.seq(Token.NEWLINE_PARSER)
			)
			.seq(
				StringParser.of("  ")
				.seq(ref("number").trim())
				.pick(1)
				.seq(
					CharacterParser.word().or(CharacterParser.anyOf("-+,()[]{}\\/.:$^'`_*?<>="))
					.plus()
					.flatten()
					.trim(CharacterParser.of(' '))
					.plus()
				)
				.map((List<?> value) -> {
					//System.out.println(value.toString());
					@SuppressWarnings("unchecked")
					List<String> list = (List<String>) value.subList(0, 1);
					@SuppressWarnings("unchecked")
					List<String> list2 = (List<String>) value. get(1);
					list.addAll(list2);
					callback.PK_ANNOTATION_ADD_LINE(list);
					return value;						
				})
				// call a Continuation Parser to validate the count of PK$ANNOTATION items per line
				.callCC((Function<Context, Result> continuation, Context context) -> {
					Result r = continuation.apply(context);
					if (r.isSuccess()) {
						List<String> pk_annotation_header = callback.PK_ANNOTATION_HEADER();
						List<List<String>> pk_annotation = callback.PK_ANNOTATION();
						if (pk_annotation_header.size() != pk_annotation.get(pk_annotation.size() - 1).size()) {
							StringBuilder sb = new StringBuilder();
							sb.append("Incorrect number of fields per PK$ANNOTATION line. ");
							sb.append(pk_annotation_header.size() + " fields expected, but " + pk_annotation.get(pk_annotation.size() - 1).size() + " fields found.\n");
							sb.append("Defined by:\n");
							sb.append("PK$ANNOTATION:");
							for (String annotation_header_item : callback.PK_ANNOTATION_HEADER())
								sb.append(" " + annotation_header_item);
							System.out.println(sb.toString());
							return context.failure(sb.toString());
						}
					}
					return r; 
				})
				.seq(Token.NEWLINE_PARSER)
				.plus()
			)
		);
		
		def("pk_num_peak",
			StringParser.of("PK$NUM_PEAK")
			.seq(ref("tagsep"))
			.seq(
				digit().plus().flatten()
				.map((String value) -> {
	        		Integer i = Integer.parseUnsignedInt(value);
	        		callback.PK_NUM_PEAK(i);
	        		return value;
	        	})
			)
			.seq(Token.NEWLINE_PARSER)
		);
		
		def("pk_peak",
			StringParser.of("PK$PEAK")
			.seq(ref("tagsep"))
			.seq(StringParser.of("m/z int. rel.int."))
			.seq(Token.NEWLINE_PARSER)
			.seq(
				StringParser.of("  ")
				.seq(
					ref("number")
					.seq(CharacterParser.of(' ')).pick(0)
				)
				.pick(1)
				.seq(
					ref("number")
					.seq(CharacterParser.of(' ')).pick(0)
				)
				.seq(
					ref("number")
				)
				.map((List<String> value) -> {
					//System.out.println(value);
					List<Double> list	= new ArrayList<Double>();
					for(String val : value)
						list.add(Double.parseDouble(val));
					callback.PK_PEAK_ADD_LINE(list);
					return value;
				})
				.seq(Token.NEWLINE_PARSER).plus()
			)
//			.map((List<?> value) -> {
//				System.out.println(value);
//				return value;						
//			})
		);
	}
	
	private Result checkSemantic(Function<Context, Result> continuation, Context context, Record callback, boolean strict) {
		Result r = continuation.apply(context);
		if (r.isSuccess()) {
			String ch_formula=callback.CH_FORMULA();
			String ch_smiles=callback.CH_SMILES();
			String ch_iupac=callback.CH_IUPAC();
			String InChiKey_from_CH_IUPAC = null;
			
			// validate the three sources of formulas against each other
			
			// if any structural information is in smiles or InChI then CH$FORMULA must be defined
			if ("N/A".equals(ch_formula)) {
				if (!("N/A".equals(ch_smiles)) && ("N/A".equals(ch_iupac))) {
					return context.failure("\"CH$FORMULA: N/A\" requires \"CH$SMILES: N/A\" and \"CH$IUPAC: N/A\".");
				}
			}
			else {
				IMolecularFormula m = MolecularFormulaManipulator.getMolecularFormula(ch_formula, DefaultChemObjectBuilder.getInstance());
				String ch_formula_from_cdk = MolecularFormulaManipulator.getString(m);
				if (!(ch_formula.equals(ch_formula_from_cdk))) {
					logger.warn("CH$FORMULA is \"" + ch_formula + "\"");
					logger.warn("but after parsing it is interpreted as \"" + ch_formula_from_cdk + "\". Please check!");
					logger.warn(callback.ACCESSION());
				}
			}
			
			// if InChI is available SMILES is also possible and the other way around
			if (!("N/A".equals(ch_smiles)) && "N/A".equals(ch_iupac)) {
				return context.failure("CH$SMILES is available but CH$IUPAC is empty.");
			}
			if ("N/A".equals(ch_smiles) && !("N/A".equals(ch_iupac))) {
				return context.failure("CH$IUPAC is available but CH$SMILES is empty.");
			}
			
			// check CH$IUPAC
			if (!"N/A".equals(ch_iupac)) {
				try {
					// Get InChIToStructure
					InChIToStructure intoStruct = InChIGeneratorFactory.getInstance().getInChIToStructure(ch_iupac, DefaultChemObjectBuilder.getInstance());
					INCHI_RET ret = intoStruct.getReturnStatus();
					if (ret == INCHI_RET.WARNING) {
						// Structure generated, but with warning message
						logger.warn("InChI warning: " + intoStruct.getMessage());
					} 
					else 
					if (ret != INCHI_RET.OKAY) {
						// Structure generation failed
						return context.failure("Can not parse InChI string in \"CH$IUPAC\" field. Structure generation failed: " + ret.toString() + " [" + intoStruct.getMessage() + "] for " + ch_iupac + ".");
					}
					// Structure generation succeeded
					IAtomContainer m = intoStruct.getAtomContainer();
					// get the molecular formula from the InChI and compare it with CH$FORMULA
					String formula_inchi = MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(m));
					logger.trace("Formula from InChI: \"" + formula_inchi + "\".");
					if (!formula_inchi.equals(callback.CH_FORMULA())) {
						logger.trace("Formula from record file " + callback.CH_FORMULA() + ".");
						return context.failure("Formula generated from InChI string in \"CH$IUPAC\" field does not match formula in \"CH$FORMULA\". "
								+ formula_inchi + "!= \"CH$FORMULA: " + ch_formula + "\"");
					}
					
					// create an InChiKey for comparison with SMILES
					InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(m);
					ret = inchiGen.getReturnStatus();
					if (ret == INCHI_RET.WARNING) {
						// Structure generated, but with warning message
						logger.warn("InChI warning: " + inchiGen.getMessage());
					} 
					else if (ret != INCHI_RET.OKAY) {
						// Structure generation failed
						return context.failure("Can not create InChIKey from InChI string in \"CH$IUPAC\" field. InChI generation failed: " + ret.toString() + " [" + inchiGen.getMessage() + "] for " + ch_iupac + ".");
					}
					// compare the temporary InChI for InChIKey generation with the original InChI
					String tmpInChI=inchiGen.getInchi();
					logger.trace("Temporary InChI: \"" + tmpInChI + "\".");
					if (!tmpInChI.equals(ch_iupac)) {
						logger.trace("InChI from \"CH$IUPAC\" field: \"" + ch_iupac + "\".");
						return context.failure("Temporary InChI for InChIKey generation differs from \"CH$IUPAC\". " + tmpInChI + "!= \"CH$IUPAC: " + ch_iupac + "\"");
					}
					InChiKey_from_CH_IUPAC = inchiGen.getInchiKey();
				} catch (CDKException e) {
					return context.failure("Can not parse InChI string in \"CH$IUPAC\" field. Error: \""+ e.getMessage() + "\" for \"" + ch_iupac + "\".");
				}		 			
			}
		
			// check CH$SMILES
			if (!"N/A".equals(ch_smiles)) {
				try {
					// get the IAtomContainer formula from the SMILES
					IAtomContainer m = new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(ch_smiles);
					// compare it with CH$FORMULA
					String formula_smiles = MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(m));
					logger.trace("Formula from SMILES: \"" + formula_smiles + "\".");
					if (!formula_smiles.equals(ch_formula)) {
						logger.trace("Formula from record file " + ch_formula + ".");
						return context.failure("Formula generated from SMILES string in \"CH$SMILES\" field does not match formula in \"CH$FORMULA\". "
								+ formula_smiles + "!= \"CH$FORMULA: " + ch_formula + "\"");
					}
					
					// this code compares the structure in the SMILES with the structure in the InChI
					// it is done by converting the SMILES structure to an InChIKey and compare that
					// with the InChIKey from the InChI itself.
					// compare it with CH$IUPAC
					InChIGenerator inchiGen = InChIGeneratorFactory.getInstance().getInChIGenerator(m);
					INCHI_RET ret = inchiGen.getReturnStatus();
					
					if (ret == INCHI_RET.WARNING) {
						// Structure generated, but with warning message
						logger.warn("InChI warning: " + inchiGen.getMessage());
					} 
					else if (ret != INCHI_RET.OKAY) {
						// InChI generation failed
						return context.failure("Can not create InChiKey from SMILES string in \"CH$SMILES\" field. InChI generation failed: " + ret.toString() + " [" + inchiGen.getMessage() + "] for " + ch_smiles + ".");
					}
					String InChiKey_from_SMILES = inchiGen.getInchiKey();
					logger.trace("InChIKey from InChI " + InChiKey_from_CH_IUPAC + ".");
					logger.trace("InChI from SMILES " + inchiGen.getInchi() + ".");
					logger.trace("InChIKey from SMILES " + InChiKey_from_SMILES + ".");
					// only field1 of InChIKey atm
					//if (!InChiKey_from_SMILES.equals(InChiKey_from_CH_IUPAC)) {
					if (!InChiKey_from_SMILES.substring(0,14).equals(InChiKey_from_CH_IUPAC.substring(0,14))) {
						return context.failure("InChIKey generated from SMILES string in \"CH$SMILES\" field does not match InChIKey from \"CH$IUPAC\". "
								+ InChiKey_from_SMILES + "!= " + InChiKey_from_CH_IUPAC + ".");
					}
				} catch (CDKException e) { 
					return context.failure("Can not parse SMILES string in \"CH$SMILES\" field.\nError: "+ e.getMessage() + " for " + ch_smiles);		 				
				}
			}
	
			// check the InChIKey against the InChi
			if (InChiKey_from_CH_IUPAC!= null) {
				String InChiKey = callback.CH_LINK_asMap().get("INCHIKEY");
				if (InChiKey!=null) {
					// we have an InChIKey in CH$LINK and an InChIKey from CH$IUPAC 
					if (!InChiKey.equals(InChiKey_from_CH_IUPAC)) {
						return context.failure("InChIKey generated from InChI string in \"CH$IUPAC\" field does not match InChIKey in \"CH$LINK\". "
							+ InChiKey_from_CH_IUPAC + " != \"CH$LINK: INCHIKEY " + InChiKey + "\"");
					}
				}
			}

			
			// validate the number of peaks in the peaklist
			Integer num_peak= callback.PK_NUM_PEAK();
			List<List<Double>> pk_peak = callback.PK_PEAK();
			if (pk_peak.size() != num_peak) {
				StringBuilder sb = new StringBuilder();
				sb.append("Incorrect number of peaks in peaklist. ");
				sb.append(num_peak + " peaks are declared in PK$NUM_PEAK line, but " + pk_peak.size()+ " peaks are found.\n");
				return context.failure(sb.toString());
			}
			
			
			// validate the SPLASH
			List<Ion> ions = new ArrayList<Ion>();
			for (List<Double> peak_line :  pk_peak) {
				ions.add(new Ion(peak_line.get(0), peak_line.get(1)));
			}
			Splash splashFactory = SplashFactory.create();
			Spectrum spectrum = new SpectrumImpl(ions, SpectraType.MS);
			String splash_from_peaks = splashFactory.splashIt(spectrum);
			String splash_from_record = callback.PK_SPLASH();
			if (!splash_from_peaks.equals(splash_from_record)) {
				StringBuilder sb = new StringBuilder();
				sb.append("SPLASH from record file does not match SPLASH calculated from peaklist. ");
				sb.append(splash_from_record + " defined in record file, but " + splash_from_peaks + " calculated from peaks.\n");
				return context.failure(sb.toString());
			}
			
			
			// check peak sorting
			for (int i=0; i<pk_peak.size()-1; i++) {
				if ((pk_peak.get(i).get(0).compareTo(pk_peak.get(i+1).get(0)))>=0) {
					StringBuilder sb = new StringBuilder();
					sb.append("The peaks in the peak list are not sorted.\n");
					sb.append("Error in line " + pk_peak.get(i).toString() + ".\n");
					return context.failure(sb.toString());
				}
			}
			
			
			// max 600 characters are supported in database for PUBLICATION
			if (callback.PUBLICATION()!=null) {
				if (callback.PUBLICATION().length()>600) {
					StringBuilder sb = new StringBuilder();
					sb.append("PUBLICATION length exeeds database limit of 600 characters.\n");
					return context.failure(sb.toString());
				}
			}
			
			// max 600 characters are supported in database for RECORD_TITLE
			if (callback.RECORD_TITLE1().length()>600) {
				return context.failure("RECORD_TITLE length exeeds database limit of 600 characters.\n");
			}
			
			
			// check for duplicate entries in CH$NAME
			List<String> ch_name = callback.CH_NAME();
			Set<String> duplicates = new LinkedHashSet<String>();
			Set<String> uniques = new HashSet<String>();
			for(String c : ch_name) {
				if(!uniques.add(c)) {
					duplicates.add(c);
				}
			}
			if (duplicates.size()>0) {
				if (strict ) {
					StringBuilder sb = new StringBuilder();
					sb.append("There are duplicate entries in \"CH$NAME\" field.");
					return context.failure(sb.toString());
				} else {
					logger.warn("There are duplicate entries in \"CH$NAME\" field.");
				}
			}
			
			
			// check for duplicate entries in CH$LINK
			List<Pair<String, String>> ch_link = callback.CH_LINK();
			duplicates = new LinkedHashSet<String>();
			uniques = new HashSet<String>();
			for(Pair<String, String> c : ch_link) {
				if(!uniques.add(c.getKey())) {
					duplicates.add(c.getKey());
				}
			}
			if (duplicates.size()>0) {
				StringBuilder sb = new StringBuilder();
				sb.append("There are duplicate entries in \"CH$LINK\" field.");
				return context.failure(sb.toString());
			}
			
		}
		return r;
	}




}
