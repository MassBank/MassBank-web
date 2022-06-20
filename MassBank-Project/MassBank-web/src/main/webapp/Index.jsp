<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!-- Copyright (C) 2021 MassBank consortium -->

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
	<title>MassBank | ${sitename} Mass Spectral DataBase</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Mass Spectral DataBase">
	<meta name="keywords" content="Mass,Spectral,Database,MassBank">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="MassBank Consortium"/>
	<link rel="icon" href="favicon.ico" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<link rel="preconnect" href="https://fonts.gstatic.com">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Mulish:ital,wght@0,400;0,600;1,400;1,600">
	<script src="js/jquery-3.5.1.min.js"></script>
	<script src="js/svg4everybody-2.1.9.min.js"></script>
	<script src="js/iframeResizer.min.js"></script>
	<script>svg4everybody();</script>
</head>

<body class="w3-theme-gradient">
	
	<noscript>
		<div class="w3-panel w3-yellow">
			<p>Your JavaScript is disabled. To properly show MassBank please enable JavaScript and reload.</p>
		</div>
	</noscript>
	
		<div>
		<jsp:include page="menu.html"/>
	</div>
	
	<header class="w3-container w3-text-dark-grey w3-grey">
		<div class="w3-bar">
			<div class="w3-left">
				<h1>
					<b>${sitename}</b>
				</h1>
			</div>
		</div>	
	</header>
	
	<div class="w3-container w3-margin-top w3-section">
		<div class="w3-cell-row w3-section">
			<div class="w3-cell w3-mobile w3-cell-middle" style="width:40%">
				<img src="img/logo.svg" alt="MassBank logo" style="width:100%">
			</div>
			<div class="w3-cell w3-mobile" style="width:10%"></div>
			
			<div class="w3-cell w3-mobile w3-cell-middle">
				<div class="w3-col w3-xlarge">
				
					<div class="w3-margin-bottom w3-animate-right w3-blue w3-text-dark-grey w3-hover-text-white w3-padding w3-round">
						<a href="Search" style="text-decoration: none;"><b>>> Search Spectra</b></a>
					</div>
					<div class="w3-cell-row">
						<div class="w3-cell" style="width:25%"></div>
						<div class="w3-cell w3-animate-right w3-blue w3-text-dark-grey w3-hover-text-white w3-padding w3-round">
							<a href="About" style="text-decoration: none;"><b>>> Learn More</b></a>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="w3-large">
			MassBank is a community effort and <span class="w3-text-blue"><strong>you are invited to contribute</strong></span>. 
			Please refer to our <a href="https://massbank.github.io/MassBank-documentation/" target="_blank">contributor documentation</a> 
			and get in touch via <a href="https://github.com/MassBank/MassBank-data" target="_blank">github</a> or <a href="mailto:massbank@massbank.eu">email</a>. 
		</div>
	</div>
	
	<div>
		<iframe src="https://massbank.github.io/MassBank-documentation/news.html" 
			id="news" style="min-width: 100%;border:none;"></iframe>
		<script>
			iFrameResize({ log: true }, '#news')
		</script>
	</div>
	
	<jsp:include page="copyrightline.html"/>
	
</body>
</html>