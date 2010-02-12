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
 * データベース管理画面
 *
 * ver 1.0.0 2010.02.12
 *
 ******************************************************************************/
%>

<%@ page import="java.util.Date" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.InetAddress" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.UnknownHostException" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="massbank.FileUpload" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.Sanitizer" %>
<%@ page import="massbank.admin.AdminCommon" %>
<%@ page import="massbank.admin.FileUtil" %>
<%@ page import="massbank.admin.OperationManager" %>
<%@ page import="massbank.admin.UpdateConfig" %>
<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%!
	/** 改行文字列 */
	private final String NEW_LINE = System.getProperty("line.separator");
	
	/** ステータス（OK） */
	private final String STATUS_OK = "<span class=\"msgFont\">ok</span>";
	
	/** ステータス（警告） */
	private final String STATUS_WARN = "<span class=\"warnFont\">warn</span>";
	
	/** ステータス（エラー） */
	private final String STATUS_ERR = "<span class=\"errFont\">error</span>";
	
	/** URL種別（local） */
	private final String URL_TYPE_LOCAL = "local";
	
	/** URL種別（external） */
	private final String URL_TYPE_EXTERNAL = "external";
	
	/** 作業ディレクトリ用日時フォーマット */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_SSS");
	
	/**
	 * HTML表示用メッセージテンプレート（情報）
	 * @param msg メッセージ（情報）
	 * @return 表示用メッセージ（情報）
	 */
	private String msgInfo(String msg) {
		StringBuilder sb = new StringBuilder( "<i>info</i> : <span class=\"msgFont\">" );
		sb.append( msg );
		sb.append( "</span><br>" );
		return sb.toString();
	}
	
	/**
	 * HTML表示用メッセージテンプレート（警告）
	 * @param msg メッセージ（警告）
	 * @return 表示用メッセージ（警告）
	 */
	private String msgWarn(String msg) {
		StringBuilder sb = new StringBuilder( "<i>warn</i> : <span class=\"warnFont\">" );
		sb.append( msg );
		sb.append( "</span><br>" );
		return sb.toString();
	}
	
	/**
	 * HTML表示用メッセージテンプレート（エラー）
	 * @param msg メッセージ（エラー）
	 * @return 表示用メッセージ（エラー）
	 */
	private String msgErr(String msg) {
		StringBuilder sb = new StringBuilder( "<i>error</i> : <span class=\"errFont\">" );
		sb.append( msg );
		sb.append( "</span><br>" );
		return sb.toString();
	}
	
	/**
	 * 値一括チェック
	 * Database、Name、Long Name、URLを一括でチェックして1つでもエラーがあればfalseを返却する
	 * @param op JspWriter
	 * @param dbName
	 * @param shortName
	 * @param longName
	 * @param url
	 * @throws IOException
	 * @return 結果
	 */
	private boolean checkValue(JspWriter op, String dbName, String shortName, String longName, String url) throws IOException {
		
		boolean ret = true;
		
		final String TYPE_DB = "Database";
		final String TYPE_NAME = "Name";
		final String TYPE_LONGNAME = "Long Name";
		final String TYPE_URL = "URL";
		
		// 一括チェック用リスト作成
		final String[][] list = { {TYPE_DB, dbName},
		                          {TYPE_NAME, shortName},
		                          {TYPE_LONGNAME, longName},
		                          {TYPE_URL, url} };
		
		for (int i=0; i<list.length; i++) {
			String type = list[i][0];
			String str = list[i][1];
			
			// null、空白の場合
			if ( StringUtils.isBlank(str) ) {
				op.println( msgErr( "[" + type + "]&nbsp;&nbsp;is blank." ) );
				ret = false;
			}
			// 不正なURLの場合
			else if ( type.equals(TYPE_URL) ) {
				boolean isUrl = true;
				try {
					new URL(str);
					if ( url.replaceAll("http://", "").replaceAll("https://", "").trim().equals("") ) {
						isUrl = false;
					}
				}
				catch ( MalformedURLException e) {
					isUrl = false;
				}
				if ( !isUrl ) {
					op.println( msgErr( "[" + type + "]&nbsp;&nbsp;not correct." ) );
					ret = false;
				}
			}
			// 半角英数字及び半角記号（アスキー文字）以外の場合
			else if ( !StringUtils.isAsciiPrintable(str) ) {
				op.println( msgErr( "[" + type + "]&nbsp;&nbsp;is not ASCII character." ) );
				ret = false;
			}
			// 使用不可文字が含まれている場合
			else if ( type.equals(TYPE_DB) ) {
				if ( StringUtils.indexOfAny(str, new String[]{"\\", "/", ":", "*", "?", "\"", "<", ">", "|", "."}) != -1 ) {
					op.println( msgErr( "[" + type + "]&nbsp;&nbsp;character that cannot be used ." ) );
					ret = false;
				}
			}
		}
		return ret;
	}
	
	/**
	 * SQL実行ファイル準備
	 * ベースとなるSQLファイルを元に、
	 * 対象データベース名などを適切な値に変更したSQLファイルを準備する
	 * @param baseSql ベースSQLファイル格納パスオブジェクト
	 * @param workSql SQL実行ファイル格納パスオブジェクト
	 * @parma dbName データベース名
	 * @return SQL実行ファイルフルパス
	 */
	private boolean preparationSql(File baseSql, File workSql, String dbName) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
				br = new BufferedReader(new FileReader(baseSql));
				bw = new BufferedWriter(new FileWriter(workSql));
				String line = "";
				while ((line = br.readLine()) != null) {
					if ( line.indexOf("SAMPLE_DB") != -1 ) {
						line = line.replaceAll("SAMPLE_DB", dbName);
					}
					bw.write(line + NEW_LINE);
				}
				bw.flush();
		}
		catch (IOException e) {
			Logger.global.severe( "sql file preparation failed." + NEW_LINE +
			                      "    base sql file : " + baseSql.toString() + NEW_LINE +
			                      "    work sql file : " + workSql.getPath() );
			e.printStackTrace();
			return false;
		}
		finally {
			try { if ( br != null ) { br.close(); } } catch (IOException e) {}
			try { if ( bw != null ) { bw.close(); } } catch (IOException e) {}
		}
		return true;
	}
	
	
	/**
	 * massbank.conf 整形処理
	 * 適切にタブを入れて見やすいように massbank.conf を整形する
	 * @param confPath massbank.conf パス
	 * @param tempPath 作業ディレクトリパス
	 * @return 結果
	 */
	private boolean confFairing(String confPath, String tmpPath) {
		File confFile = new File(confPath);
		File workFile = new File(tmpPath + "massbank.conf.tmp");
		try {
			FileUtils.copyFile(confFile, workFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
				br = new BufferedReader(new FileReader(workFile));
				bw = new BufferedWriter(new FileWriter(confFile));
				String line = "";
				while ((line = br.readLine()) != null) {
					line = line.replaceAll("\t", "").trim();
					
					// 空行削除
					if ( line.equals("") ) {
						continue;
					}
					
					// タブ追加処理
					if ( line.indexOf("<MyServer>") != -1 ||
						 line.indexOf("</MyServer>") != -1 ||
						 line.indexOf("<Related>") != -1 ||
						 line.indexOf("</Related>") != -1 ||
						 line.indexOf("<Timeout>") != -1 ||
						 line.indexOf("<TraceLog>") != -1 ||
						 line.indexOf("<QstarMask>") != -1 ||
						 line.indexOf("<LinkNum>") != -1 ||
						 line.indexOf("<NodeNum>") != -1 ||
						 line.indexOf("<Cookie>") != -1 ||
						 line.indexOf("<PollingInterval>") != -1 ) {
						
						line = "\t" + line;
					}
					else if ( line.indexOf("<Name>") != -1 ||
						 line.indexOf("<LongName>") != -1 ||
						 line.indexOf("<FrontServer") != -1 ||
						 line.indexOf("<MiddleServer") != -1 ||
						 line.indexOf("<DB>") != -1 ||
						 line.indexOf("<BrowseMode>") != -1 ||
						 line.indexOf("<URL>") != -1 ) {
						
						line = "\t\t" + line;
					}
					
					bw.write(line + NEW_LINE);
				}
				bw.flush();
		}
		catch (IOException e) {
			Logger.global.severe( "conf file fairing failed." + NEW_LINE +
			                      "    conf file : " + confFile.toString() );
			e.printStackTrace();
			return false;
		}
		finally {
			try { if ( br != null ) { br.close(); } } catch (IOException e) {}
			try { if ( bw != null ) { bw.close(); } } catch (IOException e) {}
		}
		
		return true;
	}
	
	/**
	 * DB存在確認
	 * @param dbList 削除済みDBリスト
	 * @param urlList 削除済みURLリスト
	 * @param dbName 削除しようとしているサイトNo
	 * @param dbName 削除しようとしているDB名
	 * @param urlStr 削除しようとしているURL文字列
	 * @return 結果（存在：true、存在しない）
	 */
	private boolean isExistDb(String[] dbList, String[] urlList, int siteNo, String dbName, String urlStr) {
		boolean ret = false;
		
		if ( siteNo >= dbList.length || siteNo == -1 || dbName.equals("") || urlStr.equals("") ) {
			ret = false;
		}
		else if ( dbList[siteNo].toUpperCase().equals(dbName.toUpperCase()) &&
		     urlList[siteNo].toLowerCase().equals(urlStr.toLowerCase()) ) {
			
			ret = true;
		}
		
		return ret;
	}
	
	/**
	 * 登録済確認
	 * 以下の条件で確認する
	 *  ・DB名とURLが同じものは登録できない
	 *  ・内部サイト同士でで同じDB名は登録できない
	 * 登録しようとしているDB名が登録されているかを確認する
	 * @param dbList 登録済みDBリスト
	 * @param urlList 登録済みURLリスト
	 * @param dbName 登録しようとしているDB名
	 * @param urlStr 登録しようとしているURL文字列
	 * @param judgeBase ローカル判定文字列（ベースURL）
	 * @param judgeHost ローカル判定文字列（ホスト名）
	 * @param judgeIp ローカル判定文字列（IPアドレス）
	 * @return 結果（登録済：true、未登録：false）
	 */
	private boolean isAlready(String[] dbList, String[] urlList, String dbName, String urlStr, String judgeBase, String judgeHost, String judgeIp) {
		boolean ret = false;
		
		for (int i=0; i<dbList.length; i++) {
			// DB名とURLが同じものが既に登録されている場合
			if ( dbList[i].toUpperCase().equals(dbName.toUpperCase()) &&
			     urlList[i].toLowerCase().equals(urlStr.toLowerCase()) ) {
				ret = true;
				break;
			}
			// DB名が既に登録されている場合
			else if ( dbList[i].toUpperCase().equals(dbName.toUpperCase()) ) {
				String urlType = getUrlType(judgeBase, judgeHost, judgeIp, urlList[i]);
				// 既に登録されているDB名が内部サイトの場合
				if ( !urlType.equals(URL_TYPE_EXTERNAL) ) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	
	/**
	 * サイト種別取得
	 * @param judgeBase ローカル判定文字列（ベースURL）
	 * @param judgeHost ローカル判定文字列（ホスト名）
	 * @param judgeIp ローカル判定文字列（IPアドレス）
	 * @param url URL
	 * @return URL種別
	 */
	private String getUrlType(String judgeBase, String judgeHost, String judgeIp, String url) {
		
		judgeBase = judgeBase.toLowerCase().replaceAll("http://", "").replaceAll("https://", "");
		judgeHost = judgeHost.toLowerCase();
		url = url.toLowerCase();
		
		if ( url.indexOf("localhost") != -1 ||
		     url.indexOf("127.0.0.1") != -1 ||
		     (!judgeBase.equals("") && url.indexOf(judgeBase) != -1) ||
		     (!judgeHost.equals("") && url.indexOf(judgeHost) != -1) ||
		     (!judgeIp.equals("") && url.indexOf(judgeIp) != -1) ) {
			
			return URL_TYPE_LOCAL;
		}
		else {
			return URL_TYPE_EXTERNAL;
		}
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta equiv="expires" content="0">
<link rel="stylesheet" type="text/css" href="css/admin.css">
<script type="text/javascript" src="../script/Common.js"></script>
<title>Admin | Database Manager</title>
<script language="javascript" type="text/javascript">
<!--
var OFF_COLOR = "WhiteSmoke";
var ON_COLOR = "LightSteelBlue";

/**
 * 初期ロード処理
 */
function initLoad() {
	if ( !!document.formEdit ) {	// ※古いブラウザへ対応するため2重否定を使用
		objForm = document.formEdit;
		
		// フォーカス
		objForm.siteNo.focus();
		
		no = objForm.siteNo.value;
		noList = document.formEdit.siteNo.options;
		noLength = noList.length;
		if ( noLength == 1 ) {
			return;
		}
		for (i=1; i<noLength; i++) {
			obj = document.getElementById( String("row" + noList[i].value) );
			if ( no != -1 ) {
				if (noList[i].value == no) {
					obj.style.background = ON_COLOR;
				}
				else {
					obj.style.background = OFF_COLOR;
				}
			}
			else {
				obj.style.background = OFF_COLOR;
			}
		}
		
		if ( no == -1) {
			objForm.siteDb.readOnly = false;
			objForm.siteDb.className = "";
			objForm.btnAdd.disabled = false;
			objForm.btnEdit.disabled = true;
			objForm.btnDelete.disabled = true;
		}
		else if ( no == 0 ) {
			objForm.siteDb.readOnly = true;
			objForm.siteDb.className = "readOnly";
			objForm.btnAdd.disabled = true;
			objForm.btnEdit.disabled = false;
			objForm.btnDelete.disabled = true;
		}
		else {
			objForm.siteDb.readOnly = true;
			objForm.siteDb.className = "readOnly";
			objForm.btnAdd.disabled = true;
			objForm.btnEdit.disabled = false;
			objForm.btnDelete.disabled = false;
		}
	}
	
	// 初期情報保持
	objForm.beforeUrl.value = objForm.siteUrl.value;
}

/**
 * No.選択
 */
function selNo() {
	objForm = document.formEdit;
	
	// 一覧背景色変更
	no = objForm.siteNo.value;
	noList = document.formEdit.siteNo.options;
	noLength = noList.length;
	if ( noLength == 1 ) {
		return;
	}
	for (i=1; i<noLength; i++) {
		obj = document.getElementById( String("row" + noList[i].value) );
		if ( no != -1 ) {
			if (noList[i].value == no) {
				obj.style.background = ON_COLOR;
			}
			else {
				obj.style.background = OFF_COLOR;
			}
		}
		else {
			obj.style.background = OFF_COLOR;
		}
	}
	
	// 値設定
	if ( no != -1 ) {
		// Firefox以外の場合
		if (navigator.userAgent.indexOf("Firefox") == -1) {
			objForm.siteDb.value = document.getElementById( String("no" + no + "Db") ).innerText;
			objForm.siteName.value = document.getElementById( String("no" + no + "Name") ).innerText;
			objForm.siteLongName.value = document.getElementById( String("no" + no + "LongName") ).innerText;
			objForm.siteUrl.value = document.getElementById( String("no" + no + "Url") ).innerText;
		}
		else {
			objForm.siteDb.value = document.getElementById( String("no" + no + "Db") ).textContent;
			objForm.siteName.value = document.getElementById( String("no" + no + "Name") ).textContent;
			objForm.siteLongName.value = document.getElementById( String("no" + no + "LongName") ).textContent;
			objForm.siteUrl.value = document.getElementById( String("no" + no + "Url") ).textContent;
		}
		
		// 内部サイトの場合
		if ( document.getElementById( String("no" + no + "Type") ).value == "<%=URL_TYPE_LOCAL%>" ) {
			objForm.siteType[0].disabled = false;
			objForm.siteType[1].disabled = true;
			objForm.siteType[0].checked = true;
			setClass('locLabel', 'readOnly', '');
			setClass('extLabel', '', 'readOnly');
		}
		// 外部サイトの場合
		else {
			objForm.siteType[0].disabled = true;
			objForm.siteType[1].disabled = false;
			objForm.siteType[1].checked = true;
			setClass('locLabel', '', 'readOnly');
			setClass('extLabel', 'readOnly', '');
		}
		if ( no == 0 ) {
			objForm.siteDb.readOnly = true;
			objForm.siteDb.className = "readOnly";
			objForm.btnAdd.disabled = true;
			objForm.btnEdit.disabled = false;
			objForm.btnDelete.disabled = true;
		}
		else {
			objForm.siteDb.readOnly = true;
			objForm.siteDb.className = "readOnly";
			objForm.btnAdd.disabled = true;
			objForm.btnEdit.disabled = false;
			objForm.btnDelete.disabled = false;
		}
	}
	else {
		objForm.siteDb.value = "";
		objForm.siteName.value = "";
		objForm.siteLongName.value = "";
		objForm.siteType[0].checked = true;
		objForm.siteType[0].disabled = false;
		objForm.siteType[1].disabled = false;
		objForm.siteType[0].checked = true;
		setClass('locLabel', 'readOnly', '');
		setClass('extLabel', 'readOnly', '');
		objForm.siteUrl.value = "http://localhost/MassBank/";
		
		objForm.siteDb.readOnly = false;
		objForm.siteDb.className = "";
		objForm.btnAdd.disabled = false;
		objForm.btnEdit.disabled = true;
		objForm.btnDelete.disabled = true;
	}
	
	// 選択時初期情報保持
	objForm.beforeUrl.value = objForm.siteUrl.value;
}

/**
 * URL種別選択
 */
function selType() {
	objForm = document.formEdit;
	no = objForm.siteNo.value;
	if ( no == -1 ) {
		if ( objForm.siteType[1].checked ) {
			objForm.siteUrl.value = "http://";
		}
		else {
			objForm.siteUrl.value = "http://localhost/MassBank/";
		}
	}
}

/**
 * クラス名強制変更
 * @param elementId ID
 * @param className1 初期値のクラス名
 * @param className2 切り替えクラス名
 */
function setClass(elementId, className1, className2) {
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return;
	}
	if (ns4) {											//NS4
		element = document.layers[elementId];
	}
	else {												//OP,IE,NS6
		element = document.getElementById(elementId);
	}
	if ( element.className == className1 ) {
		element.className = className2;
	}
}

/**
 * 追加処理前
 * @param judgeBase
 * @param judgeHost
 * @param judgeIp
 */
function beforeAdd(judgeBase, judgeHost, judgeIp) {
	objForm = document.formEdit;
	judgeBase = judgeBase.toLowerCase().replace("http://", "").replace("https://", "");
	judgeHost = judgeHost.toLowerCase();
	var urlVal = objForm.siteUrl.value;
	urlVal = urlVal.toLowerCase();
	
	// ローカルサイト入力で外部は指定させない
	if ( objForm.siteType[0].checked ) {
		if ( urlVal.indexOf("localhost") == -1 &&
		     urlVal.indexOf("127.0.0.1") == -1 &&
		     (judgeBase != "" && urlVal.indexOf(judgeBase) == -1) &&
		     (judgeHost != "" && urlVal.indexOf(judgeHost) == -1) &&
		     (judgeIp != "" && urlVal.indexOf(judgeIp) == -1) ) {
			
			alert("URL on the external cannot be specified.");
			return false;
		}
	}
	// 外部サイト入力でローカルは指定させない
	else if ( objForm.siteType[1].checked ) {
		if ( urlVal.indexOf("localhost") != -1 ||
		     urlVal.indexOf("127.0.0.1") != -1 ||
		     (judgeBase != "" && urlVal.indexOf(judgeBase) != -1) ||
		     (judgeHost != "" && urlVal.indexOf(judgeHost) != -1) ||
		     (judgeIp != "" && urlVal.indexOf(judgeIp) != -1) ) {
			
			alert("URL of the local cannot be specified.");
			return false;
		}
	}
	
	objForm.act.value = "add";
	return true;
}

/**
 * 編集前処理
 * @param judgeBase
 * @param judgeHost
 * @param judgeIp
 */
function beforeEdit(judgeBase, judgeHost, judgeIp) {
	objForm = document.formEdit;
	judgeBase = judgeBase.toLowerCase().replace("http://", "").replace("https://", "");
	judgeHost = judgeHost.toLowerCase();
	var urlVal = objForm.siteUrl.value;
	urlVal = urlVal.toLowerCase();
	
	// ローカルサイト入力で外部は指定させない
	if ( objForm.siteType[0].checked ) {
		if ( urlVal.indexOf("localhost") == -1 &&
		     urlVal.indexOf("127.0.0.1") == -1 &&
		     (judgeBase != "" && urlVal.indexOf(judgeBase) == -1) &&
		     (judgeHost != "" && urlVal.indexOf(judgeHost) == -1) &&
		     (judgeIp != "" && urlVal.indexOf(judgeIp) == -1) ) {
			
			alert("URL on the external cannot be specified.");
			return false;
		}
	}
	// 外部サイト入力でローカルは指定させない
	else if ( objForm.siteType[1].checked ) {
		if ( urlVal.indexOf("localhost") != -1 ||
		     urlVal.indexOf("127.0.0.1") != -1 ||
		     (judgeBase != "" && urlVal.indexOf(judgeBase) != -1) ||
		     (judgeHost != "" && urlVal.indexOf(judgeHost) != -1) ||
		     (judgeIp != "" && urlVal.indexOf(judgeIp) != -1) ) {
			
			alert("URL of the local cannot be specified.");
			return false;
		}
	}
	
	objForm.act.value = "edit";
	return true;
}

/**
 * 削除前処理
 * @param isAdmin 管理者権限フラグ
 */
function beforeDel(isAdmin) {
	
	// 削除許可判定
	if ( isAdmin != "true" ) {
		alert("Unauthorized user.\nPlease set \"admin=true\" to admin.conf.");
		return false;
	}
	
	// ユーザへの削除確認
	objForm = document.formEdit;
	objForm.act.value = "del";
	if ( confirm("Are you sure?") ) {
		if ( confirm("Is it true?") ) {
			return true;
		}
	}
	return false;
}
//-->
</script>
</head>
<body onLoad="initLoad();">
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Database Manager</h2>
<%
	//----------------------------------------------------
	// リクエストパラメータ取得
	//----------------------------------------------------
	request.setCharacterEncoding("utf-8");
	String act = "";
	int reqSiteNo = -1;
	String reqSiteDb = "";
	String reqSiteName = "";
	String reqSiteLongName = "";
	String reqSiteType = "";
	String reqSiteUrl = "";
	String reqSiteUrlBefore = "";
	Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( key.equals("act") ) {
			act = request.getParameter(key);
		}
		else if ( key.equals("siteNo") ) {
			reqSiteNo = Integer.parseInt(request.getParameter(key).trim());
		}
		else if ( key.equals("siteDb") ) {
			reqSiteDb = request.getParameter(key);
		}
		else if ( key.equals("siteName") ) {
			reqSiteName = request.getParameter(key).trim();
		}
		else if ( key.equals("siteLongName") ) {
			reqSiteLongName = request.getParameter(key).trim();
		}
		else if ( key.equals("siteType") ) {
			reqSiteType = request.getParameter(key).trim();
		}
		else if ( key.equals("siteUrl") ) {
			reqSiteUrl = request.getParameter(key).trim();
			if (reqSiteUrl.length() > 0) {
				if (reqSiteUrl.lastIndexOf("/") != reqSiteUrl.length()-1) {
					reqSiteUrl += "/";
				}
			}
		}
		else if ( key.equals("beforeUrl") ) {
			reqSiteUrlBefore = request.getParameter(key).trim();
		}
	}
	
	//----------------------------------------------------
	// 各種パラメータを取得
	//----------------------------------------------------
	final String reqUrl = request.getRequestURL().toString();
	final String baseUrl = reqUrl.substring( 0, (reqUrl.indexOf("/mbadmin") + 1 ) );
	final String realPath = application.getRealPath("/");
	AdminCommon admin = new AdminCommon(reqUrl, realPath);
	final String[] dbPathes = new String[]{admin.getDbRootPath(), admin.getMolRootPath(), admin.getProfileRootPath(), admin.getGifRootPath()};
	final String dbHostName = admin.getDbHostName();
	final String massbankConfPath = admin.getMassBankPath() + "massbank.conf";
	GetConfig gtConf = new GetConfig(baseUrl);
	UpdateConfig upConf = new UpdateConfig(baseUrl, massbankConfPath);
	final String outPath = (!admin.getOutPath().equals("")) ? admin.getOutPath() : FileUpload.UPLOAD_PATH;
	final String tmpPath = (new File(outPath + "/" + sdf.format(new Date()))).getPath() + File.separator;
	final String jspRealPath = application.getRealPath(request.getServletPath());
	final String baseSqlPath = (new File(jspRealPath)).getParent() + File.separator + "sql" + File.separator;
	boolean isResult = true;
	OperationManager om = OperationManager.getInstance();
	NumberFormat nf = NumberFormat.getNumberInstance();
	final String os = System.getProperty("os.name");
	boolean isAdmin = admin.isAdmin();
	String hostName = "";
	String ipAddress = "";
	try {
		hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
		ipAddress = InetAddress.getLocalHost().getHostAddress();
	}
	catch (UnknownHostException uhe) {
		Logger.global.severe( "get host name or get ip address failed." );
		uhe.printStackTrace();
		out.println( msgErr( "the host name or ip address cannot be taken.") );
		return;
	}
	
	String massbankPath = (new File(admin.getMassBankPath())).getName();
	if ( !massbankPath.equals("") ) {
		massbankPath += "/";
	}
	
	try {
		//----------------------------------------------------
		// 排他確認
		//----------------------------------------------------
		if ( act.equals("") ) {
			isResult = om.startOparation(om.P_MANAGER, om.TP_VIEW, null);
		}
		else if ( act.equals("add") || act.equals("edit") || act.equals("del") ) {
			isResult = om.startOparation(om.P_MANAGER, om.TP_UPDATE, null);
		}
		if ( !isResult ) {
			out.println( msgWarn( "other users are updating. please access later. ") );
			return;
		}
		
		
		//----------------------------------------------------
		// 初期処理
		//----------------------------------------------------
		// 一時ディレクトリを準備
		(new File(tmpPath)).mkdir();
		if(os.indexOf("Windows") == -1){
			isResult = FileUtil.changeMode("777", tmpPath);
			if ( !isResult ) {
				out.println( msgErr( "[" + tmpPath + "]&nbsp;&nbsp; chmod failed.") );
				return;
			}
		}
		
		// 編集前の情報を保持
		String[] beforeDbList = gtConf.getDbName();
		String[] beforeUrlList = gtConf.getSiteUrl();
		
		
		//----------------------------------------------------
		// 追加処理
		//----------------------------------------------------
		if ( act.equals("add") ) {
			reqSiteNo = beforeDbList.length;
			
			// 入力値チェック
			if ( !checkValue(out, reqSiteDb, reqSiteName, reqSiteLongName, reqSiteUrl) ) {
				out.println( msgErr("additional failure.") );
				isResult = false;
			}
			// URLチェック
			else if ( !getUrlType(baseUrl, hostName, ipAddress, reqSiteUrl).equals(reqSiteType) ) {
				if ( reqSiteType.equals(URL_TYPE_LOCAL) ) {
					out.println( msgErr("url on the external cannot be specified.") );
					isResult = false;
				}
				else if ( reqSiteType.equals(URL_TYPE_EXTERNAL) ) {
					out.println( msgErr("url of the local cannot be specified.") );
					isResult = false;
				}
			}
			// 登録済みチェック
			else if ( isAlready(beforeDbList, beforeUrlList, reqSiteDb, reqSiteUrl, baseUrl, hostName, ipAddress) ) {
				isResult = false;
				out.println( msgErr("database already exists.") );
			}
			// 追加
			else {
				// 内部サイトのデータベースの追加処理を行う
				if ( !getUrlType(baseUrl, hostName, ipAddress, reqSiteUrl).equals(URL_TYPE_EXTERNAL) ) {
				
					// SQLファイル準備
					File baseSql = new File(baseSqlPath + "create.sql" );
					File workSql = new File(tmpPath + "create.sql" );
					isResult = preparationSql( baseSql, workSql, reqSiteDb );
					if ( !isResult ) {
						Logger.global.severe( "sql file create failed." + NEW_LINE +
						                      "    file : " + workSql.getPath() );
						out.println( msgErr("add database failed.") );
						return;
					}
					
					// SQLファイル実行
					isResult = FileUtil.execSqlFile(dbHostName, "", workSql.getPath());
					if ( !isResult ) {
						Logger.global.severe( "sql file execute failed." + NEW_LINE +
						                      "    file : " + workSql.getPath() );
						out.println( msgErr("add database failed.") );
						return;
					}
					
					// フォルダ追加処理
					for (int i=0; i<dbPathes.length; i++) {
						File workDir = new File(dbPathes[i] + reqSiteDb);
						if ( !workDir.isDirectory() ) {
							isResult = workDir.mkdir();
							if ( !isResult ) {
								Logger.global.severe( "add derectory failed." + NEW_LINE +
								                      "    file : " + workDir.getPath() );
							}
							if(os.indexOf("Windows") == -1){
								isResult = FileUtil.changeMode("777", workDir.getPath());
								if ( !isResult ) {
									Logger.global.severe( "chmod derectory failed." + NEW_LINE +
									                      "    file : " + workDir.getPath() );
								}
							}
						}
					}
				}
				
				// massbank.conf 追加処理
				isResult = upConf.addConfig(reqSiteNo, reqSiteName, reqSiteLongName, reqSiteUrl, reqSiteDb);
				if ( !isResult ) {
					Logger.global.severe( "edit massbank.conf failed." );
					out.println( msgErr("edit of massbank.conf failed.") );
					return;
				}
				
				// massbank.conf 整形処理
				confFairing(massbankConfPath, tmpPath);
				
				// massbank.conf 再読み込み
				gtConf = new GetConfig(baseUrl);
				
				if ( isResult ) {
					out.println( msgInfo("additional success.") );
				}
			}
		}
		//----------------------------------------------------
		// 編集処理
		//----------------------------------------------------
		else if ( act.equals("edit") ) {
			// 入力値チェック
			if ( !checkValue(out, reqSiteDb, reqSiteName, reqSiteLongName, reqSiteUrl) ) {
				out.println( msgErr("edit failure.") );
				isResult = false;
			}
			// URLチェック
			else if ( !getUrlType(baseUrl, hostName, ipAddress, reqSiteUrl).equals(reqSiteType) ) {
				if ( reqSiteType.equals(URL_TYPE_LOCAL) ) {
					out.println( msgErr("url on the external cannot be specified.") );
					isResult = false;
				}
				else if ( reqSiteType.equals(URL_TYPE_EXTERNAL) ) {
					out.println( msgErr("url of the local cannot be specified.") );
					isResult = false;
				}
			}
			// 編集
			else {
				// massbank.conf 編集処理
				isResult = upConf.editConfig(reqSiteNo, reqSiteName, reqSiteLongName, reqSiteUrl);
				if ( !isResult ) {
					Logger.global.severe( "edit massbank.conf failed." );
					out.println( msgErr("edit of massbank.conf failed.") );
					return;
				}
				
				// massbank.conf 整形処理
				confFairing(massbankConfPath, tmpPath);
				
				// massbank.conf 再読み込み
				gtConf = new GetConfig(baseUrl);
				
				if ( isResult ) {
					out.println( msgInfo("edit success.") );
				}
			}
		}
		//----------------------------------------------------
		// 削除処理
		//----------------------------------------------------
		else if ( act.equals("del") ) {
			// 削除権限チェック
			if ( !isAdmin ) {
				out.println( msgWarn("unauthorized user.") );
				isResult = false;
			}
			// 存在しないDBの場合
			else if ( !isExistDb(beforeDbList, beforeUrlList, reqSiteNo, reqSiteDb, reqSiteUrlBefore) ) {
				reqSiteNo = -1;
				isResult = true;
			}
			// 削除
			else {
				// massbank.conf 削除処理
				isResult = upConf.delConfig(reqSiteNo);
				if ( !isResult ) {
					Logger.global.severe( "edit massbank.conf failed." );
					out.println( msgErr("edit of massbank.conf failed.") );
				}
				else {
					reqSiteNo = -1;
				}
				
				// massbank.conf 整形処理
				confFairing(massbankConfPath, tmpPath);
				
				// massbank.conf 再読み込み
				if ( isResult ) {
					gtConf = new GetConfig(baseUrl);
				}
				
				// 内部サイトのデータベースの削除処理を行う
				if ( !getUrlType(baseUrl, hostName, ipAddress, reqSiteUrlBefore).equals(URL_TYPE_EXTERNAL) ) {
					
					// フォルダ削除処理
					for (int i=0; i<dbPathes.length; i++) {
						File workDir = new File(dbPathes[i] + reqSiteDb);
						if ( workDir.isDirectory() ) {
							try {
								FileUtils.deleteDirectory(workDir);
							}
							catch ( IOException e) {
								e.printStackTrace();
								isResult = false;
								Logger.global.severe( "delete derectory failed." + NEW_LINE +
								                      "    file : " + workDir.getPath() );
							}
						}
					}
					
					// SQLファイル準備
					File baseSql = new File(baseSqlPath + "drop.sql" );
					File workSql = new File(tmpPath + "drop.sql" );
					isResult = preparationSql( baseSql, workSql, reqSiteDb );
					if ( !isResult ) {
						Logger.global.severe( "sql file create failed." + NEW_LINE +
						                      "    file : " + workSql.getPath() );
						out.println( msgErr("drop database failed.") );
						return;
					}
					
					// SQLファイル実行
					isResult = FileUtil.execSqlFile(dbHostName, "", workSql.getPath());
					if ( !isResult ) {
						Logger.global.severe( "sql file execute failed." + NEW_LINE +
						                      "    file : " + workSql.getPath() );
						out.println( msgErr("drop database failed.") );
						return;
					}
				}
				
				if ( isResult ) {
					out.println( msgInfo("deletion success.") );
				}
			}
		}
		
		//----------------------------------------------------
		// 登録済み情報取得
		//----------------------------------------------------
		String[] siteDbList = gtConf.getDbName();
		String[] siteNameList = gtConf.getSiteName();
		String[] siteLongNameList = gtConf.getSiteLongName();
		String[] siteUrlList = gtConf.getSiteUrl();
		siteUrlList[0] = gtConf.getServerUrl();
		
		//----------------------------------------------------
		// 編集領域表示
		//----------------------------------------------------
		String selDb = "";
		String selName = "";
		String selLongName = "";
		String selUrlType = "";
		String selUrl = "";
		String isUrlLocChecked = "";
		String isUrlExtChecked = "";
		String isUrlLocDisabled = "";
		String isUrlExtDisabled = "";
		String locLabelClass = "";
		String extLabelClass = "";
		if ( !isResult ) {
			// エラーの場合は値を引き継ぐ
			selDb = reqSiteDb;
			selName = reqSiteName;
			selLongName = reqSiteLongName;
			selUrlType = reqSiteType;
			selUrl = reqSiteUrl;
			if ( !selUrlType.equals(URL_TYPE_EXTERNAL) ) {
				isUrlLocChecked = " checked";
				if ( act.equals("edit") || act.equals("del") ) {
					isUrlExtDisabled = " disabled";
					extLabelClass = "readOnly";
				}
			}
			else {
				isUrlExtChecked = " checked";
				if ( act.equals("edit") || act.equals("del") ) {
					isUrlLocDisabled = " disabled";
					locLabelClass = "readOnly";
				}
			}
		}
		else if ( act.equals("add") || act.equals("edit") ) {
			selDb = siteDbList[reqSiteNo];
			selName = siteNameList[reqSiteNo];
			selLongName = siteLongNameList[reqSiteNo];
			selUrlType = reqSiteType;
			selUrl = siteUrlList[reqSiteNo];
			if ( !selUrlType.equals(URL_TYPE_EXTERNAL) ) {
				isUrlLocChecked = " checked";
				isUrlExtDisabled = " disabled";
				extLabelClass = "readOnly";
			}
			else {
				isUrlExtChecked = " checked";
				isUrlLocDisabled = " disabled";
				locLabelClass = "readOnly";
			}
		}
		else {
			selDb = "";
			selName = "";
			selLongName = "";
			selUrlType = reqSiteType;
			isUrlLocChecked = " checked";
			if ( reqSiteNo == -1 || !selUrlType.equals(URL_TYPE_EXTERNAL) ) {
				selUrl = "http://localhost/MassBank/";
			}
			else {
				selUrl = "http://";
			}
		}
		out.println( "<form name=\"formEdit\" method=\"post\" action=\"" + reqUrl + "\" onSubmit=\"doWait();\">" );
		out.println( "<div style=\"width:980px; border: 2px Gray solid; padding:15px; background-color:WhiteSmoke;\">" );
		out.println( "<table width=\"97%\" align=\"center\" cellspacing=\"2\" cellpadding=\"2\">" );
		out.println( "<tr>" );
		out.println( "<td width=\"50\" title=\"Site No.\"><b>No.</b></td>" );
		out.println( "<td width=\"15\"></td>" ) ;
		out.println( "<td width=\"180\" title=\"Database Name\"><b>Database</b></td>" );
		out.println( "<td title=\"Site Name\"><b>Name</b></td>" );
		out.println( "<td title=\"Site Name (Long)\"><b>Long Name</b></td>" );
		out.println( "</tr>" );
		out.println( "<tr height=\"10\">" );
		out.println( "<td>" );
		out.println( "<select name=\"siteNo\" style=\"width:100%;\" onChange=\"selNo();\">" );
		if ( reqSiteNo == -1 ) {
			out.println( "<option value=\"-1\" selected>+</option>" );
		}
		else {
			out.println( "<option value=\"-1\">+</option>" );
		}
		for (int i=0; i<siteDbList.length; i++) {
			if ( reqSiteNo == i ) {
				out.println( "<option value=\"" + i + "\" selected>" + i + "</option>" );
			}
			else {
				out.println( "<option value=\"" + i + "\">" + i + "</option>" );
			}
		}
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td></td>" ) ;
		out.println( "<td><input type=\"text\" style=\"width:98%;\" name=\"siteDb\" value=\"" + selDb + "\"></td>" );
		out.println( "<td><input type=\"text\" style=\"width:98%;\" name=\"siteName\" value=\"" + selName + "\"></td>" );
		out.println( "<td><input type=\"text\" style=\"width:98%;\" name=\"siteLongName\" value=\"" + selLongName + "\"></td>" );
		out.println( "</tr>" );
		
		out.println( "<tr>" );
		out.println( "<td colspan=\"2\"></td>" );
		out.println( "<td title=\"Site URL Type\"><b>URL Type</b></td>" );
		out.println( "<td title=\"URL\"><b>URL</b></td>" );
		out.println( "</tr>" );
		
		out.println( "<tr>" );
		out.println( "<td colspan=\"2\"></td>" );
		out.println( "<td>" );
		out.println( "<input type=\"radio\" name=\"siteType\" value=\"" + URL_TYPE_LOCAL + "\" onClick=\"selType();\"" + isUrlLocChecked + isUrlLocDisabled + "> <span id=\"locLabel\" class=\"" + locLabelClass + "\">local</span>&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "<input type=\"radio\" name=\"siteType\" value=\"" + URL_TYPE_EXTERNAL + "\" onClick=\"selType();\"" + isUrlExtChecked + isUrlExtDisabled + "> <span id=\"extLabel\" class=\"" + extLabelClass + "\">external</span>" );
		out.println( "</td>" );
		out.println( "<td colspan=\"2\"><input type=\"text\" style=\"width:100%;\" name=\"siteUrl\" value=\"" + selUrl + "\"></td>" );
		out.println( "</tr>");
		
		out.println( "<tr>");
		out.println( "<td colspan=\"5\" align=\"right\" height=\"40px\">" );
		out.println( "<input type=\"submit\" name=\"btnAdd\" style=\"width:100px;\" value=\"Add\" onClick=\"return beforeAdd('" + baseUrl + "', '" + hostName + "', '" + ipAddress + "');\">&nbsp;&nbsp;" );
		out.println( "<input type=\"submit\" name=\"btnEdit\" style=\"width:100px;\" value=\"Edit\" onClick=\"return beforeEdit('" + baseUrl + "', '" + hostName + "', '" + ipAddress + "');\" disabled>&nbsp;&nbsp;" );
		out.println( "<input type=\"submit\" name=\"btnDelete\" style=\"width:100px;\" value=\"Delete\" onClick=\"return beforeDel('" + isAdmin + "');\" disabled>" );
		out.println( "</td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "</div>" );
		out.println( "<input type=\"hidden\" name=\"act\" value=\"\">" );
		out.println( "<input type=\"hidden\" name=\"beforeUrl\" value=\"\">" );
		out.println( "</form>" );
		
		//----------------------------------------------------
		// 一覧表示処理
		//----------------------------------------------------
		int externalNum = 0;
		for (String url: siteUrlList) {
			if ( getUrlType(baseUrl, hostName, ipAddress, url).equals(URL_TYPE_EXTERNAL) ) {
				externalNum++;
			}
		}
		out.println( "<br><hr><br>" );
		out.println( "\t<div class=\"count baseFont\">" + nf.format(siteDbList.length) + " database&nbsp;(" + externalNum + " external site)</div>" );
		out.println( "\t<table width=\"980\" cellspacing=\"1\" cellpadding=\"0\" bgcolor=\"Lavender\" class=\"fixed\">" );
		out.println( "\t\t<thead>");
		out.println( "\t\t<tr class=\"rowHeader\">");
		out.println( "\t\t\t<td width=\"30\">No.</td>" );
		out.println( "\t\t\t<td width=\"90\">Database</td>" );
		out.println( "\t\t\t<td width=\"305\">URL</td>" );
		out.println( "\t\t\t<td width=\"110\">Name</td>" );
		out.println( "\t\t\t<td width=\"160\">Long Name</td>" );
		out.println( "\t\t\t<td width=\"64\">Status</td>" );
		out.println( "\t\t\t<td width=\"215\">Details</td>" );
		out.println( "\t\t</tr>");
		out.println( "\t\t</thead>");
		for (int i=0; i<siteDbList.length; i++) {
			
			String status = STATUS_OK;
			StringBuilder details = new StringBuilder();
			
			// massbank.conf 取得チェック
			if ( siteDbList[i].equals("") || siteNameList[i].equals("") || siteLongNameList[i].equals("") || siteUrlList[i].equals("") ) {
				status = STATUS_ERR;
				details.append( "<span class=\"errFont\">massbank.conf is wrong.</span><br />" );
			}
			
			// 内部サイトのデータベースのチェックを行う
			if ( !getUrlType(baseUrl, hostName, ipAddress, siteUrlList[i]).equals(URL_TYPE_EXTERNAL) ) {
				
				// DB 存在チェック
				File baseSql = new File(baseSqlPath + "use.sql" );
				File workSql = new File(tmpPath + "use.sql" );
				isResult = preparationSql( baseSql, workSql, siteDbList[i] );
				if ( !isResult ) {
					Logger.global.severe( "sql file create failed." + NEW_LINE +
					                      "    file : " + workSql.getPath() );
					out.println( msgErr("use database failed.") );
					return;
				}
				isResult = FileUtil.execSqlFile(dbHostName, "", workSql.getPath());
				if ( !isResult ) {
					status = STATUS_ERR;
					details.append( "<span class=\"errFont\">database unregistered.</span><br />" );
				}
				
				// フォルダ存在確認
				for (int j=0; j<dbPathes.length; j++) {
					File workDir = new File(dbPathes[j] + siteDbList[i]);
					if ( !workDir.isDirectory() ) {
						String parent = workDir.getParentFile().getName();
						if ( parent.equals("annotation") || parent.equals("molfile") ) {
							status = STATUS_ERR;
							details.append( "<span class=\"errFont\">directory not exist. [" + parent + "]</span><br />" );
						}
						else {
							if ( !status.equals(STATUS_ERR) ) { status = STATUS_WARN; }
							details.append( "<span class=\"warnFont\">directory not exist. [" + parent + "]</span><br />" );
						}
					}
				}
			}
			else {
				details.append( "<span class=\"msgFont\">external site.</span><br />" );
			}
			
			out.println( "\t\t<tr class=\"rowEnable\" id=\"row" + i + "\" height=\"50\">" );
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "No\">" + i + "</td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "Db\">" + Sanitizer.html(siteDbList[i]) + "</td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "Url\"><a href=\"" + siteUrlList[i] + "\" target=\"_blank\" class=\"urlFont\">" + Sanitizer.html(siteUrlList[i]) + "</a></td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "Name\">" + Sanitizer.html(siteNameList[i]) + "</td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "LongName\">" + Sanitizer.html(siteLongNameList[i]) + "</td>");
			out.println( "\t\t\t<td class=\"center\" id=\"no" + i + "Status\">" + status + "</td>" );
			out.println( "\t\t\t<td class=\"details\">" + details.toString() + "<input type=\"hidden\" id=\"no" + i + "Type\" value=\"" + getUrlType(baseUrl, hostName, ipAddress, siteUrlList[i]) + "\"></td>" );
			out.println( "\t\t</tr>");
		}
		out.println( "</table>" );
	}
	finally {
		File tmpDir = new File(tmpPath);
		if (tmpDir.exists()) {
			FileUtil.removeDir( tmpDir.getPath() );
		}
		if ( act.equals("add") || act.equals("edit") || act.equals("del") ) {
			om.endOparation(om.P_MANAGER, om.TP_UPDATE, null);
		}
		out.println( "</body>" );
		out.println( "</html>" );
	}
%>
