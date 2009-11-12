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
 * Instrument情報追加用モジュール
 *
 * ver 1.0.5 2009.02.06
 *
 ******************************************************************************/
%>

<%@ page import="java.net.*,java.io.*" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.GetInstInfo" %>
<%
	//---------------------------------------------
	// リクエストURLを取得
	//---------------------------------------------
	String reqUrl = request.getRequestURL().toString();
	String find = "mbadmin/";
	int pos1 = reqUrl.indexOf( find );
	String baseUrl = reqUrl.substring( 0, pos1  );
	String jspName = reqUrl.substring( pos1 + find.length() );
	String reqStr = "";
	
	//-------------------------------------------
	// 環境設定ファイルからURLリストを取得
	//-------------------------------------------
	GetConfig conf = new GetConfig(baseUrl);
	String[] urlList = conf.getSiteUrl();
	String[] dbNameList = conf.getDbName();
	String serverUrl = conf.getServerUrl();
	
	//---------------------------------------------
	// リクエストパラメータ取得
	//---------------------------------------------
	String selDbName = "";
	if ( request.getParameter("db") != null ) {
		selDbName = request.getParameter("db");
	}
	else {
		selDbName = dbNameList[GetConfig.MYSVR_INFO_NUM];
	}
	
	String act = "";
	if ( request.getParameter("act") != null ) {
		act = request.getParameter("act");
	}
	String inst_no = "";
	if ( request.getParameter("inst_no") != null ) {
		inst_no = request.getParameter("inst_no");
	}
	String inst_type = "";
	if ( request.getParameter("inst_type") != null ) {
		inst_type = request.getParameter("inst_type");
		inst_type = inst_type.trim();
	}
	String inst_name = "";
	if ( request.getParameter("inst_name") != null ) {
		inst_name = request.getParameter("inst_name");
		inst_name = inst_name.trim();
	}
	
	String err = "";
	String msg = "";
	if ( act.equals("add") ||  act.equals("del") ) {
		String strUrl = urlList[GetConfig.MYSVR_INFO_NUM] + "cgi-bin/InstUtil.cgi";
		String param = "act=" + act + "&inst_no=" + inst_no;
		
		// 追加
		if ( act.equals("add") ) {
			if ( inst_type.equals("") || inst_name.equals("") ) {
				err = "No input data.";
			}
			else {
				inst_type = inst_type.replaceAll( "'", "\\\\'" );
				inst_name = inst_name.replaceAll( "'", "\\\\'" );
				inst_name = URLEncoder.encode(inst_name , "UTF-8");
				param += "&inst_type=" + inst_type + "&inst_name=" + inst_name;
				msg = "NO. " + inst_no + " added.";
			}
		}
		else if ( act.equals("del") ) {
			msg = "NO. " + inst_no + " deleted.";
		}
		param += "&dsn=" + selDbName;
		if ( err.equals("") ) {
			URL url = new URL( strUrl );
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			PrintStream psm = new PrintStream( con.getOutputStream() );
			psm.print( param );
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			in.close();
		}
	}
	
	//---------------------------------------------
	// INSTRUMENT情報を取得
	//---------------------------------------------
	GetInstInfo instInfo = new GetInstInfo( baseUrl );
	int dbIndex = 0;
	for ( int i = 0; i < dbNameList.length; i++ ) {
		if ( dbNameList[i].equals(selDbName) ) {
			dbIndex = i;
			break;
		}
	}
	instInfo.setIndex(dbIndex);
	boolean existInstInfo = false;
	String[] instNo = instInfo.getNo();
	String[] instName = null;
	String[] instType = null;
	String insNextNo = "1";
	if ( instNo.length > 0 ) {
		instName = instInfo.getName();
		instType = instInfo.getType();
		insNextNo = String.valueOf( Integer.parseInt(instNo[instNo.length-1]) + 1 );
		existInstInfo = true;
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<link rel="stylesheet" type="text/css" href="css/admin.css">
<title>Add Instrument Information</title>
<script language="javascript" type="text/javascript">
<!--
function confirmDel(inst_no) {
	ret = confirm( 'NO. ' + inst_no + ' delete OK?' );
	if ( ret ) {
		document.forms[1].target = "_self";
		document.forms[1].method = "post";
		document.forms[1].inst_no.value = inst_no;
		document.forms[1].submit();
	}
}
//-->
</script>
</head>
<body onload="document.forms[0].inst_type.focus();">
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Add Instrument Information</h2>
<b>Database: </b>&nbsp;
<%
	String url = "'" + jspName + "?db=' + " + "this[this.selectedIndex].value";
	out.println( "<select name=\"db\" class=\"db\" onChange=\"window.open("+ url + ", '_self');\">" );
	for ( int i = 0; i < dbNameList.length; i++ ) {
		if ( i > 0 && !urlList[i].equals(serverUrl) ) {
			continue;
		}
		out.print( "<option value=\"" + dbNameList[i] + "\"" );
		if ( selDbName.equals(dbNameList[i]) ) {
			out.print( " selected" );
		}
		out.println( ">" + dbNameList[i] );
	}

%>
</select>
<br><br>
<form name="form1" method="post" action="<%out.print(jspName);%>">
<table width="750">
<tr>
<td>
<div class="tb3">
<table>
<tr>
<td width="80"><b>NO</b></td>
<td><input type="text" name="inst_no" size="4" value="<%out.print(insNextNo);%>"></td>
</tr>
<tr>
<td width="80"><b>TYPE</b></td>
<td><input type="text" name="inst_type" size="20"></td>
</tr>
<tr>
<td><b>NAME</b></td>
<td><input type="text" name="inst_name" size="50"></td>
</tr>
</table>
<input type="hidden" name="act" value="add">
<input type="hidden" name="db" value="<%out.print(selDbName);%>">
<input type="submit" value="Add">
</div>
</form>
<br>
<form name="form2" method="post" action="<%out.print(jspName);%>">
<%
	if ( !err.equals("") ) {
		out.println( "<font color=\"Crimson\"><b>Error : " + err + "</b></font><br><br>" );
	}
	if ( !msg.equals("") ) {
		out.println( "<font color=\"blue\"><b>" + msg + "</b></font><br><br>" );
	}
	
	if ( existInstInfo ) {
		out.println( "<div class=\"tbTitle\">" );
		out.println( "<table>" );
		out.println( "<tr style=\"color:white\">" );
		out.println( "<td width=\"50\">NO</td>" );
		out.println( "<td width=\"150\">TYPE</td>" );
		out.println( "<td width=\"500\">NAME</td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "</div>" );
		
		for ( int i = 0; i < instNo.length; i++ ) {
			String className = "";
			if ( i % 2 == 0 ) { className = "tb1"; }
			else              { className = "tb2"; }
			out.println( "<div class=\"" + className + "\">" );
			out.println( "<table>" );
			out.println( "<tr>" );
			out.println( "<td width=\"50\">" + instNo[i] + "</td>" );
			out.println( "<td width=\"150\">" + instType[i] + "</td>" );
			out.println( "<td width=\"500\">" + instName[i] + "</td>" );
			out.println( "<td width=\"80\" align=right>" );
			out.println( "<input type=\"button\" value=\"Delete\" onclick=\"confirmDel('" + instNo[i] + "');\">" );
			out.println( "</td>" );
			out.println( "</tr>" );
			out.println( "</table>" );
			out.println( "</div>" );
		}
	}
	else {
		out.println( "<big>No data entry</big>" );
	}
%>
<input type="hidden" name="act" value="del">
<input type="hidden" name="inst_no">
<input type="hidden" name="db" value="<%out.print(selDbName);%>">
</form>
</td>
</tr>
</table>
</body>
</html>
