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
 * 連携サーバ障害表示
 *   (慶応サーバのみ存在する)
 * 
 * ver 1.0.2 2009.10.16
 *
 ******************************************************************************/
%>
<%@ page import="massbank.ServerStatus" %>
<%@ page import="massbank.ServerStatusInfo" %>
<%
	String path = request.getRequestURL().toString();
	int pos = path.indexOf("/jsp");
	if ( pos == -1 ) {
		pos = path.indexOf("/pserver");
	}
	String baseUrl = path.substring( 0, pos+1 );
	ServerStatus svrStatus = new ServerStatus(baseUrl);
	ServerStatusInfo[] info = svrStatus.getStatusInfo();
	if ( info != null ) {
		String failServerName = "";
		for ( int i = 0; i < info.length; i++ ) {
			if ( !info[i].getStatus() ) {
				if ( !failServerName.equals("") ) {
					failServerName += " and ";
				}
				failServerName += "<font color=\"Crimson\">" + info[i].getServerName() + "</font>";
			}
		}
		if ( !failServerName.equals("") ) {
			out.println( "\t\t<div style=\"width:900px;padding:3px;margin-bottom:20px;background-color:LavenderBlush;\">" );
			out.println( "\t\t\t<img src=\"" + baseUrl + "	pserver/caution.gif\">&nbsp;"
					   + "<span style=\"font-size:10pt;vertical-align:top\">"
					   + "<b>Notice:&nbsp;" + failServerName + " server is temporarily unavailable.</span>" );
			out.println( "\t\t</div>" );
		}
	}
%>
