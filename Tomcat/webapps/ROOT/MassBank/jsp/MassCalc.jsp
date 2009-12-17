<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * MassCalculator表示用モジュール
 *
 * ver 1.0.1 2009.12.17
 *
 ******************************************************************************/
%>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%
	//-------------------------------------
	// Copyright読み込み
	//-------------------------------------
	String copyrightLine = "";
	BufferedReader br = null;
	try {
		final String reqUrl = request.getRequestURL().toString();
		URL url = new URL( reqUrl.substring(0, reqUrl.indexOf("jsp")) + "/copyrightline.html" );
		URLConnection con = url.openConnection();
		br = new BufferedReader( new InputStreamReader(con.getInputStream(), "UTF-8") );
		String line;
		while ((line = br.readLine()) != null) {
			if ( line.indexOf("<span>") != -1 ) {
				copyrightLine = line.replaceAll("<span>", "<span style=\"color:#666; font-family:Verdana, Arial, Trebuchet MS; font-size:10px; font-style:italic; text-decoration:none; font-weight:normal; clear:both; display:block; margin:0; position:relative;\">");
				break;
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		if ( br != null ) {
			br.close();
		}
	}
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="description" content="Formula to m/z.">
	<meta name="keywords" content="Mass,Calc,mz,Formula">
	<link rel="stylesheet" type="text/css" href="./css/Common.css">
	<link rel="stylesheet" type="text/css" href="./css/03_design.css" media="all">
	<script type="text/javascript" src="script/AtomicMass.js"></script>
	<script type="text/javascript" src="script/MassCalc.js"></script>
	<title>MassBank | Mass Calculator</title>
</head>
<body class="msbkFont backgroundImg cursorDefault" bgcolor="#cee6f2" onload="initFocus();">
	<h2 style="margin:0px;">Mass Calculator</h2>
	<hr size="1">
	<form style="margin:0px;">
		<table border="0" cellpadding="0" cellspacing="3">
			<tr>
				<th>Formula</th>
				<th>&nbsp;</th>
				<th><i>m/z</i></th>
			</tr>
<%
	final int numForm = 6; 
	for ( int i=0; i<numForm; i++ ) {
		out.println( "\t\t\t<tr>" );
		out.println( "\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t<input name=\"fom" + i + "\" type=\"text\" value=\"\" maxlength=\"20\" style=\"width:140px; ime-mode:disabled;\">" );
		out.println( "\t\t\t\t</td>" );
		out.println( "\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t<img src=\"./image/arrow_r.gif\" alt=\"\" style=\"margin:0 5px;\">" );
		out.println( "\t\t\t\t</td>" );
		out.println( "\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t<input name=\"mz" + i + "\" type=\"text\" size=\"15\" value=\"\" readonly tabindex=\"-1\" style=\"width:100px; text-align:right; background-color:#eeeeee;border:solid 1px #999;\"></td>" );
		out.println( "\t\t\t\t</td>" );
		out.println( "\t\t\t</tr>" );
	}
%>

			<tr>
				<td colspan="3" align="right">
					<input type="button" name="calc" value="Calc" onClick="setMZ()" style="width:70px;">&nbsp;
					<input type="button" name="clear" value="Clear" onClick="resetForm()" style="width:70px;">
				</td>
			</tr>
		</table>
	</form>
	<hr size="1">
	<%=copyrightLine%>
</body>
</html>
