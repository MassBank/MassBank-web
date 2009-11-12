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
 * Browse Page表示用モジュール
 *
 * ver 1.0.10 2009.10.29
 *
 ******************************************************************************/
%>

<%@ page import="java.util.*,java.io.*" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.ServerStatus" %>
<%
	String site = "0";
	if ( request.getParameter( "site" ) != null ) {
		site = request.getParameter( "site" );
	}
	//-------------------------------------------
	// 環境設定ファイルからURLリストを取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	int pos = path.indexOf("/jsp");
	String baseUrl = path.substring( 0, pos+1 ) ;
	GetConfig conf = new GetConfig(baseUrl);
	
	// サイト名取得
	String [] siteLongName = conf.getSiteLongName();
	// BrowseMode取得
	String [] browseMode = conf.getBrowseMode();
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta http-equiv="Content-Style-Type" content="text/css">
		<meta http-equiv="Content-Script-Type" content="text/javascript">
		<meta name="description" content="Hierarchical browsing of all data">
		<meta name="keywords" content="Browse">
		<meta name="revisit_after" content="30 days">
		<link rel="stylesheet" type="text/css" href="css/Common.css">
		<title>MassBank | Database | Browse Page</title>
	</head>
	<body class="msbkFont">
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td>
					<h1>Browse Page</h1>
				</td>
				<td align="right" class="font12px">
					<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="http://www.massbank.jp/manuals/browse_doc.html" target="_blank">user manual (in Japanese)</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				</td>
			</tr>
		</table>
		<iframe src="menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
		<hr size="1">

		<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
		<jsp:include page="../pserver/ServerInfo.jsp" />

		<form method="post" action="BrowsePage.html" name="form1" class="formStyle">
			<table border="0" cellpadding="5" cellspacing="0" class="boxA">
				<tr>

					<td>
						<table border="0" cellpadding="1" cellspacing="6" height="600" width="250">
<%
	String[] urlList    = conf.getSiteUrl();
	String[] dbNameList = conf.getDbName();
	ServerStatus svrStatus = new ServerStatus(baseUrl);
	for ( int i = 0; i < siteLongName.length; i++ ) {
		// 連携サーバ障害有無チェック(サーバ監視が行われていなければ無条件にTrueが返ってくる)
		if ( !svrStatus.isServerActive(urlList[i], dbNameList[i]) ) {
			continue;
		}

		out.println( "\t\t\t\t\t\t\t<tr>" );
		out.print( "\t\t\t\t\t\t\t\t<td valign=\"top\" width=\"10\">" );
		out.print( "<input type=\"radio\" name=\"site\" value=\""
					+ String.valueOf(i) + "\" onClick=\"document.form1.submit();\"" );
		if ( i == Integer.parseInt(site) ) {
			out.print( " checked" );
		}
		out.print( ">" );
		out.println( "</td>" );
		
		if (i+1 != siteLongName.length) {
			out.print( "\t\t\t\t\t\t\t\t<td align=\"left\" valign=\"top\" height=\"26\">" );
		}
		else {
			out.print( "\t\t\t\t\t\t\t\t<td align=\"left\" valign=\"top\" height=\"\">" );
		}
		out.print( "<b>" + siteLongName[i] + "</b>" );
		out.println( "</td>" );
		out.println( "\t\t\t\t\t\t\t</tr>" );
	}
	out.print( "\t\t\t\t\t\t" );
%>
						</table>
					</td>
					
					<td>
						<table border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td>
									<applet code="BrowsePage.class" archive="applet/Browse2.jar" width="450" height="600">
<%
	out.println( "\t\t\t\t\t\t\t\t\t\t<param name=\"site\" value=\"" + site + "\">" );
	out.println( "\t\t\t\t\t\t\t\t\t\t<param name=\"mode\" value=\"" + browseMode[Integer.parseInt(site)] + "\">" );
	out.print( "\t\t\t\t\t\t\t\t\t" );
%>
									</applet>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<hr size="1">
			<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
		</form>
	</body>
</html>
