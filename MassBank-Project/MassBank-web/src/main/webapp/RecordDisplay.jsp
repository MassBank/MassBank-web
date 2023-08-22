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
	<title>${short_name}</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="${description}">
	<meta name="keywords" content="${keywords}">
	<meta name="author" content="${author}">
	<meta name="copyright" content="MassBank Consortium"/>
	<link rel="icon" href="favicon.ico" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<link rel="preconnect" href="https://fonts.gstatic.com">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Mulish:ital,wght@0,400;0,600;1,400;1,600">
	<script src="js/jquery-3.5.1.min.js"></script>
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
	
	<script type="application/ld+json">
${structureddata}
	</script>
</head>

<body class="w3-theme-gradient">
	<script>
		var data=${peaklist};
	</script>

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
					<b>MassBank Record: ${accession}</b>
				</h1>
			</div>
		</div>	
	</header>
	
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
						href="https://metabolomics-usi.gnps2.org/spectrum/?usi=mzspec:MASSBANK::accession:${accession}"
						target="_blank">metabolomics-usi visualisation</a>
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
