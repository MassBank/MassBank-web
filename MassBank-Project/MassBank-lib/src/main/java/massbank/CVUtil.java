package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.model.OWLClass;


/**
 * Controled vocabulary handler.
 * 
 * @author rmeier
 * @version 07-10-2022
 */
public final class CVUtil {
	private static CVUtil instance;
	private static Object mutex = new Object();
	//Ontology ontology;

	private CVUtil(){
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("cv/psi-ms.obo")))) {
//			OboParser parser = new OboParser();
//			try {
//				ontology = parser.parseOBO(reader, "Mass spectrometry ontology", "A structured controlled vocabulary for the annotation of experiments concerned with proteomics mass spectrometry.");
//			} catch (ParseException ex) {
//				System.err.println("Parsing exception: " + ex.getLocalizedMessage());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLOntology o=null;
//		try {
//			o = man.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
//			o = man.loadOntologyFromOntologyDocument(getClass().getClassLoader().getResourceAsStream("cv/psi-ms.owl"));
//		} catch (OWLOntologyCreationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(OWLAxiom ax:o.getLogicalAxioms()) {
//			System.out.println(ax);
//		}
//		System.out.println("Axioms: "+o.getAxiomCount()+", Format:"+man.getOntologyFormat(o));
//		ArrayList t = new ArrayList<OWLAxiom>();
//		for(OWLClass ax:o.getClassesInSignature()) {
//			t.add(ax);
//			System.out.println(ax.getIRI());
//		}
		
		
//		o.signature().filter((e->(!e.isBuiltIn()&&e.getIRI().getFragment().startsWith("M"))));
		//o.signature().filter((e->(!e.isBuiltIn()&&e.getIRI().getFragment().startsWith("M")))).forEach(System.out::println);
//		System.out.println(o);
		//OWLReasonerFactory rf = new ReasonerFactory();
		//OWLReasoner r = rf.createReasoner(o);
//		
//		OWLDataFactory df = man.getOWLDataFactory();
//		OWLReasoner r = new StructuralReasoner(o, new SimpleConfiguration(),BufferingMode.BUFFERING);
//		System.out.println(df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MS_1000044")).getEntityType());
//		System.out.println(df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MS_1000044")).getIRI());
//		//NodeSet<OWLClass> result = 	r.getSuperClasses(df.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Hot")), false);
//		NodeSet<OWLClass> result = 	r.getSuperClasses(df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MS_1000044")), true);
//		System.out.println(result);
//		((StructuralReasoner) r).dumpClassHierarchy(false);
//		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//		r.getSubClasses(df.getOWLClass("http://purl.obolibrary.org/obo/MS:1000044"), false).forEach(System.out::println);
	}

	public static CVUtil get(){
		CVUtil result = instance;
		if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new CVUtil();
			}
		}
		return result;
	}
		
//	public boolean containsTerm(String name) {
//		return ontology.containsTerm(name);
//	}
//	
//	public Term getTerm(String name) {
//		Term term = ontology.getTerm(name);
//		return term;
//	}
	
	public boolean termIsA(String name, String isA) {
//		Term term = ontology.getTerm(name);
//		Set<Term> keys = ontology.getTerms();
//		System.out.println(keys.toString());
//		System.out.println(term.getName());
//		System.out.println(term.getDescription());
//		System.out.println(term.getAnnotation());
//		System.out.println(term.getSynonyms().toString());
//		System.out.println(term.getOntology());
//		System.out.println(term.getOntology());
//		
//		System.out.println(ontology.getTriples(ontology.getTerm("MS:1003294"), ontology.getTerm("MS:1000250") , ontology.getTerm(IS_A)));
//		System.out.println(ontology.getTriples(ontology.getTerm("MS:1000250"), ontology.getTerm("MS:1003294") , ontology.getTerm(IS_A)));
//		System.out.println(ontology.getTriples(ontology.getTerm("MS:1003294"), ontology.getTerm("MS:1000044") , ontology.getTerm(IS_A)));
//		System.out.println(ontology.getTerm("MS:1003294").getAnnotation());
//		

//		assertTrue(getAnnotationForTerm(ontology).containsProperty(NAMESPACE));
//		assertEquals("sequence", getAnnotationForTerm(ontology).getProperty(NAMESPACE));
//		getAnnotationForTerm(ontology).getProperty(NAMESPACE)
//		//(List<String>) getAnnotationForTerm(ontology).getProperty(ALT_ID);


		

		return true;
	}

}
