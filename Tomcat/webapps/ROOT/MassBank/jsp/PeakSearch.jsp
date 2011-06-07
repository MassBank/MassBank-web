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
 * ver 1.0.14 2011.05.31
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
<%@ page import="massbank.admin.AdminCommon" %>
<%@ include file="./Common.jsp"%>
<%
	AdminCommon admin = new AdminCommon();
	boolean isPeakAdv = admin.isPeakAdv();
	
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	final int NUM_FORMULA_STD = 6; 
	final int NUM_FORMULA_ADV = 5;
	String searchOf = "peak";
	String searchBy = "mz";
	String relInte  = "100";
	String tol  = "0.3";
	String ionMode  = "1";
	String mode = "and";
	Map inputFormula= new HashMap();
	boolean isFirst = true;
	List<String> instGrpList = new ArrayList<String>();
	List<String> instTypeList = new ArrayList<String>();
	List<String> msTypeList = new ArrayList<String>();
	Hashtable<String, String> params = new Hashtable<String, String>();
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
		else if ( key.equals("ms") ) {
			String[] vals = request.getParameterValues( key );
			instTypeList = Arrays.asList(vals);
		}
		else {
			String val = request.getParameter( key );
			if ( key.equals("searchof") )			searchOf = val;
			else if ( key.equals("searchby") )		searchBy = val;
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

	String type = "";
	if ( searchBy.equals("mz") ) {
		if ( searchOf.equals("peak") ) type = "peak";
		else type = "diff";
	}
	else {
		if ( searchOf.equals("peak") ) type = "product";
		else type = "neutral";
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
	for ( int i=0; i<instTypeList.size(); i++ ) {
		instType += instTypeList.get(i);
		if ( i < instTypeList.size() - 1 ) {
			instType += ",";
		}
	}
	String msType = "";
	for ( int i=0; i<msTypeList.size(); i++ ) {
		msType += msTypeList.get(i);
		if ( i < msTypeList.size() - 1 ) {
			msType += ",";
		}
	}
	
	//-------------------------------------
	// ポスト先
	//-------------------------------------
	String formAction = "./jsp/Result.jsp";
	if ( searchBy.equals("formula")) {
		formAction = "./jsp/ResultAdv.jsp";
	}
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
	<meta name="description" content="Search spectra by the m/z value and molecular formula. Retrieves spectra containing the peaks or neutral losses that users specify by m/z values. Retrieves spectra containing the peaks or neutral losses that users specify by molecular formulae.">
	<meta name="keywords" content="Peak,m/z,Formula,difference,Product,Ion,Neutral,Loss">
	<meta name="revisit_after" content="30 days">
	<link rel="stylesheet" type="text/css" href="css/Common.css">
	<link rel="stylesheet" type="text/css" href="css/FormulaSuggest.css" />
	<script type="text/javascript" src="script/Common.js"></script>
	<script type="text/javascript" src="script/jquery.js"></script>
<% if ( isPeakAdv ) { %>
	<script type="text/javascript" src="script/FormulaSuggest.js"></script>
<% } %>
	<script type="text/javascript" src="script/AtomicMass.js"></script>
	<script type="text/javascript" src="script/PeakSearch.js"></script>
	<title>MassBank | Database | Peak Search</title>
</head>
<body class="msbkFont backgroundImg cursorDefault" onload="loadCheck('<%=searchOf%>', '<%=searchBy%>');">
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
				<td width="90"><b>Search of</b></td>
				<td width="100">
					<input type="radio" name="searchof" value="peak" tabindex="1" onClick="return changeSearchType('peak','');"<% if(searchOf.equals("peak")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('peak','')"><b>Peaks</b></span></i></b>
				</td>
				<td width="20">&nbsp;</td>
				<td width="170">
					<input type="radio" name="searchof" value="diff" tabindex="2" onClick="return changeSearchType('diff','');"<% if(searchOf.equals("diff")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('diff','')">Peak&nbsp;Differences</span></i></b>
				</td>
			</tr>
			<tr>
				<td></td>
				<td id="underbar1" height="4"<% if(type.equals("peak")){out.print(" bgcolor=\"OliveDrab\"");}else if(type.equals("product")){out.print(" bgcolor=\"MidnightBlue\"");} %>></td>
				<td></td>
				<td id="underbar2" height="4"<% if(type.equals("diff")){out.print(" bgcolor=\"DarkOrchid\"");}else if(type.equals("neutral")){out.print(" bgcolor=\"DarkGreen\"");} %>></td>
			</tr>
<% if ( isPeakAdv ) { %>
			<tr>
				<td><b>Search by</b></td>
				<td>
					<input type="radio" name="searchby" value="mz" tabindex="3" onClick="return changeSearchType('','mz');"<% if(searchBy.equals("mz")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('','mz')"><b><i>m/z</i>-Value</b></span></i></b>
				</td>
				<td></td>
				<td>
					<input type="radio" name="searchby" value="formula" tabindex="4" onClick="return changeSearchType('','formula');"<% if(searchBy.equals("formula")) out.print(" checked"); %>><b><i><span name="typeLbl" onclick="return changeSearchType('','formula')">Molecular&nbsp;Formula</span></i></b>
				</td>
			</tr>
			<tr>
				<td></td>
				<td id="underbar3" height="4"<% if(type.equals("peak")){out.print(" bgcolor=\"OliveDrab\"");}else if(type.equals("product")){out.print(" bgcolor=\"MidnightBlue\"");} %>></td>
				<td></td>
				<td id="underbar4" height="4"<% if(type.equals("diff")){out.print(" bgcolor=\"DarkOrchid\"");}else if(type.equals("neutral")){out.print(" bgcolor=\"DarkGreen\"");} %>></td>
			</tr>
<% } else { %>
			<input type="hidden" name ="searchby" value="mz">
<% } %>
		</table>
		<hr size="1">
		
		<!--// Peak Search-->
<%
	if ( searchBy.equals("mz") ) {
		out.println( "\t\t<div id=\"standard\" class=\"showObj\">" );
	}
	else {
		out.println( "\t\t<div id=\"standard\" class=\"hidObj\">" );
	}
	
	String mzLabel = "<i>m/z</i>";
	String allowImage = "<img src=\"image/arrow_peak.gif\" alt=\"\">";
	if ( searchOf.equals("diff") && searchBy.equals("mz") ) {
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
	String lblLogic = logic[0].toUpperCase();
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
		if ( i == 0 ) {
			out.println( "\t\t\t\t\t\t\t\t<td>" );
			out.println( "\t\t\t\t\t\t\t\t\t<select name=\"op0\" class=\"mzLogics\" tabindex=\"5\">" );
			for ( int j = 0; j < logic.length; j++ ) {
				out.print( "\t\t\t\t\t\t\t\t\t\t<option value=\"" + logic[j] + "\"" );
				if ( logic[j].equals(op[0]) ) {
					out.print( " selected" );
					lblLogic = logic[j].toUpperCase();
				}
				out.println( ">" + logic[j].toUpperCase() + "</option>" );
			}
			out.println( "\t\t\t\t\t\t\t\t\t</select>" );
			out.println( "\t\t\t\t\t\t\t\t</td>" );
		}
		else {
			out.println( "\t\t\t\t\t\t\t\t<td align=\"right\"><span class=\"logic\">" + lblLogic + "</span>&nbsp;</td>" );
		}
		
		// m/z
		out.println( "\t\t\t\t\t\t\t\t<td><input name=\"mz" + i + "\" type=\"text\" size=\"14\" value=\"" + mz[i] + "\" class=\"Mass\" tabindex=\"" + (i+5) + "\"></td>" );
		
		// Formula
		out.println( "\t\t\t\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t\t\t\t<span id=\"arrow" + i + "\">" + allowImage + "</span>" );
		out.println( "\t\t\t\t\t\t\t\t\t<input name=\"fom" + i + "\" type=\"text\" size=\"20\" value=\"\" class=\"Formula\" tabindex=\"" + (i+11) + "\">" );
		out.println( "\t\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t\t</tr>" );
	}
%>
							<tr>
								<td colspan="3" height="1"></td>
							</tr>
							<tr>
								<td colspan="3">
									<b>Rel.Intensity</b>&nbsp;<input name="int" type="text" size="10" value="<%= relInte %>" tabindex="17">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Tolerance</b>&nbsp;<input name="tol" type="text" size="10" value="<%= tol %>" tabindex="18">
								</td>
							</tr>
							<tr>
								<td colspan="3" align="right">
									<input type="button" name="reset" value="Reset" onClick="resetForm()">
								</td>
							</tr>
						</table>
						<br>
						<table>
							<tr>
								<td>
									<input type="submit" value="Search" onclick="return checkSubmit();" class="search" tabindex="19">
									<input type="hidden" name="op1" value="<%=lblLogic.toLowerCase()%>">
									<input type="hidden" name="op2" value="<%=lblLogic.toLowerCase()%>">
									<input type="hidden" name="op3" value="<%=lblLogic.toLowerCase()%>">
									<input type="hidden" name="op4" value="<%=lblLogic.toLowerCase()%>">
									<input type="hidden" name="op5" value="<%=lblLogic.toLowerCase()%>">
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
							<jsp:param name="inst" value="<%= msType %>" />
						</jsp:include>
					</td>
				</tr>
			</table>
		</div>

		<!--// Peak Search Advanced -->
<%
	if ( searchBy.equals("formula") ) {
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
	String str = "Ion&nbsp;";
	if ( searchBy.equals("formula") && searchOf.equals("diff") ) {
		style = "bgNeutral";
		str = "Neutral&nbsp;Loss&nbsp;";
	}
	String condition = "<span class=\"logic\">AND</span>";
	if ( mode.equals("or") ) {
		condition = "<span class=\"logic\">OR</span>";
	}
	else if ( mode.equals("seq") ) {
		condition = "<img src=\"./image/arrow_neutral.gif\">";
	}

	out.println("\t\t\t\t\t<tr>");
	for ( int i = 1; i <= NUM_FORMULA_ADV; i++ ) {
		out.println( "\t\t\t\t\t\t<td align=\"center\" width=\"110\" id=\"advanceType" + i +"\" class=\"" + style + "\">"
					+ str + String.valueOf(i) + "</td>" );
		if ( i < NUM_FORMULA_ADV ) {
			out.println( "\t\t\t\t\t\t<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>" );
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
	if ( searchBy.equals("formula") && searchOf.equals("diff") ) {
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
			<input type="hidden" name="type" value="<%=type%>">
			<input type="submit" value="Search" class="search">
		</div>

		<div id="loaded"></div>
	</form>
	<br>
	<hr size="1">
	<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
