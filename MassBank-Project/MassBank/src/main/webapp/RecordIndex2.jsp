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
<html lang="en" style="max-width:90%">
<!-- <html lang="en"> -->
<title>MassBank | Database | Record Index</title>


<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="Categorized list of spectra. To list up all spectra in a specific category including contributors, instrument types and ionization modes.">
	<meta name="keywords" content="APCI,ITFT,QFT,ESI,EI,LC,IT,GC,TOF,QTOF,FAB,MALDI,APPI,MS,MS/MS,MS2,MS3,MS4,CI,FI,FD,QQ,Merged,Positive,Negative,QIT,ITTOF,EB,mass spectra,MassBank,m/z">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<script src="script/jquery-3.3.1.min.js"></script>
	<script src="script/massbank.js"></script>
	<script src="script/search.js"></script>
</head>

<body class="w3-theme-gradient">
	
	<header class="w3-cell-row w3-text-grey">
		<div class="w3-container w3-cell w3-mobile" style="width:60%">
			<h1>
				<b>Record Index</b>
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
	
	<div style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-row w3-padding-small">
			<div class="w3-col" style="width:100px">
				<b>Contributor</b>
			</div>
			<div class="w3-col" style="width:10px">
				<b>:</b>
			</div>
			<div class="w3-rest">
				<ul style="-webkit-columns:270px 5;-moz-columns:270px 5;columns:270px 5;margin:0px;list-style:none;">
					<c:forEach items="${sites}" var="site">
						<li>
							<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=site&srchkey=${site.key}&sortKey=name&sortAction=1&pageNo=1&exec="
							title="${site.key}" target="_self">${site.key}</a> (${site.value})
						</li>
					</c:forEach>
				</ul>
  			</div>
		</div> 
	</div>
	<div style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-row w3-padding-small">
			<div class="w3-col" style="width:100px">
				<b>Instrument Type</b>
			</div>
			<div class="w3-col" style="width:10px">
				<b>:</b>
			</div>
			<div class="w3-rest">
				<ul style="-webkit-columns:150px 5;-moz-columns:150px 5;columns:150px 5;margin:0px;list-style:none;">
					<c:forEach items="${instruments}" var="instrument">
						<li>
							<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=inst&srchkey=${instrument.key}&sortKey=name&sortAction=1&pageNo=1&exec="
							title="${instrument.key}" target="_self">${instrument.key}</a> (${instrument.value})
						</li>
					</c:forEach>
				</ul>
  			</div>
		</div> 
	</div>
	<div style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-row w3-padding-small">
			<div class="w3-col" style="width:100px">
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
	</div>
	<div style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-row w3-padding-small">
			<div class="w3-col" style="width:100px">
				<b>Merged Type</b>
			</div>
			<div class="w3-col" style="width:10px">
				<b>:</b>
			</div>
			<div class="w3-rest">
				<ul style="-webkit-columns:150px 5;-moz-columns:150px 5;columns:150px 5;margin:0px;list-style:none;">
					<c:forEach items="${mergedtypes}" var="mergedtype">
						<li>
							<a class="w3-text-blue" href="Result.jsp?type=rcdidx&idxtype=merged&srchkey=${mergedtype.key}&sortKey=name&sortAction=1&pageNo=1&exec="
							title="${mergedtype.key}" target="_self">${mergedtype.key}</a> (${mergedtype.value})
						</li>
					</c:forEach>
				</ul>
  			</div>
		</div> 
	</div>
	<div style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-row w3-padding-small">
			<div class="w3-col" style="width:100px">
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
	</div>
	<div style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-row w3-padding-small">
			<div class="w3-col" style="width:100px">
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
	</div>

	<main context="http://schema.org" property="schema:about" resource="https://massbank.eu/MassBank/RecordIndex.jsp" typeof="schema:DataCatalog" >
	<div style="display:none" property="schema:citation" typeof="schema:ScholarlyArticle">
		<div property="schema:name">Horai, Arita, Kanaya, Nihei, Ikeda, Suwa, Ojima, Tanaka, Tanaka, Aoshima, Oda, Kakazu, Kusano, Tohge, Matsuda, Sawada, Hirai, Nakanishi, Ikeda, Akimoto, Maoka, Takahashi, Ara, Sakurai, Suzuki, Shibata, Neumann, Iida, Tanaka, Funatsu, Matsuura, Soga, Taguchi, Saito, Nishioka. MassBank: a public repository for sharing mass spectral data for life sciences. Journal of mass spectrometry. 2010 Jul;45(7):703-14. doi: 10.1002/jms.1777.</div>
		<div property="schema:headline">MassBank: a public repository for sharing mass spectral data for life sciences.</div>
		<div property="schema:image">https://massbank.eu/MassBank/image/sub_logo.jpg</div>
	</div>
	<div style="display:none" property="schema:description">
		<div property="schema:name">Categorized list of spectra. To list up all spectra in a specific category including contributors, instrument types and ionization modes.</div>
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
		

	<table>
	<tr>
	<td>
	<img src="http://localhost:8080/MassBank/temp/massbank_Contributor_Graph.jpg" alt="" border="0">
	</td>
	</tr>
	<tr>
	<td>
	<img src="http://localhost:8080/MassBank/temp/massbank_Instrument Type_Graph.jpg" alt="" border="0">
	</td>
	</tr>
	<tr>
	<td>
	<img src="http://localhost:8080/MassBank/temp/massbank_MS Type_Graph.jpg" alt="" border="0">
	</td>
	</tr>
	</table>



	<br>
	<div id="copyrightline"></div>
		
</body>
</html>