<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%
/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 ******************************************************************************/
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="massbank.Config"%>
<%
	String siteLongName = Config.get().LongName();
%>
<!DOCTYPE html>
<html lang="en">
<title>MassBank | <%=siteLongName%> Mass Spectral DataBase</title>

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="Mass Spectral DataBase">
<meta name="keywords" content="Mass,Spectral,Database,MassBank">

<script src="script/html-imports.min.js"></script>
<link rel="import" href="common.html"></link>

</head>

<body class="w3-theme-gradient">

	<header class="w3-center w3-text-grey w3-topbar w3-border-light-grey">
		<img src="image/sub_logo.jpg" alt="MassBank" style="max-width:100%;height:auto;">
		<h2>
			<b><%=siteLongName%></b>
		</h2>
	</header>

	<div class="w3-row w3-white w3-round" style="max-width:90%;height:auto;margin:auto;">
		<div class="w3-third w3-panel w3-white">
			<div class="w3-panel w3-leftbar w3-border-teal w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="newsearch.jsp"><b>Quick Search</b></a>
					<a href="newsearch.jsp"><img src="image/quick.gif" style="width:100%;"></a>
				</h5>
			</div>
		</div>

		<div class="w3-third w3-panel w3-white">
			<div
				class="w3-panel w3-leftbar w3-border-amber w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="PeakSearch.jsp"><b>Peak Search</b></a>
					<a href="PeakSearch.jsp"><img src="image/peak.gif" style="width:100%;"></a>
				</h5>
			</div>
		</div>

		<div class="w3-third w3-panel w3-white">
			<div
				class="w3-panel w3-leftbar w3-border-pink w3-light-grey w3-text-blue w3-round">
				<h5>
					<a href="RecordIndex.jsp"><b>Record Index</b></a>
					<a href="RecordIndex.jsp"><img src="image/list.gif" style="width:100%;"></a>
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