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
 * Admin Tool メニュー
 *
 * ver 1.0.9 2010.02.26
 *
 ******************************************************************************/
%>

<%@ page import="massbank.admin.AdminCommon" %>
<%
	//----------------------------------------------------
	// パラメータ取得
	//----------------------------------------------------
	final String reqUrl = request.getRequestURL().toString();
	final String baseUrl = reqUrl.substring( 0, (reqUrl.indexOf("/mbadmin") + 1 ) );
	final String realPath = application.getRealPath("/");
	AdminCommon admin = new AdminCommon(reqUrl, realPath);
	boolean isPortal = admin.isPortal();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<style type="text/css">
<!--
body {
	font-size:10pt;
	font-family: Arial, Helvetica, sans-serif;
}
.titl {
	font-size: 10pt;
	font-weight: bold;
	color: navy;
	text-align: center;
	cursor: pointer;
}
.menu {
	font-size: 10pt;
	font-weight: bold;
	color: #3300CC;
	background-color: Beige;
	border: 1px Gainsboro solid;
	border-left: 3px MediumBlue solid;
	padding-left: 5px;
	padding-right: 8px;
}
.menuOver {
	font-size: 10pt;
	font-weight: bold;
	color: white;
	background-color: Crimson;
	border: 1px Gainsboro solid;
	border-left: 3px MediumBlue solid;
	padding-left: 5px;
	padding-right: 8px;
	cursor: pointer;
}
.menuSelect {
	font-size: 10pt;
	font-weight: bold;
	color: Beige;
	background-color: MediumBlue;
	border: 1px Gainsboro solid;
	border-left: 3px MediumBlue solid;
	padding-left: 5px;
	padding-right: 8px;
}
.home {
	font-size: 10pt;
	font-weight: bold;
	color:#3300CC;
	background-color: Beige;
	border: 1px Gainsboro solid;
	vertical-align: middle;
	cursor: pointer;
	text-align: center;
}
-->
</style>
<title>MassBank | Admin | Menu</title>
</head>

<%
	//----------------------------------------------------
	// リファラでのメニュー背景色変更
	//----------------------------------------------------
	final String referer = request.getHeader("referer");
	String rvMenu = (referer.indexOf("/RecordValidator.jsp") == -1) ? "menu" : "menuSelect";
	String ieMenu = (referer.indexOf("/InstEdit.jsp") == -1)        ? "menu" : "menuSelect";
	String rrMenu = (referer.indexOf("/RecordRegist.jsp") == -1)    ? "menu" : "menuSelect";
	String rlMenu = (referer.indexOf("/RecordList.jsp") == -1)      ? "menu" : "menuSelect";
	String srMenu = (referer.indexOf("/StructureRegist.jsp") == -1) ? "menu" : "menuSelect";
	String slMenu = (referer.indexOf("/StructureList.jsp") == -1)   ? "menu" : "menuSelect";
	String fuMenu = (referer.indexOf("/FileUpload.jsp") == -1)      ? "menu" : "menuSelect";
	String rgMenu = (referer.indexOf("/GenRecordList.jsp") == -1)   ? "menu" : "menuSelect";
	String viMenu = (referer.indexOf("/DispVersion.jsp") == -1)     ? "menu" : "menuSelect";
	String dbMenu = (referer.indexOf("/Manager.jsp") == -1)         ? "menu" : "menuSelect";
%>

<body>
<table cellspacing="0" cellpadding="0">
	<tr>
		<td>
			<table cellspacing="3" cellpadding="0" bgcolor="Lavender" style="border:1px Gainsboro solid;" width="920px">
<%
	if ( isPortal ) {
%>
				<tr>
					<td class="titl" onClick="parent.location.href='./'" rowspan="2">&nbsp;ADMIN TOOL&nbsp;<br>MENU</td>
					<td class="<%=rvMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rvMenu%>'" onClick="parent.location.href='RecordValidator.jsp'">Record Validator</td>
					<td class="<%=rrMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rrMenu%>'" onClick="parent.location.href='RecordRegist.jsp'">Record Registration</td>
					<td class="<%=srMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=srMenu%>'" onClick="parent.location.href='StructureRegist.jsp'">Structure Registration</td>
					<td class="<%=fuMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=fuMenu%>'" onClick="parent.location.href='FileUpload.jsp'">File Upload</td>
					<td class="<%=viMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=viMenu%>'" onClick="parent.location.href='../jsp/DispVersion.jsp'">Version Information</td>
				</tr>
				<tr>
					<td class="<%=ieMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=ieMenu%>'" onClick="parent.location.href='InstEdit.jsp'">Instrument Editor</td>
					<td class="<%=rlMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rlMenu%>'" onClick="parent.location.href='RecordList.jsp'">Record List</td>
					<td class="<%=slMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=slMenu%>'" onClick="parent.location.href='StructureList.jsp'">Structure List</td>
					<td class="<%=rgMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rgMenu%>'" onClick="parent.location.href='GenRecordList.jsp'">Record List Generator</td>
					<td class="<%=dbMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=dbMenu%>'" onClick="parent.location.href='Manager.jsp'">Dababase Manager</td>
				</tr>
<%
	}
	else  {
%>
				<tr>
					<td class="titl" onClick="parent.location.href='./'" rowspan="2">&nbsp;ADMIN TOOL&nbsp;<br>MENU</td>
					<td class="<%=rvMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rvMenu%>'" onClick="parent.location.href='RecordValidator.jsp'">Record Validator</td>
					<td class="<%=rrMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rrMenu%>'" onClick="parent.location.href='RecordRegist.jsp'">Record Registration</td>
					<td class="<%=srMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=srMenu%>'" onClick="parent.location.href='StructureRegist.jsp'">Structure Registration</td>
					<td class="<%=dbMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=dbMenu%>'" onClick="parent.location.href='Manager.jsp'">Dababase Manager</td>
				</tr>
				<tr>
					<td class="<%=ieMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=ieMenu%>'" onClick="parent.location.href='InstEdit.jsp'">Instrument Editor</td>
					<td class="<%=rlMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=rlMenu%>'" onClick="parent.location.href='RecordList.jsp'">Record List</td>
					<td class="<%=slMenu%>" onMouseOver="className='menuOver'" onMouseOut="className='<%=slMenu%>'" onClick="parent.location.href='StructureList.jsp'">Structure List</td>
				</tr>
<%
	}
%>
			</table>
		</td>
		<td class="home" onClick="parent.location.href='../'" width="58px">&nbsp;HOME&nbsp;</td>
	</tr>
</table>
</body>
</html>
