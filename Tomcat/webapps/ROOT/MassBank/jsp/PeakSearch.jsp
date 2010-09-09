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
 * ver 1.0.10 2010.09.09
 *
 ******************************************************************************/
%>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Map" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetInstInfo" %>
<%@ include file="./Common.jsp"%>
<%
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	final int NUM_FORMULA_STD = 6; 
	final int NUM_FORMULA_ADV = 5;
	String type = "peak";
	String relInte  = "100";
	String tol  = "0.3";
	String ionMode  = "1";
	String mode = "and";
	Map inputFormula= new HashMap();
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
			if ( key.equals("type") )				type     = val;
			else if ( key.equals("mode") )			mode     = val;
			else if ( key.indexOf("formula") >= 0 )	inputFormula.put( key, val );
			else if ( key.equals("int") )			relInte  = val;
			else if ( key.equals("tol") )			tol      = val;
			else if ( key.equals("ion") )			ionMode  = val;
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
	
	
	//-------------------------------------
	// ポスト先
	//-------------------------------------
	String formAction = "./jsp/Result.jsp";
	if (type.equals("product") || type.equals("neutral")) {
		formAction = "./jsp/ResultAdv.jsp";
	}
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="description" content="Search by ion and neutral loss">
	<meta name="keywords" content="Peak, m/z, Formula, difference, Product, Ion, Neutral, Loss">
	<meta name="revisit_after" content="30 days">
	<link rel="stylesheet" type="text/css" href="css/Common.css">
	<link rel="stylesheet" type="text/css" href="css/FormulaSuggest.css" />
	<script type="text/javascript" src="script/Common.js"></script>
	<script type="text/javascript" src="script/jquery.js"></script>
	<script type="text/javascript" src="script/FormulaSuggest.js"></script>
	<script type="text/javascript" src="script/AtomicMass.js"></script>
	<script type="text/javascript" src="script/PeakSearch.js"></script>
	<title>MassBank | Database | Peak Search</title>
