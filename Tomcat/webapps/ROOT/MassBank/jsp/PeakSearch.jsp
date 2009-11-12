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
 * Peak Search Page表示用モジュール
 *
 * ver 1.0.7 2009.10.29
 *
 ******************************************************************************/
%>
<%@ page import="java.util.*" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetInstInfo" %>
<%
	//-------------------------------------
	// ブラウザ優先言語による言語判別
	//-------------------------------------
	String browserLang = (request.getHeader("accept-language") != null) ? request.getHeader("accept-language") : "";
	boolean isJp = false;
	if ( browserLang.startsWith("ja") || browserLang.equals("") ) {
		isJp = true;
	}
	
	String manualUrl = "http://www.massbank.jp/manuals/UserManual_ja.pdf";
	if ( !isJp ) {
		manualUrl = "http://www.massbank.jp/manuals/UserManual_en.pdf";
	}
	
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	String type = "peak";
	String relInte  = "100";
	String tol  = "0.3";
	String ionMode  = "1";
	boolean isFirst = true;
	List instGrpList = new ArrayList<String>();
	List instTypeList = new ArrayList<String>();
	Hashtable params = new Hashtable<String, String>();
	int paramCnt = 0;
	Enumeration names = request.getParameterNames();
	if ( names.hasMoreElements() ) {
		isFirst = false;
	}
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( key.equals("inst_grp") ) {
			String[] vals = request.getParameterValues( key );
			instGrpList = Arrays.asList(vals);
		}
		else if ( key.equals("inst") ) {
			String[] vals = request.getParameterValues( key );
			instTypeList = Arrays.asList(vals);
		}
		else {
			String val = request.getParameter( key );
			if ( key.equals("type") )		type = val;
			else if ( key.equals("int") )	relInte  = val;
			else if ( key.equals("tol") )	tol      = val;
			else if ( key.equals("ion") )	ionMode  = val;
			else if ( key.indexOf("mz") >= 0 || key.indexOf("op") >= 0 ) {
				params.put( key, val );
			}
		}
	}
	
	if ( paramCnt > 0 ) {
		isFirst = false;
	}
	String instGrp = "";
	for ( int i = 0; i < instGrpList.size(); i++ ) {
		instGrp += instGrpList.get(i);
		if ( i < instGrpList.size() - 1 ) {
			instGrp += ",";
		}
	}
	String instType = "";
	for ( int i = 0; i < instTypeList.size(); i++ ) {
		instType += instTypeList.get(i);
		if ( i < instTypeList.size() - 1 ) {
			instType += ",";
		}
	}
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="description" content="Search by ion and neutral loss">
	<meta name="keywords" content="Peak, m/z, Formula, difference">
	<meta name="revisit_after" content="30 days">
	<link rel="stylesheet" type="text/css" href="css/Common.css">
	<script type="text/javascript" src="script/AtomicMass.js"></script>
	<script type="text/javascript" src="script/Common.js"></script>
	<script type="text/javascript" src="script/PeakSearch.js"></script>
	<title>MassBank | Database | Peak Search</title>
</head>
<body class="msbkFont backgroundImg cursorDefault" onload="initFocus();">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Peak Search</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=manualUrl%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<iframe src="menu.html" width="860" height="30" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	<hr size="1">

	<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
	<jsp:include page="../pserver/ServerInfo.jsp" />

	<form name="form_query" method="post" action="jsp/Result.jsp" style="display:inline">
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
					<table border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td width="130">
								<input type="radio" name="type" value="peak" onClick="changeSearchType(this.value)"<% if(type.equals("peak")) out.print(" checked"); %>><b><i>Peak Search</i></b>
							</td>
							<td width="50"></td>
							<td width="210">
								<input type="radio" name="type" value="diff" onClick="changeSearchType(this.value)"<% if(type.equals("diff")) out.print(" checked"); %>><b><i>Peak Difference Search</i></b>
							</td>
						</tr>
						<tr>
							<td id="underbar1" height="4"<% if(type.equals("peak")) out.print(" bgcolor=\"OliveDrab\""); %>></td>
							<td></td>
							<td id="underbar2" height="4"<% if(type.equals("diff")) out.print(" bgcolor=\"DarkOrchid\""); %>></td>
						</tr>
						<tr>
							<td colspan="3" height="10"></td>
						</tr>
					</table>

					<table border="0" cellpadding="0" cellspacing="12" class="form-box">
						<tr>
							<th></th>
							<th id="mz"><i>m/z</i></th>
							<th>Formula</th>
						</tr>
