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
 * Search Page表示用モジュール
 *
 * ver 1.0.15 2011.06.16
 *
 ******************************************************************************/
%>

<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
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
		<meta name="author" content="MassBank" />
		<meta name="coverage" content="worldwide" />
		<meta name="Targeted Geographic Area" content="worldwide" />
		<meta name="rating" content="general" />
		<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
		<meta name="description" content="Search similar spectra on a peak-to-peak basis. Retrieves spectra similar to user’s spectrum in terms of the m/z value. This search is helpful to identify chemical compound by comparing similar spectra on a 3D-display.">
		<meta name="keywords" content="Search,Similarity,Spectrum">
		<meta name="revisit_after" content="30 days">
		<link rel="stylesheet" type="text/css" href="./css/Common.css">
		<script type="text/javascript" src="./script/Common.js"></script>
		<title>MassBank | Database | Spectrum Search</title>
	</head>
	<body class="msbkFont">
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td>
					<h1>Spectrum Search</h1>
				</td>
				<td align="right" class="font12px">
					<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%><%=SPECTRUM_PAGE%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				</td>
			</tr>
		</table>
		<iframe src="./menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
		<hr size="1">
		
		<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
		<jsp:include page="../pserver/ServerInfo.jsp" />
		
		<form action="./SearchPage.html" enctype="multipart/form-data" method="POST">
			<img src="./image/file.gif" align="left">
			<input type="file" name="File" size="32">&nbsp;
			<input type="submit" value="File Read" onClick="return checkFileExtention(forms[0].File.value);">&nbsp;&nbsp;
			<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="font12px text" href="<%=SAMPLE_URL%>" target="_blank">sample file</a></b>&nbsp;
			<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="font12px text" href="<%=SAMPLE_ZIP_URL%>">sample archive</a></b>&nbsp;
		</form>
		<hr size="1">
		<applet code="SearchPage.class" archive="./applet/SearchApplet.jar" width="100%" height="100%" MAYSCRIPT>
<%
			if (isMultiContent) {
				out.println( "\t\t\t<param name=\"file\" value=\"" + tempName + "\">" );
			}
			else {
				String[] ids = request.getParameterValues("id");
				if ( ids != null && ids.length > 0 ) {
					out.println( "\t\t\t<param name=\"num\" value=\"" + String.valueOf(ids.length) + "\">" );
					for ( int i=0; i<ids.length ; i++ ) {
						String[] fields = ids[i].split("\t");
						String id = fields[1];
						String name = fields[0];
						String site = fields[fields.length-1];
						String pnum = String.valueOf(i+1);
						out.println( "\t\t\t<param name=\"qid" + pnum + "\" value=\"" + id + "\">" );
						out.println( "\t\t\t<param name=\"name" + pnum + "\" value=\"" + name +"\">" );
						out.println( "\t\t\t<param name=\"site" + pnum + "\" value=\"" + site +"\">" );
					}
				}
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