</head>
<body class="msbkFont backgroundImg cursorDefault" onload="loadCheck('<%=type%>');">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Peak Search</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<iframe src="menu.html" width="860" height="30" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	<hr size="1">

	<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
	<jsp:include page="../pserver/ServerInfo.jsp" />

	<form name="form_query" method="post" action="<%=formAction%>" style="display:inline">
		<table border="0" cellpadding="0">
			<tr>
				<td colspan="7" height="10"></td>
			</tr>
			<tr>
				<td width="130">
					<input type="radio" name="type" value="peak" onClick="return changeSearchType(this.value);"<% if(type.equals("peak")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('peak');">Peak Search</span></i></b>
				</td>
				<td width="30"></td>
				<td width="210">
					<input type="radio" name="type" value="diff" onClick="return changeSearchType(this.value);"<% if(type.equals("diff")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('diff');">Peak Difference Search</span></i></b>
				</td>
				<td width="30"></td>
				<td width="120">
					<input type="radio" name="type" value="product" onClick="return changeSearchType(this.value);"<% if(type.equals("product")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('product');">Product Ion</span></i></b>
				</td>
				<td width="30"></td>
				<td width="130">
					<input type="radio" name="type" value="neutral" onClick="return changeSearchType(this.value);"<% if(type.equals("neutral")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('neutral');">Neutral Loss</span></i></b>
				</td>
			</tr>
			<tr>
				<td id="underbar1" height="4"<% if(type.equals("peak")) out.print(" bgcolor=\"OliveDrab\""); %>></td>
				<td></td>
				<td id="underbar2" height="4"<% if(type.equals("diff")) out.print(" bgcolor=\"DarkOrchid\""); %>></td>
				<td></td>
				<td id="underbar3" height="4"<% if(type.equals("product")) out.print(" bgcolor=\"MidnightBlue\""); %>></td>
				<td></td>
				<td id="underbar4" height="4"<% if(type.equals("neutral")) out.print(" bgcolor=\"DarkGreen\""); %>></td>
			</tr>
			<tr>
				<td colspan="7" height="10"></td>
			</tr>
		</table>
		<hr size="1">
		
		<!--// Peak Search-->
<%
	if (type.equals("peak") || type.equals("diff")) {
		out.println( "\t\t<div id=\"standard\" class=\"showObj\">" );
	}
	else {
		out.println( "\t\t<div id=\"standard\" class=\"hidObj\">" );
	}
	
	String mzLabel = "<i>m/z</i>";
	String allowImage = "<img src=\"image/arrow_peak.gif\" alt=\"\">";
	if (type.equals("diff")) {
		mzLabel = "<i>m/z</i> Diff.";
		allowImage = "<img src=\"image/arrow_diff.gif\" alt=\"\">";
	}
%>
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<table border="0" cellpadding="0" cellspacing="12" class="form-box">
							<tr>
								<th></th>
								<th id="mz"><%=mzLabel%></th>
								<th>Formula</th>
							</tr>
<%
	final String[] logic = { "and", "or" };
	String[] mz = new String[NUM_FORMULA_STD];
	String[] op = new String[NUM_FORMULA_STD];
	for ( int i = 0; i < NUM_FORMULA_STD; i++ ) {
		String key = "mz" + String.valueOf(i);
		if ( params.containsKey(key) ) {
			mz[i] = (String)params.get(key);
			op[i] = (String)params.get( "op" + String.valueOf(i) );
		}
		else {
			mz[i] = "";
			op[i] = "";
		}
		out.println( "\t\t\t\t\t\t\t<tr>" );
		if ( i != 0 ) {
			out.println( "\t\t\t\t\t\t\t\t<td>" );
			out.println( "\t\t\t\t\t\t\t\t\t<select name=\"op" + i + "\">" );
			for ( int j = 0; j < logic.length; j++ ) {
				out.print( "\t\t\t\t\t\t\t\t\t\t<option value=\"" + logic[j] + "\"" );
				if ( logic[j].equals(op[i]) ) {
					out.print( " selected" );
				}
				out.println( ">" + logic[j].toUpperCase() + "</option>" );
			}
			out.println( "\t\t\t\t\t\t\t\t\t</select>" );
			out.println( "\t\t\t\t\t\t\t\t</td>" );
		}
		else {
			out.println( "\t\t\t\t\t\t\t\t<td></td>" );
		}

		// m/z
		out.println( "\t\t\t\t\t\t\t\t<td><input name=\"mz" + i + "\" type=\"text\" size=\"10\" value=\"" + mz[i] + "\"></td>" );
		
		// Formula
		out.println( "\t\t\t\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t\t\t\t<span id=\"arrow" + i + "\">" + allowImage + "</span>" );
		out.println( "\t\t\t\t\t\t\t\t\t<input name=\"fom" + i + "\" type=\"text\" size=\"20\" value=\"\">" );
		out.println( "\t\t\t\t\t\t\t\t\t<input name=\"calc" + i + "\" type=\"button\" value=\"Mass Calc\" onClick=\"setMZ(" + i + ", fom" + i + ".value)\">" );
		out.println( "\t\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t\t</tr>" );
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
					<td style="padding:0px 15px;" valign="top">
						<jsp:include page="Instrument.jsp" flush="true">
							<jsp:param name="ion" value="<%= ionMode %>" />
							<jsp:param name="first" value="<%= isFirst %>" />
							<jsp:param name="inst_grp" value="<%= instGrp %>" />
							<jsp:param name="inst" value="<%= instType %>" />
						</jsp:include>
					</td>
				</tr>
			</table>
		</div>

		<!--// Peak Search Advanced -->
<%
	if (type.equals("product") || type.equals("neutral")) {
		out.println( "\t\t<div id=\"advance\" class=\"showObj\">" );
	}
	else {
		out.println( "\t\t<div id=\"advance\" class=\"hidObj\">" );
	}
%>
			<div class="boxA" style="width:720px">
				<br>
				<table border="0" cellpadding="0" cellspacing="3" style="margin:8px">
<%
	String style = "bgProduct";
	String str = "Product&nbsp;Ion&nbsp;";
	if ( type.equals("neutral") ) {
		style = "bgNeutral";
		str = "Neutral&nbsp;Loss&nbsp;";
	}
	String condition = "<b class=\"logic\">AND</b>";
	if ( mode.equals("or") ) {
		condition = "<b class=\"logic\">OR</b>";
	}
	else if ( mode.equals("seq") ) {
		condition = "<img src=\"./image/arrow_neutral.gif\">";
	}

	out.println("\t\t\t\t\t<tr>");
	for ( int i = 1; i <= NUM_FORMULA_ADV; i++ ) {
		out.println( "\t\t\t\t\t\t<td align=\"center\" width=\"110\"><span id=\"advanceType" + i +"\" class=\"" + style + "\">"
					+ str + String.valueOf(i) + "</span></td>" );
		if ( i < NUM_FORMULA_ADV ) {
			out.println( "\t\t\t\t\t\t<td></td>" );
		}
	}
	out.println("\t\t\t\t\t</tr>");

	out.println("\t\t\t\t\t<tr>");
	for ( int i = 1; i <= NUM_FORMULA_ADV; i++ ) {
		out.println( "\t\t\t\t\t\t<td align=\"center\">Formula</td>" );
		if ( i < NUM_FORMULA_ADV ) {
			out.println( "\t\t\t\t\t\t<td></td>" );
		}
	}
	out.println("\t\t\t\t\t</tr>");

	out.println("\t\t\t\t\t<tr>");
	for ( int i = 1; i <= NUM_FORMULA_ADV; i++ ) {
		String key = "formula" + String.valueOf(i);
		String val = "";
		if ( inputFormula.containsKey(key) ) {
			val = (String)inputFormula.get(key);
		}
		out.println( "\t\t\t\t\t\t<td align=\"center\">" );
		out.println( "\t\t\t\t\t\t\t<input id=\"" + key + "\" class=\"FormulaSuggest\" name=\"" + key + "\" type=\"text\" size=\"12\" value=\"" + val + "\" autocomplete=\"off\">" );
		out.println( "\t\t\t\t\t\t</td>" );
		if ( i < NUM_FORMULA_ADV ) {
			out.println( "\t\t\t\t\t\t<td id=\"cond" + String.valueOf(i) + "\" width=\"26\" align=\"center\">" + condition + "</td>");
		}
	}
	out.println("\t\t\t\t\t</tr>");
	out.println("\t\t\t\t\t<tr height=\"50\">");
	out.println("\t\t\t\t\t\t<td colspan=\"7\">");
	
	String[] valMode = new String[]{ "and", "or" };
	String[] strMode = new String[]{ "AND", "OR" };
	if ( type.equals("neutral") ) {
		valMode = new String[]{ "and", "seq" };
		strMode = new String[]{ "AND", "SEQUENCE" };
	}
	for ( int i = 0; i < valMode.length; i++ ) {
		out.print( "\t\t\t\t\t\t\t<input type=\"radio\" name=\"mode\" value=\"" + valMode[i] + "\" onClick=\"chageMode(this.value)\"" );
		if ( mode.equals(valMode[i]) ) {
			out.print(" checked");
		}
		out.println( "><b><span id=\"modeTxt" + i + "\">" + strMode[i] + "</span></b>&nbsp;&nbsp;&nbsp;" );
	}
	out.println("\t\t\t\t\t\t</td>");
	out.println("\t\t\t\t\t\t<td colspan=\"4\" align=\"right\">");
	out.println("\t\t\t\t\t\t\t<input type=\"button\" name=\"reset\" value=\"Reset\" onClick=\"resetForm()\">");
	out.println("\t\t\t\t\t\t</td>");
	out.println("\t\t\t\t\t</tr>");
%>
				</table>
				<table border="0" cellpadding="10" cellspacing="0">
					<tr>
						<td><font class="font12px">* The targets of Peak Search Advanced are only Keio and Riken data.</font></td>
					</tr>
				</table>
			</div>
			<br>
			<input type="submit" value="Search" class="search">
		</div>

		<div id="loaded"></div>
	</form>
	<br>
	<hr size="1">
	<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
