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
 * BatchSearch表示用モジュール
 *
 * ver 1.0.11 2010.12.24
 *
 ******************************************************************************/
%>
<%@ page import="java.io.File" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="massbank.JobManager" %>
<%@ page import="massbank.JobInfo" %>
<%@ include file="./Common.jsp"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta name="author" content="MassBank" />
<meta name="coverage" content="worldwide" />
<meta name="Targeted Geographic Area" content="worldwide" />
<meta name="rating" content="general" />
<meta name="copyright" content="Copyright (c) since 2006 JST-BIRD MassBank" />
<meta name="description" content="Similarity search of MSn spectra in batch process. To obtain a whole search results for many user's spectra in an e-mail.">
<meta name="keywords" content="Batch,Similarity,MSn,mail">
<meta name="revisit_after" content="30 days">
<link rel="stylesheet" type="text/css" href="./css/Common.css">
<script type="text/javascript" src="./script/Common.js"></script>
<title>MassBank | Database | Batch Service</title>
</head>
<body class="msbkFont">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Batch Service</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<iframe src="./menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	<hr size="1">
	
	<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
	<jsp:include page="../pserver/ServerInfo.jsp" />
<%
	boolean isHtml = false;
	boolean isHtmlResult = false;
	String time = "";
	String instGrp = "";
	String instType = "";
	String ionMode  = "1";
	
	boolean isFirst = false;
	List<String> instGrpList = new ArrayList<String>();
	List<String> instTypeList = new ArrayList<String>();
	
	String tempDir = System.getProperty("java.io.tmpdir");
	
	// パラメータ取得
	String mailAddress = "";
	String tempName = "";
	String flName = "";
	File temp = null;
	long fileSize = 0;
	
	String message = "";
	if ( !DiskFileUpload.isMultipartContent(request) ) {
		isHtml = true;
	}
	else {
		DiskFileUpload dfu = new DiskFileUpload();
		dfu.setSizeMax(-1);
		dfu.setSizeThreshold(1024);
		dfu.setRepositoryPath(tempDir);
		dfu.setHeaderEncoding("utf-8");
		List list = dfu.parseRequest(request);
		Iterator iterator = list.iterator();
		
		
		//-------------------------------------
		// リクエストパラメータ取得
		//-------------------------------------
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
				mailAddress = fItem.getString().trim();
			}
			else if ( fItem.getFieldName().equals("inst_grp") ) {
				instGrpList.add(fItem.getString().trim());
			}
			else if ( fItem.getFieldName().equals("inst") ) {
				instTypeList.add(fItem.getString().trim());
			}
			else if ( fItem.getFieldName().equals("ion") ) {
				ionMode = fItem.getString().trim();
			}
		}
		for ( int i = 0; i < instGrpList.size(); i++ ) {
			instGrp += instGrpList.get(i);
			if ( i < instGrpList.size() - 1 ) {
				instGrp += ",";
			}
		}
		for ( int i = 0; i < instTypeList.size(); i++ ) {
			instType += instTypeList.get(i);
			if ( i < instTypeList.size() - 1 ) {
				instType += ",";
			}
		}
		
		
		//-------------------------------------
		// ジョブ実行
		//-------------------------------------
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
			isHtml = true;
		}
		else {
			// セッションID, IPアドレス, 時刻
			Calendar cal = Calendar.getInstance();
			long timeMillis = cal.getTimeInMillis();
			SimpleDateFormat sdf2 = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
			time = sdf2.format(new Date(timeMillis));

			JobInfo jobInfo = new JobInfo();
			jobInfo.setSessionId( request.getSession(true).getId() );
			jobInfo.setIpAddr( request.getRemoteAddr() );
			jobInfo.setMailAddr( mailAddress );
			jobInfo.setTimeStamp( time );
			jobInfo.setQueryFileName( flName );
			jobInfo.setQueryFileSize( String.valueOf(fileSize) );
			jobInfo.setSearchParam( "inst=" + instType + "&ion=" + ionMode );
			jobInfo.setTempName( tempName );
			
			// 重複ジョブエントリを有無をチェックする
			JobManager jobMgr = new JobManager();
			if ( !jobMgr.checkDuplicateEntry(jobInfo) ) {
				// クエリファイルを削除する
				File f = new File( tempDir + "/" + tempName );
				f.delete();
				message = "Your job is already running.";
				isHtml = true;
			}
			else {
				// ジョブエントリを追加する
				String jobId = jobMgr.addJobInfo(jobInfo);
				isHtmlResult = true;
			}
			jobMgr.end();
		}
	}
	
	
	//-------------------------------------
	// HTML出力
	//-------------------------------------
	// 通常用HTML
	if (isHtml) {
		out.println("\t<form name=\"form_query\" action=\"./BatchSearch.html\" enctype=\"multipart/form-data\" method=\"POST\">");
		out.println("\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
		out.println("\t\t\t<tr>");
		out.println("\t\t\t\t<td valign=\"top\">");
		out.println("\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"15\" class=\"form-box\" width=\"488\">");
		out.println("\t\t\t\t\t\t<tr>");
		out.println("\t\t\t\t\t\t\t<td valign=\"top\" width=\"100px\"><b>Query File</b></td>");
		out.println("\t\t\t\t\t\t\t<td>");
		out.println("\t\t\t\t\t\t\t\t<input type=\"file\" name=\"file\" size=\"32\"><br>&nbsp;&nbsp;&nbsp;");
		out.println("\t\t\t\t\t\t\t\t<img src=\"./img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"font12px text\" href=\"" + SAMPLE_URL + "\" target=\"_blank\">sample file</a></b>&nbsp;&nbsp;");
		out.println("\t\t\t\t\t\t\t\t<img src=\"./img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"font12px text\" href=\"" + SAMPLE_ZIP_URL + "\" target=\"_blank\">sample archive</a></b>&nbsp;");
		out.println("\t\t\t\t\t\t\t</td>");
		out.println("\t\t\t\t\t\t</tr>");
		out.println("\t\t\t\t\t\t<tr>");
		out.println("\t\t\t\t\t\t\t<td width=\"100px\"><b>Mail Address</b></td>");
		out.println("\t\t\t\t\t\t\t<td><input type=\"text\" name=\"mail\" size=\"32\" value=\"" + mailAddress + "\"></td>");
		out.println("\t\t\t\t\t\t</tr>");
		out.println("\t\t\t\t\t\t<tr style=\"padding-top:10px;\">");
		out.println("\t\t\t\t\t\t\t<td colspan=\"2\" class=\"font12px\" style=\"text-indent:20px;\">This service will appear as a part of Quick Search Page.</td>");
		out.println("\t\t\t\t\t\t</tr>");
		out.println("\t\t\t\t\t</table>");
		out.println("\t\t\t\t\t<br>");
		out.println("\t\t\t\t\t<table>");
		out.println("\t\t\t\t\t\t<tr>");
		out.println("\t\t\t\t\t\t\t<td>");
		out.println("\t\t\t\t\t\t\t\t<input type=\"submit\" value=\"Submit\" class=\"search\" onClick=\"return checkFileExtention(form_query.file.value) && checkSubmit();\">");
		out.println("\t\t\t\t\t\t\t</td>");
		out.println("\t\t\t\t\t\t</tr>");
		out.println("\t\t\t\t\t</table>");
		out.println("\t\t\t\t</td>");
		out.println("\t\t\t\t<td valign=\"top\" style=\"padding:0px 15px;\">");
%>
<jsp:include page="Instrument.jsp" flush="true">
	<jsp:param name="ion" value="<%= ionMode %>" />
	<jsp:param name="first" value="<%= isFirst %>" />
	<jsp:param name="inst_grp" value="<%= instGrp %>" />
	<jsp:param name="inst" value="<%= instType %>" />
</jsp:include>
<%
		out.println("\t\t\t\t</td>");
		out.println("\t\t\t</tr>");
		out.println("\t\t</table>");
		out.println("\t\t<input type=\"hidden\" name=\"isFirst\" value=\"" + isFirst + "\"");
		out.println("\t</form>");
		if ( message != null && !message.equals("") ) {
			out.println("\t<font color=\"red\"><b>" + message + "</b></font><br>");
		}
	}
	// 結果用HTML
	if (isHtmlResult) {
		out.println("\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"15\">");
		out.println("\t\t<tr>");
		out.println("\t\t\t<td>");
		out.println("\t\t\t\t<span>[ " + time + " ]" + "</span><br>");
		out.println("\t\t\t\t<span>Your batch search is accepted.</span><br>");
		out.println("\t\t\t\t<span>The results will be sent to " + mailAddress + " later.</span>");
		out.println("\t\t\t</td>");
		out.println("\t\t</tr>");
		out.println("\t</table>");
	}
%>
	<br>
	<hr size="1">
	<iframe src="./copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
