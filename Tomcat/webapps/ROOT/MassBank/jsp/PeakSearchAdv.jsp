<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
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
 * Peak Search Advanced
 *
 * ver 1.0.5 2011.06.16
 *
 ******************************************************************************/
%>
<%@ include file="./Common.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<meta name="author" content="MassBank" />
<meta name="coverage" content="worldwide" />
<meta name="Targeted Geographic Area" content="worldwide" />
<meta name="rating" content="general" />
<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
<meta name="description" content="Peak Search Advanced">
<meta name="keywords" content="Peak,Product Ion,Neutral Loss,Formula">
<meta name="revisit_after" content="30 days">
<link rel="stylesheet" type="text/css" href="css/Common.css">
<link rel="stylesheet" type="text/css" href="css/Result.css">
<link rel="stylesheet" type="text/css" href="css/FormulaSuggest.css" />
<script type="text/javascript" src="script/Common.js"></script>
<script type="text/javascript" src="script/jquery.js"></script>
<script type="text/javascript" src="script/FormulaSuggest.js"></script>
<script>
<!--
function changeType() {
	document.forms[0].target = "_self";
	document.forms[0].submit();
}

function chageMode(reqType) {
	if ( reqType == "seq" ) {
		val = "<img src=\"image/arrow_green.gif\">"
	}
	else if ( reqType == "and" || reqType == "or" ) {
		if ( reqType == "and" )     { logic = "AND"; }
		else if ( reqType == "or" ) { logic = "OR";  }
		val = "<b class=\"logic\">" + logic + "</b>";
	}
	else if ( reqType == "or" ) {
		val = "<b class=\"logic\">OR</b>";
	}
	for ( i = 1; i <= 4; i++ ) {
		ele = document.getElementById( "arrow"+ String(i) );
		ele.innerHTML = val;
	}
}

$(function(){
	$("input.FormulaSuggest").FormulaSuggest();
});
-->
</script>
<title>MassBank | Database | Peak Search Advanced</title>
</head>
<%@ page import="java.util.*" %>
<%
	final int NUM_INPUT_FORMULA = 5;
	boolean isProductIon = true;
	String stype = "product";
	String mode = "and";
	Map inputFormula= new HashMap();
	Enumeration names = request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		String val = request.getParameter(key);
		if ( key.equals("type") ) {
			if ( val.equals("neutral") ) {;
				isProductIon = false;
				stype = "neutral";
			}
		}
		if ( key.equals("mode") ) {
			mode = val;
		}
		else if ( key.indexOf("formula") >= 0  ) {
			inputFormula.put( key, val );
		}
	}
%>
<body class="msbkFont">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Peak Search Advanced&nbsp;<span style="font-size:16px;color:Tomato;">BETA</span></h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%><%=ADVANCED_PAGE%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
<iframe src="menu.html" width="860" height="30" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<hr size="1">
<form name="change" method="post" action="PeakSearchAdv.html">
	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td rowspan="3" valign="top"><b>Search for</b>&nbsp;&nbsp;</td>
			<td width="120">
				<input type="radio" name="type" value="product" onClick="changeType()" <%if(isProductIon){out.print("checked");}%>><b>Product Ion</b>
			</td>
			<td width="30"></td>
			<td width="130">
				<input type="radio" name="type" value="neutral" onClick="changeType()" <%if(!isProductIon){out.print("checked");}%>><b>Neutral Loss</b>
			</td>
			<tr>
				<td id="underbar1" height="4"<% if(isProductIon) out.print(" bgcolor=\"MidnightBlue\""); %>></td>
				<td></td>
				<td id="underbar2" height="4"<% if(!isProductIon) out.print(" bgcolor=\"DarkGreen\""); %>></td>
			</tr>
		</tr>
	</table>
</form>
<form method="post" action="jsp/ResultAdv.jsp">
	<div class="boxA" style="width:710px">
		<br>
		<table border="0" cellpadding="0" cellspacing="3" style="margin:8px">
