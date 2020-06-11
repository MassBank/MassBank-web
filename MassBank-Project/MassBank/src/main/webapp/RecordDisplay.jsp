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
	<title>${short_name}</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="${description}">
	<meta name="keywords" content="${keywords}">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<link href="favicon.ico" rel="icon" type="image/x-icon">
	<link href="favicon.ico" rel="shortcut icon" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<link rel="stylesheet" type="text/css" href="fontawesome-free-5.9.0-web/css/all.min.css">
	<script src="js/jquery-3.4.1.js"></script>
	<script src="js/MassCalc.js"></script>
	<script src="js/svg4everybody-2.1.9.min.js"></script>
	<script>svg4everybody();</script>
	<!-- SpeckTackle CSS containing chart stylings -->
    <link rel="stylesheet" type="text/css" href="css/st.css">
    <!-- SpeckTackle dependencies-->
    <script src="js/d3.v3.min.js"></script>
	<!-- SpeckTackle library -->
    <script src="js/st.js"></script>
    <!-- SpeckTackle MassBank loading script-->
	<script src="js/massbank_specktackle.js"></script>
    
	<!-- 	hier anpassen -->
<!-- 	<meta name="variableMeasured" content="m/z"> -->

${structureddata}
</head>

<script type="text/javascript">
	var data=${peaklist};
</script>

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
					<b>MassBank Record: ${accession}</b>
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
	
	<div class="w3-padding">
		<h3><b>${record_title}</b></h3>
		<c:if test="${not isDeprecated}">
			<div class="w3-row">
				<div class="w3-twothird w3-text-grey w3-small">
					Mass Spectrum
					<div id="spectrum_canvas" style="height:200px; width:600px; max-width:100%; background-color:white"></div>
				</div>
				<div class="w3-third w3-text-grey w3-small">
					Chemical Structure<br>
					${svg}
				</div>
			</div>
			<div class="w3-row">
				<div class="w3-twothird w3-text-grey w3-small">
					<a
						href="https://metabolomics-usi.ucsd.edu/spectrum/?usi=mzspec:MASSBANK:${accession}"
						target=”_blank”>metabolomics-usi visualisation</a>
				</div>
			</div>
		</c:if>
	</div>
	
	<div class="w3-padding">
		<hr>
		${recordstring}
	</div>
	
	<br>
	<jsp:include page="copyrightline.html"/>
	
</body>
</html>