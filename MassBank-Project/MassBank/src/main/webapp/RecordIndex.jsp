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
	<title>MassBank | Database | Record Index</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="Categorized list of spectra in MassBank.eu. Here we list up all spectra in a specific category including contributors, instrument types and ionization modes.">
	<meta name="keywords" content="APCI,ITFT,QFT,ESI,EI,LC,IT,GC,TOF,QTOF,FAB,MALDI,APPI,MS,MS/MS,MS2,MS3,MS4,CI,FI,FD,QQ,Merged,Positive,Negative,QIT,ITTOF,EB,mass spectra,MassBank,m/z">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" >
	<link href="favicon.ico" rel="icon" type="image/x-icon">
	<link href="favicon.ico" rel="shortcut icon" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<link rel="stylesheet" type="text/css" href="fontawesome-free-5.9.0-web/css/all.min.css">
	<script src="js/jquery-3.4.1.min.js"></script>
	<script src="js/MassCalc.js"></script>
	<script src="js/svg4everybody-2.1.9.min.js"></script>
	<script>svg4everybody();</script>
</head>


<body class="w3-theme-gradient">
	<noscript>
		<div class="w3-panel w3-yellow">
  			<p>Your JavaScript is disabled. To properly show MassBank please enable JavaScript and reload.</p>
  		</div>
  	</noscript>
  	
	<main context="http://schema.org" property="schema:about" resource="https://massbank.eu/MassBank/RecordIndex" typeof="schema:DataCatalog" >
		<div style="display:none" property="schema:citation" typeof="schema:ScholarlyArticle">
			<div property="schema:name">Horai, Arita, Kanaya, Nihei, Ikeda, Suwa, Ojima, Tanaka, Tanaka, Aoshima, Oda, Kakazu, Kusano, Tohge, Matsuda, Sawada, Hirai, Nakanishi, Ikeda, Akimoto, Maoka, Takahashi, Ara, Sakurai, Suzuki, Shibata, Neumann, Iida, Tanaka, Funatsu, Matsuura, Soga, Taguchi, Saito, Nishioka. MassBank: a public repository for sharing mass spectral data for life sciences. Journal of mass spectrometry. 2010 Jul;45(7):703-14. doi: 10.1002/jms.1777.</div>
			<div property="schema:headline">MassBank: a public repository for sharing mass spectral data for life sciences.</div>
			<div property="schema:image">https://massbank.eu/MassBank/img/sub_logo.jpg</div>
		</div>
		<div style="display:none" property="schema:description">
			<div property="schema:name">Categorized list of spectra in MassBank.eu. Here we list up all spectra in a specific category including contributors, instrument types and ionization modes.</div>
		</div>
		<div style="display:none" property="schema:headline">
			<div property="schema:name">European MassBank (NORMAN MassBank)</div>
		</div>
		<div style="display:none" property="schema:keywords">
			<div property="schema:name">APCI,ITFT,QFT,ESI,EI,LC,IT,GC,TOF,QTOF,FAB,MALDI,APPI,MS,MS/MS,MS2,MS3,MS4,CI,FI,FD,QQ,Merged,Positive,Negative,QIT,ITTOF,EB,mass spectra,MassBank,m/z</div>
		</div>
		<div style="display:none" property="schema:name">
			<div property="schema:name">European MassBank (NORMAN MassBank)</div>
		</div>
		<div style="display:none" property="schema:mainEntity">
			<div property="schema:name">Record index of the European MassBank (NORMAN MassBank)</div>
		</div>
		<div style="display:none" property="schema:offers">
			<div property="schema:name">Mass spectra of pure chemicals (standards)</div>
		</div>
		<div style="display:none" property="schema:producer">
			<div property="schema:name">MassBank consortium</div>
		</div>
		<div style="display:none" property="schema:provider">
			<div property="schema:name">MassBank consortium</div>
		</div>
	</main>

	<header class="w3-container w3-top w3-text-dark-grey w3-grey">
		<div class="w3-bar">
			<div class="w3-left">
				<h1>
					<b>Record Index</b>
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
	
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Contributor</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:280px 5;-moz-columns:280px 5;columns:280px 5;margin:0px;list-style:none;">
				<c:forEach items="${sites}" var="site">
					<li>
						<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=site&srchkey=${site.key}&sortKey=name&sortAction=1&pageNo=1&exec="
						title="${site.key}" target="_self">${site.key}</a> (${site.value})
					</li>
				</c:forEach>
			</ul>
		</div>
	</div> 
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Instrument Type</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:160px 5;-moz-columns:160px 5;columns:160px 5;margin:0px;list-style:none;">
				<c:forEach items="${instruments}" var="instrument">
					<li>
						<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=inst&srchkey=${instrument.key}&sortKey=name&sortAction=1&pageNo=1&exec="
						title="${instrument.key}" target="_self">${instrument.key}</a> (${instrument.value})
					</li>
				</c:forEach>
			</ul>
		</div>
	</div> 
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>MS Type</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:150px 5;-moz-columns:150px 5;columns:150px 5;margin:0px;list-style:none;">
				<c:forEach items="${mstypes}" var="mstype">
					<li>
						<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=ms&srchkey=${mstype.key}&sortKey=name&sortAction=1&pageNo=1&exec="
						title="${mstype.key}" target="_self">${mstype.key}</a> (${mstype.value})
					</li>
				</c:forEach>
			</ul>
		</div>
	</div> 

	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Ion Mode</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:150px 5;-moz-columns:150px 5;columns:150px 5;margin:0px;list-style:none;">
				<c:forEach items="${ionmodes}" var="ionmode">
					<li>
						<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=ion&srchkey=${ionmode.key}&sortKey=name&sortAction=1&pageNo=1&exec="
						title="${ionmode.key}" target="_self">${ionmode.key}</a> (${ionmode.value})
					</li>
				</c:forEach>
			</ul>
 		</div>
	</div> 
	

	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Compound Name</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:100px 7;-moz-columns:100px 7;columns:100px 7;margin:0px;list-style:none;">
				<c:forEach items="${symbols}" var="symbol">
					<li>
						<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=cmpd&srchkey=${symbol.key}&sortKey=name&sortAction=1&pageNo=1&exec="
						title="${symbol.key}" target="_self">${symbol.key}</a> (${symbol.value})
					</li>
				</c:forEach>
			</ul>
		</div>
	</div>
	
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Unique Spectra</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:100px 7;-moz-columns:100px 7;columns:100px 7;margin:0px;list-style:none;">
				<li>${spectra}</li>
			</ul>
		</div>
	</div>
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Unique Compounds</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:100px 7;-moz-columns:100px 7;columns:100px 7;margin:0px;list-style:none;">
				<li>${compounds}</li>
			</ul>
		</div>
	</div>
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Unique Isomers</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:100px 7;-moz-columns:100px 7;columns:100px 7;margin:0px;list-style:none;">
				<li>${isomers}</li>
			</ul>
		</div>
	</div>
	<div class="w3-row w3-padding">
		<div class="w3-col" style="width:120px">
			<b>Database version</b>
		</div>
		<div class="w3-col" style="width:10px">
			<b>:</b>
		</div>
		<div class="w3-rest">
			<ul style="-webkit-columns:100px 7;-moz-columns:100px 7;columns:100px 7;margin:0px;list-style:none;white-space:nowrap;">
				<li>${version}</li>
			</ul>
		</div>
	</div>
	
	<div class="w3-padding">
		<div class="w3-padding-small">
			${sitechartSVG}
		</div>
		<div class="w3-padding-small">
			${instchartSVG}
		</div>
		<div class="w3-padding-small">
			${mschartSVG}
		</div>
	</div> 

	<br>
	<jsp:include page="copyrightline.html"/>

</body>
</html>