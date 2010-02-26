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
 * Admin Tool トップページ
 *
 * ver 1.0.10 2010.02.26
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
	<link rel="stylesheet" type="text/css" href="css/admin.css">
	<title>Admin | MassBank Administration Tool</title>
</head>
<body id="top">
<h2>MassBank Administration Tool</h2>
<br />
<hr>
<br />
<div class="base" style="height:290px;">
	<div class="menu">Main Menu</div>
	<div class="baseHerf" style="float:left;">
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='RecordValidator.jsp'">Record Validator</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='InstEdit.jsp'">Instrument Editor</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='RecordRegist.jsp'">Record Registration</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='RecordList.jsp'">Record List</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='StructureRegist.jsp'">Structure Registration</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='StructureList.jsp'">Structure List</div>
	</div>
	<div class="baseHerf" style="margin-left:490px;">
<%
	if ( isPortal ) {
%>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='RecordUtil.jsp?act=check'">Validator</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='RecordUtil.jsp?act=sql'">SQL File Generator</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='FileUpload.jsp'">File Upload</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='GenRecordList.jsp'">Record List Generator</div>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='../jsp/DispVersion.jsp'">Version Information</div>
<%
	}
%>
		<div class="item" onMouseOver="className='itemOver'" onMouseOut="className='item'" onClick="parent.location.href='Manager.jsp'">Database Manager</div>
	</div>
</div>
<br />
<hr size="1">
<iframe src="../copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<br />
</body>
</html>
