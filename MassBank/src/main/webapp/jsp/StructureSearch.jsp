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
 * 部分構造検索クエリ表示用モジュール
 *
 * ver 1.0.21 2012.02.17
 *
 ******************************************************************************/
%>

<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.PrintStream" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.lang.Boolean" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.commons.lang3.math.NumberUtils" %>
<%@ include file="./Common.jsp"%>
<%!
	private HttpServletRequest req = null;

	/**
	 * パラメータセット処理
	 * @param key リクエストパラメータのキー名
	 * @param val リクエストパラメータの値
	 * @param molData molfileデータ格納用配列
	 * @param mz mz値格納用配列
	 */ 
	private void setAryParams(
		String key, String val, String[] molData, String[] mz ) {

		String[] findKeys = { "moldata", "mz" };
		int numKey = -1;
		int index = 0;
		for ( int i = 0; i < findKeys.length; i++ ) {
			if ( key.indexOf(findKeys[i]) == 0 ) {
				String num = key.substring(findKeys[i].length());
				if ( NumberUtils.isCreatable(num) ) {
					index = Integer.parseInt(num);
					numKey = i;
				}
				break;
			}
		}
		switch (numKey) {
			case 0:		// moldataフィールド
				molData[index] = val;
				break;
			case 1:		// mzフィールド
				mz[index] = val;
				break;
			default:
				break;
		}
	}
	
	/**
	 * molfileデータCount Line行チェック処理
	 * @param line 1行分のデータ
	 * @return true:正常 / false:不正
	 */
	private boolean checkMolCntLine(String line) {
		/* Count line行は39文字あるはず
			 バージョンの記述以降に多少のごみがあっても許すが
			 行が長すぎる場合はエラーにする
		*/
		if ( line.length() < 39 || line.length() > 100 ) {
			return false;
		}
		String atomLine = line.substring(0,3);
		String bondLine = line.substring(3,6);
		String[] numbers = new String[]{ atomLine.trim(), bondLine.trim() };
		for ( int i = 0; i < numbers.length; i++ ) {
			if ( NumberUtils.isCreatable(numbers[i]) ) {
				int val = Integer.parseInt(numbers[i]);
				if ( val == 0 || val > 999 ) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		return true;
	}

	/**
	 * リクエストパラメータを取得する
	 */
	private HashMap<String, Object> getParams() throws Exception {
		boolean isRead = false;
		String readMolData = "";
		String updateIndex = "";
		String[] molData = { "", "" };
		String[] mz = { "", "", "" };
		String tol = "0.3";
		String piCheck = "2";
		String ionMode = "1";
		List<String> instGrpList = new ArrayList<String>();
		List<String> instTypeList = new ArrayList<String>();
		List<String> msTypeList = new ArrayList<String>();
		boolean isInvalidQuery = false;
		boolean isFirst = true;
		String isSelectM = "true";

		DiskFileUpload dfu = new DiskFileUpload();
		//=============================================
		// マルチパートフォームの場合（Read Molfile時)
		//=============================================
		if ( DiskFileUpload.isMultipartContent(req) ) {
			isFirst = false;
			String tempDir = System.getProperty("java.io.tmpdir");
			dfu.setSizeMax(-1);								// サイズ
			dfu.setSizeThreshold(1024);						// バッファサイズ
			dfu.setRepositoryPath(tempDir);					// 保存先フォルダ
			dfu.setHeaderEncoding("Windows-31J");			// ヘッダの文字エンコーディング
			List list = dfu.parseRequest(req);
			Iterator iterator = list.iterator();
			while ( iterator.hasNext() ) {
				FileItem fItem = (FileItem)iterator.next();
				//-----------------------------------------
				// 通常フィールドの場合
				//-----------------------------------------
				if ( fItem.isFormField() ) {
					String key = fItem.getFieldName();
					String val = fItem.getString();
					if ( key.equals("num") ) {
						updateIndex = val;
					}
					else if ( key.equals("tol") ) {
						tol = val;
					}
					else if ( key.equals("pi_check") ) {
						piCheck = val;
					}
					else if ( key.equals("inst_grp") ) {
						instGrpList.add(val);
					}
					else if ( key.equals("inst") ) {
						instTypeList.add(val);
					}
					else if ( key.equals("ms") ) {
						msTypeList.add(val);
					}
					else if ( key.equals("ion") ) {
						ionMode = val;
					}
					else if ( key.equals("isSelectM") ) {
						isSelectM = val;
					}
					else {
						setAryParams( key, val, molData, mz );
					}
				}
				//-----------------------------------------
				// フィールドがマルチパートの場合、
				// アップロードファイルを読み込む
				//-----------------------------------------
				else {
					//** テンポラリファイルに書き込む **
					File temp = File.createTempFile( "query", ".mol" );
					fItem.write(temp);
					fItem.delete();
					
					//** テンポラリファイル読込み **
					String filePath = tempDir + "/"  + temp.getName();
					BufferedReader in = new BufferedReader( new FileReader(filePath) );
					String line = "";
					StringBuffer data = new StringBuffer();
					int cntLine = 0;
					while ( ( line = in.readLine() ) != null ) {
						cntLine++;
						data.append( line + "\n" );
						if ( cntLine == 4 ) {
							// molfileデータCount Line行チェック
							if ( !checkMolCntLine(line) ) {
								isInvalidQuery = true;
								break;
							}
						}
					}
					in.close();
					if ( !isInvalidQuery && cntLine > 3 ) {
						readMolData = data.toString();
					}
					else {
						isInvalidQuery = true;
					}
					
					//** テンポラリファイル削除 **
					File f = new File(filePath);
					f.delete();
				}
			}
			isRead = true;
		}
		//=============================================
		// 通常フォームの場合（Previous Query時）
		//=============================================
		else {
			Enumeration names = req.getParameterNames();
			if ( names.hasMoreElements() ) {
				isFirst = false;
			}
			while ( names.hasMoreElements() ) {
				String key = (String)names.nextElement();
				String val = req.getParameter(key);
				if ( key.equals("tol") ) {
					tol = val;
				}
				else if ( key.equals("pi_check") ) {
					piCheck = val;
				}
				else if ( key.equals("inst_grp") ) {
					String[] vals = req.getParameterValues(key);
					instGrpList = Arrays.asList(vals);
				}
				else if ( key.equals("inst") ) {
					String[] vals = req.getParameterValues(key);
					instTypeList = Arrays.asList(vals);
				}
				else if ( key.equals("ms") ) {
					String[] vals = req.getParameterValues(key);
					msTypeList = Arrays.asList(vals);
				}
				else if ( key.equals("ion") ) {
					ionMode = val;
				}
				else if ( key.equals("isSelectM") ) {
					isSelectM = val;
				}
				else {
					setAryParams( key, val, molData, mz );
				}
			}
		}

		// Molfile読込み時に変数を更新する
		if ( isRead && NumberUtils.isCreatable(updateIndex) ) {
			molData[Integer.parseInt(updateIndex)] = "@data=" + readMolData;
		}

		HashMap<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put( "tol", tol );
		mapParam.put( "pi_check", piCheck );
		mapParam.put( "inst_grp", instGrpList );
		mapParam.put( "inst", instTypeList );
		mapParam.put( "ms", msTypeList );
		mapParam.put( "ion", ionMode );
		mapParam.put( "molData", molData );
		mapParam.put( "mz", mz );
		mapParam.put( "isInvalidQuery", new Boolean(isInvalidQuery) );
		mapParam.put( "isFirst", new Boolean(isFirst) );
		mapParam.put( "updateIndex", updateIndex );
		mapParam.put( "reqUrl", req.getRequestURL().toString() );
		mapParam.put( "isSelectM", isSelectM );
		return mapParam;
	}

	/**
	 * HTMLを取得する
	 * @param mapParam
	 * @param isDispM
	 */
	private String getHtml( HashMap<String, Object> mapParam, boolean isDispM ) throws Exception {
		
		String tol = (String)mapParam.get("tol");
		String piCheck = (String)mapParam.get("pi_check");
		List<String> instGrpList = (List<String>)mapParam.get("inst_grp");
		List<String> instTypeList = (List<String>)mapParam.get("inst");
		List<String> msTypeList = (List<String>)mapParam.get("ms");
		String ionMode = (String)mapParam.get("ion");
		String[] molData = (String[])mapParam.get("molData");
		String[] mz = (String[])mapParam.get("mz");
		String updateIndex = (String)mapParam.get("updateIndex");
		boolean isInvalidQuery = ((Boolean)mapParam.get("isInvalidQuery")).booleanValue() ;
		boolean isFirst = ((Boolean)mapParam.get("isFirst")).booleanValue();
		String reqUrl = (String)mapParam.get("reqUrl");
		String strIsSelectM = (String)mapParam.get("isSelectM");
		boolean isSelectM = (new Boolean(strIsSelectM)).booleanValue();
		
		StringBuffer html = new StringBuffer();
		if ( isDispM ) {
			html.append("<div id=\"tbl_queryM\">\n");
			html.append("\t\t\t<form name=\"form_queryM\" method=\"post\" "
				+ "action=\"./jsp/Result.jsp\" style=\"display:inline\" onSubmit=\"doWait('Searching...')\">\n");
		}
		else {
			html.append("<div id=\"tbl_queryK\">\n");
			html.append("\t\t<form name=\"form_queryK\" method=\"post\" "
				+ "action=\"./extend/KnapsackResult.jsp\" style=\"display:inline\" onSubmit=\"doWait('Searching...')\">\n");
		}
		html.append("\t\t\t\t<input type=\"hidden\" name=\"type\" value=\"struct\">\n");
		html.append("\t\t\t\t<table>\n");
		html.append("\t\t\t\t\t<tr>\n");
		html.append("\t\t\t\t\t\t<td>\n");
		if ( isDispM ) {
			html.append("\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"form-box-M\">\n");
		}
		else {
			html.append("\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"form-box-K\">\n");
		}
		html.append("\t\t\t\t\t\t\t\t<tr>\n");
		html.append("\t\t\t\t\t\t\t\t\t<td bgcolor=\"dimgray\"><font color=\"white\"><b>&nbsp;Substructure</b></font></td>\n");
		html.append("\t\t\t\t\t\t\t\t</tr>\n");
		html.append("\t\t\t\t\t\t\t\t<tr>\n");
		html.append("\t\t\t\t\t\t\t\t\t<td>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t<table border=\"0\" cellspacing=\"0\" cellpadding=\"20\">\n");

		String appletName = "";
		if ( isDispM ) {
			appletName = "jme_queryM";
		}
		else {
			appletName = "jme_queryK";
		}
		for ( int i = 0; i < molData.length; i++ ) {
			if ( i % 2 == 0 ) {
				html.append("\t\t\t\t\t\t\t\t\t\t\t\t<tr>\n" );
			}
			String number = String.valueOf(i);
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td>\n" );
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<b>Query" + String.valueOf(i+1)  + "</b><br>\n");
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<applet name=\"" +  appletName + "\" code=\"JME.class\" archive=\"./applet/JME.jar\" width=\"200\" height=\"145\">\n");
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<param name=\"options\" value=\"depict,border\">\n");
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</applet><br>\n");
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"button\" value=\"Edit\" onClick=\"toEditor(" + isDispM + ", " + number + ");\">\n");
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"button\" value=\"Molfile\" onClick=\"readMolfile(" + isDispM + ", " + number + ");\">\n");
			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"button\" value=\"Clear\" onClick=\"clearQuery(" + isDispM + ", " + number + ");\"><br>\n");
			if ( isDispM == isSelectM ) {
				html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"moldata" + number + "\" value=\"" + molData[i] + "\">\n");
			}
			else {
				html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"moldata" + number + "\" value=\"\">\n");
			}
			if ( isInvalidQuery && Integer.parseInt(updateIndex) == i ) {
				html.append("<font color=\"red\"><b>>> invalid data</b></font>\n" );
			}
			else {
				html.append( "<br>\n" );
			}

			html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>\n" );

			if ( i % 2 == 0 ) {
				html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td width=\"24\" style=\"padding:0px;\" align=\"center\"><span class=\"logic\">AND</span></td>\n" );
			}

			if ( i % 2 != 0 ) {
				html.append("\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>\n" );
			}
		}

		html.append("\t\t\t\t\t\t\t\t\t\t\t\t</table>\n" );
		html.append("\t\t\t\t\t\t\t\t\t\t\t</td>\n" );
		html.append("\t\t\t\t\t\t\t\t\t\t</tr>\n" );
		html.append("\t\t\t\t\t\t\t\t\t\t<tr>\n" );
		html.append("\t\t\t\t\t\t\t\t\t\t\t<td style=\"padding:0px 20px 0px 20px;\"><b>Comparison of pi-electron for each atom</b><br><div align=\"right\">\n" );
		
		String[] piCheckName = { "number in query = number in target", "number in query <= number in target", "none" };
		String[] piChekNum = { "2", "1", "0" };
		html.append( "\t\t\t\t\t\t\t\t\t\t\t\t<select name=\"pi_check\">" );
		for ( int i = 0; i < piCheckName.length; i++ ) {
			html.append( "\t\t\t\t\t\t\t\t\t\t\t\t\t<option value=\"" + piChekNum[i] + "\"" );
			if ( piCheck.equals(piChekNum[i]) ) {
				html.append(" selected");
			}
			html.append( ">" + piCheckName[i] + "\n" );
		}
		html.append("\t\t\t\t\t\t\t\t\t\t\t\t</select><br>\n");
		html.append("<b><span class=\"fontNote\">* Double and triple bound is translated to pi-electrons of the bonded atoms.</span></b></div><br>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t\t</td>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t</tr>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t<tr>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t\t<td align=\"right\">");
		html.append("\t\t\t\t\t\t\t\t\t\t\t\t<span style=\"color:#666; font-family:Verdana, Arial, Trebuchet MS; font-size:10px; font-style:italic; margin:0;\">Copyright &copy; 2008 K. Tanaka and S. Kanaya, NAIST, Japan&nbsp;&nbsp;</span>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t\t</td>\n");
		html.append("\t\t\t\t\t\t\t\t\t\t<tr>\n");
		html.append("\t\t\t\t\t\t\t\t\t</table>\n");
		html.append("\t\t\t\t\t\t\t\t</td>\n");

		String instGrp = "";
		for ( int i=0; i<instGrpList.size(); i++ ) {
			instGrp += instGrpList.get(i);
			if ( i < instGrpList.size() - 1 ) {
				instGrp += ",";
			}
		}
		String instType = "";
		for ( int i=0; i<instTypeList.size(); i++ ) {
			instType += instTypeList.get(i);
			if ( i < instTypeList.size() - 1 ) {
				instType += ",";
			}
		}
		String msType = "";
		for ( int i=0; i<msTypeList.size(); i++ ) {
			msType += msTypeList.get(i);
			if ( i < msTypeList.size() - 1 ) {
				msType += ",";
			}
		}
		
		if ( isDispM ) {
			html.append("\t\t\t\t\t\t\t\t<td valign=\"top\" style=\"padding:2px 15px;\">\n");
			String strUrl = reqUrl.substring( 0, reqUrl.lastIndexOf("/")+1 ) + "Instrument.jsp";
			String param = "ion=" + ionMode + "&first=" + isFirst + "&inst_grp=" + instGrp + "&inst=" + instType + "&ms=" + msType;
			URL url = new URL( strUrl );
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			
			// クッキー情報を渡す
			Cookie[] allCookies = req.getCookies();
			if ( allCookies != null ) {
				for ( int i = 0; i < allCookies.length; i++ ) {
					if ( allCookies[i].getName().equals("Common") ) {
						con.setRequestProperty("Cookie", "Common=" + allCookies[i].getValue() );
						break;
					}
				}
			}
			PrintStream psm = new PrintStream( con.getOutputStream() );
			psm.print( param );
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( (line = in.readLine()) != null ) {
				html.append( line + "\n" );
			}
			in.close();
		}
		html.append("\t\t\t\t\t\t\t\t</td>\n");
		html.append("\t\t\t\t\t\t\t</tr>\n");
		html.append("\t\t\t\t\t\t</table>\n");
		
		if ( isDispM ) {
			html.append("\t\t\t\t\t\t<table>\n");
			html.append("\t\t\t\t\t\t\t<tr>\n");
			html.append("\t\t\t\t\t\t\t\t<td colspan=\"2\" style=\"padding-left:5px;\">\n");
			html.append("\t\t\t\t\t\t\t\t<b>Peak Search&nbsp;&nbsp;(Option)</b>\n");
			html.append("\t\t\t\t\t\t\t\t</td>\n");
			html.append("\t\t\t\t\t\t\t</tr>\n");
			html.append("\t\t\t\t\t\t\t<tr>\n");
			html.append("\t\t\t\t\t\t\t\t<td width=\"150\" style=\"padding-left:15px;\">\n");
			html.append("\t\t\t\t\t\t\t\t\t<i>m/z</i>\n");
			html.append("\t\t\t\t\t\t\t\t</td>\n");
			html.append("\t\t\t\t\t\t\t\t<td>\n");
			
			for ( int i = 0; i < mz.length; i++ ) {
				String num = String.valueOf(i);
				html.append( "\t\t\t\t\t\t\t\t\t<input name=\"mz" + num + "\" type=\"text\" size=\"10\" value=\"" + mz[i] + "\">");
				if ( i < mz.length - 1 ) {
					html.append("&nbsp;,&nbsp;");
				}
				html.append("\n");
			}
			
			html.append("\t\t\t\t\t\t\t\t</td>\n");
			html.append("\t\t\t\t\t\t\t</tr>\n");
			html.append("\t\t\t\t\t\t\t<tr>\n");
			html.append("\t\t\t\t\t\t\t\t<td style=\"padding-left:15px;\">Tolerance of <i>m/z</i></td>\n");
			html.append("\t\t\t\t\t\t\t\t<td>\n");
			html.append("\t\t\t\t\t\t\t\t\t<input name=\"tol\" type=\"text\" size=\"10\" value=\"" + tol + "\">\n");
			html.append("\t\t\t\t\t\t\t\t</td>\n");
			html.append("\t\t\t\t\t\t\t</tr>\n");
			html.append("\t\t\t\t\t\t</table><br>\n");
		}
		else {
			html.append("\t\t\t\t\t\t<br>\n");
		}
		html.append("\t\t\t\t\t\t<input type=\"submit\" value=\"Search\" onclick=\"return checkSubmitForStruct();\" class=\"search\">\n");

		if ( isDispM ) {
			html.append("\t\t\t\t\t\t<input type=\"hidden\" name=\"isSelectM\" value=\"true\">\n");
		}
		else {
			html.append("\t\t\t\t\t\t<input type=\"hidden\" name=\"isSelectM\" value=\"false\">\n");
		}
		html.append("\t\t\t\t\t</form>\n");
		html.append("\t\t\t\t</div>\n");
		return html.toString();
	}
