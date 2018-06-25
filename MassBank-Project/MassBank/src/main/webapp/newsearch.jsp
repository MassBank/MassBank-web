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
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-red w3-blue" id="KeywordButton" onclick="openSearch('Keyword')">Search by Keyword</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-amber" id="PeakListButton" onclick="openSearch('PeakList')">Search by Peak List</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-deep-purple" id="InChIKeyButton" onclick="openSearch('InChIKey')">Search by InChIKey</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-cyan" id="SplashButton" onclick="openSearch('Splash')">Search by SPLASH</button>
		</div>

		<div id="Keyword" class="w3-animate-opacity search_keyword" style="display:none">
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
			
		<div id="PeakList" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="peaklist_query" method="post" action="QpeakResult.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>Peak List</h5>
				<div class="w3-border w3-padding-small">
					<div class="w3-cell-row">
						<div class="w3-cell w3-mobile" style="width:75%">
							<label><b>Peak Data (<i>m/z</i> and relative intensities(0-999), delimited by a space)</b></label>
							<textarea class="w3-input w3-round w3-border" id="qpeak" name="qpeak" cols="40" rows="10"></textarea>
						</div>
						<div class="w3-cell w3-mobile w3-cell-bottom">
							<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" value="Search" 
								style="width:80px;float:right" onclick="append_ms_information(this)">
<!-- 								<input type="submit" value="Search" onclick="beforeSubmit(); return checkSubmit(0);" class="search"> -->
						</div>
					</div>
					<div class="w3-bar" style="padding-top:4px">
						<input type="button" class="w3-input w3-bar-item w3-round w3-border" value="Example1" onclick="insertExample1()">
						<input type="button" class="w3-input w3-bar-item w3-round w3-border" value="Example2" onclick="insertExample2()">
					</div>
				</div>
				<div class="w3-cell-row w3-border w3-padding-small">
					<div class="w3-cell w3-mobile" style="width:40%">
						<label><b>Cutoff threshold of relative intensities</b></label>
						<input class="w3-input w3-round w3-border" name="CUTOFF" type="text" value="5">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom">
						<label><b>Number of Results</b></label>
						<select class="w3-input w3-round w3-border" name="num" style="width:30%">
							<option value="20" selected>20</option>
							<option value="50">50</option>
							<option value="100">100</option>
							<option value="500">500</option>
						</select>
					</div>
				</div>
			</div>
			<input type="hidden" name="type" value="quick">
			<input type="hidden" name="searchType" value="peak">
			<input type="hidden" name="sortKey" value="not">
			<input type="hidden" name="sortAction" value="1">
			<input type="hidden" name="pageNo" value="1">
			<input type="hidden" name="exec" value="">
			</form>
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
		
		<script>
		// setting the search tab from localStorage here to prevent flickering
		function openSearch(searchName) {
			$(".search_button").removeClass("w3-blue");
			$("#"+searchName+"Button").addClass("w3-blue");
			$(".search_keyword").hide();
			$("#"+searchName).show();
			localStorage.setItem("searchName", searchName);
		}
		// get last open search tab from localStorage or load default Keyword if empty
		var searchTab = localStorage.getItem("searchName") || "Keyword";
		// show search tab
		openSearch(searchTab);
		</script>
		
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
								<input class="w3-check" id="${list.key}" type="checkbox" name="inst_grp" value="${list.key}" onclick="masterclick(this)">${list.key}
							</div>
							<div class="w3-cell">
								<c:forEach items="${list.value}" var="instrument_type">
								<input class="w3-check ${list.key}" id="${instrument_type}" type="checkbox" name="inst" value="${instrument_type}" onclick="itemclick(this)">${instrument_type}<br>
								</c:forEach>
							</div>
						</div>
					</div>
					</c:forEach>
				</div>
			</div>
			<div class="w3-border w3-padding-small">
				<b>MS Type</b><br>
				<input class="w3-check" id="ms" type="checkbox" name="ms" value="all" onclick="masterclick(this)">All&nbsp;&nbsp;&nbsp;&nbsp;
				<c:forEach items="${ms_info}" var="item">
				<input class="w3-check ms" id="${item}" type="checkbox" name="ms" value="${item}" onclick="itemclick(this)">${item}
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
	function append_ms_information(elmnt) {
		var elements = document.forms["ms_information"].elements;
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
	
	function masterclick(elmnt) {
		// copy the state of the master checkbox to all items
		$("."+elmnt.id).prop("checked", elmnt.checked);
	}
	
	function itemclick(elmnt) {
		var id = elmnt.classList[1];
		// if one item gets unchecked master should be uncheck
		if (elmnt.checked == false) {
		 	$("#"+id).prop("checked",false);
		}
		else {
			// if all items are checked master should be checked
			if ($("."+id).length == $("."+id+":checked").length) $("#"+id).prop("checked",true)
		}
	}
	
	function insertExample1() {
		$("#qpeak").val("273.096 22\n289.086 107\n290.118 14\n291.096 999\n"
			  + "292.113 162\n293.054 34\n579.169 37\n580.179 15\n");
	}
	
	function insertExample2() {
		$("#qpeak").val("70 51; 71 13; 72 49; 73 999; 74 98;"
		  + "75 158; 76 21; 77 235; 78 21; 79 77;"
		  + "80 5; 81 3; 82 1; 83 1; 84 8;"
		  + "85 4; 86 28; 87 12; 88 22; 89 4;"
		  + "90 2; 91 5; 92 2; 94 14; 95 1;"
		  + "96 2; 97 1; 98 3; 99 3; 100 67;"
		  + "101 20; 102 19; 103 44; 104 6; 105 5;"
		  + "106 1; 107 26; 108 1; 109 1; 110 15;"
		  + "112 1; 113 2; 114 10; 115 13; 116 845;"
		  + "117 105; 118 40; 119 5; 126 1; 127 5;"
		  + "128 15; 129 3; 130 50; 131 23; 132 9;"
		  + "133 23; 134 58; 135 7; 136 2; 143 2;"
		  + "144 4; 145 1; 146 5; 147 183; 148 30;"
		  + "149 15; 150 2; 174 1; 184 12; 185 1;"
		  + "190 34; 191 8; 192 2; 199 2; 218 10;"
		  + "219 2; 220 1;");
	}
	
	$(document).ready(function() {
		function updateStorage() {
			$checkboxes.each(function() {
				formValues[this.id] = this.checked;
			});
			localStorage.setItem("formValues", JSON.stringify(formValues));
		}
		// save all checkboxes in ms_information form, state will persist reload
		var $checkboxes = $("[name='ms_information'] :checkbox");
		// get last values from localStorage or load defaults if empty
		var formValues = JSON.parse(localStorage.getItem('formValues'));
		// load defaults if empty
		if (formValues == null) {
			formValues = {};
			$('#ESI').trigger('click');
			$('#ms').trigger('click');
			updateStorage();
		};
		// set checkboxes according to localStorage
		$.each(formValues, function(key, value) {
			$("#" + key).prop('checked', value);
		});
		// update checkbox state on each change
		$checkboxes.on("change", updateStorage);
	});
	</script>

	<br>
	<div id="copyrightline"></div>
		
</body>
</html>
