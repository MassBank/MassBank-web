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
 * BatchSearch表示用モジュール
 *
 * ver 1.0.7 2009.10.30
 *
 ******************************************************************************/
%>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="massbank.BatchJobManager" %>
<%@ page import="massbank.BatchJobInfo" %>
<%!
	/**
	 * HTML出力
	 * @param url
	 * @param message
	 * @param mailAddress
	 * @param isJp
	 * @return html
	 */
	private String outInputForm( String url, String message, String mailAddress, boolean isJp ) {
		String sampleUrl = "http://www.massbank.jp/sample/sample1_ja.txt";
		if ( !isJp ) {
			sampleUrl = "http://www.massbank.jp/sample/sample1_en.txt";
		}
		StringBuffer html = new StringBuffer();
		html.append("<form action=\"" + url + "\" enctype=\"multipart/form-data\" method=\"POST\">\n");
		html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"15\" class=\"form-box\">\n");
		html.append("<tr>\n");
		html.append("<td><b>Query File</b></td>\n");
		html.append("<td>\n");
		html.append("<input type=\"file\" name=\"file\" size=\"32\">&nbsp;&nbsp;&nbsp;&nbsp;\n");
		html.append("<img src=\"./img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"font12px text\" href=\"" + sampleUrl + "\" target=\"_blank\">sample file</a></b>&nbsp;&nbsp;");
		html.append("<img src=\"./img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"font12px text\" href=\"http://www.massbank.jp/sample/sample.zip\" target=\"_blank\">sample archive</a></b>&nbsp;");
		html.append("</td>\n");
		html.append("</tr>\n");
		html.append("<tr>\n");
		html.append("<td><b>Mail Address</b></td>\n");
		html.append("<td><input type=\"text\" name=\"mail\" size=\"32\" value=\"" + mailAddress + "\"></td>\n");
		html.append("</tr>\n");
		html.append("<tr>\n");
		html.append("<td colspan=\"2\">This service will appear as a part of Quick Search Page.</td>\n");
		html.append("</tr>\n");
		html.append("</table>\n");
		html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
		html.append("<tr>\n");
		html.append("<td>\n");
		html.append("<br><input type=\"submit\" value=\"Submit\" class=\"search\">\n");
		html.append("</td>\n");
		html.append("</tr>\n");
		html.append("</table>\n");
		html.append("</form>\n");
		if ( message != null && !message.equals("") ) {
			html.append("<font color=\"red\"><b>" + message + "</b></font><br>\n");
		}
		return html.toString();
	}
%>
<%
	//-------------------------------------
	// ブラウザ優先言語による言語判別
	//-------------------------------------
	String browserLang = (request.getHeader("accept-language") != null) ? request.getHeader("accept-language") : "";
	boolean isJp = false;
	if ( browserLang.startsWith("ja") || browserLang.equals("") ) {
		isJp = true;
	}
	
	String manualUrl = "http://www.massbank.jp/manuals/UserManual_ja.pdf";
	if ( !isJp ) {
		manualUrl = "http://www.massbank.jp/manuals/UserManual_en.pdf";
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta name="description" content="Similarity search of MSn spectra in batch process">
<meta name="keywords" content="Batch, Similarity">
<meta name="revisit_after" content="30 days">
<link rel="stylesheet" type="text/css" href="./css/Common.css">
<title>MassBank | Database | Batch Service</title>
</head>
<body class="msbkFont">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Batch Service</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=manualUrl%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
<iframe src="./menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<hr size="1">
<%
	String url = "./BatchSearch.html";
	String tempDir = System.getProperty("java.io.tmpdir");
	
	// パラメータ取得
	String mailAddress = "";
	String tempName = "";
	String flName = "";
	File temp = null;
	long fileSize = 0;
	
	boolean isInput = false;
	String message = "";
	DiskFileUpload dfu = new DiskFileUpload();
	if ( !dfu.isMultipartContent(request) ) {
		isInput = true;
		out.print( outInputForm(url, "", "", isJp) );
	}
	else {
		dfu.setSizeMax(-1);
		dfu.setSizeThreshold(1024);
		dfu.setRepositoryPath(tempDir);
		dfu.setHeaderEncoding("Windows-31J");
		List list = dfu.parseRequest(request);
		Iterator iterator = list.iterator();
		
		while ( iterator.hasNext() ) {
			FileItem fItem = (FileItem)iterator.next();
			if ( ! fItem.isFormField() ) {
				flName = fItem.getName();
				fileSize = fItem.getSize();
				if ( (flName != null) && (!flName.equals("")) ) {
					temp = File.createTempFile("batch", ".txt");
					fItem.write(temp);
					tempName= temp.getName();
				}
			}
			else if ( fItem.getFieldName().equals("mail") ) {
				mailAddress = fItem.getString();
			}
		}
		BatchJobManager job = new BatchJobManager();
		int cnt = job.getCount();
		boolean isError = false;
		if ( flName.equals("") ) {
			message = "No input query file.<br>";
			isError = true;
		}
		// メールアドレスチェック
		if ( mailAddress.equals("")
		 || !mailAddress.matches("[\\p{Alnum}._%+-]+@[\\p{Alnum}.-]+\\.\\p{Alpha}{2,4}") ) {
			message += "Bad mail address.<br>";
			if ( !tempName.equals("") ) {
				File f = new File( tempDir + "/" + tempName );
				f.delete();
			}
			isError = true;
		}
		if ( isError ) {
			out.print( outInputForm(url, message, mailAddress, isJp) );
		}
		// ジョブ同時実行数は5まで
		else if ( cnt > 5 ) {
			message = "System is busy now. Please retry later.";
			out.print( outInputForm(url, message, mailAddress, isJp) );
		}
		else {
			// セッションID, IPアドレス, 時刻
			Calendar cal = Calendar.getInstance();
			long timeMillis = cal.getTimeInMillis();
			SimpleDateFormat sdf1 = new SimpleDateFormat( "yyyyMMddHHmmss" );
			BatchJobInfo jobInfo = new BatchJobInfo();
			jobInfo.setSessionId( request.getSession(true).getId() );
			jobInfo.setIpAddr( request.getRemoteAddr() );
			jobInfo.setMailAddr( mailAddress );
			jobInfo.setTimeStamp( sdf1.format(new Date(timeMillis)) );
			jobInfo.setFileName( flName);
			jobInfo.setFileSize( String.valueOf(fileSize) );
			jobInfo.setTempName( tempName );
			
			SimpleDateFormat sdf2 = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
			String time = sdf2.format(new Date(timeMillis));
			
			// ジョブエントリをセット
			job.setEntry(jobInfo);
			
			// ジョブエントリのチェック
			if ( !job.checkEntry() ) {
				// ファイル削除
				File f = new File( tempDir + "/" + tempName );
				f.delete();
				message = "Your job is already running.";
				out.print( outInputForm(url, message, mailAddress, isJp) );
			}
			else {
				// ジョブエントリを追加する
				job.addEntry();
				
				out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"15\">");
				out.println("<tr>");
				out.println("<td>");
				out.println("[" + time + "]" + "<br>");
				out.println("Your batch search is accepted.<br>");
				out.println("The results will be sent to " + mailAddress + " later.");
				out.println("</td>");
				out.println("</tr>");
				out.println("</table>");
			}
		}
	}
%>
<hr size="1">
<iframe src="./copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
