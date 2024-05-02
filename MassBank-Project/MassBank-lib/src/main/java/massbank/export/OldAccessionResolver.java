package massbank.export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolve old MassBank accessions to new ones based on the mapping in OldAccessionResolver.txt.
 * 
 * @author rmeier
 * @version 16-06-2022
 */
public final class OldAccessionResolver {
	private static OldAccessionResolver instance;
	private Map<String, String> accessionResolverMap;

	private OldAccessionResolver() {
		accessionResolverMap = new HashMap<>();
		String line;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("OldAccessionResolver.txt")))) {
			while ((line = reader.readLine()) != null) {
				String[] keyValuePair = line.split(" ", 2);
				if (keyValuePair.length > 1) {
					String key = keyValuePair[0];
					String value = keyValuePair[1];
					accessionResolverMap.put(key, value);
				} else {
					System.out.println("No Key:Value found in line, ignoring: " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized OldAccessionResolver get() {
		if (OldAccessionResolver.instance == null) {
			OldAccessionResolver.instance = new OldAccessionResolver();
		}
		return OldAccessionResolver.instance;
	}

	public String resolve(String accession) {
		String result = accessionResolverMap.get(accession);
		return result != null ? result : accession;
	}
}
