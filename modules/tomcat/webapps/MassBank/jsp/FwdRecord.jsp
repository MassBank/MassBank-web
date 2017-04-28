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
 * MassBank Record Direct Access モジュール
 *
 * ver 1.0.3 2010.12.24
 *
 ******************************************************************************/
%>

<%@ page import="java.util.*,java.io.*" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetConfig" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) since 2006 JST-BIRD MassBank" />
	<title>MassBank Record Direct Access</title>
</head>
<body>
<%
	String id = "";
	if ( request.getParameter( "id" ) != null ) {
		id = request.getParameter( "id" ).trim();
	}
	
	//-------------------------------------------
	// 環境設定ファイルからURLリストを取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	int pos = path.indexOf("/jsp");
	String baseUrl = path.substring( 0, pos+1 ) ;
	GetConfig conf = new GetConfig(baseUrl);
	String serverUrl = conf.getServerUrl();
	
	MassBankCommon mbcommon = new MassBankCommon();
	String typeName = mbcommon.CGI_TBL[mbcommon.CGI_TBL_NUM_TYPE][mbcommon.CGI_TBL_TYPE_RECORD];
	ArrayList result = mbcommon.execMultiDispatcher( serverUrl, typeName, "id=" + id );
	String line = "";
	Boolean isFound = false;
	String[] val = null;
	for ( int i = 0; i < result.size(); i++ ) {
		line = (String)result.get(i);
		if ( !line.equals("") ) {
			val = line.split("\t");
			if ( val[0].equals(id) ) {
				isFound = true;
				break;
			}
		}
	}
	if ( !isFound ) {
		out.println( "<h1>" + id + " Record Not Found</h1>" );
	}
	else {
		String dsn = "";
		if ( request.getParameter( "dsn") != null) {
			// forward to dispathcer
			String url = "Dispatcher.jsp?type=disp&id=" + id + "&site=" + val[1];
%>
<jsp:forward page="<%= url %>" />
<%			
		} else {
			// redirect ot a fwdRecord url with dsn parameter
			String url = "FwdRecord.jsp?" + request.getQueryString() + "&dsn=" + conf.getDbName()[Integer.parseInt(val[1])];
			response.sendRedirect(url);
		}
	}
%>
</body>
</html>
