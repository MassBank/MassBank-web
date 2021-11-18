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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * This class fetches PubChem information.
 * @author rmeier
 * @version 11-16-2021
 */
public class PubchemResolver {
	class LookupObject {
		List<Integer> cids = null;
		List<Integer> preferredCids = null;
		Integer preferredCid = null;
	}
	private static final Logger logger = LogManager.getLogger(PubchemResolver.class);
	private static Map<String, LookupObject> lookup = new HashMap<String, LookupObject>();
	String inchiKey = null;
	List<Integer> cids = null;
	List<Integer> preferredCids = null;
	Integer preferredCid = null;
	
	public PubchemResolver(String inchiKey) {
		this.inchiKey = inchiKey;
		if (lookup.containsKey(inchiKey)) {
			this.cids = lookup.get(inchiKey).cids;
			this.preferredCids = lookup.get(inchiKey).preferredCids;
			this.preferredCid = lookup.get(inchiKey).preferredCid;
			logger.info("Take cids for " + inchiKey + " from lookup");
			return;
		}
		
		// get cids
		try {
			ResponseCidFromInchikey r1 = new Gson().fromJson(IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/"
					+ inchiKey + "/cids/JSON?cids_type=preferred"), Charset.forName("UTF-8")), ResponseCidFromInchikey.class);
			ResponseCidFromInchikey r2 = new Gson().fromJson(IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/"
					+ inchiKey + "/cids/JSON"), Charset.forName("UTF-8")), ResponseCidFromInchikey.class);
			preferredCids = r1.getCID();
			cids = r2.getCID();
		} catch (IOException e) {
			logger.info("Not possible to retrive PubChem CIDs for InChIKey " + inchiKey + ".");
		}
		
		if (preferredCids!=null) {
			for (Integer cid : preferredCids) {
				if (!cid.equals(preferredCids.get(0)))  logger.error("preferredCids list has different entries"); 
			}
			// check if the preferred cid is in cids
			boolean preferredCidIsInCids = false;
			for (Integer cid : cids) {
				if (cid.equals(preferredCids.get(0))) preferredCidIsInCids = true; 
			}
			if (!preferredCidIsInCids) {
				 logger.error("preferredCid is not in cids.");
			}
			else preferredCid = preferredCids.get(0);

			// check back InChIKey
			try {
				ResponseInchikeyFromCid r = new Gson().fromJson(IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+preferredCid+"/property/InChIKey/JSON"), Charset.forName("UTF-8")), ResponseInchikeyFromCid.class);
				String inChIKeyFrompreferredCid = r.getInChIKey();
				if (!inChIKeyFrompreferredCid.equals(inchiKey)) {
					logger.error("InChIKey from preferred PubChem CID does not match InChIKey from constructor.");
					logger.error("InChIKey from preferred PubChem CID: " + inChIKeyFrompreferredCid);
					logger.error("InChIKey from constructor: " + inchiKey);
					preferredCid = null;
				}
			} catch (IOException e) {
				logger.error("Not possible to retrive InChIKey for preferred PubChem CID for InChIKey " + inchiKey + ".");
				preferredCid = null;
			}
		}
			
		if (cids != null && preferredCids != null && preferredCid != null) {
			if (preferredCids.size() != cids.size()) logger.error("Size of preferredCids != Size of cids");
			LookupObject lookupObject = new LookupObject();
			lookupObject.cids = cids;
			lookupObject.preferredCids = preferredCids;
			lookupObject.preferredCid = preferredCid;
			lookup.put(inchiKey, lookupObject);
		}
	}
	
	public Integer getPreferred() {
		return preferredCid;
	}
	
	public boolean isCid(Integer cid) {
		for (Integer i : cids) {
			if (i.equals(cid)) return true;
		}
		return false;
	}
	
	
	// Respons class for json structure
	static class ResponseCidFromInchikey {
		// {
		//  "IdentifierList": {
		//   "CID": [
		//    xxxxx,
		//    xxxxx
		//   ]
		//  }
		// }
		@SerializedName("IdentifierList")
		private IdentifierList identifierList;
		class IdentifierList {
			@SerializedName("CID")
			List<Integer> CID;
		}
		public Integer getFirstCID() {
			return identifierList.CID.get(0);
		}
		public List<Integer> getCID() {
			return identifierList.CID;
		}
	}
	// Respons class for json structure
	static class ResponseInchikeyFromCid {
		// {
		//  "PropertyTable": {
		//	    "Properties": [
		//	      {
		//	        "CID": xxxxxx,
		//			"InChIKey": "xxxxxxxxx-xxxxxxxxxxx-x"
		//	      }
		//	    ]
		//	  }
		//	}
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
}
