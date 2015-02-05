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
 * 連携サーバの状態表示および監視・非監視を制御する
 *   (慶応サーバのみ存在する)
 *
 * ver 1.0.0 2009.01.26
 *
 ******************************************************************************/
%>
<%@ page import="java.net.URL, java.net.URLConnection" %>
<%@ page import="java.io.BufferedReader, java.io.InputStreamReader" %>
<%@ page import="java.text.SimpleDateFormat" %> 
<%@ page import="java.util.Date" %>
<%@ page import="massbank.ServerStatus" %>
<%@ page import="massbank.ServerStatusInfo" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<link rel="stylesheet" type="text/css" href="./pserver.css">
<title>Server Monitor</title>
<meta http-equiv="refresh" content="10">
</head>
<body">
<h2>Server Monitor</h2>
<%
	// リクエストパラメータ取得
	String act = "";
	if ( request.getParameter("act") != null ) {
		act = request.getParameter("act");
	}

	String reqUrl = request.getRequestURL().toString();
	int pos = reqUrl.indexOf("/pserver");
	// JSPファイル名をセット
	String jspName = reqUrl.substring( reqUrl.lastIndexOf("/") + 1 );
	// ベースURLセット
	String baseUrl = reqUrl.substring( 0, pos + 1 );

	// 監視・非監視制御
	if ( act.equals("Managed") || act.equals("Unmanaged") ) {
		// サーブレットを実行
		URL url = new URL( baseUrl + "ServerMonitor?act=" + act );
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		in.close();
	}

	ServerStatus svrStatus = new ServerStatus(baseUrl);

	// ポーリング周期を取得
	int pollInterval = svrStatus.getPollInterval();

	// 監視状態を取得
	boolean isManaged = svrStatus.isManaged();
	String	manageState = "<font color=\"green\"><b>Managed</b></font>";
	String	buttonString = "Unmanaged";
	if ( !isManaged ) {
		manageState = "<font color=\"red\"><b>Unmanaged</b></font>";
		buttonString = "Managed";
	}
	// 監視状態、ポーリング周期を表示
	out.println( "<form name=\"form\" action=\"" + jspName + "\" method=\"post\">" );
	out.println( "<b>Management State&nbsp;:&nbsp;</b>" + manageState );
	out.println( "&nbsp;&nbsp;<input type=\"submit\" name=\"act\" value=\"" + buttonString + "\"><br>" );
	out.println( "<b>Polling Interval&nbsp;:&nbsp;" + pollInterval + "sec.</b>");
	out.println( "</form>" );

	// 監視対象のサーバがある場合
	if ( isManaged && svrStatus.getServerNum() > 0 ) {
		ServerStatusInfo[] info = svrStatus.getStatusInfo();

		// 日時表示
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
		String time = sdf.format( new Date() );
%>
<table>
<tr>
<td width="750" align="right">
<% out.println(time); %>
</td>
</tr>
</table>
<table class="tbTitle">
<tr>
<td width="200"><b>Site</b></td>
<td width="400"><b>URL</b></td>
<td width="100" align="center"><b>Status</b></td>
</tr>
</table>
<%
		for ( int i = 0; i < info.length; i++ ) {
			String className = "tb1";
			String status = "active";
			String bgColor = "GreenYellow";
			if ( i % 2 == 0 ) {
				className = "tb2";
			}
			if ( !info[i].getStatus() ) {
				status = "inactive";
				bgColor = "red";
			}
			out.println( "<table class=\"" + className + "\">" );
			out.println( "<tr>" );
			out.println( "<td width=\"200\"><b>" + info[i].getServerName() + "</b></td>" );
			out.println( "<td width=\"400\">" + info[i].getUrl() + "</td>" );
			out.println( "<td width=\"100\" bgcolor=\"" + bgColor + "\" align=\"center\"><b>" + status + "</b></td>" );
			out.println( "</tr>" );
			out.println( "</table>" );
		}
	}
%>
</body>
</html>
