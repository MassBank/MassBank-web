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
 * ver 1.0.5 2011.07.13
 *
 ******************************************************************************/
%>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="massbank.Config" %>
<%
	//AdminCommon admin = new AdminCommon();
	//boolean isPeakAdv = admin.isPeakAdv();
	boolean isPeakAdv = false;
	
	//-------------------------------------
	// Copyright読み込み
	//-------------------------------------
	String copyrightLine = "";
	BufferedReader br = null;
	try {
		URL url = new URL("copyrightline.html" );
		URLConnection con = url.openConnection();
		br = new BufferedReader( new InputStreamReader(con.getInputStream(), "UTF-8") );
		String line;
		while ((line = br.readLine()) != null) {
			if ( line.indexOf("<span>") != -1 ) {
				copyrightLine = line.replaceAll("<span>", "<span style=\"color:#666; font-family:Verdana, Arial, Trebuchet MS; font-size:10px; font-style:italic; text-decoration:none; font-weight:normal; clear:both; display:block; margin:0; position:relative;\">");
				break;
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	finally {
		if ( br != null ) { br.close(); }
	}
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
	<meta name="description" content="Mass Calculator is a basic mass calculation tool that you can use everywhere in MassBank. There are Formula to m/z and m/z to formula as a function.">
	<meta name="keywords" content="Mass,Calc,Calculator,mz,Formula">
	<link rel="stylesheet" type="text/css" href="./css.old/Common.css">
	<script type="text/javascript" src="js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="./script/MassCalc.js"></script>
	<title>MassBank | Mass Calculator</title>
	</head>
<body class="msbkFont backgroundImg cursorDefault" bgcolor="#cee6f2">
	<h2 style="margin:0px;">Mass Calculator</h2>
	<hr size="1" style="margin-top:5; margin-bottom:5;">
	<noscript>
		<p id="js_use" class="clr">
			<em class="e16">Javascript is used in this site</em><br />
			&nbsp;&nbsp;Javascript is used in this site. If Javascript cannot be used, it is not correctly displayed. Please enable the use of Javascript. And reload.
		</p>
	</noscript>
	<form style="margin:0px;">
<% if ( isPeakAdv ) { %>
		<input type="radio" name="type" value="fm" onClick="changeType('fm');" checked><span name="typeLbl" onClick="changeType('fm');" style="text-decoration:underline;"><b>Formula to <i>m/z</i></b></span></i></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="radio" name="type" value="mf" onClick="changeType('mf');"><span name="typeLbl" onClick="changeType('mf');"><b><i>m/z</i> to formula</b></span></i></b>
		<hr size="1" style="margin-top:5; margin-bottom:5;">
<% } %>
		<div id="fCalc" style="height:230px;">
			<table border="0" cellpadding="0" cellspacing="3">
				<tr>
					<th>Formula</th>
					<th>&nbsp;</th>
					<th><i>m/z</i></th>
				</tr>
<%
	final int numForm = 6; 
	for ( int i=0; i<numForm; i++ ) {
		out.println( "\t\t\t\t<tr>" );
		out.println( "\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t<input name=\"fom" + i + "\" type=\"text\" value=\"\" maxlength=\"20\" style=\"width:170px; ime-mode:disabled;\" class=\"fFormula\">" );
		out.println( "\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t<img src=\"./image/arrow_r.gif\" alt=\"\" style=\"margin:0 5px;\">" );
		out.println( "\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t<input name=\"mz" + i + "\" type=\"text\" size=\"15\" value=\"\" readonly style=\"width:100px; text-align:right; background-color:#eeeeee;border:solid 1px #999;\" class=\"fMass\"></td>" );
		out.println( "\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t</tr>" );
	}
%>

				<tr>
					<td colspan="3" align="right">
						<input type="button" name="clear" value="Clear" onClick="resetForm()" style="width:70px;">
					</td>
				</tr>
			</table>
		</div>
		<div id="mCalc" style="height:230px; display:none;">
			<table border="0" cellpadding="0" cellspacing="3">
				<tr>
					<th><i>m/z</i></th>
					<th>&nbsp;</th>
					<th>Formula</th>
				</tr>
				<tr>
					<td valign="top">
						<input name="mz" type=\"text" value="" maxlength="20" style="width:90px; ime-mode:disabled;" class="mMass"><br /><br />
						<b><span class="fontNote">* Based on data</span></b><br />
						<b><span class="fontNote">&nbsp;&nbsp;&nbsp;from Keio and</span></b><br />
						<b><span class="fontNote">&nbsp;&nbsp;&nbsp;Riken.</span></b><br />
					</td>
					<td valign="top">
						<img src="./image/arrow_r.gif" alt="" style="margin:5 5px;">
					</td>
					<td>
						<textarea name="fom" rows="10" cols="20" wrap="off" readonly style="width:220px; height:195px; background-color:#eeeeee;border:solid 1px #999;" class="mFormula"></textarea>
					</td>
				</tr>
			</table>
		</div>
	</form>
	<hr size="1">
	<%=copyrightLine%>
</body>
</html>
