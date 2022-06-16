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
 * package massbank;
 * 
 ******************************************************************************/
package massbank;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;

import massbank.db.DatabaseManager;
import massbank.export.OldAccessionResolver;

@WebServlet("/RecordDisplay")
public class RecordDisplay extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(RecordDisplay.class);
	
//		@id  https://massbank.eu/MassBank/RecordDisplay.jsp?id=WA001202&dsn=Waters
//		measurementTechnique LC-ESI-Q

//			"biologicalRole": [
//				{
//					"@type": "DefinedTerm",
//					"@id": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C66892",
//					"inDefinedTermSet":
//						{
//							"@type":"DefinedTermSet",
//							"@id":"http://data.bioontology.org/ontologies/NCIT/submissions/69/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb",
//							"name": "National Cancer Institute Thesaurus"
//						},
//					"termCode": "C66892",
//					"name": "natural product",
//					"url": "http://bioportal.bioontology.org/ontologies/NCIT?p=classes&conceptid=http%3A%2F%2Fncicb.nci.nih.gov%2Fxml%2Fowl%2FEVS%2FThesaurus.owl%23C66892"
//				}
//		  ]
//		}
		

//			case "AUTHORS":
//				//sb.append(tag + ": " + value + "\n");
//				String[] authorTokens	= value.split(", ");
//				
//				// check which tokens are authors
//				int lastAuthorIdx	= -1;
//				for(int i = 0; i < authorTokens.length; i++){
//					// check if string is author like 'Akimoto AV'
//					boolean isAuthor	= authorTokens[i].matches("\\w+ \\w+");
//					if(isAuthor){
//						// 2nd word is initials?
//						String[] initials	= authorTokens[i].split(" ")[1].split("");
//						for(int j = 0; j < initials.length; j++)
//							if(!Character.isUpperCase(initials[j].toCharArray()[0])){
//								isAuthor	= false;
//								break;
//							}
//					}
//					if(isAuthor){
//						lastAuthorIdx	= i;
//					}
//				}
//				
//				// create affiliation
//				int numberOfAuthors	= lastAuthorIdx + 1;
//				String affiliation	= String.join(", ", Arrays.copyOfRange(authorTokens, numberOfAuthors, authorTokens.length));
//				
//				// create authors
//				String[] authors	= new String[numberOfAuthors];
//				for(int i = 0; i < numberOfAuthors; i++)
//					authors[i]	= 
//							"<span property=\"schema:author\" typeof=\"schema:Person\">" +
//								"<span property=\"schema:name\">" + authorTokens[i] + "</span>" +
//								//((affiliation.length() > 0) ?"<span property=\"schema:affiliation\" style=\"visibility:hidden\">" + affiliation + "</span>" : "") +
//								((affiliation.length() > 0) ?"<span property=\"schema:affiliation\" style=\"display:none\">" + affiliation + "</span>" : "") +
//							"</span>";
//				
//				// paste
//				sb.append(tag + ": ");
//				sb.append(String.join(", ", authors));
//				if(affiliation.length() > 0)
//					sb.append(", " + affiliation);
//				sb.append("\n");
//				break;



//			case "CH$CDK_DEPICT_SMILES":
//			case "CH$CDK_DEPICT_GENERIC_SMILES":
//			case "CH$CDK_DEPICT_STRUCTURE_SMILES":
//				ClickablePreviewImageData clickablePreviewImageData2	= StructureToSvgStringGenerator.createClickablePreviewImage(
//						tag, null, value,
//						tmpFileFolder, tmpUrlFolder,
//						80, 200, 436
//				);
//				if(clickablePreviewImageData2 != null)
//					sb.append(tag + ": " + clickablePreviewImageData2.getMediumClickablePreviewLink("CH$CDK_DEPICT_SMILES", value));
//				else
//					sb.append(tag + ": " + value + "\n");
//				break;

//			// AC$INSTRUMENT
//			case "AC$INSTRUMENT_TYPE":
//				// TODO property="schema:measurementTechnique"
//				sb.append(tag + ": <span property=\"schema:measurementTechnique\">" + value + "</span>\n");
//				instrumentType	= value;
//				break;

