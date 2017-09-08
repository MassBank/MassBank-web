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
 * ver 1.0.8 2010.12.24
 *
 ******************************************************************************/
%>

<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankCommon" %>
<%
	String path = request.getRequestURL().toString();
	String baseUrl = path.substring( 0, ( path.indexOf("/jsp") + 1 ) );
	GetConfig conf = new GetConfig( baseUrl );
	String [] siteName = conf.getSiteName();
	
	int mySiteNo = 0;
	if ( request.getParameter( "site" ) != null ) {
		mySiteNo = Integer.parseInt(request.getParameter( "site" ));
	}
	String compoundName = "";
	if ( request.getParameter( "cname" ) != null ) {
		compoundName = request.getParameter( "cname" );
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
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) since 2006 JST-BIRD MassBank" />
	<script type="text/javascript" src="./script/Common.js"></script>
	<title><%=compoundName%></title>
	<script type="text/javascript" src="./script/Piwik.js"></script>
	</head>
<body style="margin-top:2px;">
	<table  cellspacing="0" cellpadding="0" border="0">
		<tr>
			<td><font style="font-size:9pt;font-family:Arial;color:navy"><b><%=compoundName%></b></font></td>
		</tr>
		<tr>
			<td>
<%
	String serverUrl = conf.getServerUrl();
	String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_MOL];
	String param = "&query=" + URLEncoder.encode(compoundName , "UTF-8") + "&qtype=n&otype=q";
	MassBankCommon mbcommon = new MassBankCommon();
	ArrayList<String> result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );
	
	// 結果退避
	String[] gifList = new String[siteName.length];
	String[] molList = new String[siteName.length];
	for ( String line : result ) {
		String[] info = (String[])line.split("\t");
		// 簡易レスポンスチェック
		if ( info.length != 3 ) {
			continue;
		}
		if ( !info[0].equals("N/A") ) {
			gifList[Integer.parseInt(info[2])] = info[0];
		}
		if ( !info[1].equals("N/A") ) {
			molList[Integer.parseInt(info[2])] = info[1];
		}
	}
	
	// 表示に必要な情報
	String gifInfoUrl = null;
	String molInfoSite = String.valueOf(mySiteNo);
	boolean isFind = false;
	
	// 表示する構造式の取得処理
	// 優先順位は、自サイトGif＞自サイトMolfile＞他サイトGif＞他サイトMolfile
	// ※他サイトはサイト番号の若い順で優先
	int targetSiteNo = mySiteNo;
	String gifFileName = gifList[targetSiteNo];
	String molFileName = molList[targetSiteNo];
	if ( gifFileName != null ) {
		String dbName = conf.getDbName()[targetSiteNo];
		String siteUrl = (targetSiteNo == GetConfig.MYSVR_INFO_NUM) ? conf.getServerUrl() : conf.getSiteUrl()[targetSiteNo];
		gifInfoUrl = (new URL(siteUrl + "DB/gif/" + dbName + "/" + gifFileName )).toString();
		isFind = true;
	}
	else if ( molFileName != null ) {
		molInfoSite = String.valueOf(targetSiteNo);
		isFind = true;
	}
	else {
		for ( int i=0; i<siteName.length; i++) {
			targetSiteNo = i;
			gifFileName = gifList[targetSiteNo];
			molFileName = molList[targetSiteNo];
			
			if ( mySiteNo == targetSiteNo ) {
				continue;
			}
			
			if ( gifFileName != null ) {
				String dbName = conf.getDbName()[targetSiteNo];
				String siteUrl = (targetSiteNo == GetConfig.MYSVR_INFO_NUM) ? conf.getServerUrl() : conf.getSiteUrl()[targetSiteNo];
				gifInfoUrl = (new URL(siteUrl + "DB/gif/" + dbName + "/" + gifFileName )).toString();
				isFind = true;
				break;
			}
			else if ( molFileName != null ) {
				molInfoSite = String.valueOf(targetSiteNo);
				isFind = true;
				break;
			}
		}
	}
	
	// 表示処理
	if ( isFind ) {
		if ( gifInfoUrl != null ) {
			out.println( "\t\t\t\t<table cellspacing=\"10\" cellpadding=\"0\">" );
			out.println( "\t\t\t\t\t<tr>" );
			out.println( "\t\t\t\t\t\t<td>" );
			out.println( "\t\t\t\t\t\t\t<img src=\"" + gifInfoUrl + "\" alt=\"\" border=\"1\" width=\"180\" height=\"180\" onClick=\"expandMolView('" + gifInfoUrl + "')\">" );
			out.println( "\t\t\t\t\t\t</td>" );
			out.println( "\t\t\t\t\t</tr>" );
			out.println( "\t\t\t\t</table>" );
		}
		else {
			out.println( "\t\t\t\t<applet code=\"MolView.class\" archive=\"" + baseUrl + "applet/MolView.jar\" width=\"200\" height=\"200\">" );
			out.println( "\t\t\t\t\t<param name=\"site\" value=\"" + molInfoSite + "\">" );
			out.println( "\t\t\t\t\t<param name=\"compound_name\" value=\"" + compoundName + "\">" );
			out.println( "\t\t\t\t</applet>" );
		}
	}
	else {
		out.println( "\t\t\t\t<table cellspacing=\"0\" cellpadding=\"0\">" );
		out.println( "\t\t\t\t\t<tr>" );
		out.println( "\t\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t\t<img src=\"../image/not_available.gif\" alt=\"\" border=\"0\">" );
		out.println( "\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t</tr>" );
		out.println( "\t\t\t\t</table>" );
	}
%>
			</td>
		</tr>
	</table>
</body>
</html>
