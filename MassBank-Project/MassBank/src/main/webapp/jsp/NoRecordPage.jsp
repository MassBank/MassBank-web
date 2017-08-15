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
 ******************************************************************************/
%>

<%@ page import="java.util.Enumeration" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankEnv" %>
<%
	// ##################################################################################################
	// get parameters
	// http://localhost/MassBank/jsp/NoRecordPage.jsp?id=XXX00000&dsn=MassBank&error=Some error
	//String accession	= "XXX00000";
	//String database	= "MassBank";
	//String error		= "Some error";
	String accession		= null;
	String databaseName		= null;
	String error			= null;
	
	Enumeration<String> names = request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String) names.nextElement();
		String val = (String) request.getParameter( key );
		
		switch(key){
			case "id":		accession		= val; break;
			case "dsn":		databaseName	= val; break;
			case "error":	error			= val; break;
			default: System.out.println("Warning: Unused argument " + key + "=" + val);
		}
	}
	%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta name="author" content="MassBank" />
		<meta name="coverage" content="worldwide" />
		<meta name="Targeted Geographic Area" content="worldwide" />
		<meta name="rating" content="general" />
		<meta name="copyright" content="Copyright (c) 2006 MassBank Project and (c) 2011 NORMAN Association" />
		<meta name="description" content="MassBank Record of <%=accession%>">
		<meta name="keywords" content="No MassBank record">
		<meta name="revisit_after" content="30 days">
		<meta name="hreflang" content="en">
		<meta name="variableMeasured" content="m/z">
		<meta http-equiv="Content-Style-Type" content="text/css">
		<link rel="stylesheet" type="text/css" href="../css/Common.css">
		<title>No Mass Spectrum</title>
	</head>
	<body style="font-family:Times;">
		No MassBank record can be displayed for accession <%=accession%> in database <%=databaseName%>.<br>
		Error: <b><%=error%></b></b>
		<hr size=1>
		<iframe src="../copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	</body>
</html>