<%
	final String[] logic = { "and", "or" };
	final int numForm = 6; 
	String[] mz = new String[numForm];
	String[] op = new String[numForm];
	for ( int i = 0; i < numForm; i++ ) {
		String key = "mz" + String.valueOf(i);
		if ( params.containsKey(key) ) {
			mz[i] = (String)params.get(key);
			op[i] = (String)params.get( "op" + String.valueOf(i) );
		}
		else {
			mz[i] = "";
			op[i] = "";
		}
		out.println( "\t\t\t\t\t\t<tr>" );
		if ( i != 0 ) {
			out.println( "\t\t\t\t\t\t\t<td>" );
			out.println( "\t\t\t\t\t\t\t\t<select name=\"op" + i + "\">" );
			for ( int j = 0; j < logic.length; j++ ) {
				out.print( "\t\t\t\t\t\t\t\t\t<option value=\"" + logic[j] + "\"" );
				if ( logic[j].equals(op[i]) ) {
					out.print( " selected" );
				}
				out.println( ">" + logic[j].toUpperCase() + "</option>" );
			}
			out.println( "\t\t\t\t\t\t\t\t</select>" );
			out.println( "\t\t\t\t\t\t\t</td>" );
		}
		else {
			out.println( "\t\t\t\t\t\t\t<td></td>" );
		}

		// m/z
		out.println( "\t\t\t\t\t\t\t<td><input name=\"mz" + i + "\" type=\"text\" size=\"10\" value=\"" + mz[i] + "\"></td>" );
		
		// Formula
		out.println( "\t\t\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t\t\t<img src=\"image/arrow.gif\" alt=\"\">" );
		out.println( "\t\t\t\t\t\t\t\t<input name=\"fom" + i + "\" type=\"text\" size=\"20\" value=\"\">" );
		out.println( "\t\t\t\t\t\t\t\t<input name=\"calc" + i + "\" type=\"button\" value=\"Mass Calc\" onClick=\"setMZ(" + i + ", fom" + i + ".value)\">" );
		out.println( "\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t</tr>" );
	}
%>
						<tr>
							<td colspan="5" height="1"></td>
						</tr>
						<tr>
							<td colspan="3">
								<b>Rel.Intensity</b>&nbsp;<input name="int" type="text" size="10" value="<%= relInte %>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Tolerance</b>&nbsp;<input name="tol" type="text" size="10" value="<%= tol %>">
							</td>
						</tr>
						<tr>
							<td colspan="4" align="right">
								<input type="button" name="reset" value="Reset" onClick="resetForm()">
							</td>
						</tr>
					</table>
					<br>
					<table>
						<tr>
							<td>
								<input type="submit" value="Search" onclick="return checkSubmit();" class="search">
								<input type="hidden" name="op0" value="or">
								<input type="hidden" name="sortKey" value="name">
								<input type="hidden" name="sortAction" value="1">
								<input type="hidden" name="pageNo" value="1">
								<input type="hidden" name="exec" value="">
							</td>
						</tr>
					</table>
				</td>
				<td style="padding:15px 15px;" valign="top">
					<br>
					<jsp:include page="Instrument.jsp" flush="true">
						<jsp:param name="ion" value="<%= ionMode %>" />
						<jsp:param name="first" value="<%= isFirst %>" />
						<jsp:param name="inst_grp" value="<%= instGrp %>" />
						<jsp:param name="inst" value="<%= instType %>" />
					</jsp:include>
				</td>
			</tr>
		</table>
	</form>
	<br>
	<hr size="1">
	<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
