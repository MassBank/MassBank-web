package massbank.export;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OldAccessionResolverTest {

	@Test
	public void testOldAccessionResolver() {
		OldAccessionResolver oldAccessionResolver = OldAccessionResolver.get();
		Assertions.assertEquals("MSBNK-AAFC-AC000854", oldAccessionResolver.resolve("AC000854"));
		Assertions.assertEquals("MSBNK-Waters-WA001253", oldAccessionResolver.resolve("WA001253"));
		Assertions.assertEquals("", oldAccessionResolver.resolve(""));
		Assertions.assertEquals("MSBNK-Waters-WA001253", oldAccessionResolver.resolve("MSBNK-Waters-WA001253"));
		Assertions.assertEquals(null, oldAccessionResolver.resolve(null));
	}
}