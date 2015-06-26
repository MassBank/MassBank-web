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
 * File Uploader
 *
 * ver 1.0.5 2011.11.09
 *
 ******************************************************************************/
%>

<%@ page import="java.io.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="massbank.FileUpload" %>
<%@ page import="massbank.admin.AdminCommon" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<link rel="stylesheet" type="text/css" href="css/admin.css">
	<script type="text/javascript" src="../script/Common.js"></script>
	<script language="javascript" type="text/javascript">
<!--
var OFF_COLOR = "";
var ON_COLOR = "LightSteelBlue";
var prevIndex = -1;
var ev;
window.document.onkeydown = function(e){ ev = e; }
	
/**
 * アップロード前チェック
 */
function fileCheck() {
	
	// empty check
	var upFileName = document.forms[0].file.value;
	if ( upFileName == "" ) {
		alert( "please select upload file." );
		return false;
	}
	
	// overwrite check
	var pos = upFileName.lastIndexOf("\\");
	if ( pos > -1 ) {
		upFileName = upFileName.substring( pos + 1 );
	}
	pos = upFileName.lastIndexOf("/");
	if ( pos > -1 ) {
		upFileName = upFileName.substring( pos + 1 );
	}
	for ( i=0; i<document.forms[0].exist.length; i++ ) {
		if ( document.forms[0].exist[i].value == upFileName ) {
			return confirm( "[" + upFileName + "] already exists. Overwrite?" );
		}
	}
	return true;
}

/**
 * 削除前処理
 */
function beforeDelete() {
	
	objForm = document.form2;
	
	chkFlag = false;
	if ( objForm.fileId.length != null ) {
		// 2件以上
		for (i=0; i<objForm.fileId.length; i++){
			if (objForm.fileId[i].disabled) {
				continue;
			}
			else if (objForm.fileId[i].checked) {
				chkFlag = true;
				break;
			}
		}
	}
	else {
		// 1件
		if (objForm.fileId.checked) {
			chkFlag = true;
		}
	}
	
	if ( !chkFlag ) {
		alert("Please select one or more checkbox.");
		return false;
	}
	
	if ( confirm("are you sure?") ) {
		objForm.act.value = "del";
		return true;
	}
	else {
		return false;
	}
}

	/**
	 * チェックボックスチェック
	 */
	function check(index) {
	
		// SHIFTキーが押されているか？
		isShift = false;
		if ( navigator.appName.indexOf("Microsoft") != -1 ) {
			// IEの場合
			if ( window.event.shiftKey ) {
				isShift = true;
			}
		}
		else {
			if ( ev != null && ev.shiftKey ) {
				isShift = true;
				ev = null;
			}
		}
		
		objForm = document.form2;
		// SHIFTキーが押されている場合は複数行選択
		if ( isShift && prevIndex != -1 ) {
			if ( index < prevIndex ) {
				start = index; end = prevIndex;
			}
			else {
				start = prevIndex;
				end = index;
			}
			isCheck = objForm.fileId[index].checked;
			if ( isCheck ) {
				bgcolor = ON_COLOR;
			}
			else {
				bgcolor = OFF_COLOR;
			}
			for ( i = start; i <= end; i++ ) {
				if (objForm.fileId[i].disabled) {
					continue;
				}
				objForm.fileId[i].checked = isCheck;
				obj = document.getElementById( String("row" + i) );
				obj.style.background = bgcolor;
			}
		}
		else {
			obj = document.getElementById( String("row" + index) );
			if ( objForm.fileId[index].checked ) {
				obj.style.background = ON_COLOR;
			}
			else {
				obj.style.background = OFF_COLOR;
			}
		}
		prevIndex = index;
	}
	
/**
 * チェックボックス一括チェック
 */
function checkAll() {
	
	objForm = document.form2;
	isCheck = objForm.chkAll.checked;
	if ( isCheck ) {
		bgcolor = ON_COLOR;
	}
	else {
		bgcolor = OFF_COLOR;
	}
	
	if ( objForm.fileId.length != null ) {
		for ( i=0; i<objForm.fileId.length; i++ ) {
			if (objForm.fileId[i].disabled) {
				continue;
			}
			objForm.fileId[i].checked = isCheck;
			document.getElementById(String("row" + i)).style.background = bgcolor;
		}
	}
	else {
		objForm.fileId.checked = isCheck;
		document.getElementById(String("row0")).style.background = bgcolor;
	}
}
//-->
	</script>
	<title>File Upload</title>
