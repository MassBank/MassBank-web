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
 * ver 1.0.2 2008.12.05
 *
 ******************************************************************************/
%>

<%@ page import="java.util.*,java.io.*,java.text.*" %>
<%@ page import="massbank.admin.AdminCommon" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<pre>
<%
	String reqUrl = request.getRequestURL().toString();
	String realPath = application.getRealPath("/");
	String dbRootPath = new AdminCommon( reqUrl, realPath ).getDbRootPath();
	String fileName = request.getParameter("fname");
	String dbName = request.getParameter("db");
	String line = "";
	try {
		String filePath = dbRootPath + dbName + "/" + fileName;
		BufferedReader in = new BufferedReader( new FileReader( filePath ) );
		while ( ( line = in.readLine() ) != null ) {
			out.println( line );
		}
		in.close();
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
%>
</pre>
</body>
</html>
