package massbank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class CVUtilTest {

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
}