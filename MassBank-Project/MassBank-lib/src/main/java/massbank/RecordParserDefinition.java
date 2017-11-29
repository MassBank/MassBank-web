package massbank;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.letter;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.petitparser.context.Token;
import org.petitparser.parser.primitive.CharacterParser;
import org.petitparser.parser.primitive.StringParser;
import org.petitparser.tools.GrammarDefinition;

import net.sf.jniinchi.INCHI_RET;


public class RecordParserDefinition extends GrammarDefinition {
	
	public Double tuwas(String value) {
		return Double.parseDouble(value);
	}
	
	
	public RecordParserDefinition(Record callback) {
		def("start",
				ref("accession")
				.seq(ref("record_title")));/*
				.seq(ref("date_value"))
				.seq(ref("authors_value"))
				.seq(ref("license_value"))
				.seq(ref("copyright_value").optional())
				.seq(ref("publication_value").optional())
				.seq(ref("comment_value").optional())
				.seq(ref("ch_name_value"))
				.seq(ref("ch_compound_class_value"))
				.seq(ref("ch_formula_value"))
				.seq(ref("ch_exact_mass_value"))
				.seq(ref("ch_smiles_value"))
				.seq(ref("ch_iupac_value"))
				.seq(ref("ch_link_value").optional())
				.seq(ref("sp_scientific_name_value").optional())
				.seq(ref("sp_lineage_value").optional())
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
				.seq(ref("tagsep")).pick(0)
				// parse accession and put it in the record
				.seq(
						letter().times(2).seq(digit().times(6))
						.or(letter().times(3).seq(digit().times(5)))
						.flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.ACCESSION( (String) value.get(1));
						//System.out.println(value.toString());
						return value;						
					})
			);
		
		// 2.1.2 RECORD_TITLE (CH$NAME ; AC$INSTRUMENT_TYPE ; AC$MASS_SPECTROMETRY: MS_TYPE)
		// Brief Description of MassBank Record. Mandatory
		// Example: RECORD_TITLE: (-)-Nicotine; ESI-QQ; MS2; CE 40 V; [M+H]+
		// It consists of the values of CH$NAME; AC$INSTRUMENT_TYPE; AC$MASS_SPECTROMETRY: MS_TYPE;.
		def("record_title",
				StringParser.of("RECORD_TITLE")
				.seq(ref("tagsep")).pick(0)
				.seq(
						ref("ch_name_value")
						.seq(
								ref("valuesep")
							).pick(0)
					)
				.seq(
						ref("ac_instrument_type_value")
						.seq(
								ref("valuesep")
							).pick(0)
					)
				.seq(
						ref("ac_mass_spectrometry_ms_type_value")
					)
				// TODO curation required because ionisation energy is not in format doc
				.seq(
						ref("valuesep")
						.seq(
								CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
							).pick(1).optional()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						if (value.get(value.size()-1) == null) value.remove(value.size()-1); 
						callback.RECORD_TITLE(value.subList(1, value.size()).toString());
						//System.out.println(value.toString());
						return value;						
					})
			);
		
		// 2.1.3 DATE
		// Date of the Creation or the Last Modification of MassBank Record. Mandatory
		// Example
		// DATE: 2011.02.21 (Created 2007.07.07)
		// DATE: 2016.01.15
		// DATE: 2016.01.19 (Created 2006.12.21, modified 2011.05.06)
		def("date_helper",
				CharacterParser.digit().times(4).flatten().trim(CharacterParser.of('.')).map((String value) -> Integer.parseInt(value))
				.seq(
						CharacterParser.digit().times(2).flatten().trim(CharacterParser.of('.')).map((String value) -> Integer.parseInt(value))
					)
				.seq(
						CharacterParser.digit().times(2).flatten().map((String value) -> Integer.parseInt(value))
					)
				.map((List<Integer> value) -> {
					return LocalDate.of(value.get(0),value.get(1),value.get(2));		
				})
			);
		
		def("date_value",
				StringParser.of("DATE")
    			.seq(ref("tagsep")).pick(0)
    			.seq(
    					ref("date_helper")
    				)
    			.seq(
    					ref("date_helper").trim(StringParser.of(" (Created "),StringParser.of(", modified "))
    						.seq(ref("date_helper").trim(CharacterParser.none(), CharacterParser.of(')')))
 						.or(
 								ref("date_helper").trim(StringParser.of(" (Created "),CharacterParser.of(')'))
    						).optional()
    				)
    			.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
					value.remove(value.size()-1);
					if (value.get(value.size()-1) == null) value.remove(value.size()-1); 
					callback.DATE((LocalDate) value.get(1));
					//System.out.println(value.toString());
					return value;						
				})
			);
		
		// 2.1.4 AUTHORS
		// Authors and Affiliations of MassBank Record. Mandatory
		// Example
		// AUTHORS: Akimoto N, Grad Sch Pharm Sci, Kyoto Univ and Maoka T, Res Inst Prod Dev.
		// Only single-byte characters are allowed.  For example, ö is not allowed.
		def("authors_value",
				StringParser.of("AUTHORS")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.AUTHORS((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
			);
		
		// 2.1.5 LICENSE
		// License of MassBank Record. Mandatory
		// Example
		// LICENSE: CC BY
		// TODO fix format doc
		def("license_value",
				StringParser.of("LICENSE")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.LICENSE((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
		);
			
		// 2.1.6 COPYRIGHT
		// Copyright of MassBank Record. Optional
		// Example
		// COPYRIGHT: Keio University
		def("copyright_value",
				StringParser.of("COPYRIGHT")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.COPYRIGHT((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
		);
		
		// 2.1.7 PUBLICATION
		// Reference of the Mass Spectral Data. Optional
		// Example
		// PUBLICATION: Iida T, Tamura T, et al, J Lipid Res. 29, 165-71 (1988). [PMID: 3367086]
		// Citation with PubMed ID is recommended.
		def("publication_value",
				StringParser.of("PUBLICATION")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.PUBLICATION((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
			);
		
		// 2.1.8 COMMENT
		// Comments.   Optional and Iterative 
		// In MassBank, COMMENT fields are often used to show the relations of the present record with other MassBank records and with data files. In these cases, the terms in brackets [ and ] are reserved for the comments specific to the following five examples.
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
		def("comment_value",
				StringParser.of("COMMENT")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten().trim(CharacterParser.none(), Token.NEWLINE_PARSER)
					).plus()
				.map((List<?> value) -> {
						List<String> comments = new ArrayList<String>();
						for (int i = 0; i < value.size(); i++) {
							@SuppressWarnings("unchecked")
							List<String> tmp = (List<String>) value.get(i);
							comments.add(tmp.get(1));
						}
						callback.COMMENT(comments);
						//System.out.println(comments.toString());
						return value;
						
					})
			);
		
		
		// 2.2 Information of Chemical Compound Analyzed
		// 2.2.1 CH$NAME
		// Name of the Chemical Compound Analyzed. Mandatory and Iterative
		// Example
		// CH$NAME: D-Tartaric acid
		// CH$NAME: (2S,3S)-Tartaric acid
		// No prosthetic molecule of adducts (HCl, H2SO3, H2O, etc), conjugate ions (Chloride, etc) , and protecting groups (TMS, etc.) is included.
		// Chemical names which are listed in the compound list are recommended.  Synonyms could be added.
		// If chemical compound is a stereoisomer, stereochemistry should be indicated.
		def("ch_name_value",
				CharacterParser.word().or(CharacterParser.anyOf("-+, ()'"))
						.plusLazy(Token.NEWLINE_PARSER.or(CharacterParser.of(';')))
						.flatten()
			);
				
		def("ch_name", 
				StringParser.of("CH$NAME")
				.seq(ref("tagsep")).pick(0)
				.seq(ref("ch_name_value"))
				.plus()
				.map((List<?> value) -> {
					List<String> ch_name = new ArrayList<String>();
					for (int i = 0; i < value.size(); i++) {
						@SuppressWarnings("unchecked")
						List<String> tmp = (List<String>) value.get(i);
						ch_name.add(tmp.get(1));
					}
					callback.CH_NAME(ch_name);
					//System.out.println(ch_name.toString());
					return value;						
				})
			);

		
		// 2.2.2 CH$COMPOUND_CLASS
		// Category of Chemical Compound. Mandatory
		// Example
		// CH$COMPOUND_CLASS: Natural Product; Carotenoid; Terpenoid; Lipid
		// Either Natural Product or Non-Natural Product should be precedes the other class names .
		def("ch_compound_class_value",
				StringParser.of("CH$COMPOUND_CLASS")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
					value.remove(value.size()-1);
					callback.CH_COMPOUND_CLASS((String) value.get(1));
					//System.out.println(value.toString());
					return value;
				})
			);
		
		// 2.2.3 CH$FORMULA
		// Molecular Formula of Chemical Compound. Mandatory
		// Example
		// CH$FORMULA: C9H10ClNO3
		// It follows the Hill's System.
		// No prosthetic molecule is included (see 2.2.1 CH$NAME).
		// Molecular formulae of derivatives by chemical modification with TMS, etc. should be given in the MS$FOCUSED_ION: DERIVATIVE_FORM (2.5.1) field.
		def("ch_formula_value",
				StringParser.of("CH$FORMULA")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.CH_FORMULA((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
			);
		
		
		// 2.2.4 CH$EXACT_MASS
		// Monoisotopic Mass of Chemical Compound. Mandatory
		// Example
		// CH$EXACT_MASS: 430.38108
		// A value with 5 digits after the decimal point is recommended.
		def("ch_exact_mass_value",
				StringParser.of("CH$EXACT_MASS")
				.seq(ref("tagsep")).pick(0)
				.seq(
						digit().plus()
				        .seq(
				        	CharacterParser.of('.')
				        	.seq(digit().plus()).optional()
				        	).flatten().map((String value) -> Double.parseDouble(value))
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						//System.out.println(value.toString());
						callback.CH_EXACT_MASS((Double) value.get(1));
						return value;
					})
			);
		
		// 2.2.5 CH$SMILES *
		// SMILES String. Mandatory
		// Example
		// CH$SMILES: NCC(O)=O
		// Isomeric SMILES but not a canonical one.
		def("ch_smiles_value",
				StringParser.of("CH$SMILES")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						try {
						     SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
						     IAtomContainer m = sp.parseSmiles((String) value.get(1));
						     callback.CH_SMILES(m);
						 } catch (InvalidSmilesException e) {
							 throw new IllegalStateException("Can not parse SMILES string in \"CH$SMILES\" field.", e);
						 }
						//System.out.println(value.toString());
						return value;
					})
			);

		// 2.2.6 CH$IUPAC *
		// IUPAC International Chemical Identifier (InChI Code). Mandatory
		// Example
		// CH$IUPAC: InChI=1S/C2H5NO2/c3-1-2(4)5/h1,3H2,(H,4,5)
		// Not IUPAC name.
		def("ch_iupac_value",
				StringParser.of("CH$IUPAC")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						try {
							InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
							// Get InChIToStructure
							InChIToStructure intostruct = factory.getInChIToStructure((String) value.get(1), DefaultChemObjectBuilder.getInstance());

							INCHI_RET ret = intostruct.getReturnStatus();
							if (ret == INCHI_RET.WARNING) {
								// Structure generated, but with warning message
								System.out.println("InChI warning: " + intostruct.getMessage());
							} 
							else if (ret != INCHI_RET.OKAY) {
								// Structure generation failed
								throw new IllegalStateException("Structure generation failed: " + ret.toString() + " [" + intostruct.getMessage() + "]");
							}
							IAtomContainer m = intostruct.getAtomContainer();
							callback.CH_IUPAC(m);
						} catch (CDKException e) {
							throw new IllegalStateException("Can not parse INCHI string in \"CH$IUPAC\" field.", e);
						}
						
						//System.out.println(value.toString());
						return value;
					})
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
		// Currently MassBank records have links to the following external databases :
		// CAS
		// CHEBI
		// CHEMPDB
		// CHEMSPIDER
		// COMPTOX
		// INCHIKEY
		// KEGG
		// KNAPSACK
		// LIPIDBANK
		// LIPIDMAPS
		// PUBCHEM
		// CH$LINK fields should be arranged by the alphabetical order of database names.
		// InChI Key, a hashed version of InChI code, is a common link by chemical structures.
		def("ch_link_value",
				StringParser.of("CH$LINK")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten().trim(CharacterParser.none(), Token.NEWLINE_PARSER)
					).plus()
				.map((List<?> value) -> {
						List<String> link = new ArrayList<String>();
						for (int i = 0; i < value.size(); i++) {
							@SuppressWarnings("unchecked")
							List<String> tmp = (List<String>) value.get(i);
							link.add(tmp.get(1));
						}
						callback.CH_LINK(link);
						//System.out.println(link.toString());
						return value;
						
					})
			);
		
		// 2.3.1 SP$SCIENTIFIC_NAME
		// Scientific Name of Biological Species, from Which Sample was Prepared.  Optional
		// Example
		// SP$SCIENTIFIC_NAME: Mus musculus
		def("sp_scientific_name_value",
				StringParser.of("SP$SCIENTIFIC_NAME")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.SP_SCIENTIFIC_NAME((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
		);
		
		def("sp_lineage_value",
				StringParser.of("SP$LINEAGE")
				.seq(ref("tagsep")).pick(0)
				.seq(
						CharacterParser.any().plusLazy(Token.NEWLINE_PARSER).flatten()
					)
				.seq(Token.NEWLINE_PARSER)
				.map((List<?> value) -> {
						value.remove(value.size()-1);
						callback.SP_LINEAGE((String) value.get(1));
						//System.out.println(value.toString());
						return value;
					})
		);

		// 2.3.2 SP$LINK subtag identifier
		// Identifier of Biological Species in External Databases.  Optional
		// Example
		// SP$LINK: NCBI-TAXONOMY 10090

		// 2.3.3 SP$SAMPLE
		// Tissue or Cell, from which Sample was Prepared.   Optional and iterative
		// Example
		// SP$SAMPLE: Liver extracts
		
		
		
		
		
		
		
		
		
		
		
		// 2.4.2 AC$INSTRUMENT_TYPE
		// General Type of Instrument.  Mandatory
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
		def("ac_instrument_type_value_sep",
				StringParser.of("CE")
				.or(StringParser.of("GC"))
				.or(StringParser.of("LC"))
				.seq(CharacterParser.of('-'))
				.pick(0)
				);
		def("ac_instrument_type_value_ionisation",
				StringParser.of("APCI")
				.or(StringParser.of("APPI"))
				.or(StringParser.of("EI"))
				.or(StringParser.of("ESI"))
				.or(StringParser.of("FAB"))
				.or(StringParser.of("MALDI"))
				.seq(CharacterParser.of('-'))
				.pick(0)
				);
		def("ac_instrument_type_value_analyzer",
				StringParser.of("B")
				.or(StringParser.of("E"))
				.or(StringParser.of("FT"))
				.or(StringParser.of("IT"))
				.or(StringParser.of("Q"))
				.or(StringParser.of("TOF"))
				);
		def("ac_instrument_type_value", 
				ref("ac_instrument_type_value_sep")
				.optional()
				.seq(ref("ac_instrument_type_value_ionisation"))
				.seq(ref("ac_instrument_type_value_analyzer").plus())
				);
		def("ac_instrument_type", 
				StringParser.of("AC$INSTRUMENT_TYPE")
				.seq(ref("tagsep"))
				.seq(ref("ac_instrument_type_value"))
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
				.seq(ref("ac_mass_spectrometry_ms_type_value"))
				);
		
		
		
		
		
	}

}
