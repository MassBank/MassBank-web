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

<head>
	<title>MassBank | ${sitename} Mass Spectral DataBase</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Mass Spectral DataBase">
	<meta name="keywords" content="Mass,Spectral,Database,MassBank">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<link rel="icon" href="/favicon.ico" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<script src="js/jquery-3.5.1.min.js"></script>
	<script src="js/iframeResizer.min.js"></script>
</head>

<body class="w3-theme-gradient">
	<noscript>
		<div class="w3-panel w3-yellow">
  			<p>Your JavaScript is disabled. To properly show MassBank please enable JavaScript and reload.</p>
  		</div>
  	</noscript>
  	
	<header class="w3-center w3-text-grey w3-topbar w3-border-light-grey">
		<img src="img/sub_logo.jpg" alt="MassBank" style="max-width:100%;height:auto;">
		<h2>
			<b>${sitename}</b>
		</h2>
	</header>
	
	<div style="max-width:90%;height:auto;margin:auto;">
		<jsp:include page="menu.html"/>
	</div>

	<div class="w3-row w3-white w3-round" style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-third w3-panel w3-white">
			<div class="w3-panel w3-leftbar w3-border-teal w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="Search"><b>Search</b></a>
					<a href="Search"><img src="img/search.svg" style="width:100%;"></a>
				</h5>
			</div>
		</div>

<!-- 		<div class="w3-third w3-panel w3-white"> -->
<!-- 			<div class="w3-panel w3-leftbar w3-border-amber w3-light-grey w3-text-blue w3-round"> -->
<!-- 				<h5> -->
<!-- 					<a href="Export"><b>Export</b></a> -->
<!-- 					<a href="Export"><img src="img/export.gif" style="width:100%;"></a> -->
<!-- 				</h5> -->
<!-- 			</div> -->
<!-- 		</div> -->

		<div class="w3-third w3-panel w3-white">
			<div class="w3-panel w3-leftbar w3-border-pink w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="RecordIndex"><b>Record Index</b></a>
					<a href="RecordIndex"><img src="img/index.svg" style="width:100%;"></a>
				</h5>
			</div>
		</div>
		
		<div class="w3-third w3-panel w3-white">
			<div class="w3-panel w3-leftbar w3-border-indigo w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="#" onclick="document.getElementById('id01').style.display='block'"><b>Documentation</b></a>
					<a href="#" onclick="document.getElementById('id01').style.display='block'"><img src="img/book.svg" style="width:100%;"></a>
				</h5>
			</div>
		</div>

	</div>
	
	<div style="max-width: 90%; height: auto; margin: auto;">
		<iframe src="https://massbank.github.io/MassBank-documentation/massbank.eu-news.html" 
			id="news" style="min-width: 100%;border:none;"></iframe>
		<script>
  			iFrameResize({ log: true }, '#news')
		</script>
	</div>
	<jsp:include page="copyrightline.html"/>

	<div id="id01" class="w3-modal">
		<div class="w3-modal-content w3-animate-top" style="height: 90%;">
			<header class="w3-container w3-indigo">
				<div class="w3-display-topright">
					<a href="https://massbank.github.io/massbank.eu-documentation/" target="_blank"
						onclick="document.getElementById('id01').style.display='none'" class="w3-button">
						&#8599;
					</a>
					<span onclick="document.getElementById('id01').style.display='none'" class="w3-button">
						&times;
					</span>
				</div>
				<h4>Documentation at
					https://massbank.github.io/massbank.eu-documentation/</h4>
			</header>

			<iframe src="https://massbank.github.io/massbank.eu-documentation/"
				id="docs" style="width: 100%; height: 100%;"></iframe>
		</div>
	</div>
</body>
</html>
