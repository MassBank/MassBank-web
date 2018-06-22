<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!-- Copyright (C) 2010 JST-BIRD MassBank -->
<!-- Copyright (C) 2017 MassBank consortium -->

<!-- This file is part of MassBank. -->

<!-- MassBank is free software; you can redistribute it and/or -->
<!-- modify it under the terms of the GNU General Public License -->
<!-- as published by the Free Software Foundation; either version 2 -->
<!-- of the License, or (at your option) any later version. -->

<!-- This program is distributed in the hope that it will be useful, -->
<!-- but WITHOUT ANY WARRANTY; without even the implied warranty of -->
<!-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the -->
<!-- GNU General Public License for more details. -->

<!-- You should have received a copy of the GNU General Public License -->
<!-- along with this program; if not, write to the Free Software -->
<!-- Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="en">
<title>MassBank | Database | Quick Search</title>

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<meta name="description" content="Search of spectra by chemical name, peak, InChIKey or SPLASH.">
<meta name="keywords" content="Search,Compound,ExactMass,Formula,InChIKey,SPLASH">
<meta name="author" content="MassBank">
<link rel="stylesheet" type="text/css" href="css/w3.css">
<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
<link rel="stylesheet" type="text/css" href="css/massbank.css">
<script src="script/jquery-3.3.1.min.js"></script>
<script src="script/massbank.js"></script>

</head>

