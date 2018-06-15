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

<body class="w3-theme-gradient" onload="initSearch()">
	
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
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-deep-purple" onclick="openSearch(event,'InChIKey')">Search by Peak</button>
			<button class="search_button w3-bar-item w3-round w3-bottombar w3-border-cyan" onclick="openSearch(event,'Splash')">Search by Peak</button>
		</div>
	  
		<div id="Keyword" class="w3-animate-opacity search_keyword">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>Compound Information</h5>
				<div class="w3-cell-row w3-border  w3-padding-small">
					<div class="w3-cell w3-mobile" style="width:75%">
						<label><b>Compound name</b></label>
						<input class="w3-input w3-round w3-border" name="compound" type="text">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" value="Search" style="width:80px;float:right">
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
			<br>
			
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>Mass Spectrometry Information</h5>
				<div class="w3-border w3-padding-small">
					<b>Instrument Type</b><br>
					<div class="w3-cell-row">
						<div class="w3-cell w3-mobile">
							<div class="w3-cell-row">
								<div class="w3-cell">
									<input class="w3-check" type="checkbox" name="inst_grp" value="EI">EI
								</div>
								<div class="w3-cell">
									<input class="w3-check" type="checkbox" name="inst" value="EI-B">EI-B<br>
									<input class="w3-check" type="checkbox" name="inst" value="EI-EBEB">EI-EBEB<br>
									<input class="w3-check" type="checkbox" name="inst" value="GC-EI-Q">GC-EI-Q<br>
									<input class="w3-check" type="checkbox" name="inst" value="GC-EI-QQ">GC-EI-QQ<br>
									<input class="w3-check" type="checkbox" name="inst" value="GC-EI-TOF">GC-EI-TOF<br>
								</div>
							</div>
						</div>
						<div class="w3-cell w3-mobile">
							<div class="w3-cell-row">
								<div class="w3-cell">
									<input class="w3-check" type="checkbox" name="inst_grp" value="ESI" checked>ESI
								</div>
								<div class="w3-cell">
									<input class="w3-check" type="checkbox" name="inst" value="CE-ESI-TOF" checked>CE-ESI-TOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="ESI-ITFT" checked>ESI-ITFT<br>
									<input class="w3-check" type="checkbox" name="inst" value="ESI-ITTOF" checked>ESI-ITTOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="ESI-QTOF" checked>ESI-QTOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="ESI-TOF" checked>ESI-TOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-IT" checked>LC-ESI-IT<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-ITFT" checked>LC-ESI-ITFT<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-ITTOF" checked>LC-ESI-ITTOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-Q" checked>LC-ESI-Q<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-QFT" checked>LC-ESI-QFT<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-QIT" checked>LC-ESI-QIT<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-QQ" checked>LC-ESI-QQ<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-QTOF" checked>LC-ESI-QTOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-ESI-TOF" checked>LC-ESI-TOF<br>
								</div>
							</div>
						</div>
						<div class="w3-cell w3-mobile">
							<div class="w3-cell-row">
								<div class="w3-cell w3-mobile">
									<input class="w3-check" type="checkbox" name="inst_grp" value="Others" >Others
								</div>
								<div class="w3-cell">
									<input class="w3-check" type="checkbox" name="inst" value="APCI-ITFT">APCI-ITFT<br>
									<input class="w3-check" type="checkbox" name="inst" value="APCI-ITTOF">APCI-ITTOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="CI-B">CI-B<br>
									<input class="w3-check" type="checkbox" name="inst" value="FAB-B">FAB-B<br>
									<input class="w3-check" type="checkbox" name="inst" value="FAB-BE">FAB-BE<br>
									<input class="w3-check" type="checkbox" name="inst" value="FAB-EB">FAB-EB<br>
									<input class="w3-check" type="checkbox" name="inst" value="FAB-EBEB">FAB-EBEB<br>
									<input class="w3-check" type="checkbox" name="inst" value="FD-B">FD-B<br>
									<input class="w3-check" type="checkbox" name="inst" value="FI-B">FI-B<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-APCI-ITFT">LC-APCI-ITFT<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-APCI-Q">LC-APCI-Q<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-APCI-QTOF">LC-APCI-QTOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="LC-APPI-QQ">LC-APPI-QQ<br>
									<input class="w3-check" type="checkbox" name="inst" value="MALDI-QIT">MALDI-QIT<br>
									<input class="w3-check" type="checkbox" name="inst" value="MALDI-TOF">MALDI-TOF<br>
									<input class="w3-check" type="checkbox" name="inst" value="MALDI-TOFTOF">MALDI-TOFTOF<br>
								</div>
							</div>
						</div>
					</div>
				</div>
				
				<div class="w3-border w3-padding-small">
					<b>MS Type</b><br>
					<input class="w3-check" type="checkbox" name="ms" value="all" checked>All&nbsp;&nbsp;&nbsp;&nbsp;
					<input class="w3-check" type="checkbox" name="ms" value="MS" checked>MS
					<input class="w3-check" type="checkbox" name="ms" value="MS2" checked>MS2
					<input class="w3-check" type="checkbox" name="ms" value="MS3" checked>MS3
					<input class="w3-check" type="checkbox" name="ms" value="MS4" checked>MS4
				</div>
				<div class="w3-border w3-padding-small">
					<b>Ion Mode</b><br>
					<input class="w3-check" type="checkbox" name="ion" value="0" checked>Both&nbsp;&nbsp;&nbsp;&nbsp;
					<input class="w3-check" type="checkbox" name="ion" value="1" checked>Positive
					<input class="w3-check" type="checkbox" name="ion" value="-1" checked>Negative
				</div>
			</div>
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
