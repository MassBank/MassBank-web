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
<html lang="en" style="max-width:900px">
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
<script src="script/newsearch.js"></script>
</head>

<body class="w3-theme-gradient">
	
	<header class="w3-cell-row w3-text-grey">
		<div class="w3-container w3-cell w3-mobile" style="width:60%">
			<h1>
				<b>Quick Search</b>
			</h1>
		</div>
		<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align w3-text-blue">
			<svg viewBox="0 0 251 222" xmlns="http://www.w3.org/2000/svg" style="width:15px">
				<path  fill="currentColor" 
					d="m0 91v40c0 6.6 5.4 12 12 12h116v67c0 10.7 12.9 16 20.5 8.5l99-99c4.7-4.7 4.7-12.3 0-17l-99-99c-7.6-7.6-20.5-2.2-20.5 8.5v67h-116c-6.6 0-12 5.4-12 12z"/>
			</svg>
			<a class="text w3-text-grey"  href="javascript:openMassCalc();"><b>mass calculator</b></a>
		</div>
		<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align w3-text-blue">
			<svg viewBox="0 0 251 222" xmlns="http://www.w3.org/2000/svg" style="width:15px">
				<path  fill="currentColor" 
					d="m0 91v40c0 6.6 5.4 12 12 12h116v67c0 10.7 12.9 16 20.5 8.5l99-99c4.7-4.7 4.7-12.3 0-17l-99-99c-7.6-7.6-20.5-2.2-20.5 8.5v67h-116c-6.6 0-12 5.4-12 12z"/>
			</svg>
			<a class="text w3-text-grey" href="manuals/UserManual_en.pdf" target="_blank"><b>user manual</b></a>
		</div>
	</header>
	
	<div class="w3-border-bottom w3-border-dark-grey w3-padding-16" id="menu"></div>
	
	<div>
		<h3><b>Search for:</b></h3>
		<div class="w3-bar w3-margin-bottom" style="display:flex;flex-wrap:wrap">
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-red w3-white" 
				id="KeywordButton" onclick="openSearch('Keyword')" style="flex:1 1">Keyword</button>
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-amber w3-white"
				id="PeakListButton" onclick="openSearch('PeakList')" style="flex:1 1">Peak List</button>
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-teal w3-white" 
				id="PeakButton" onclick="openSearch('Peak')" style="flex:1 1">Peaks</button>
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-deep-purple w3-white"
				id="PeakDifferenceButton" onclick="openSearch('PeakDifference')" style="flex:1 1">Peak Differences</button>
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-brown w3-white"
				id="InChIKeyButton" onclick="openSearch('InChIKey')" style="flex:1 1">InChIKey</button>
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-cyan w3-white" 	
				id="SplashButton" onclick="openSearch('Splash')" style="flex:1 1">SPLASH</button>
		</div>

		<div id="Keyword" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="keyword_query" class="query" action="Result.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>Compound Information</h5>
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>Compound name</b></label>
						<input class="w3-input w3-round w3-border" name="compound" type="text">
					</div>
				</div>
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:70px">
						<select class="w3-select w3-round w3-border" name="op1">
							<option value="and">AND</option>
							<option value="or">OR</option>
						</select>
					</div>
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>Exact Mass</b></label>
						<input class="w3-input w3-round w3-border" name="mz" type="text" size="10">
					</div>
					<div class="w3-cell w3-mobile w3-padding-small" style="width:25%">
						<label><b>Tolerance</b></label>
						<input class="w3-input w3-round w3-border" name="tol" type="text" size="6" value="0.3">
					</div>
				</div>
				
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:70px">
						<select class="w3-select w3-round w3-border" name="op2">
							<option value="and">AND</option>
							<option value="or">OR</option>
						</select>
					</div>
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>Formula ( e.g. C6H7N5, C5H*N5, C5* )</b></label>
						<input class="w3-input w3-round w3-border" name="formula" type="text" size="10">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:25%">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
							value="Search" style="width:80px;float:right">
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
			<form name="peaklist_query" class="query" method="post" action="QpeakResult.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>Peak List</h5>
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>Peak Data (<i>m/z</i> and relative intensities(0-999), delimited by a space)</b></label>
						<textarea class="w3-input w3-round w3-border" id="qpeak" name="qpeak" cols="40" rows="10"></textarea>
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small">
						<input type="button" class="w3-input w3-bar-item w3-round w3-border" 
							value="Example1" onclick="insertExample1()"><br>
						<input type="button" class="w3-input w3-bar-item w3-round w3-border" 
							value="Example2" onclick="insertExample2()">
					</div>
				</div>
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>Cutoff threshold of relative intensities</b></label>
						<input class="w3-input w3-round w3-border" name="CUTOFF" type="text" value="5">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small">
						<label><b>Number of Results</b></label>
						<select class="w3-select w3-round w3-border" name="num" style="display:block;">
							<option value="20" selected>20</option>
							<option value="50">50</option>
							<option value="100">100</option>
							<option value="500">500</option>
						</select>
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:25%">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
							value="Search" style="width:80px;float:right">
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
		
		<div id="Peak" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="peak_query" class="query" method="get" action="Result.jsp">
				<div class="w3-container w3-card-4 w3-padding-small">
					<h5>Search for Peaks</h5>
					<div class="w3-border">
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:70px">
								<select class="w3-select w3-round w3-border" name="op0">
									<option value="and">AND</option>
									<option value="or">OR</option>
								</select>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>m/z</b></label>
								<input class="w3-input w3-round w3-border Mass" name="mz0" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>Formula</b></label>
								<input class="w3-input w3-round w3-border Formula" name="fom0" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz1" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom1" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz2" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom2" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz3" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom3" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz4" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom4" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz5" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom5" type="text">
							</div>						
						</div>
					</div>	
					
					<div class="w3-cell-row w3-border">
						<div class="w3-cell w3-mobile w3-padding-small">
							<label><b>Rel.Intensity</b></label>
							<input class="w3-input w3-round w3-border" name="int" type="text" value="100">
						</div>
						<div class="w3-cell w3-mobile w3-padding-small">
							<label><b>Tolerance</b></label>
							<input class="w3-input w3-round w3-border" name="tol" type="text" value="0.3">
						</div>
						<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:25%">
							<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
								value="Search" style="width:80px;float:right">
						</div>
					</div>
				</div>
				
				<input type="hidden" name="type" value="peak">
				<input type="hidden" name="mode" value="and">
				<input type="hidden" name="op1" value="and">
				<input type="hidden" name="op2" value="and">
				<input type="hidden" name="op3" value="and">
				<input type="hidden" name="op4" value="and">
				<input type="hidden" name="op5" value="and">
				
				<input type="hidden" name="sortKey" value="name">
				<input type="hidden" name="sortAction" value="1">
				<input type="hidden" name="pageNo" value="1">
				<input type="hidden" name="exec" value="">
				
				<input type="hidden" name ="searchby" value="mz">
				
				<input type="hidden" name="searchof" value="peak">
			</form>
		</div>
		
		<div id="PeakDifference" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="peak_query" class="query" method="get" action="Result.jsp">
				<div class="w3-container w3-card-4 w3-padding-small">
					<h5>Search for Peak Differences</h5>
					<div class="w3-border">
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:70px">
								<select class="w3-select w3-round w3-border" name="op0">
									<option value="and">AND</option>
									<option value="or">OR</option>
								</select>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>m/z</b></label>
								<input class="w3-input w3-round w3-border Mass" name="mz0" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>Formula</b></label>
								<input class="w3-input w3-round w3-border Formula" name="fom0" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz1" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom1" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz2" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom2" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz3" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom3" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz4" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom4" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz5" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 0 251 272" xmlns="http://www.w3.org/2000/svg" style="width:25px">
								<path  fill="currentColor" 
									d="m251 91v40c0 6.6-5.4 12-12 12h-116v67c0 10.7-12.9 16-20.5 8.5l-99-99c-4.7-4.7-4.7-12.3 0-17l99-99c7.6-7.6 20.5-2.2 20.5 8.5v67h116c6.6 0 12 5.4 12 12z"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom5" type="text">
							</div>						
						</div>
					</div>	
					
					<div class="w3-cell-row w3-border">
						<div class="w3-cell w3-mobile w3-padding-small">
							<label><b>Rel.Intensity</b></label>
							<input class="w3-input w3-round w3-border" name="int" type="text" value="100">
						</div>
						<div class="w3-cell w3-mobile w3-padding-small">
							<label><b>Tolerance</b></label>
							<input class="w3-input w3-round w3-border" name="tol" type="text" value="0.3">
						</div>
						<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:25%">
							<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
								value="Search" style="width:80px;float:right">
						</div>
					</div>
				</div>
				
				<input type="hidden" name="type" value="peak">
				<input type="hidden" name="mode" value="and">
				<input type="hidden" name="op1" value="and">
				<input type="hidden" name="op2" value="and">
				<input type="hidden" name="op3" value="and">
				<input type="hidden" name="op4" value="and">
				<input type="hidden" name="op5" value="and">
				
				<input type="hidden" name="sortKey" value="name">
				<input type="hidden" name="sortAction" value="1">
				<input type="hidden" name="pageNo" value="1">
				<input type="hidden" name="exec" value="">
				
				<input type="hidden" name ="searchby" value="mz">
				
				<input type="hidden" name="searchof" value="diff">
			</form>
		</div>

		<div id="InChIKey" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="inchikey_query" class="query" action="Result.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>InChIKey</h5>
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>InChIKey (complete or parts)</b></label>
						<input class="w3-input w3-round w3-border" name="inchikey" type="text">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:25%">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
							value="Search" style="width:80px;float:right">
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
			<form name="splash_query" class="query" action="Result.jsp">
			<div class="w3-container w3-card-4 w3-padding-small">
				<h5>SPLASH</h5>
				<div class="w3-cell-row w3-border">
					<div class="w3-cell w3-mobile w3-padding-small">
						<label><b>SPLASH</b></label>
						<input class="w3-input w3-round w3-border" name="splash" type="text">
					</div>
					<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small" style="width:25%">
						<input type="submit" class="w3-input w3-round w3-border w3-blue w3-btn" 
							value="Search" style="width:80px;float:right">
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
			$(".search_button").removeClass("w3-light-grey").addClass("w3-bottombar w3-white");
			$("#"+searchName+"Button").removeClass("w3-bottombar w3-white").addClass("w3-light-grey");
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
					<div class="w3-cell w3-mobile ms_information_column w3-border w3-padding-small">
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
  
	<br>
	<div id="copyrightline"></div>
		
</body>
</html>
