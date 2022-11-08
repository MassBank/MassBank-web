package massbank;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.biojava.nbio.ontology.Ontology;
import org.biojava.nbio.ontology.Term;
import org.biojava.nbio.ontology.io.OboParser;
import org.biojava.nbio.ontology.utils.Annotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.OWLOntology;

import static org.biojava.nbio.ontology.obo.OboFileHandler.NAMESPACE;
import static org.biojava.nbio.ontology.obo.OboFileHandler.ALT_ID;
import static org.biojava.nbio.ontology.obo.OboFileHandler.IS_A;


public class CVUtilTest {
	
	private OboParser parser;

	@Test
	public void testCVUtilGetFirst() {
		CVUtil cvUtil = CVUtil.get();
		Assertions.assertTrue(cvUtil.ontology.containsTerm("MS:0000000"));
		Assertions.assertFalse(cvUtil.ontology.containsTerm("MS:2000000"));
	}
	@Test
	public void testCVUtilGetSecond() {
		CVUtil cvUtil = CVUtil.get();
		Assertions.assertTrue(cvUtil.ontology.containsTerm("MS:0000000"));
		Assertions.assertFalse(cvUtil.ontology.containsTerm("MS:2000000"));
	}
	
	@Test
	public void testTermIsA() throws IOException {
		CVUtil cvUtil = CVUtil.get();
		Assertions.assertTrue(cvUtil.termIsA("MS:1003294","MS:1000250"));
		IOHelper ioHelper = new IOHelper();
		OWLOntology full = ioHelper.loadOntology("/home/rene/GIT/MassBank-web/MassBank-Project/MassBank-lib/src/main/resources/cv/psi-ms.owl");
		
	}
	
}