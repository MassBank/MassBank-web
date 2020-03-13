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

<head>
	<title>MassBank | Database | Search</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="Search of spectra by chemical name, peak, InChIKey or SPLASH.">
	<meta name="keywords" content="Search,Compound,ExactMass,Formula,InChIKey,SPLASH">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<link href="favicon.ico" rel="icon" type="image/x-icon">
	<link href="favicon.ico" rel="shortcut icon" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
    <link rel="stylesheet" type="text/css" href="css/jquery-confirm.min.css">
	<link rel="stylesheet" type="text/css" href="fontawesome-free-5.9.0-web/css/all.min.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<script src="js/jquery-3.4.1.min.js"></script>
	<script src="js/MassCalc.js"></script>
	<script src="js/svg4everybody-2.1.9.min.js"></script>
	<script src="js/jquery-confirm.min.js"></script>
	<script src="js/search.js"></script>
	<script>svg4everybody();</script>
</head>

<body class="w3-theme-gradient">
	<noscript>
		<div class="w3-panel w3-yellow">
  			<p>Your JavaScript is disabled. To properly show MassBank please enable JavaScript and reload.</p>
  		</div>
  	</noscript>
	
	<header class="w3-container w3-top w3-text-dark-grey w3-grey">
		<div class="w3-bar">
			<div class="w3-left">
				<h1>
					<b>Search</b>
				</h1>
			</div>
			<div style="position: absolute; transform: translateY(-50%); bottom: 0; right: 0">
				<div class="w3-container">
					<div class="w3-text-blue">
						<svg viewBox="0 0 32 28" style="width: 16px">
							<use href="img/arrow.svg#arrow_right" />
						</svg>
						<a id="openMassCalc" class="w3-text-dark-grey" href=""><b>mass calculator</b></a>
					</div>
				</div>
			</div>
		</div>
		<jsp:include page="masscalc.html"/>
	</header>

	<div style="padding-top:74px">
		<jsp:include page="menu.html"/>
	</div>

	<div style="max-width:90%;height:auto;margin:auto;">
		<h3><b>Search for:</b></h3>
		<div class="w3-bar w3-margin-bottom" style="display:flex;flex-wrap:wrap">
			<button class="search_button w3-bar-item w3-round w3-border w3-bottombar w3-border-red w3-white" 
				id="BasicButton" onclick="openSearch('Basic')" style="flex:1 1">Basic Search</button>
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

		<div id="Basic" class="w3-animate-opacity search_keyword" style="display:none">
			<form name="basic_query" class="query" action="Result.jsp">
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
						<textarea class="w3-input w3-round w3-border" id="qpeak" name="qpeak" cols="40" rows="10" required></textarea>
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
								<select class="w3-select w3-round w3-border searchop" name="op0">
									<option value="and">AND</option>
									<option value="or">OR</option>
								</select>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>m/z</b></label>
								<input class="w3-input w3-round w3-border Mass" name="mz0" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>Formula</b></label>
								<input class="w3-input w3-round w3-border Formula" name="fom0" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz1" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom1" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz2" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom2" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz3" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom3" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz4" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom4" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz5" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-teal" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
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
								<select class="w3-select w3-round w3-border searchop" name="op0">
									<option value="and">AND</option>
									<option value="or">OR</option>
								</select>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>m/z</b></label>
								<input class="w3-input w3-round w3-border Mass" name="mz0" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<label><b>Formula</b></label>
								<input class="w3-input w3-round w3-border Formula" name="fom0" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz1" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom1" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz2" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom2" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz3" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom3" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz4" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
								</svg>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Formula" name="fom4" type="text">
							</div>						
						</div>
						<div class="w3-cell-row">
							<div class="w3-cell w3-mobile w3-cell-bottom w3-padding-small searchoptext" 
								style="width:70px;text-align:center">
								<b>AND</b>
							</div>
							<div class="w3-cell w3-mobile w3-padding-small">
								<input class="w3-input w3-round w3-border Mass" name="mz5" type="text">
							</div>
							<div class="w3-cell w3-mobile w3-cell-bottom w3-text-deep-purple" style="width:25px">
								<svg viewBox="0 4 32 36" style="width:25px">
									<use href="img/arrow.svg#arrow_left"/>
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
				
				<input type="hidden" name="type" value="diff">
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
		// get last open search tab from localStorage or load default Basic if empty
		var searchTab = localStorage.getItem("searchName") || "Basic";
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
	<jsp:include page="copyrightline.html"/>

</body>
</html>
