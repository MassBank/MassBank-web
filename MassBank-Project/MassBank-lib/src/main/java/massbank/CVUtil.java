package massbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import org.biojava.nbio.ontology.Ontology;
import org.biojava.nbio.ontology.io.OboParser;

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

}