<%
	String style = "bgProduct";
	String str = "Product&nbsp;Ion&nbsp;";
	if ( !isProductIon ) {
		style = "bgNeutral";
		str = "Neutral&nbsp;Loss&nbsp;";
	}
	String condition = "<b class=\"logic\">AND</b>";
	if ( mode.equals("or") ) {
		condition = "<b class=\"logic\">OR</b>";
	}
	else if ( mode.equals("seq") ) {
		condition = "<img src=\"image/arrow_green.gif\"></td>";
	}

	out.println("\t\t\t<tr>");
	for ( int i = 1; i <= NUM_INPUT_FORMULA; i++ ) {
		out.println( "\t\t\t\t<td align=\"center\" width=\"110\"><b class=\"" + style + "\">"
					+ str + String.valueOf(i) + "</b></td>" );
		if ( i < NUM_INPUT_FORMULA ) {
			out.println( "\t\t\t\t<td></td>" );
		}
	}
	out.println("\t\t\t</tr>");

	out.println("\t\t\t<tr>");
	for ( int i = 1; i <= NUM_INPUT_FORMULA; i++ ) {
		out.println( "\t\t\t\t<td align=\"center\">Formula</td>" );
		if ( i < NUM_INPUT_FORMULA ) {
			out.println( "\t\t\t\t<td></td>" );
		}
	}
	out.println("\t\t\t</tr>");

	out.println("\t\t\t<tr>");
	for ( int i = 1; i <= NUM_INPUT_FORMULA; i++ ) {
		String key = "formula" + String.valueOf(i);
		String val = "";
		if ( inputFormula.containsKey(key) ) {
			val = (String)inputFormula.get(key);
		}
		out.println( "\t\t\t\t<td align=\"center\">" );
		out.println( "\t\t\t\t\t<input id=\"" + key + "\" class=\"FormulaSuggest\" name=\"" + key + "\" type=\"text\" size=\"12\" value=\"" + val + "\" autocomplete=\"off\">" );
		out.println( "\t\t\t\t</td>" );
		if ( i < NUM_INPUT_FORMULA ) {
			out.println( "\t\t\t\t<td id=\"arrow" + String.valueOf(i) + "\">" + condition );
		}
	}
	out.println("\t\t\t</tr>");
	out.println("\t\t\t<tr height=\"50\">");
	out.println("\t\t\t\t<td colspan=\"7\">");
	if ( isProductIon ) {
		String[] valMode = { "and", "or" };
		String[] strMode = { "AND", "OR" };
		for ( int i = 0; i < valMode.length; i++ ) {
			out.print( "\t\t\t\t\t<input type=\"radio\" name=\"mode\" value=\"" + valMode[i] + "\" onClick=\"chageMode(this.value)\"" );
			if ( mode.equals(valMode[i]) ) {
				out.print(" checked");
			}
			out.println( "><b>" + strMode[i] + "</b>&nbsp;&nbsp;&nbsp;" );
		}
	}
	else {
		String[] valMode = { "and", "seq" };
		String[] strMode = { "AND", "SEQUENCE" };
		for ( int i = 0; i < valMode.length; i++ ) {
			out.print( "\t\t\t\t\t<input type=\"radio\" name=\"mode\" value=\"" + valMode[i] + "\" onClick=\"chageMode(this.value)\"" );
			if ( mode.equals(valMode[i]) ) {
				out.print(" checked");
			}
			out.println( "><b>" + strMode[i] + "</b>&nbsp;&nbsp;&nbsp;" );
		}
	}
	out.println("\t\t\t\t</td>");
	out.println("\t\t\t</tr>");
	out.print("\t\t\t");
%>
		</table>
	</div>
	<input type="hidden" name="type" value="<%= stype %>">
	<input type="submit" value="Search" style="background:PaleGoldenrod;">
</form>
<hr size="1">
<iframe src="copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
