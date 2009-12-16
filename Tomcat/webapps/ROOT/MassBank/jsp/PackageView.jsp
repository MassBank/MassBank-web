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
 * Package View表示用モジュール
 *
 * ver 1.0.9 2009.12.16
 *
 ******************************************************************************/
%>

<%@ page import="java.util.*,java.io.*" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ include file="./Common.jsp"%>
<%
	// テンポラリディレクトリ
	String tempDir = System.getProperty("java.io.tmpdir");
	
	String fileName = "";
	if ( request.getParameter( "file" ) != null ) {;
		fileName = request.getParameter( "file" );
	}
	
	if ( fileName.equals("") ) {
		try {
			boolean isMultiContent = false;					// フォームのマルチパートデータフラグ
			String tempName = "";
			if (!DiskFileUpload.isMultipartContent(request)) {
				isMultiContent = false;
			}
			else {
				isMultiContent = true;
				
				//-------------------------------------------
				// アップロード設定
				//-------------------------------------------
				DiskFileUpload dfu = new DiskFileUpload();
				dfu.setSizeMax(-1);							// サイズ
				dfu.setSizeThreshold(1024);					// バッファサイズ
				dfu.setRepositoryPath(tempDir);				// 保存先フォルダ
				dfu.setHeaderEncoding("Windows-31J");		// ヘッダの文字エンコーディング
				
				List list = dfu.parseRequest(request);
				Iterator iterator = list.iterator();
				
				File temp = null;
				//-------------------------------------------
				// アップロードファイルはテンポラリに格納
				//-------------------------------------------
				while ( iterator.hasNext() ) {
					FileItem fItem = (FileItem)iterator.next();
					temp = File.createTempFile( "massbank", ".txt" );
					fItem.write(temp);
					fItem.delete();
					tempName= temp.getName();
				}
			}
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta http-equiv="Content-Style-Type" content="text/css">
		<meta http-equiv="imagetoolbar" content="no">
		<meta name="description" content="3D viewer of user's spectra">
		<meta name="keywords" content="Spectral,3D,Package,View">
		<meta name="revisit_after" content="30 days">
		<link rel="stylesheet" type="text/css" href="./css/Common.css">
		<script type="text/javascript" src="./script/Common.js"></script>
		<title>MassBank | Database | Spectral Browser</title>
	</head>
	<body class="msbkFont">
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td>
					<h1>Spectral Browser</h1>
				</td>
				<td align="right" class="font12px">
					<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="http://www.massbank.jp/manuals/package_doc.html" target="_blank">user manual (in Japanese)</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				</td>
			</tr>
		</table>
		<iframe src="./menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
		<hr size="1">
		<form action="./PackageView.html" enctype="multipart/form-data" method="POST">
			<img src="./image/file.gif" align="left">
			<input type="file" name="File" size="32">&nbsp;
			<input type="submit" value="File Read" onClick="return checkFileExtention(forms[0].File.value);">&nbsp;&nbsp;
			<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="font12px text" href="<%=SAMPLE_URL%>" target="_blank">sample file</a></b>&nbsp;
			<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="font12px text" href="<%=SAMPLE_ZIP_URL%>">sample archive</a></b>&nbsp;
		</form>
		<hr size="1">
		<applet code="PackageView.class" archive="./applet/PackageView.jar" width="100%" height="100%">
<%
			if (isMultiContent) {
%>
			<param name="file" value="<%=tempName%>">
<%
			}
%>
		</applet>
	</body>
</html>
<%
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	else {
		//-------------------------------------------
		// テンポラリファイル読込み
		//-------------------------------------------
		String filePath = tempDir + "/"  + fileName;
		BufferedReader in = new BufferedReader( new FileReader(filePath) );
		String line;
		while ( ( line = in.readLine() ) != null ) {
			out.println( line );
		}
		in.close();
		
		//-------------------------------------------
		// ファイル削除
		//-------------------------------------------
		File f = new File( filePath );
		f.delete();
	}
%>
