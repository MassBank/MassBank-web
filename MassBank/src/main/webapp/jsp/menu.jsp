<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
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
 *******************************************************************************
 *
 * MassBank メニュー
 *
 * ver 1.0.1 2010.12.24
 *
 ******************************************************************************/
%>

<%@ page import="massbank.admin.AdminCommon" %>
<%
	//----------------------------------------------------
	// パラメータ取得
	//----------------------------------------------------
	AdminCommon admin = new AdminCommon();
	boolean isBatch = admin.isBatch();
	boolean isAdvanced = admin.isAdvanced();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta http-equiv="Content-Style-Type" content="text/css">
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta name="author" content="MassBank" />
		<meta name="coverage" content="worldwide" />
		<meta name="Targeted Geographic Area" content="worldwide" />
		<meta name="rating" content="general" />
		<meta name="copyright" content="Copyright (c) since 2006 JST-BIRD MassBank" />
		<meta name="ROBOTS" content="NOINDEX,FOLLOW">
		<link rel="stylesheet" type="text/css" href="./css/Common.css">
		<title>MassBank | Menu Link</title>
		</head>
	<body class="msbkFont">
		<form method="post" action="./jsp/FwdRecord.jsp" target="_blank" class="formStyle">
			<table  border="0" cellpadding="0" cellspacing="0" width="980" class="menuFont">
				<tr>
					<td>
&nbsp;&nbsp;
<a href="./index.html" target="_parent" title="MassBank">Home</a>
|
<!-- <a href="./SearchPage.html" target="_parent" title="Spectrum Search">Spectrum</a>
| -->
<a href="./QuickSearch.html" target="_parent" title="Quick Search">Quick Search</a>
|
<a href="./PeakSearch.html" target="_parent" title="Peak Search">Peak Search</a>
|
<!-- <a href="./StructureSearch.html" target="_parent" title="Substructure Search">Substructure</a>
| -->
<%
	if ( isAdvanced ) {
%>
<!-- <a href="./AdvancedSearch.html" target="_parent" title="Advanced Search">Advanced</a>
| -->
<%
	}
%>
<!-- <a href="./PackageView.html" target="_parent" title="Spectral Browser">Browser</a>
| -->
<%
	if ( isBatch ) {
%>
<!-- <a href="http://www.massbank.jp/BatchSearch.html" target="_blank" title="Batch Service at MassBank Japan">Batch</a>
| -->
<%
	}
%>
<!-- <a href="./BrowsePage.html" target="_parent" title="Browse Page">Browse</a>
| -->
<a href="./RecordIndex.html" target="_parent" title="Record Index">Record Index</a>
|
<a href="./Statistics.html" target="_blank" title="Record and Compounds Statistics">Statistics</a>
|
&nbsp;&nbsp;&nbsp;MassBank ID:&nbsp;<input name="id" type="text" size="10" value="" maxlength="8">&nbsp;<input type="submit" value=" Go ">
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