//		
//		if(recordTitle == null)
//			recordTitle	= "NA";
//		
//		String shortName	= recordTitle.split(";")[0].trim();
//		
//		ClickablePreviewImageData clickablePreviewImageData	= StructureToSvgStringGenerator.createClickablePreviewImage(
//				accession, inchi, smiles, tmpFileFolder, tmpUrlFolder,
//				80, 200, 436
//		);
//		String svgMedium	= null;
//		if(clickablePreviewImageData != null)
//			svgMedium	= clickablePreviewImageData.getMediumClickableImage();
//		
//		// meta data
//		String[] compoundClasses	= compoundClass.split("; ");
//		String compoundClass2	= compoundClasses[0];
//		if(compoundClass2.equals("NA") && compoundClasses.length > 1)	compoundClass2	= compoundClasses[1];
//		
//		String description	= 
//				"This MassBank Record with Accession " + accession + 
//				" contains the " + msType + " mass spectrum" + 
//				" of '" + name + "'" +
//				(inchiKey			!= null		? " with the InChIKey '" + inchiKey + "'"					: "") + 
//				(!compoundClass2.equals("N/A")	? " with the compound class '" + compoundClass2 + "'"	: "") + 
//				"." +
//				" The mass spectrum was acquired on a " + instrumentType + 
//				//" with " + (ionization != null	? ionization											: "") + 
//				" with " + ionMode + " ionisation" +
//				(fragmentation		!= null		? " using " + fragmentation + " fragmentation"			: "") +
//				(collisionEnergy	!= null		? " with the collision energy '" + collisionEnergy + "'": "") + 
//				(collisionEnergy	!= null		? " at a resolution of " + resolution					: "") + 
//				(splash				!= null		? " and has the SPLASH '" + splash + "'"				: "") +
//				".";
//		
//		// record
//		String recordString	= sb.toString();
		
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// preprocess request
		Record record = null;
		try {
			// get parameters
			String accession = null;
			
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				String val = (String) request.getParameter( key );
				switch(key){
					case "id": accession = val; break;
					default: logger.warn("unused argument " + key + "=" + val);
				}
			}
			
			// error handling
			if("".equals(accession)) accession = null;
			if(accession == null){
				String errormsg ="missing argument 'id'.";
				logger.error(errormsg);
				String redirectUrl	= "/NoRecordPage" + "?error=" + errormsg;
				response.sendRedirect(request.getContextPath()+redirectUrl);
				return;
			}
			
			// old ACCESSION resolved to new ACCESSION
			String resolvedAccession=OldAccessionResolver.get().resolve(accession);
			if (!accession.equals(resolvedAccession)) {
				response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
				response.setHeader("Location", request.getRequestURL().append("?id=").append(resolvedAccession).toString());
				return;
			}
			

			// load record for display
			DatabaseManager dbMan	= new DatabaseManager("MassBank");
			record	= dbMan.getAccessionData(accession);
			dbMan.closeConnection();
			if(record == null) {
				String errormsg	= "retrieval of '" + accession + "' from database failed";
				logger.error(errormsg);
				String redirectUrl	= "/NoRecordPage" + "?id=" + accession + "&error=" + errormsg;
				response.sendRedirect(request.getContextPath()+redirectUrl);
				return;
			}
			
			if (record.DEPRECATED()) {
				logger.trace("Show deprecated record " + accession + ".");
				String shortname = "DEPRECATED RECORD " + accession;
				request.setAttribute("short_name", shortname);
				request.setAttribute("description", shortname);
				
				request.setAttribute("accession", accession);
				request.setAttribute("isDeprecated", true);
				request.setAttribute("record_title", accession + " has been deprecated.");	
				request.setAttribute("recordstring", "<pre>\nACCESSION: "+ accession + "\nDEPRECATED: "+ record.DEPRECATED_CONTENT() + "\n<pre>");
				request.setAttribute("author","MassBank");
				
			} else {
				logger.trace("Show record "+accession+".");
				String shortname = record.RECORD_TITLE().get(0)+ " Mass Spectrum";
				request.setAttribute("short_name", shortname);
				// find InChIKey in CH_LINK
				String inchikey = record.CH_LINK().get("INCHIKEY");
				String description	= 
						"This MassBank Record with Accession " + accession + 
						" contains the " + record.AC_MASS_SPECTROMETRY_MS_TYPE() + " mass spectrum" + 
						" of '" + record.RECORD_TITLE().get(0) + "'" +
						(inchikey != null ? " with the InChIKey '" + inchikey + "'" : "") + 
						//(!compoundClass2.equals("N/A")	? " with the compound class '" + compoundClass2 + "'"	: "") + 
						//"." +
						//" The mass spectrum was acquired on a " + instrumentType + 
						//" with " + (ionization != null	? ionization											: "") + 
						//" with " + ionMode + " ionisation" +
						//(fragmentation		!= null		? " using " + fragmentation + " fragmentation"			: "") +
						//(collisionEnergy	!= null		? " with the collision energy '" + collisionEnergy + "'": "") + 
						//(collisionEnergy	!= null		? " at a resolution of " + resolution					: "") + 
						//(splash				!= null		? " and has the SPLASH '" + splash + "'"				: "") +
						".";
				request.setAttribute("description", description);

				String keywords =
					accession + ", " 
					+ shortname +", "
					+ (inchikey != null ? inchikey + ", " : "")
				    + "mass spectrum, MassBank record, mass spectrometry, mass spectral library";
				request.setAttribute("keywords", keywords);
				String author = record.AUTHORS();
				request.setAttribute("author", author);				
				
				String recordstring = record.createRecordString();
				String structureddata = record.createStructuredData();
				IAtomContainer mol = record.CH_SMILES_obj();
				String svg = new DepictionGenerator().withAtomColors().withMolTitle().withTitleColor(Color.black).depict(mol).toSvgStr(Depiction.UNITS_PX);				
				
				//adjust svg to fit nicely in RecordDisplay page
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(false);
				factory.setValidating(false);
				factory.setFeature("http://xml.org/sax/features/namespaces", false);
				factory.setFeature("http://xml.org/sax/features/validation", false);
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				Document svgDoc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(svg)));
				Element svgNode = (Element) svgDoc.getElementsByTagName("svg").item(0);
				NamedNodeMap attr = svgNode.getAttributes();
				attr.getNamedItem("width").setTextContent("100%");
				attr.getNamedItem("height").setTextContent("200px");
				svgNode.setAttribute("preserveAspectRatio", "xMinYMin meet");
				StringWriter writer = new StringWriter();
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(svgDoc), new StreamResult(writer));
				svg = writer.getBuffer().toString();   

				request.setAttribute("peaklist", record.createPeakListData());
				request.setAttribute("accession", accession);
		        
		        request.setAttribute("record_title", record.RECORD_TITLE1());	        		
				
				request.setAttribute("recordstring", recordstring);
		        request.setAttribute("structureddata", structureddata);
		        request.setAttribute("svg", svg);
			}
	        request.getRequestDispatcher("/RecordDisplay.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Cannot load record", e);
        }
     }

}
