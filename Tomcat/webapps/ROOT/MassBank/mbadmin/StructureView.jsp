<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
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
 * 構造式表示用モジュール
 *
 * ver 1.0.1 2010.10.01
 *
 ******************************************************************************/
%>

<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankCommon" %>

<%!
	/**
	 * 構造式画像URLを取得する
	 * 必ず自サーバにある構造式情報を取得する
	 * @param dbName
	 * @param name
	 * @param serverUrl
	 * @param dbNameList
	 * @return List<String> 画像URLを格納したLIST
	 */
	private List<String> getStructure(String dbName, String name, String serverUrl, String[] dbNameList) {
		List<String> resultList = new ArrayList<String>(4);
		
		String param = "";
		if ( !name.equals("") ) {
			try {
				param = "&names=" + URLEncoder.encode( name, "utf-8" );
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETSTRUCT];
		ArrayList<String> result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );
		
		int targetSiteNo = -1;
		for(int i=0; i<dbNameList.length; i++) {
			if (dbNameList[i].equals(dbName)) {
				targetSiteNo = i;
				break;
			}
		}
		
		String gifUrl = "";
		String gifSmallUrl = "";
		String gifLargeUrl = "";
		String molData = "";
		boolean isFind = false;
		for ( int i = 0; i < result.size(); i++ ) {
			String temp = (String)result.get(i);
			String[] item = temp.split("\t");
			String line = item[0];
			int relSiteNo = Integer.parseInt(item[item.length -1]);
			if (targetSiteNo != relSiteNo) {
				continue;
			}
			
			if ( line.indexOf("---NAME:") >= 0 ) {
				if ( !isFind ) {
					isFind = true;
				}
				else {
					break;
				}
			}
			else if ( line.indexOf("---GIF:") != -1 ) {
				String gifFile = line.replaceAll("---GIF:", "");
				if ( !gifFile.equals("") ) {
					gifUrl = serverUrl + "DB/gif/" + dbNameList[targetSiteNo] + "/" + gifFile;
				}
			}
			else if ( line.indexOf("---GIF_SMALL:") != -1 ) {
				String gifFile = line.replaceAll("---GIF_SMALL:", "");
				if ( !gifFile.equals("") ) {
					gifSmallUrl = serverUrl + "DB/gif_small/" + dbNameList[targetSiteNo] + "/" + gifFile;
				}
			}
			else if ( line.indexOf("---GIF_LARGE:") != -1 ) {
				String gifFile = line.replaceAll("---GIF_LARGE:", "");
				if ( !gifFile.equals("") ) {
					gifLargeUrl = serverUrl + "DB/gif_large/" + dbNameList[targetSiteNo] + "/" + gifFile;
				}
			}
			else {
				if ( line.indexOf("M  CHG") >= 0 ) {
					continue;
				}
				molData += line + "|\n";
			}
		}
		resultList.add(0, gifUrl);
		resultList.add(1, gifSmallUrl);
		resultList.add(2, gifLargeUrl);
		resultList.add(3, molData);
		
		return resultList;
	}
%>
<%
	String path = request.getRequestURL().toString();
	String baseUrl = path.substring( 0, ( path.indexOf("/mbadmin") + 1 ) );
	GetConfig conf = new GetConfig( baseUrl );
	String serverUrl = conf.getServerUrl();
	String[] dbNameList = conf.getDbName();
	
	String reqDbName = "";
	if ( request.getParameter( "dname" ) != null ) {
		reqDbName = request.getParameter( "dname" );
	}
	
	String reqCompoundName = "";
	if ( request.getParameter( "cname" ) != null ) {
		reqCompoundName = request.getParameter( "cname" );
	}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Cache-Control" content="no-cache">
	<meta equiv="expires" content="0">
	<script type="text/javascript" src="../script/Common.js"></script>
	<title><%=reqCompoundName%></title>
</head>
<body style="margin-top:2px;">
	<table  cellspacing="0" cellpadding="0" border="0">
		<tr>
			<td><font style="font-size:9pt;font-family:Arial;color:navy"><b><%=reqCompoundName%></b></font></td>
		</tr>
		<tr>
			<td>
<%
	// 化学構造式表示情報を取得する
	List<String> structureResult = getStructure(reqDbName, reqCompoundName, serverUrl, dbNameList);
	String gifUrl = structureResult.get(0);
	String gifSmallUrl = structureResult.get(1);
	String gifLargeUrl = structureResult.get(2);
	String molData = structureResult.get(3);
	
	// 表示処理
	if ( !gifUrl.equals("") ) {
		if ( gifLargeUrl.equals("") ) {
			gifLargeUrl = "../image/not_available_l.gif";
		}
		out.println( "\t\t\t\t<table cellspacing=\"10\" cellpadding=\"0\">" );
		out.println( "\t\t\t\t\t<tr>" );
		out.println( "\t\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t\t<img src=\"" + gifUrl + "\" alt=\"\" border=\"1\" width=\"180\" height=\"180\" onClick=\"expandMolView('" + gifLargeUrl + "')\">" );
		out.println( "\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t</tr>" );
		out.println( "\t\t\t\t</table>" );
	}
	else {
		out.println( "\t\t\t\t<applet code=\"MolView.class\" archive=\"../applet/MolView.jar\" width=\"200\" height=\"200\">" );
		out.println( "\t\t\t\t\t<param name=\"moldata\" value=\"" + molData + "\">" );
		out.println( "\t\t\t\t</applet>" );
	}
%>
			</td>
		</tr>
	</table>
</body>
</html>
