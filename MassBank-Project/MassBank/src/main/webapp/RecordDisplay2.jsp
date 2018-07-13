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
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<script src="script/jquery-3.3.1.min.js"></script>
	<script src="script/massbank.js"></script>
	<script src="script/search.js"></script>

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
		<h3><b>${recordTitle}</b></h3>
		<div class="w3-row w3-padding-small">
			<div class="w3-twothird w3-text-grey w3-small">
				Mass Spectrum
				<div id="spectrum_canvas" peaks="${peaks}" style="height: 200px; width: 650px; background-color: white"></div>
			</div>
			<div class="w3-onethird w3-text-grey w3-small">
				Chemical Structure
				${svgMedium}
			</div>
		</div>
	</div>
		
	
	<main context="http://schema.org" property="schema:about" resource="https://massbank.eu/MassBank/RecordDisplay.jsp?id=KO003710&dsn=Keio_Univ" typeof="schema:Dataset" >
		
		<table>
			<tr>
				<td valign="top">
					<font style="font-size:10pt;" color="dimgray">Mass Spectrum</font>
					<br>
<!-- 					<div id="spectrum_canvas" peaks="41.100,46@55.100,142@56.000,3@59.100,14@60.000,60@72.800,29@74.200,11@76.900,33@91.100,4@92.100,44@109.100,999@" style="height: 200px; width: 750px; background-color: white"></div> -->
				</td>
				<td valign="top">
					<font style="font-size:10pt;" color="dimgray">Chemical Structure</font><br>
					
						<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg version='1.2' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' width="200" height="200" style="cursor:pointer" viewBox='0 0 17.69 18.76'>
  <desc>Generated by the Chemistry Development Kit (http://github.com/cdk)</desc>
  <g stroke-linecap='round' stroke-linejoin='round' stroke='#000000' stroke-width='.21' fill='#3050F8'>
    <rect x='.0' y='.0' width='18.0' height='19.0' fill='#FFFFFF' stroke='none'/>
    <g id='mol1' class='mol'>
      <line id='mol1bnd1' class='bond' x1='4.85' y1='2.1' x2='8.18' y2='4.02'/>
      <g id='mol1bnd2' class='bond'>
        <line x1='12.58' y1='1.48' x2='8.18' y2='4.02'/>
        <line x1='12.58' y1='2.54' x2='9.1' y2='4.55'/>
      </g>
      <line id='mol1bnd3' class='bond' x1='8.18' y1='4.02' x2='8.18' y2='9.1'/>
      <g id='mol1bnd4' class='bond'>
        <line x1='8.18' y1='9.1' x2='12.58' y2='11.64'/>
        <line x1='9.1' y1='8.57' x2='12.58' y2='10.58'/>
      </g>
      <line id='mol1bnd5' class='bond' x1='12.58' y1='11.64' x2='12.58' y2='15.37'/>
      <line id='mol1bnd6' class='bond' x1='12.58' y1='11.64' x2='16.98' y2='9.1'/>
      <g id='mol1bnd7' class='bond'>
        <line x1='16.98' y1='9.1' x2='16.98' y2='4.02'/>
        <line x1='16.07' y1='8.57' x2='16.07' y2='4.55'/>
      </g>
      <line id='mol1bnd8' class='bond' x1='12.58' y1='1.48' x2='16.98' y2='4.02'/>
      <g id='mol1atm1' class='atom'>
        <path d='M3.09 .56h.33l.82 1.54v-1.54h.24v1.85h-.34l-.82 -1.54v1.54h-.24v-1.85z' stroke='none'/>
        <path d='M.56 .56h.25v.76h.91v-.76h.25v1.85h-.25v-.88h-.91v.88h-.25v-1.85z' stroke='none'/>
        <path d='M2.36 2.84h.52v.13h-.7v-.13q.09 -.09 .23 -.24q.15 -.15 .19 -.19q.07 -.08 .1 -.14q.03 -.06 .03 -.11q.0 -.09 -.06 -.14q-.06 -.05 -.16 -.05q-.07 .0 -.15 .02q-.08 .02 -.17 .07v-.15q.09 -.04 .17 -.05q.08 -.02 .14 -.02q.17 .0 .28 .09q.1 .09 .1 .23q.0 .07 -.03 .13q-.03 .06 -.09 .14q-.02 .02 -.12 .13q-.1 .1 -.28 .29z' stroke='none'/>
      </g>
      <g id='mol1atm6' class='atom'>
        <path d='M11.89 15.79h.33l.82 1.54v-1.54h.24v1.85h-.34l-.82 -1.54v1.54h-.24v-1.85z' stroke='none'/>
        <path d='M13.49 15.79h.25v.76h.91v-.76h.25v1.85h-.25v-.88h-.91v.88h-.25v-1.85z' stroke='none'/>
        <path d='M15.29 18.08h.52v.13h-.7v-.13q.09 -.09 .23 -.24q.15 -.15 .19 -.19q.07 -.08 .1 -.14q.03 -.06 .03 -.11q.0 -.09 -.06 -.14q-.06 -.05 -.16 -.05q-.07 .0 -.15 .02q-.08 .02 -.17 .07v-.15q.09 -.04 .17 -.05q.08 -.02 .14 -.02q.17 .0 .28 .09q.1 .09 .1 .23q.0 .07 -.03 .13q-.03 .06 -.09 .14q-.02 .02 -.12 .13q-.1 .1 -.28 .29z' stroke='none'/>
      </g>
    </g>
  <rect class="btn" x="0" y="0" width="200" height="200" onclick="expandMolView('http://localhost:8080/MassBank/temp//180713_151921_399_KO003710_big.svg')" fill-opacity="0.0" stroke-width="0" /> </g>\n</svg>
</td>
			</tr>
		</table>
<hr size="1">
<pre style="font-family:Courier New;font-size:10pt">
ACCESSION: KO003710
RECORD_TITLE: <span property="schema:headline">1,3-Phenylenediamine; LC-ESI-QQ; MS2; CE:10 V; [M+H]+</span>
DATE: <span property="schema:datePublished">2016.01.19</span> (<span property="schema:dateCreated">2007.07.07</span>, <span property="schema:dateModified">2011.05.10</span>)
AUTHORS: <span property="schema:author" typeof="schema:Person"><span property="schema:name">Kakazu Y</span><span property="schema:affiliation" style="display:none">Institute for Advanced Biosciences, Keio Univ.</span></span>, <span property="schema:author" typeof="schema:Person"><span property="schema:name">Horai H</span><span property="schema:affiliation" style="display:none">Institute for Advanced Biosciences, Keio Univ.</span></span>, Institute for Advanced Biosciences, Keio Univ.
LICENSE: <a href="https://creativecommons.org/licenses/" target="_blank" property="schema:license">CC BY-NC-SA</a>
COMMENT: <span property="schema:comment">KEIO_ID P035</span>
<hr size="1" color="silver" width="98%" align="left">CH$NAME: 1,3-Phenylenediamine
CH$NAME: 1,3-Benzenediamine
CH$NAME: Benzenediamine
CH$NAME: Phenylenediamine
CH$NAME: m-Diaminobenzene
CH$NAME: Diaminobenzene
CH$NAME: m-Phenylenediamine
CH$COMPOUND_CLASS: Non-Natural Product
CH$FORMULA: <a href="http://www.chemspider.com/Search.aspx?q=C6H8N2" target="_blank">C6H8N2</a>
CH$EXACT_MASS: 108.06875
CH$SMILES: Nc(c1)cc(N)cc1
CH$IUPAC: InChI=1S/C6H8N2/c7-5-2-1-3-6(8)4-5/h1-4H,7-8H2
CH$LINK: CAS <a href="https://www.google.com/search?q=&quot;25265-76-3 108-45-2&quot;" target="_blank">25265-76-3 108-45-2</a>
CH$LINK: CHEBI <a href="https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:8092" target="_blank">8092</a>
CH$LINK: KEGG <a href="http://www.genome.jp/dbget-bin/www_bget?cpd:C02454" target="_blank">C02454</a>
CH$LINK: PUBCHEM <a href="https://pubchem.ncbi.nlm.nih.gov/substance/5474" target="_blank">SID:5474</a>
<hr size="1" color="silver" width="98%" align="left">AC$INSTRUMENT: API3000, Applied Biosystems
AC$INSTRUMENT_TYPE: <span property="schema:measurementTechnique">LC-ESI-QQ</span>
AC$MASS_SPECTROMETRY: MS_TYPE MS2
AC$MASS_SPECTROMETRY: ION_MODE POSITIVE
AC$MASS_SPECTROMETRY: COLLISION_ENERGY 10 V
<hr size="1" color="silver" width="98%" align="left">MS$FOCUSED_ION: PRECURSOR_M/Z 109
MS$FOCUSED_ION: PRECURSOR_TYPE [M+H]+
<hr size="1" color="silver" width="98%" align="left">PK$SPLASH: <a href="http://mona.fiehnlab.ucdavis.edu/#/spectra/splash/splash10-0a4i-3900000000-429f215bf3772b34f884" target="_blank">splash10-0a4i-3900000000-429f215bf3772b34f884</a>
PK$NUM_PEAK: 11
PK$PEAK: m/z int. rel.int.
  41.100 801981.0 46
  55.100 2460398.5 142
  56.000 54455.5 3
  59.100 242574.5 14
  60.000 1044555.5 60
  72.800 509901.5 29
  74.200 198020.0 11
  76.900 579208.5 33
  91.100 69307.0 4
  92.100 767327.5 44
  109.100 17356453.0 999
//

</pre>
		<hr size=1>
		<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	</main>
	</body>
</html>