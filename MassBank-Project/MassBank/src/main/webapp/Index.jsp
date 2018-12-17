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
<title>MassBank | ${sitename} Mass Spectral DataBase</title>

<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Mass Spectral DataBase">
	<meta name="keywords" content="Mass,Spectral,Database,MassBank">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<script src="script/jquery-3.3.1.min.js"></script>
	<script src="script/massbank.js"></script>
	<noscript>
		<div class="w3-panel w3-yellow">
  			<p>Your JavaScript is disabled. To properly show MassBank please enable JavaScript and reload.</p>
  		</div>
  	</noscript>
</head>

<body class="w3-theme-gradient">

	<header class="w3-center w3-text-grey w3-topbar w3-border-light-grey">
		<img src="image/sub_logo.jpg" alt="MassBank" style="max-width:100%;height:auto;">
		<h2>
			<b>${sitename}</b>
		</h2>
	</header>

	<div class="w3-row w3-white w3-round" style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-third w3-panel w3-white">
			<div class="w3-panel w3-leftbar w3-border-teal w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="Search"><b>Search</b></a>
					<a href="Search"><img src="image/quick.gif" style="width:100%;"></a>
				</h5>
			</div>
		</div>

		<div class="w3-third w3-panel w3-white">
			<div
				class="w3-panel w3-leftbar w3-border-amber w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="Export"><b>Export</b></a>
					<a href="Export"><img src="image/peak.gif" style="width:100%;"></a>
				</h5>
			</div>
		</div>

		<div class="w3-third w3-panel w3-white">
			<div
				class="w3-panel w3-leftbar w3-border-pink w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="RecordIndex"><b>Record Index</b></a>
					<a href="RecordIndex"><img src="image/list.gif" style="width:100%;"></a>
				</h5>
			</div>
		</div>
	</div>

	<div class="w3-container w3-large" style="max-width:90%;height:auto;margin:auto;">
		<ul class="w3-text-blue">
			<li><a href="./api/services/MassBankAPI?wsdl" target="_blank">WEB-API WSDL</a></li>
		</ul>
		<h4><b>Announcements</b></h4>
		<p>
		Dear customers,<br> The Java applet technology was deprecated.
		Therefore many services of MassBank are out of service (e.g.
		spectral search). The main services quick search, record index and
		record display are working properly. Our apologies for any
		inconvience.
		</p>
	</div>
	
	<div id="copyrightline"></div>

</body>
</html>