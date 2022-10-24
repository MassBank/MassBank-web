package massbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Set;

import org.biojava.nbio.ontology.Ontology;
import org.biojava.nbio.ontology.Term;
import org.biojava.nbio.ontology.io.OboParser;
import static org.biojava.nbio.ontology.obo.OboFileHandler.IS_A;

/**
 * Controled vocabulary handler.
 * 
 * @author rmeier
 * @version 07-10-2022
 */
public final class CVUtil {
	private static CVUtil instance;
	Ontology ontology;

	private CVUtil() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("cv/psi-ms.obo")))) {
			OboParser parser = new OboParser();
			try {
				ontology = parser.parseOBO(reader, "Mass spectrometry ontology", "A structured controlled vocabulary for the annotation of experiments concerned with proteomics mass spectrometry.");
			} catch (ParseException ex) {
				System.err.println("Parsing exception: " + ex.getLocalizedMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized CVUtil get() {
		if (CVUtil.instance == null) {
			CVUtil.instance = new CVUtil();
		}
		return CVUtil.instance;
	}
	
	public boolean containsTerm(String name) {
		return ontology.containsTerm(name);
	}
	
	public Term getTerm(String name) {
		Term term = ontology.getTerm(name);
		return term;
	}
	
	public boolean termIsA(String name, String isA) {
		Term term = ontology.getTerm(name);
		Set<Term> keys = ontology.getTerms();
		System.out.println(keys.toString());
		System.out.println(term.getName());
		System.out.println(term.getDescription());
		System.out.println(term.getAnnotation());
		System.out.println(term.getSynonyms().toString());
		System.out.println(term.getOntology());
		System.out.println(term.getOntology());
		
		System.out.println(ontology.getTriples(ontology.getTerm("MS:1003294"), ontology.getTerm("MS:1000250") , ontology.getTerm(IS_A)));
		System.out.println(ontology.getTriples(ontology.getTerm("MS:1000250"), ontology.getTerm("MS:1003294") , ontology.getTerm(IS_A)));
		System.out.println(ontology.getTriples(ontology.getTerm("MS:1003294"), ontology.getTerm("MS:1000044") , ontology.getTerm(IS_A)));
		System.out.println(ontology.getTerm("MS:1003294").getAnnotation());
		

//		assertTrue(getAnnotationForTerm(ontology).containsProperty(NAMESPACE));
//		assertEquals("sequence", getAnnotationForTerm(ontology).getProperty(NAMESPACE));
//		getAnnotationForTerm(ontology).getProperty(NAMESPACE)
//		//(List<String>) getAnnotationForTerm(ontology).getProperty(ALT_ID);


		

		return true;
	}

}