%>
<%
	AdminCommon admin = new AdminCommon();
	boolean isKnapsack = admin.isKnapsack();
	
	this.req = request;
	
	// セッション情報をクリアする
	session.setAttribute("RESULT", null);
	
	// リクエストパラメータを取得する
	HashMap<String, Object> mapParam = getParams();
	
	String isSelectM = "true";
	if ( mapParam.get("isSelectM") != null ) {
		isSelectM = (String)mapParam.get("isSelectM");
	}
	String tabIndex = "0";
	if ( isSelectM.equals("false") ) {
		tabIndex = "1";
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta name="author" content="MassBank" />
<meta name="coverage" content="worldwide" />
<meta name="Targeted Geographic Area" content="worldwide" />
<meta name="rating" content="general" />
<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
<meta name="description" content="Search chemical compounds by substructures. Retrieves chemical compounds whose chemical structure contains the substructures specified by users and display their spectra.">
<meta name="keywords" content="Structure,compound,chemical">
<meta name="revisit_after" content="30 days">
<link rel="stylesheet" type="text/css" href="./css/Common.css">
<%if ( isKnapsack ) {%>
<link rel="stylesheet" type="text/css" href="./Knapsack/css/jquery-ui-1.7.2.custom.css">
<%}%>
<title>MassBank | Database | Substructure Search</title>
<script type="text/javascript" src="./script/Common.js"></script>
<script type="text/javascript" src="./script/StructSearch.js"></script>
<script type="text/javascript" src="./script/jquery.js"></script>
<%if ( isKnapsack ) {%>
<script type="text/javascript" src="./Knapsack/script/jquery-ui-1.7.2.custom.min.js"></script>
<script type="text/javascript">
$(function(){
	var $tabs = $('#struct_search').tabs();
	$tabs.tabs('select', <%= tabIndex %>);
});
</script>
<%}%>
<style type="text/css">
<!--
table.form-box-M {
	background-color:WhiteSmoke;
	border:1px silver solid;
}
table.form-box-K {
	background-color:#EEEDDE;
	border:1px #CCC99A solid;
}
-->
</style>
</head>
<body class="msbkFont" onload="changeTab(<%= isSelectM %>);">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td>
			<h1>Substructure Search</h1>
		</td>
		<td align="right" class="font12px">
			<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%><%=STRUCTURE_PAGE%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		</td>
	</tr>
</table>
<iframe src="./menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<hr size="1">
<%if ( isKnapsack ) {%>
<div id="struct_search">
	<ul>
		<li><a href="#massbank" onmouseout="changeTab(true);">Search MassBank</a></li>
		<li><a href="#knapsack" onmouseout="changeTab(false);">Search KNApSAcK</a></li>
	</ul>

	<div id="massbank">
		<jsp:include page="../pserver/ServerInfo.jsp" />
		<%= getHtml(mapParam, true) %>
	</div>
	<div id="knapsack">
		<%= getHtml(mapParam, false) %>
	</div>
</div>
<%} else { %>
		<jsp:include page="../pserver/ServerInfo.jsp" />
		<%= getHtml(mapParam, true) %>
<%}%>
<br>
<hr size="1">
<iframe src="./copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
