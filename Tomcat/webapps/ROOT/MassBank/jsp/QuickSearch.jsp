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
 * Quick Search Page表示用モジュール
 *
 * ver 1.0.15 2011.07.12
 *
 ******************************************************************************/
%>
<%@ page import="java.util.*" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetInstInfo" %>
<%@ include file="./Common.jsp"%>
<%
	//-------------------------------------------
	// 検索タイプを設定
	//-------------------------------------------
	String searchType = (String)request.getParameter( "searchType" );
	if ( searchType == null ) {
		searchType = "keyword";
	}
	boolean isKeyword = false;
	String postJspName = "";
	if ( searchType.equals("keyword") ) {
		isKeyword = true;
		postJspName = "Result.jsp";
	}
	else {
		postJspName = "QpeakResult.jsp";
	}
	
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	String compound = "";
	String mass     = "";
	String tol      = "0.3";
	String formula  = "";
	String op1      = "";
	String op2      = "";
	String peakData = "";
	String cutOff   = "5";
	String num      = "20";
	String ionMode  = "1";
	boolean isFirst = true;
	List instGrpList = new ArrayList<String>();
	List instTypeList = new ArrayList<String>();
	List msTypeList = new ArrayList<String>();
	int paramCnt = 0;
	Enumeration names = request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( !key.equals("searchType") ) {
			paramCnt++;
		}
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
			msTypeList = Arrays.asList(vals);
		}
		else {
			String val = request.getParameter( key );
			if ( key.equals("compound") ) 		compound = val;
			else if ( key.equals("mz") )		mass     = val;
			else if ( key.equals("tol") )		tol      = val;
			else if ( key.equals("formula") ) 	formula  = val;
			else if ( key.equals("op1") )		op1      = val;
			else if ( key.equals("op2") )		op2      = val;
			else if ( key.equals("qpeak") )		peakData = val;
			else if ( key.equals("CUTOFF") )	cutOff   = val;
			else if ( key.equals("num") )		num      = val;
			else if ( key.equals("ion") )		ionMode  = val;
		}
	}
	if ( paramCnt > 0 ) {
		isFirst = false;
	}

	String instGrp = "";
	for ( int i=0; i<instGrpList.size(); i++ ) {
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
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
	<meta name="description" content="Keyword search of chemical compounds. Retrieves the chemical compound(s) specified by chemical name or molecular formula, and displays its spectra.">
	<meta name="keywords" content="Quick,Compound,ExactMass,Formula">
	<meta name="revisit_after" content="30 days">
	<link rel="stylesheet" type="text/css" href="css/Common.css">
	<script type="text/javascript" src="./script/Common.js"></script>
	<script type="text/javascript" src="./script/QuickSearch.js"></script>
	<title>MassBank | Database | Quick Search</title>
</head>
<body class="msbkFont backgroundImg cursorDefault" onload="initFocus();">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Quick Search</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%><%=QUICK_PAGE%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<iframe src="menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	<hr size="1">

	<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
	<jsp:include page="../pserver/ServerInfo.jsp" />

	<form name="change" method="post" action="QuickSearch.html" style="display:inline">
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="180">
					<input type="radio" name="searchType" value="keyword" onClick="changeSearchType()"<% if(isKeyword) out.print(" checked"); %>><b><i>Search by Keyword</i></b>
				</td>
				<td width="50"></td>
				<td width="150">
					<input type="radio" name="searchType" value="peak" onClick="changeSearchType()"<% if(!isKeyword) out.print(" checked"); %>><b><i>Search by Peak</i></b>
				</td>
			</tr>
			<tr>
				<td id="underbar1" height="4"<% if(isKeyword) out.print(" bgcolor=\"IndianRed\""); %>></td>
				<td></td>
				<td id="underbar2" height="4"<% if(!isKeyword) out.print(" bgcolor=\"Goldenrod\""); %>></td>
			</tr>
			<tr>
				<td colspan="3" height="10"></td>
			</tr>
		</table>
	</form>
	<form name="form_query" method="post" action="jsp/<% out.print(postJspName); %>" style="display:inline">
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
<%
	if ( isKeyword ) {
%>
					<table border="0" cellpadding="0" cellspacing="15" class="form-box">
						<tr>
							<td colspan="2">
								<b>Compound Name</b>&nbsp;
								<input name="compound" type="text" size="47" value="<%= compound %>">
							</td>
						</tr>
						<tr>
							<td>&nbsp;&nbsp;&nbsp;
								<select name="op1">
									<option value="and"<% if(op1.equals("and")) out.print(" selected"); %>>AND</option>
									<option value="or"<%  if(op1.equals("or"))  out.print(" selected"); %>>OR</option>
								</select>
							</td>
							<td>
								<b>Exact Mass</b>&nbsp;
								<input name="mz" type="text" size="10" value="<%= mass %>">
								&nbsp;&nbsp;&nbsp;<b>Tolerance</b>&nbsp;
								<input name="tol" type="text" size="6" value="<%= tol %>">
							</td>
						</tr>
						<tr>
							<td valign="top">&nbsp;&nbsp;&nbsp;
								<select name="op2">
									<option value="and"<% if(op2.equals("and")) out.print(" selected"); %>>AND</option>
									<option value="or"<%  if(op2.equals("or"))  out.print(" selected"); %>>OR</option>
								</select>
							</td>
							<td>
								<b>Formula</b>&nbsp;
								<input name="formula" type="text" size="14" value="<%= formula %>"><br>
								<b><span class="fontNote">&nbsp;&nbsp;&nbsp;&nbsp;( e.g. C6H7N5, C5H*N5, C5* )</span></b>
							</td>
						</tr>
						<tr>
							<td colspan="2" align="right">
								<input type="button" value="Reset" onClick="resetForm();">
							</td>
						</tr>
					</table>
<%
	} else {
%>
					<table border="0" cellpadding="0" cellspacing="15" style="background-color:WhiteSmoke;border:1px silver solid;">
						<tr>
							<td>
								<b>Peak Data</b><br>
								<textarea name="qpeak" cols="40" rows="10"><%= peakData %></textarea><br>
								<b><span class="fontNote">* <i>m/z</i> and relative intensities(0-999), delimited by a space.</span></b><br>
								<input type="button" value="Example1" onClick="insertExample1()">
								<input type="button" value="Example2" onClick="insertExample2()">
							</td>
						</tr>
						<tr>
							<td>
								<hr size=1 color="silver">
								<b>Cutoff threshold of relative intensities</b>&nbsp;&nbsp;<input name="CUTOFF" type="text" size="4" value="<%= cutOff %>"><br>
							</td>
						</tr>
						<tr>
							<td>
								<b>Number of Results</b>
								<select name="num">
									<option value="20"<%  if(num.equals("20"))  out.print(" selected"); %>>20</option>
									<option value="50"<%  if(num.equals("50"))  out.print(" selected"); %>>50</option>
									<option value="100"<% if(num.equals("100")) out.print(" selected"); %>>100</option>
									<option value="500"<% if(num.equals("500")) out.print(" selected"); %>>500</option>
								</select>
							</td>
						</tr>
					</table>
<%
	}
%>
					<br>
					<table>
						<tr>
							<td>
								<input type="submit" value="Search" onclick="<% if(!isKeyword){out.print("beforeSubmit(); ");} %>return checkSubmit();" class="search">
								<input type="hidden" name="type" value="quick">
								<input type="hidden" name="searchType" value="<% if(isKeyword){out.print("keyword");}else{out.print("peak");}%>">
								<input type="hidden" name="sortKey" value="name">
								<input type="hidden" name="sortAction" value="1">
								<input type="hidden" name="pageNo" value="1">
								<input type="hidden" name="exec" value="">
							</td>
						</tr>
					</table>
				</td>
				<td valign="top" style="padding:0px 15px;">
					<jsp:include page="Instrument.jsp" flush="true">
						<jsp:param name="ion" value="<%= ionMode %>" />
						<jsp:param name="first" value="<%= isFirst %>" />
						<jsp:param name="inst_grp" value="<%= instGrp %>" />
						<jsp:param name="inst" value="<%= instType %>" />
						<jsp:param name="ms" value="<%= msType %>" />
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
