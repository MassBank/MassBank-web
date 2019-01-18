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
<html lang="en" style="max-width:90%"">
<title>${shortName} Mass Spectrum</title>

<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="${description}">
	<meta name="keywords" content="${accession}, ${shortName}, ${inchiKey}, mass spectrum, MassBank record, mass spectrometry, mass spectral library">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<link rel="stylesheet" type="text/css" href="css.new/w3.css">
	<link rel="stylesheet" type="text/css" href="css.new/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css.new/massbank.css">
	<script src="js/jquery-3.3.1.min.js"></script>
	<script src="script/massbank.js"></script>
	<script src="script/search.js"></script>
	<script src="js/svg4everybody-2.1.9.min.js"></script>
	<script> svg4everybody();</script>

	<!-- 	hier anpassen -->
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="variableMeasured" content="m/z">
	
	<link rel="stylesheet" type="text/css" href="css/Common.css">
	<script type="text/javascript" src="script/Common.js"></script>
	<!-- SpeckTackle dependencies-->
	<script type="text/javascript" src="script/jquery-1.8.3.min.js" ></script>
	<script type="text/javascript" src="script/d3.v3.min.js"></script>
	<!-- SpeckTackle library-->
	<script type="text/javascript" src="script/st.min.js" charset="utf-8"></script>
	<!-- SpeckTackle style sheet-->
	<link rel="stylesheet" href="css/st.css" type="text/css" />
	<!-- SpeckTackle MassBank loading script-->
	<script type="text/javascript" src="script/massbank_specktackle.js"></script>
</head>

<body class="w3-theme-gradient" typeof="schema:WebPage">	

	<header class="w3-cell-row w3-text-grey">
		<div class="w3-container w3-cell w3-mobile" style="width:60%">
			<h1>
				<b>MassBank Record: ${accession}</b>
			</h1>
		</div>
		<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align w3-text-blue">
			<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align w3-text-blue">
			<svg viewBox="0 0 32 28" style="width:16px">
				<use href="img/arrow.svg#arrow_right"/>
			</svg>
			<a class="text w3-text-grey"  href="javascript:openMassCalc();"><b>mass calculator</b></a>
		</div>
		<div class="w3-container w3-cell w3-mobile w3-cell-middle w3-right-align w3-text-blue">
			<svg viewBox="0 0 32 28" style="width:16px">
				<use href="img/arrow.svg#arrow_right"/>
			</svg>
			<a class="text w3-text-grey" href="manuals/UserManual_en.pdf" target="_blank"><b>user manual</b></a>
		</div>
	</header>
	
	<div class="w3-border-bottom w3-border-dark-grey w3-padding-16" id="menu"></div>
	
	<div style="max-width:90%;height:auto;margin:auto;">
		<h3><b>${recordTitle}</b></h3>
		<div class="w3-row w3-padding-small">
			<div class="w3-twothird w3-text-grey w3-small">
				Mass Spectrum
				<div id="spectrum_canvas" peaks="${peaks}" style="height:200px; width:600px; background-color:white"></div>
			</div>
			<div class="w3-third w3-text-grey w3-small">
				Chemical Structure<br>
				<a href="figure/${accession}.svg">
					<img src="figure/${accession}.svg" style="width:100%">
				</a>
			</div>
		</div>
	</div>
	
	<div class="monospace w3-small" style="max-width:90%;height:auto;margin:auto;overflow-x:scroll;white-space:nowrap">
		${recordstring}
	</div>
	
	<br>
	<div id="copyrightline"></div>
	</body>
${structureddata}
</html>