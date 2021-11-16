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
import java.util.List;

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
	private static final Logger logger = LogManager.getLogger(PubchemResolver.class);
	String inchiKey = null;
	List<Integer> cids = null;
	List<Integer> preferredCids = null;
	
	public PubchemResolver(String inchiKey) {
		this.inchiKey = inchiKey;
		// get prefered cids
		try {
			String jsonString = IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/"
					+ inchiKey + "/cids/JSON?cids_type=preferred"), Charset.forName("UTF-8"));
			ResponseCidFromInchikey r = new Gson().fromJson(jsonString, ResponseCidFromInchikey.class);
			preferredCids = r.getCID();
		} catch (IOException e) {
			logger.error("Not possible to retrive preferred PubChem CID for InChIKey " + inchiKey + ".");
		}
				
		// get cids
		try {
			String jsonString = IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/"
					+ inchiKey + "/cids/JSON"), Charset.forName("UTF-8"));
			ResponseCidFromInchikey r = new Gson().fromJson(jsonString, ResponseCidFromInchikey.class);
			cids = r.getCID();
		} catch (IOException e) {
			logger.error("Not possible to retrive PubChem CID for InChIKey " + inchiKey + ".");
		}
		if (preferredCids.size() != cids.size()) logger.error("Size of preferredCids != Size of cids"); 
	}
	
	public Integer getPreferred() {
		Integer result = null;
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
				 return null;
			}
			else result = preferredCids.get(0);
			// check back InChIKey
			String jsonString = null;
			try {
				jsonString = IOUtils.toString(new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+result+"/property/InChIKey/JSON"), Charset.forName("UTF-8"));
			} catch (IOException e) {
				logger.error("Not possible to retrive InChIKey for preferred PubChem CID for InChIKey " + inchiKey + ".");
			}
			ResponseInchikeyFromCid r = new Gson().fromJson(jsonString, ResponseInchikeyFromCid.class);
			String inChIKeyFrompreferredCid = r.getInChIKey();
			if (!inChIKeyFrompreferredCid.equals(inchiKey)) {
				logger.error("InChIKey from preferred PubChem CID does not match InChIKey from constructor.");
				logger.error("InChIKey from preferred PubChem CID: " + inChIKeyFrompreferredCid);
				logger.error("InChIKey from constructor: " + inchiKey);
				return null;
			}
		}
		return result;
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
