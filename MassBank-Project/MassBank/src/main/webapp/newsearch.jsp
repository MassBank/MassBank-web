<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
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
 ******************************************************************************/
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<title>MassBank | Database | Quick Search</title>

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<meta name="description" content="Search of spectra by chemical name, peak, InChIKey or SPLASH.">
<meta name="keywords" content="Search,Compound,ExactMass,Formula,InChIKey,SPLASH">


<script src="script/html-imports.min.js"></script>
<link rel="import" href="common.html"></link>
</head>

<body class="w3-theme-gradient" onload="myFunction()">
	<header class="w3-cell-row w3-text-grey">
		<div class="w3-container w3-cell w3-mobile" style="width:60%">
			<h1>
				<b>Quick Search</b>
			</h1>
		</div>
		<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align">
			<img src="image/bullet_link.gif">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>
		</div>
		<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align">
			<img src="image/bullet_link.gif">&nbsp;<b><a class="text" href="manuals/UserManual_en.pdf" target="_blank">user manual</a></b>
		</div>
	</header>

	<div id="menu"></div>
	
		<div class="w3-bar">
		<div class="w3-bar-item w3-round w3-bottombar w3-border-red">
			<input class="w3-radio" onclick="openSearch(event,'Keyword')" type="radio"
			value="Keyword" name="search_keyword" checked>
		 	<label>Search by Keyword</label>
		</div>
		<div class="w3-bar-item w3-round w3-bottombar w3-border-amber">
			<input class="w3-radio" onclick="openSearch(event,'Peak')" type="radio"
			value="Peak" name="search_keyword">
		 	<label>Search by Peak</label>
		</div>
		<div class="w3-bar-item w3-round w3-bottombar w3-border-deep-purple">
			<input class="w3-radio" onclick="openSearch(event,'InChIKey')" type="radio"
			value="InChIKey" name="search_keyword">
			<label>Search by InChIKey</label>
		</div>
		<div class="w3-bar-item w3-round w3-bottombar w3-border-cyan">
			<input class="w3-radio" onclick="openSearch(event,'Splash')" type="radio"
			value="Splash" name="search_keyword">
			<label>Search by Splash</label>
		</div>
	</div>
  
  	<div class="w3-row">
		<div class="w3-half">
			<div id="Keyword" class="w3-container w3-animate-opacity search_keyword">
				<h2>Keyword</h2>
			</div>
		
			<div id="Peak" class="w3-container w3-animate-opacity search_keyword" style="display:none">
				<h2>Peak</h2>
			</div>
		
			<div id="InChIKey" class="w3-container w3-animate-opacity search_keyword" style="display:none">
				<h2>InChIKey</h2>
			</div>
		
			<div id="Splash" class="w3-container w3-animate-opacity search_keyword" style="display:none">
				<h2>Splash</h2>
			</div>
		</div>
		<div class="w3-half">
		
		</div>
	</div>
  
	<script>
	function openSearch(evt, searchName) {
		var i, x;
		x = document.getElementsByClassName("search_keyword");
		for (i = 0; i < x.length; i++) {
			x[i].style.display = "none";
		}
		document.getElementById(searchName).style.display = "block";
	}
	
	function myFunction() {
		var i, x;
		x = document.getElementsByClassName("search_keyword");
		for (i = 0; i < x.length; i++) {
			x[i].style.display = "none";
		}
		document.getElementById(document.querySelector('input[name="search_keyword"]:checked').value).style.display = "block";
	}
	</script>


     

	
	<div id="copyrightline"></div>
		
</body>

<%@ page import="java.util.*" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetInstInfo" %>
<%@ page import="massbank.SearchDefaults" %>
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
	boolean isInChIKey = false;
	boolean isSplash = false;
	boolean isPeak = false;
	String postJspName = "";
	if ( searchType.equals("keyword") ) {
		isKeyword = true;
		postJspName = "Result.jsp";
	}
	else if ( searchType.equals("inchikey")) {
		isInChIKey = true;
		postJspName = "Result.jsp";
	}
	else if ( searchType.equals("splash")) {
		isSplash = true;
		postJspName = "Result.jsp";
	}
	else if ( searchType.equals("peak")) {
		isPeak = true;
		postJspName = "QpeakResult.jsp";
	} else {
		isKeyword = true;
		postJspName = "Result.jsp";
	}
	
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	String compound = "";
	String mass     = "";
	String tol      = SearchDefaults.tol;
	String formula  = "";
	String op1      = "";
	String op2      = "";
	String peakData = "";
	String cutOff   = SearchDefaults.cutOff;
	String num      = SearchDefaults.num;
	String ionMode  = SearchDefaults.ionMode;
	String inchikey = "";
	String splash = "";
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
			else if ( key.equals("inchikey") )   inchikey = val;
			else if ( key.equals("splash") )		splash = val;
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

