<%@page import="massbank.DatabaseManager"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 ******************************************************************************/
%>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.Config" %>
<%@ page import="massbank.DatabaseManager"%>
<%@ page import="massbank.FileUtil" %>
<%@ page import="massbank.Record" %>
<%@ page import="massbank.StructureToSvgStringGenerator" %>
<%@ page import="massbank.StructureToSvgStringGenerator.ClickablePreviewImageData" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%
	// ##################################################################################################
	// get parameters
	// http://localhost/MassBank/jsp/RecordDisplay.jsp?id=XXX00001
	//String accession	= "XXX00001";
	String accession		= null;
	
	Enumeration<String> names = request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String) names.nextElement();
		String val = (String) request.getParameter( key );
		
		//System.out.println(key + "\t" + val);
		
		switch(key){
			case "id":	accession		= val; break;
			default: System.out.println("Warning: Unused argument " + key + "=" + val);
		}
	}
	
	// ##################################################################################################
	// error handling
	if(accession != null && accession.equals(""))
		accession		= null;
	
	if(accession == null){
		String error	= "Error: Missing argument 'id'";
		System.out.println(error);
		String baseUrl	= "";//Config.get().BASE_URL();
		String urlStub	= baseUrl + "NoRecordPage.jsp";
		String redirectUrl	= urlStub + "?id=" + accession + "&error=" + error;
		
		response.sendRedirect(redirectUrl);
		return;
	}
	
	String databaseName = null;
	DatabaseManager dbManager	= new DatabaseManager(Config.get().dbName());
	Record.Contributor contributorObj	= dbManager.getContributorFromAccession(accession);
	Record record = dbManager.getAccessionData(accession);
	dbManager.closeConnection();
	if(contributorObj != null) databaseName	= contributorObj.SHORT_NAME;
	
	if(databaseName == null){
		String error	= "Error: Can not find contributor";
		System.out.println(error);
		String redirectUrl	= "/NoRecordPage" + "?id=" + accession + "&error=" + error;
		response.sendRedirect(request.getContextPath()+redirectUrl);
		return;
	}
	if(!FileUtil.existsFile(databaseName, accession)){
		String error	= "Error: accession '" + accession + "'" + 
							(databaseName != null ? " in database '" + databaseName + "'" : "") + 
							" does not exist.";
		System.out.println(error);
		String redirectUrl	= "/NoRecordPage" + "?id=" + databaseName + "&error=" + error;
		response.sendRedirect(request.getContextPath()+redirectUrl);
		return;
	}
	
	// paths
	String tmpUrlFolder		= Config.get().TOMCAT_TEMP_URL();
	String tmpFileFolder	= Config.get().TOMCAT_TEMP_PATH(getServletContext());
	
	// ##################################################################################################
	// get accession data
	
	// read file
	File file	= FileUtil.getFile(databaseName, accession);
	List<String> list	= FileUtil.readFromFile(file);
	StringBuilder sb	= new StringBuilder();
	String description = null;
	String shortName = null;
	String inchiKey = null;
	String recordTitle = null;
	String svgMedium = null;
	JSONObject peaks = null;
	if (record.DEPRECATED()) {
		sb.append("<pre>\n");
		for(String line : list) sb.append(line+"\n");
		sb.append("</pre>");
		shortName = "DEPRECATED RECORD " + accession;
		recordTitle = accession + " has been deprecated.";
	}
	else {
	
		// process record
		// TODO property="schema:fileFormat"
		// TODO property="schema:isAccessibleForFree"
		// TODO property="schema:keywords"
		// TODO property="schema:publisher"
		final String delimiter	= ": ";
		String inchi			= null;
		String smiles			= null;
		String msType			= null;
		String name				= null;
		String compoundClass	= null;
		String splash			= null;
		String instrumentType	= null;
		String ionization		= null;
		String ionMode			= null;
		String fragmentation	= null;
		String collisionEnergy	= null;
		String resolution		= null;
		
		// CH, AC, MS, PK
		boolean ch	= false;
		boolean ac	= false;
		boolean ms	= false;
		boolean pk	= false;
		int PK_PEAK_idx	= -1;
		
		for(int lineIdx = 0; lineIdx < list.size(); lineIdx++){
			String line	= list.get(lineIdx);
			int delimiterIndex	= line.indexOf(delimiter);
			if(delimiterIndex == -1){
				sb.append(line + "\n");
				continue;
			}
			
			String tag		= line.substring(0, delimiterIndex);
			String value	= line.substring(delimiterIndex + delimiter.length());
			
			// horizontal ruler
			if(tag.startsWith("CH$") && (!ch))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	ch	= true;	}
			if(tag.startsWith("AC$") && (!ac))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	ac	= true;	}
			if(tag.startsWith("MS$") && (!ms))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	ms	= true;	}
			if(tag.startsWith("PK$") && (!pk))	{	sb.append("<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">");	pk	= true;	}
			
			switch(tag){
			case "ACCESSION":
				// TODO schema:identifier
				sb.append(tag + ": " + value + "\n");
				
				break;
			case "RECORD_TITLE":
				// sb.append(tag + ": " + value + "\n");
				sb.append(tag + ": <span property=\"schema:headline\">" + value + "</span>\n");
				recordTitle	= value;
				break;
			case "DATE":
				//sb.append(tag + ": " + value + "\n");
				
				// property="schema:dateCreated"
				// property="schema:dateModified"
				// property="schema:datePublished"
				
				// DATE: 2016.01.19 (Created 2010.10.06, modified 2011.05.11)
				String datePublished	= null;
				String dateCreated		= null;
				String dateModified		= null;
				
				String[] tokens	= value.split(" ");
				datePublished	= tokens[0];
				if(tokens.length > 1){
					tokens	= value.substring(datePublished.length()).replaceAll("\\(", "").replaceAll("\\)", "").trim().split(", ");
					for(int i = 0; i < tokens.length; i++){
						if(tokens[i].startsWith("Created"))		dateCreated		= tokens[i].split(" ")[1];
						if(tokens[i].startsWith("modified"))	dateModified	= tokens[i].split(" ")[1];
					}
				}
				
				sb.append(tag + ": <span property=\"schema:datePublished\">" + datePublished + "</span>");
				if(dateCreated != null || dateModified != null)
					sb.append(" (");
				if(dateCreated != null)
					sb.append("<span property=\"schema:dateCreated\">" + dateCreated + "</span>");
				if(dateModified != null){
					if(dateCreated != null)
						sb.append(", ");
					sb.append("<span property=\"schema:dateModified\">" + dateModified + "</span>");
				}
				if(dateCreated != null || dateModified != null)
					sb.append(")");
				
				sb.append("\n");
				break;
			case "AUTHORS":
// 				//sb.append(tag + ": " + value + "\n");
// 				String[] authorTokens	= value.split(", ");
				
// 				// check which tokens are authors
// 				int lastAuthorIdx	= -1;
// 				for(int i = 0; i < authorTokens.length; i++){
// 					// check if string is author like 'Akimoto AV'
// 					boolean isAuthor	= authorTokens[i].matches("\\w+ \\w+");
// 					if(isAuthor){
// 						// 2nd word is initials?
// 						String[] initials	= authorTokens[i].split(" ")[1].split("");
// 						for(int j = 0; j < initials.length; j++)
// 							if(!Character.isUpperCase(initials[j].toCharArray()[0])){
// 								isAuthor	= false;
// 								break;
// 							}
// 					}
// 					if(isAuthor){
// 						lastAuthorIdx	= i;
// 					}
// 				}
				
// 				// create affiliation
// 				int numberOfAuthors	= lastAuthorIdx + 1;
// 				String affiliation	= String.join(", ", Arrays.copyOfRange(authorTokens, numberOfAuthors, authorTokens.length));
				
// 				// create authors
// 				String[] authors	= new String[numberOfAuthors];
// 				for(int i = 0; i < numberOfAuthors; i++)
// 					authors[i]	= 
// 							"<span property=\"schema:author\" typeof=\"schema:Person\">" +
// 								"<span property=\"schema:name\">" + authorTokens[i] + "</span>" +
// 								//((affiliation.length() > 0) ?"<span property=\"schema:affiliation\" style=\"visibility:hidden\">" + affiliation + "</span>" : "") +
// 								((affiliation.length() > 0) ?"<span property=\"schema:affiliation\" style=\"display:none\">" + affiliation + "</span>" : "") +
// 							"</span>";
				
// 				// paste
// 				sb.append(tag + ": ");
// 				sb.append(String.join(", ", authors));
// 				if(affiliation.length() > 0)
// 					sb.append(", " + affiliation);
// 				sb.append("\n");
				sb.append(tag + ": " + value + "\n");
				break;
			case "LICENSE":
				sb.append(tag + ": " + "<a href=\"https://creativecommons.org/licenses/\" target=\"_blank\" property=\"schema:license\">" + value + "</a>" + "\n");
				break;
			case "COPYRIGHT":
				sb.append(tag + ": " + value + "\n");
				break;
			case "PUBLICATION":
				String regex_pmid	= "PMID:[ ]?\\d{8,8}";
				String regex_doi	= "10\\.\\d{3,9}\\/[\\-\\._;\\(\\)\\/:a-zA-Z0-9]+[a-zA-Z0-9]";
				String regex_doiUrl	= "https?\\:\\/\\/(dx\\.)?doi\\.org\\/" + regex_doi;
				Pattern pattern_pmid	= Pattern.compile(".*" + "(" + regex_pmid	+ ")" + ".*");
			    Matcher matcher_pmid	= pattern_pmid.matcher(value);
			    Pattern pattern_doi		= Pattern.compile(".*" + "(" + regex_doi	+ ")" + ".*");
			    Matcher matcher_doi		= pattern_doi.matcher(value);
			    Pattern pattern_doiUrl	= Pattern.compile(".*" + "(" + regex_doiUrl	+ ")" + ".*");
			    Matcher matcher_doiUrl	= pattern_doiUrl.matcher(value);
			    
			    if(matcher_pmid.matches()){
			    	// link pubmed id
			    	String PMID		= value.substring(matcher_pmid.start(1), matcher_pmid.end(1));
			    	String id		= PMID.substring("PMID:".length()).trim();
			    	value			= value.replaceAll(PMID, "<a href=\"http:\\/\\/www.ncbi.nlm.nih.gov/pubmed/" + id + "?dopt=Citation\" target=\"_blank\">" + PMID + "</a>");
			    }
			    if(matcher_doiUrl.matches()){
			    	// link https://doi.org/<doi> url
			    	String doiUrl	= value.substring(matcher_doiUrl.start(1), matcher_doiUrl.end(1));
			    	value			= value.replaceAll(doiUrl, "<a href=\"" + doiUrl + "\" target=\"_blank\">" + doiUrl + "</a>");
			    } else 
			    if(matcher_doi.matches()){
			    	// link doi
			    	String doi	= value.substring(matcher_doi.start(1), matcher_doi.end(1));
			    	value			= value.replaceAll(doi, "<a href=\"https:\\/\\/doi.org/" + doi + "\" target=\"_blank\">" + doi + "</a>");
			    }
				
			    //sb.append(tag + ": " + value + "\n");
				sb.append(tag + ": <span property=\"schema:citation\" typeof=\"schema:ScholarlyArticle\">" + "<span property=\"schema:name\">" + value + "</span>" + "</span>\n");
				break;
			case "COMMENT":
				// property="schema:text" / property="schema:comment"
				sb.append(tag + ": <span property=\"schema:comment\">" + value + "</span>\n");
				break;
			case "CH$NAME":
				// TODO property="schema:name"
				// TODO property="schema:alternateName"
				sb.append(tag + ": " + value + "\n");
				name	= value;
				//sb.append(tag + ": <span property=\"schema:alternateName\">" + value + "</span>\n");
				break;
			case "CH$COMPOUND_CLASS":
				sb.append(tag + ": " + value + "\n");
				compoundClass	= value;
				break;
			case "CH$FORMULA":
				sb.append(tag + ": " + "<a href=\"http://www.chemspider.com/Search.aspx?q=" + value + "\" target=\"_blank\">" + value + "</a>" + "\n");
				break;
			// CH$EXACT_MASS
			case "CH$SMILES":
				sb.append(tag + ": " + value + "\n");
				smiles	= value;
				break;
			case "CH$IUPAC":
				sb.append(tag + ": " + value + "\n");
				inchi	= value;
				break;
			case "CH$CDK_DEPICT_SMILES":
			case "CH$CDK_DEPICT_GENERIC_SMILES":
			case "CH$CDK_DEPICT_STRUCTURE_SMILES":
				ClickablePreviewImageData clickablePreviewImageData2	= StructureToSvgStringGenerator.createClickablePreviewImage(
						tag, null, value,
						tmpFileFolder, tmpUrlFolder,
						80, 200, 436
				);
				if(clickablePreviewImageData2 != null)
					sb.append(tag + ": " + clickablePreviewImageData2.getMediumClickablePreviewLink("CH$CDK_DEPICT_SMILES", value));
				else
					sb.append(tag + ": " + value + "\n");
				break;
			case "CH$LINK":
				String delimiter2	= " ";
				int delimiterIndex2	= value.indexOf(delimiter2);
				
				if(delimiterIndex2 == -1){
					sb.append(tag + ": " + value + "\n");
					break;
				}
				
				String CH$LINK_NAME	= value.substring(0, delimiterIndex2);
				String CH$LINK_ID	= value.substring(delimiterIndex2 + delimiter2.length());
				
				if(CH$LINK_NAME == "INCHIKEY")	inchiKey	= CH$LINK_ID;
				
				switch(CH$LINK_NAME){
					case "CAS":								CH$LINK_ID	= "<a href=\"https://www.google.com/search?q=&quot;" + CH$LINK_ID + "&quot;"									+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "CAYMAN":                      	CH$LINK_ID	= "<a href=\"https://www.caymanchem.com/app/template/Product.vm/catalog/" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "CHEBI":                       	CH$LINK_ID	= "<a href=\"https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:" + CH$LINK_ID								+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "CHEMPDB":                     	CH$LINK_ID	= "<a href=\"https://www.ebi.ac.uk/msd-srv/chempdb/cgi-bin/cgi.pl?FUNCTION=getByCode&amp;CODE=" + CH$LINK_ID	+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "CHEMSPIDER":                  	CH$LINK_ID	= "<a href=\"https://www.chemspider.com/" + CH$LINK_ID															+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "COMPTOX": 	                	CH$LINK_ID	= "<a href=\"https://comptox.epa.gov/dashboard/dsstoxdb/results?search=" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "FLAVONOIDVIEWER":             	CH$LINK_ID	= "<a href=\"http://www.metabolome.jp/software/FlavonoidViewer/"												+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "HMDB":                        	CH$LINK_ID	= "<a href=\"http://www.hmdb.ca/metabolites/" + CH$LINK_ID														+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "INCHIKEY":                    	CH$LINK_ID	= "<a href=\"https://www.google.com/search?q=&quot;" + CH$LINK_ID + "&quot;"									+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "KAPPAVIEW":                   	CH$LINK_ID	= "<a href=\"http://kpv.kazusa.or.jp/kpv4/compoundInformation/view.action?id=" + CH$LINK_ID						+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "KEGG":                        	CH$LINK_ID	= "<a href=\"http://www.genome.jp/dbget-bin/www_bget?cpd:" + CH$LINK_ID											+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "KNAPSACK":                    	CH$LINK_ID	= "<a href=\"http://kanaya.naist.jp/knapsack_jsp/info.jsp?sname=C_ID&word=" + CH$LINK_ID						+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "LIPIDBANK":                   	CH$LINK_ID	= "<a href=\"http://lipidbank.jp/cgi-bin/detail.cgi?id=" + CH$LINK_ID											+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "LIPIDMAPS":                   	CH$LINK_ID	= "<a href=\"http://www.lipidmaps.org/data/get_lm_lipids_dbgif.php?LM_ID=" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "NIKKAJI":                     	CH$LINK_ID	= "<a href=\"https://jglobal.jst.go.jp/en/redirect?Nikkaji_No=" + CH$LINK_ID + "&CONTENT=syosai"		+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "OligosaccharideDataBase":     	CH$LINK_ID	= "<a href=\"http://www.fukuyama-u.ac.jp/life/bio/biochem/" + CH$LINK_ID + ".html" + CH$LINK_ID					+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "OligosaccharideDataBase2D":   	CH$LINK_ID	= "<a href=\"http://www.fukuyama-u.ac.jp/life/bio/biochem/" + CH$LINK_ID + ".html"								+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "NCBI-TAXONOMY":               	CH$LINK_ID	= "<a href=\"https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + CH$LINK_ID							+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>"; break;
					case "PUBCHEM":{
						if(CH$LINK_ID.startsWith("CID:"))	CH$LINK_ID	= "<a href=\"https://pubchem.ncbi.nlm.nih.gov/compound/" + CH$LINK_ID.substring("CID:".length())				+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>";
						if(CH$LINK_ID.startsWith("SID:"))	CH$LINK_ID	= "<a href=\"https://pubchem.ncbi.nlm.nih.gov/substance/" + CH$LINK_ID.substring("SID:".length())				+ "\" target=\"_blank\">" + CH$LINK_ID + "</a>";
						break;
					}
				}
				
				sb.append(tag + ": " + CH$LINK_NAME + delimiter2 + CH$LINK_ID + "\n");
				break;
			// AC$INSTRUMENT
			case "AC$INSTRUMENT_TYPE":
				// TODO property="schema:measurementTechnique"
				sb.append(tag + ": <span property=\"schema:measurementTechnique\">" + value + "</span>\n");
				instrumentType	= value;
				break;
			case "AC$MASS_SPECTROMETRY":
				sb.append(tag + ": " + value + "\n");
				
				String[] tokens2	= value.split(" ");
				String subTag	= tokens2[0];
				switch(subTag){
					case "MS_TYPE":
						msType			= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
						break;
					case "IONIZATION":
						ionization		= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
						break;
					case "ION_MODE":
						ionMode			= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
						break;
					case "FRAGMENTATION_MODE":
						fragmentation	= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
						break;
					case "COLLISION_ENERGY":
						collisionEnergy	= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
						break;
					case "RESOLUTION":
						resolution		= String.join(" ", Arrays.copyOfRange(tokens2, 1, tokens2.length));
						break;
				}
				
				break;
			// AC$CHROMATOGRAPHY
			// MS$FOCUSED_ION
			// MS$DATA_PROCESSING
			case "PK$SPLASH":
				sb.append(tag + ": " + "<a href=\"http://www.google.com/search?q=" + value + "\" target=\"_blank\">" + value + "</a>" + "\n"); // https://www.google.com/search?q=&quot;%s&quot;
				splash	= value;
				break;
			// PK$NUM_PEAK
			case "PK$PEAK":{
				PK_PEAK_idx	= lineIdx;
				sb.append(line + "\n");
				break;
			}
			default:
				sb.append(line + "\n");
				break;
			}
		}
		
		// get peaks for specktackle
		peaks = new JSONObject();
		JSONArray peaklist = new JSONArray();
		for(int lineIdx = PK_PEAK_idx + 1; lineIdx < list.size() - 1; lineIdx++){
			String[] tokens	= list.get(lineIdx).trim().split(" ");
			peaklist.put(new JSONObject().put("mz", Double.parseDouble(tokens[0])).put("intensity", Double.parseDouble(tokens[2])));
		}
		peaks.put("peaks", peaklist);
		//System.out.println(peaks.toString());

		
		if(recordTitle == null)
			recordTitle	= "NA";
		
		shortName	= recordTitle.split(";")[0].trim() + " Mass Spectrum";
		
		ClickablePreviewImageData clickablePreviewImageData	= StructureToSvgStringGenerator.createClickablePreviewImage(
				accession, inchi, smiles, tmpFileFolder, tmpUrlFolder,
				80, 200, 436
		);
		
		if(clickablePreviewImageData != null)
			svgMedium	= clickablePreviewImageData.getMediumClickableImage();
		
		// meta data
		String[] compoundClasses	= compoundClass.split("; ");
		String compoundClass2	= compoundClasses[0];
		if(compoundClass2.equals("NA") && compoundClasses.length > 1)	compoundClass2	= compoundClasses[1];
		
		description	= 
				"This MassBank Record with Accession " + accession + 
				" contains the " + msType + " mass spectrum" + 
				" of '" + name + "'" +
				(inchiKey			!= null		? " with the InChIKey '" + inchiKey + "'"					: "") + 
				(!compoundClass2.equals("N/A")	? " with the compound class '" + compoundClass2 + "'"	: "") + 
				"." +
				" The mass spectrum was acquired on a " + instrumentType + 
				//" with " + (ionization != null	? ionization											: "") + 
				" with " + ionMode + " ionisation" +
				(fragmentation		!= null		? " using " + fragmentation + " fragmentation"			: "") +
				(collisionEnergy	!= null		? " with the collision energy '" + collisionEnergy + "'": "") + 
				(collisionEnergy	!= null		? " at a resolution of " + resolution					: "") + 
				(splash				!= null		? " and has the SPLASH '" + splash + "'"				: "") +
				".";
		
		// record
	}
	String recordString	= sb.toString();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta name="author" content="MassBank" />
		<meta name="coverage" content="worldwide" />
		<meta name="Targeted Geographic Area" content="worldwide" />
		<meta name="rating" content="general" />
		<meta name="copyright" content="Copyright (c) 2006 MassBank Project and (c) 2011 NORMAN Association" />
		<meta name="description" content="<%=description%>">
		<meta name="keywords" content="<%=accession%>, <%=shortName%>, <%=inchiKey%>, mass spectrum, MassBank record, mass spectrometry, mass spectral library">
		<meta name="revisit_after" content="30 days">
		<meta name="hreflang" content="en">
		<meta name="variableMeasured" content="m/z">
		<meta http-equiv="Content-Style-Type" content="text/css">
		<meta http-equiv="Content-Script-Type" content="text/javascript">
		<link rel="stylesheet" type="text/css" href="css.old/Common.css">
		<script type="text/javascript" src="script/Common.js"></script>
		<!-- SpeckTackle dependencies-->
		<script type="text/javascript" src="js/jquery-3.4.1.min.js" ></script>
		<script type="text/javascript" src="js/d3.v3.min.js"></script>
		<!-- SpeckTackle library-->
		<script type="text/javascript" src="js/st.js" charset="utf-8"></script>
		<!-- SpeckTackle style sheet-->
		<link rel="stylesheet" href="css/st.css" type="text/css" />
		<!-- SpeckTackle MassBank loading script-->
		<script type="text/javascript" src="js/massbank_specktackle.js"></script>
		<title><%=shortName%></title>
	</head>
	<body style="font-family:Times;" typeof="schema:WebPage">
	<main context="http://schema.org" property="schema:about" resource="https://massbank.eu/MassBank/RecordDisplay.jsp?id=<%=accession%>&dsn=<%=databaseName%>" typeof="schema:Dataset" >
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td>
					<h1>MassBank Record: <%=accession%> </h1>
				</td>
			</tr>
		</table>
		<jsp:include page="menu.html"/>
		<hr size="1">
		<br>
		<font size="+1" style="background-color:LightCyan"><%=recordTitle%></font>
		
		<%if (!record.DEPRECATED()) {%>
		<script type="text/javascript">
			var data=<%out.println(peaks.toString());%>;
			console.log(data);
			
		</script>
		<hr size="1">
		<table>
			<tr>
				<td valign="top">
					<font style="font-size:10pt;" color="dimgray">Mass Spectrum</font>
					<br>
					<div id="spectrum_canvas" style="height: 200px; width: 750px; background-color: white"></div>
				</td>
				<td valign="top">
					<font style="font-size:10pt;" color="dimgray">Chemical Structure</font><br>
					<% // display svg
					if(svgMedium != null){
						// paste small image to web site %>
						<%=svgMedium%><%
					} else {
						// no structure there or svg generation failed%>
						<img src="image/not_available_s.gif" width="200" height="200" style="margin:0px;">
					<%}%></td>
			</tr>
			<tr>
			<td><a href="https://metabolomics-usi.ucsd.edu/spectrum/?usi=mzspec:MASSBANK:<%=accession%>" target=”_blank”>metabolomics-usi visualisation</a></td>
			</tr>
		</table>
		<%}%>		
<hr size="1">
<pre style="font-family:Courier New;font-size:10pt">
<%=recordString%>
</pre>
		<hr size=1>
		<jsp:include page="copyrightline.html"/>
	</main>
	</body>
</html>
