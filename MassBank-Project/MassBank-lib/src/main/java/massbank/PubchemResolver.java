/*******************************************************************************
 * Copyright (C) 2021 MassBank consortium
 * 
 * This file is part of MassBank.
 * 
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 ******************************************************************************/
package massbank;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * This class fetches PubChem information.
 * 
 * @author rmeier
 * @version 11-16-2021
 */
public class PubchemResolver {
	private static final Logger logger = LogManager.getLogger(PubchemResolver.class);

	class PubchemID {
		Map<Integer, Integer> cids = null;
		Set<Integer> preferredCids = null;
		Set<Integer> liveCids = null;
		Integer preferredCid = null;
	}
	private static Map<String, PubchemID> lookup = new ConcurrentHashMap<String, PubchemID>();
	
	String inchiKey = null;
	PubchemID pubchemId = null;

	public PubchemResolver(String inchiKey) {
		this.inchiKey = inchiKey;
		if (lookup.containsKey(inchiKey)) {
			pubchemId = lookup.get(inchiKey);
			logger.info("Take cids for " + inchiKey + " from cache.");
			return;
		}
		pubchemId = new PubchemID();

		String s1 = getJSONFromUrl("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchiKey
				+ "/cids/JSON?cids_type=preferred");
		ResponseCidFromInchikey r1 = new Gson().fromJson(s1, ResponseCidFromInchikey.class);
		if (r1 == null) {
			pubchemId.preferredCids = new HashSet<Integer>();
		} else {
			pubchemId.preferredCids = r1.getCID();
		}	

		String s2 = getJSONFromUrl("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchiKey
				+ "/sids/JSON");
		ResponseSidFromInchikey r2 = new Gson().fromJson(s2, ResponseSidFromInchikey.class);
		if (r2 == null) {
			pubchemId.cids = new HashMap<Integer, Integer>();
		} else {
			pubchemId.cids = r2.getSidCount().entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) 			
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
			(oldValue, newValue) -> oldValue, LinkedHashMap::new));
		}

		pubchemId.liveCids = pubchemId.cids.entrySet().stream()
				.filter(x -> x.getValue() != 0).map(Entry::getKey)
				.collect(Collectors.toCollection( LinkedHashSet::new ));
		
		// we only report cid as preferred cid if the cid is live, is the preferred one
		// and has the same InChiKey
		Set<Integer> livePreferredCids = new LinkedHashSet<>(pubchemId.preferredCids);
		livePreferredCids.retainAll(pubchemId.liveCids); // Intersection
		if (livePreferredCids.isEmpty()) {
			logger.error("Preferred CID can not be determined.");
			logger.error("Preferred CID: " + pubchemId.preferredCids.toString());
			logger.error("cids: " + pubchemId.cids.toString());
			logger.error("live cids: " + pubchemId.liveCids.toString());
		} else {
			Iterator<Integer> iterator = livePreferredCids.iterator();
			Integer i = iterator.next();
			if (pubchemId.cids.keySet().contains(i)) {
				pubchemId.preferredCid = i;
			}
		}

		if (pubchemId.preferredCid != null) {
			// check the InChIKey of the preferred cid
			String s = getJSONFromUrl("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + pubchemId.preferredCid
					+ "/property/InChIKey/JSON");
			ResponseInchikeyFromCid r = new Gson().fromJson(s, ResponseInchikeyFromCid.class);
			String inChIKeyFrompreferredCid = r.getInChIKey();
			if (!inChIKeyFrompreferredCid.equals(inchiKey)) {
				logger.error("InChIKey from preferred PubChem CID does not match InChIKey from constructor.");
				logger.error("InChIKey from preferred PubChem CID: " + inChIKeyFrompreferredCid);
				logger.error("InChIKey from constructor: " + inchiKey);
				pubchemId.preferredCid = null;
			}
		}

		if (pubchemId.preferredCid != null) {
			lookup.put(inchiKey, pubchemId);
		}
	}

	public Integer getPreferred() {
		return pubchemId.preferredCid;
	}

	public boolean isCid(Integer cid) {
		return pubchemId.cids.keySet().contains(cid);
	}

	public String getJSONFromUrl(String url) {
		try (CloseableHttpClient httpClient = HttpClients.custom()
			.setDefaultRequestConfig(RequestConfig.custom()
				.setConnectTimeout(Timeout.ofSeconds(5))
				.setResponseTimeout(Timeout.ofSeconds(60))
				.setCookieSpec(StandardCookieSpec.STRICT)
				.build())
			.build()) {
			HttpGet httpGet = new HttpGet(url);
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				int status = response.getCode();
				if (status == 404) {
					logger.warn("PUGREST.NotFound");
					try {
						return EntityUtils.toString(response.getEntity());
					} catch (org.apache.hc.core5.http.ParseException e) {
						logger.error("Error parsing HTTP response entity", e);
						return "";
					}
				}
				if (status != 200) {
					logger.error("HTTP response: " + status);
					try {
						logger.error(EntityUtils.toString(response.getEntity()));
					} catch (org.apache.hc.core5.http.ParseException e) {
						logger.error("Error parsing HTTP response entity", e);
						return "";
					}
					throw new IOException("Pubchem no success.");
				}
				try {
					return EntityUtils.toString(response.getEntity());
				} catch (org.apache.hc.core5.http.ParseException e) {
					logger.error("Error parsing HTTP response entity", e);
					return "";
				}
			}
		} catch (IOException e) {
			logger.error("Error fetching JSON from URL: " + url, e);
			return "";
		}
	}

	// Respons classes for json structure
	static class ResponseCidFromInchikey {
		// {
		// "IdentifierList": {
		// "CID": [
		// xxxxx,
		// xxxxx
		// ]
		// }
		// }
		@SerializedName("IdentifierList")
		private IdentifierList identifierList;

		class IdentifierList {
			@SerializedName("CID")
			Set<Integer> CID;
		}

		public Set<Integer> getCID() {
			if (identifierList == null || identifierList.CID == null) {
				return new HashSet<Integer>();
			}
			return identifierList.CID;
		}
	}

	static class ResponseInchikeyFromCid {
		// {
		// "PropertyTable": {
		// "Properties": [
		// {
		// "CID": xxxxxx,
		// "InChIKey": "xxxxxxxxx-xxxxxxxxxxx-x"
		// }
		// ]
		// }
		// }
		@SerializedName("PropertyTable")
		private PropertyTable propertyTable;

		class PropertyTable {
			@SerializedName("Properties")
			List<Propertie> properties;
		}

		class Propertie {
			@SerializedName("CID")
			Integer cid;
			@SerializedName("InChIKey")
			String inChIKey;
		}

		public String getInChIKey() {
			return propertyTable.properties.get(0).inChIKey;
		}
	}

	static class ResponseSidFromInchikey {
		// {
		// "InformationList": {
		// "Information": [
		// {
		// "CID": xxxx,
		// "SID": [
		// xxxxx,
		// xxxxx
		// ]
		// },
		// {
		// "CID": xxxxx
		// }
		// ]
		// }
		// }
		@SerializedName("InformationList")
		private InformationList informationList;

		class InformationList {
			@SerializedName("Information")
			List<Information> information;
		}

		class Information {
			@SerializedName("CID")
			Integer cid;
			@SerializedName("SID")
			List<Integer> sids;
		}

		Map<Integer, Integer> getSidCount() {
			Map<Integer, Integer> result = new HashMap<>();
			if (informationList != null) {
				for (Information i : informationList.information) {
					if (i.sids == null) {
						result.put(i.cid, 0);
					} else {
						result.put(i.cid, i.sids.size());
					}
				}
			}
			return result;
		}
	}
}