</head>
<body>
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>File Upload</h2>
<%
	//---------------------------------------------
	// 各種パラメータ取得および設定
	//---------------------------------------------
	request.setCharacterEncoding("utf-8");
	AdminCommon admin = new AdminCommon();
	final String outPath = (!admin.getOutPath().equals("")) ? admin.getOutPath() : System.getProperty("java.io.tmpdir");
	FileUpload up = new FileUpload(request, outPath);
	
	//---------------------------------------------
	// ファイル削除処理
	//---------------------------------------------
	if ( !FileUpload.isMultipartContent(request) ) {
		String act = "";
		String[] fileIds = null;
		
		Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			if ( key.equals("act") ) {
				act = request.getParameter(key);
			}
			else if ( key.equals("fileId") ) {
				fileIds = request.getParameterValues(key);
			}
		}
		
		if ( act.equals("del") ) {
			if (fileIds == null || fileIds.length != 0) {
				for ( String fileName : fileIds ) {
					File file = new File( outPath + "/" + fileName );
					try {
						if ( file.exists() ) {
							file.delete();
						}
					}
					catch (SecurityException e) {
						Logger.global.severe( "Molfile : " + file.getPath() );
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	//---------------------------------------------
	// フォーム表示
	//---------------------------------------------
	out.println( "<form name=\"form1\" action=\"./FileUpload.jsp\" enctype=\"multipart/form-data\" method=\"post\" onSubmit=\"doWait()\">" );
	out.println( "<span class=\"baseFont\">Upload File :</span>&nbsp;" );
	out.println( "<input type=\"file\" name=\"file\" size=\"70\">&nbsp;" );
	out.println( "<input type=\"submit\" value=\"Upload\" onClick=\"return fileCheck()\"><br><br>" );
	
	//---------------------------------------------
	// ファイルアップロード処理
	//---------------------------------------------
	String tmpUpFileName = "";
	if ( FileUpload.isMultipartContent(request) ) {
		String upFileName = "";
		boolean upResult = false;
		HashMap<String, Boolean> upFileMap = up.doUpload();
		if ( upFileMap != null ) {
			for (Map.Entry<String, Boolean> e : upFileMap.entrySet()) {
				upFileName = e.getKey();
				upResult = e.getValue();
				break;
			}
			if ( upFileName.equals("") ) {
				out.println( "<span class=\"errFont\">please select upload file.</span><br>" );
			}
			else if ( !upResult ) {
				out.println( "<span class=\"errFont\">[" + upFileName + "]&nbsp;&nbsp;uploaded failed.</span><br>" );
			}
			else {
				out.println( "<span class=\"msgFont\">[" + upFileName + "]&nbsp;&nbsp;uploaded.</span><br>" );
				tmpUpFileName = upFileName;
			}
		}
		else {
			out.println( "<span class=\"errFont\">server error.</span><br>" );
		}
		up.deleteFileItem();
	}
	else {
		out.println( "<span class=\"msgFont\">&nbsp;</span><br>" );
	}
	
	String[] fileList = (new File( outPath )).list();
	Arrays.sort(fileList);
	for ( String fileName : fileList ) {
		if ( fileName.indexOf(".") == 0 ) {
			continue;
		}
		out.println( "<input type=\"hidden\" name=\"exist\" value=\"" + fileName + "\">" );
	}
	out.println( "</form>" );
	
	//---------------------------------------------
	// 一覧表示
	//---------------------------------------------
	out.println( "<hr size=\"1\" width=\"780\" align=\"left\"><br>" );
	out.println( "\t<span class=\"baseFont\">Upload Directory :</span><br>" );
	out.println( "&nbsp;&nbsp;<span class=\"pathFont\">" + outPath + "</span><br><br>" );
	
	out.println( "<form name=\"form2\" action=\"./FileUpload.jsp\" method=\"post\" onSubmit=\"doWait();\">" );
	out.println( "\t<table width=\"710\" cellspacing=\"0\" cellpadding=\"0\">" );
	out.println( "\t\t<tr>" );
	out.println( "\t\t\t<td colspan=\"5\">\t\t\t\t" );
	out.println( "<input type=\"submit\" value=\"Delete\" onClick=\"return beforeDelete();\">&nbsp;&nbsp;" );
	out.println( "\t\t</td>" );
	out.println( "\t\t</tr>" );
	
	out.println( "\t\t<tr height=\"15\">" );
	out.println( "\t\t</tr>" );
	
	out.println( "\t\t<tr class=\"rowHeader\">");
	out.println( "\t\t\t<td width=\"30\"><input type=\"checkbox\" name=\"chkAll\" value=\"\" onClick=\"checkAll();\"></td>" );
	out.println( "\t\t\t<td width=\"160\">LAST MODIFIED</td>" );
	out.println( "\t\t\t<td>FILE NAME</td>" );
	out.println( "\t\t</tr>");
	out.println( "\t</table>" );
	
	out.println( "\t<table width=\"710\" cellspacing=\"1\" cellpadding=\"0\" bgcolor=\"Lavender\">" );
	SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/ MM/ dd HH:mm" );
	for ( int i=0; i<fileList.length; i++ ) {
		if ( fileList[i].indexOf(".") == 0 ) {
			continue;
		}
		File file = new File( outPath + "/" + fileList[i] );
		String strImg = ( file.isFile() ) ? "file.gif" : "folder.gif";
		String strDate = sdf.format( new Date(file.lastModified()) );
		String strFont = ( tmpUpFileName.equals(fileList[i]) ) ? " msgFont" : "";
		
		out.println( "<tr class=\"rowEnable\" id=\"row" + i + "\">" );
		out.println( "<td class=\"center\" width=\"30\"><input type=\"checkbox\" name=\"fileId\" value=\"" + fileList[i] + "\" onClick=\"check(" + i + ");\"></td>" );
		out.println( "<td class=\"leftIndent" + strFont + "\" width=\"160\">" + strDate + "</td>" );
		out.println( "<td class=\"leftIndent" + strFont + "\"><img src=\"image/" + strImg + "\" alt=\"\">&nbsp;&nbsp;" + fileList[i] + "</td>" );
		out.println( "</tr>" );
	}
	out.println( "</table>" );
	out.println( "\t<input type=\"hidden\" name=\"act\" value=\"\">\n" );
	out.println( "</form>" );
	out.println( "</body>" );
	out.println( "</html>" );
%>
