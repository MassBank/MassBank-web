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
 * レコードファイル表示
 *
 * ver 1.0.3 2010.11.09
 *
 ******************************************************************************/
%>

<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="massbank.Config" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<pre>
<%
	String dbRootPath = Config.get().DataRootPath();
	String fileName = request.getParameter("fname");
	String dbName = request.getParameter("db");
	BufferedReader in = null;
	try {
		String filePath = dbRootPath + dbName + "/" + fileName;
		in = new BufferedReader( new FileReader( filePath ) );
		String line = "";
		while ( ( line = in.readLine() ) != null ) {
			out.println( line );
		}
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	finally {
		try { if ( in != null ) { in.close(); } } catch (Exception e) {}
	}
%>
</pre>
</body>
</html>