<body class="msbkFont backgroundImg cursorDefault" onload="initFocus();">

	<form name="change" method="post" action="QuickSearch.jsp" style="display:inline" onSubmit="doWait('Searching...')">
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="180">
					<input type="radio" name="searchType" value="keyword" onClick="changeSearchType()"<% if(isKeyword) out.print(" checked"); %>><b><i>Search by Keyword</i></b>
				</td>
				<td width="40"></td>
				<td width="160">
					<input type="radio" name="searchType" value="peak" onClick="changeSearchType()"<% if(isPeak) out.print(" checked"); %>><b><i>Search by Peak</i></b>
				</td>
			</tr>
			<tr>
				<td id="underbar1" height="4"<% if(isKeyword) out.print(" bgcolor=\"IndianRed\""); %>></td>
				<td></td>
				<td id="underbar2" height="4"<% if(isPeak) out.print(" bgcolor=\"Goldenrod\""); %>></td>
			</tr>
			<tr>
				<td colspan="3" height="10"></td>
			</tr>
			<tr>
				<td width="150">
					<input type="radio" name="searchType" value="inchikey" onClick="changeSearchType()"<% if(isInChIKey) out.print(" checked"); %>><b><i>Search by InChIKey</i></b>
				</td>
				<td width="40"></td>
				<td width="160">
					<input type="radio" name="searchType" value="splash" onClick="changeSearchType()"<% if(isSplash) out.print(" checked"); %>><b><i>Search by Splash</i></b>
				</td>				
			</tr>
			<tr>
				<td id="underbar3" height="4"<% if(isInChIKey) out.print(" bgcolor=\"Goldenrod\""); %>></td>
				<td></td>
				<td id="underbar4" height="4"<% if(isSplash) out.print(" bgcolor=\"Goldenrod\""); %>></td>
			</tr>
			<tr>
				<td colspan="3" height="10"></td>
			</tr>
		</table>
	</form>
	<form name="form_query" method="post" action="<% out.print(postJspName); %>" style="display:inline" onSubmit="doWait('Searching...')">
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
	} else if ( isInChIKey ) {
%>
					<table border="0" cellpadding="0" cellspacing="15" class="form-box">
						<tr>
							<td colspan="2">
								<b>InChIKey</b>&nbsp;
								<input name="inchikey" type="text" size="47" value="<%= inchikey %>">
							</td>
						</tr>
						<tr>
							<td colspan="2" align="right">
								<input type="button" value="Reset" onClick="resetForm();">
							</td>
						</tr>
					</table>
<%
	} else if ( isSplash ) {
%>
					<table border="0" cellpadding="0" cellspacing="15" class="form-box">
						<tr>
							<td colspan="2">
								<b>Splash</b>&nbsp;
								<input name="splash" type="text" size="47" value="<%= splash %>">
							</td>
						</tr>
						<tr>
							<td colspan="2" align="right">
								<input type="button" value="Reset" onClick="resetForm();">
							</td>
						</tr>
					</table>					
<%
	} else if ( isPeak ) {
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
								<input type="submit" value="Search" onclick="<% 
									if (isPeak) {
										out.print("beforeSubmit(); ");
									} 
								%>return checkSubmit(0);" class="search">
								<input type="hidden" name="type" value="<% 
									if (isKeyword || isPeak) {
										out.print("quick");
									} else if (isInChIKey) {
										out.print("inchikey");
									} else if (isSplash) {
										out.print("splash");
									}
								%>">
								<input type="hidden" name="searchType" value="<% 
									if (isKeyword) {
										out.print("keyword");
									} else if (isInChIKey) {
										out.print("inchikey");
									} else if (isSplash) {
										out.print("splash");	
									} else {
										out.print("peak");
									}
								%>">
								<!-- <input type="hidden" name="sortKey" value="name">  -->
								<input type="hidden" name="sortKey" value="not">
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
</body>
</html>