<body class="w3-theme-gradient">
	
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
	
	<div class="w3-border-bottom w3-border-dark-grey w3-padding-16" id="menu"></div>
	
	<div class="w3-container">
		<div class="w3-bar w3-margin-top w3-margin-bottom">
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-red w3-blue" onclick="openSearch(event,'Keyword')">Search by Keyword</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-amber" onclick="openSearch(event,'Peak')">Search by Peak</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-deep-purple" onclick="openSearch(event,'InChIKey')">Search by InChIKey</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-cyan" onclick="openSearch(event,'Splash')">Search by SPLASH</button>
		</div>

		<div id="Keyword" class="w3-animate-opacity search_keyword">
			<form name="keyword_query" action="Result.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>Compound Information</h5>
				<div class="w3-cell-row w3-border  w3-padding-small">
					<div class="w3-cell w3-mobile" style="width:75%">
						<label><b>Compound name</b></label>
						<input class="w3-input w3-round w3-border" name="compound" type="text">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
							value="Search" style="width:80px;float:right" onclick="append_ms_information(this)">
					</div>
				</div>
				
				<div class="w3-cell-row w3-border w3-padding-small">
					<div class="w3-cell w3-cell-bottom" style="width:60px;padding-right:5px;">
						<select class="w3-select w3-round w3-border" name="op1">
							<option value="and">AND</option>
							<option value="or">OR</option>
						</select>
					</div>
					<div class="w3-cell w3-mobile">
						<label><b>Exact Mass</b></label>
						<input class="w3-input w3-round w3-border" name="mz" type="text" size="10">
					</div>
					<div class="w3-cell w3-mobile" style="width:25%">
						<label><b>Tolerance</b></label>
						<input class="w3-input w3-round w3-border" name="tol" type="text" size="6" value="0.3">
					</div>
				</div>
				
				<div class="w3-cell-row w3-border w3-padding-small">
					<div class="w3-cell w3-cell-bottom" style="width:60px;padding-right:5px;">
						<select class="w3-select w3-round w3-border" name="op2">
							<option value="and">AND</option>
							<option value="or">OR</option>
						</select>
					</div>
					<div class="w3-cell">
						<label><b>Formula ( e.g. C6H7N5, C5H*N5, C5* )</b></label>
						<input class="w3-input w3-round w3-border" name="formula" type="text" size="14">
					</div>
				</div>
			</div>
			<input type="hidden" name="type" value="quick">
			<input type="hidden" name="searchType" value="keyword">
			<input type="hidden" name="sortKey" value="not">
			<input type="hidden" name="sortAction" value="1">
			<input type="hidden" name="pageNo" value="1">
			<input type="hidden" name="exec" value="">
			</form>
		</div>
			
		<div id="Peak" class="w3-animate-opacity search_keyword" style="display:none">
			<h2>Peak</h2>
		</div>

		<div id="InChIKey" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="inchikey_query" action="Result.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>InChIKey</h5>
				<div class="w3-cell-row w3-border  w3-padding-small">
					<div class="w3-cell w3-mobile" style="width:75%">
						<label><b>InChIKey (complete or parts)</b></label>
						<input class="w3-input w3-round w3-border" name="inchikey" type="text">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" value="Search" 
							style="width:80px;float:right" onclick="append_ms_information(this)">
					</div>
				</div>
			</div>
			<input type="hidden" name="type" value="inchikey">
			<input type="hidden" name="searchType" value="inchikey">
			<input type="hidden" name="sortKey" value="not">
			<input type="hidden" name="sortAction" value="1">
			<input type="hidden" name="pageNo" value="1">
			<input type="hidden" name="exec" value="">
			</form>
		</div>
	
		<div id="Splash" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="splash_query" action="Result.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>SPLASH</h5>
				<div class="w3-cell-row w3-border  w3-padding-small">
					<div class="w3-cell w3-mobile" style="width:75%">
						<label><b>SPLASH</b></label>
						<input class="w3-input w3-round w3-border" name="splash" type="text">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" value="Search" 
							style="width:80px;float:right" onclick="append_ms_information(this)">
					</div>
				</div>
			</div>
			<input type="hidden" name="type" value="splash">
			<input type="hidden" name="searchType" value="splash">
			<input type="hidden" name="sortKey" value="not">
			<input type="hidden" name="sortAction" value="1">
			<input type="hidden" name="pageNo" value="1">
			<input type="hidden" name="exec" value="">
			</form>
		</div>
		
		<br>
		
		<div class="w3-container w3-card-4 w3-padding-small">
			<form name="ms_information">
			<h5>Mass Spectrometry Information</h5>
			<div class="w3-border w3-padding-small">
				<b>Instrument Type</b><br>
				<div class="w3-cell-row">
					<c:forEach items="${instrument_info}" var="list">
					<div class="w3-cell w3-mobile">
						<div class="w3-cell-row">
							<div class="w3-cell">
								<input class="w3-check" type="checkbox" name="inst_grp" value="${list.key}">${list.key}
							</div>
							<div class="w3-cell">
								<c:forEach items="${list.value}" var="instrument_type">
								<input class="w3-check" type="checkbox" name="inst" value="${instrument_type}">${instrument_type}<br>
								</c:forEach>
							</div>
						</div>
					</div>
					</c:forEach>
				</div>
			</div>
			<div class="w3-border w3-padding-small">
				<b>MS Type</b><br>
				<input class="w3-check" type="checkbox" name="ms" value="all" onclick="myFunction(this)" checked>All&nbsp;&nbsp;&nbsp;&nbsp;
				<c:forEach items="${ms_info}" var="item">
				<input class="w3-check" type="checkbox" name="ms" value="${item}" onclick="myFunction(this)" checked>${item}
				</c:forEach>
			</div>
			<div class="w3-border w3-padding-small">
				<b>Ion Mode</b><br>
				<input class="w3-radio" type="radio" name="ion" value="0" checked>Both&nbsp;&nbsp;&nbsp;&nbsp;
				<input class="w3-radio" type="radio" name="ion" value="1">Positive
				<input class="w3-radio" type="radio" name="ion" value="-1">Negative
			</div>
			</form>
		</div>
	</div>
  
	<script>
	function openSearch(evt, searchName) {
		var i, x;
		x = document.getElementsByClassName("search_button");
		for (i = 0; i < x.length; i++) {
			x[i].className = x[i].className.replace(" w3-blue", "");
		}
		evt.currentTarget.className += " w3-blue";
				
		x = document.getElementsByClassName("search_keyword");
		for (i = 0; i < x.length; i++) {
			x[i].style.display = "none";
		}
		document.getElementById(searchName).style.display = "block";
	}
	
	function append_ms_information(elmnt) {
		var elements = document.forms['ms_information'].elements;
		var target_form = elmnt.form;
		for (i=0; i<elements.length; i++){
			if (elements[i].checked == true) {
				var hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden"); 
				hiddenField.setAttribute("name", elements[i].name);
				hiddenField.setAttribute("value", elements[i].value);
				// append the newly created control to the form
				target_form.appendChild(hiddenField); 
			}
		}
	}
	
	function myFunction(elmnt) {
		var allms = document.getElementsByName("ms");
		if (elmnt.value == "all") {
			if (elmnt.checked == true) {
				for (i=0; i<allms.length; i++){
					allms[i].checked = true;
				}
			} else {
				for (i=0; i<allms.length; i++){
					allms[i].checked = false;
				}
			}
				
			console.log("'all");
		}
		
		var allms = document.getElementsByName("ms");
		
		for (i=0; i<allms.length; i++){
			console.log(allms[i].value);
		}
	    //var checkBox = document.getElementById("myCheck");
	    //var text = document.getElementById("text");
	   // if (checkBox.checked == true){
	    //    text.style.display = "block";
	    //} else {
	    //   text.style.display = "none";
	    //}
	}
// 	function initSearch() {
// 		var i, x;
// 		x = document.getElementsByClassName("search_keyword");
// 		for (i = 0; i < x.length; i++) {
// 			x[i].style.display = "none";
// 		}
// 		document.getElementById(document.querySelector('input[name="search_keyword"]:checked').value).style.display = "block";
// 	}
	</script>

	<br>
	<div id="copyrightline"></div>
		
</body>
</html>
