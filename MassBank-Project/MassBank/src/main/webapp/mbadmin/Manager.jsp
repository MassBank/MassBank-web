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
 * ver 1.0.15 2012.02.21
 *
 ******************************************************************************/
%>

<%@ page import="java.util.ArrayList" %>
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
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.Sanitizer" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.admin.AdminCommon" %>
<%@ page import="massbank.admin.DatabaseAccess" %>
<%@ page import="massbank.admin.FileUtil" %>
<%@ page import="massbank.admin.OperationManager" %>
<%@ page import="massbank.admin.UpdateConfig" %>
<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>

<%!
	/** 改行文字列 */
	private final String NEW_LINE = System.getProperty("line.separator");
	
	/** ステータス（OK） */
	private final String STATUS_OK = "<span class=\"msgFont\">ok</span>";
	
	/** ステータス（警告） */
	private final String STATUS_WARN = "<span class=\"warnFont\">warn</span>";
	
	/** ステータス（エラー） */
	private final String STATUS_ERR = "<span class=\"errFont\">error</span>";
	
	/** URL種別（internal） */
	private final String URL_TYPE_INTERNAL = "internal";
	
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
	 * @param shortLabel
	 * @param longLabel
	 * @param url
	 * @throws IOException
	 * @return 結果
	 */
	private boolean checkValue(JspWriter op, String dbName, String shortLabel, String longLabel, String url) throws IOException {
		
		boolean ret = true;
		
		final String TYPE_DB = "DB Name";
		final String TYPE_SHORTLABEL = "Short Label";
		final String TYPE_LONGLABEL = "Long Label";
		final String TYPE_URL = "URL";
		
		// 一括チェック用リスト作成
		final String[][] list = { {TYPE_DB, dbName},
		                          {TYPE_SHORTLABEL, shortLabel},
		                          {TYPE_LONGLABEL, longLabel},
		                          {TYPE_URL, url} };
		
		for (int i=0; i<list.length; i++) {
			String type = list[i][0];
			String str = list[i][1];
			
			// null、トリム後空白の場合
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
					op.println( msgErr( "[" + type + "]&nbsp;&nbsp;character that cannot be used." ) );
					ret = false;
				}
				else if ( str.toLowerCase().equals("mysql") || str.toLowerCase().equals("test") ) {
					op.println( msgErr( "[" + type + "]&nbsp;&nbsp; that cannot be used." ) );
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
						line = StringUtils.replace(line, "SAMPLE_DB", dbName);
					}
					bw.write(line + NEW_LINE);
				}
				bw.flush();
		}
		catch (IOException e) {
			Logger.getLogger("global").severe( "sql file preparation failed." + NEW_LINE +
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
			Logger.getLogger("global").severe( "conf file fairing failed." + NEW_LINE +
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
		
		if ( siteNo >= dbList.length || siteNo == -1 || dbName == null || dbName.equals("") || urlStr == null || urlStr.equals("") ) {
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
		String urlHost = url.replaceAll("http://", "").replaceAll("https://", "").replaceAll("/.*", "");
		
		if ( url.indexOf("localhost") != -1 ||
		     url.indexOf("127.0.0.1") != -1 ||
		     (!judgeBase.equals("") && url.indexOf(judgeBase) != -1) ||
		     urlHost.equals(judgeHost) ||
		     urlHost.equals(judgeIp) ) {
			
			return URL_TYPE_INTERNAL;
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
<script type="text/javascript" src="../script/jquery.js"></script>
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
			objForm.siteUrl.readOnly = true;
			objForm.siteUrl.className = "readOnly";
			if (navigator.userAgent.indexOf("Firefox") == -1) {
				objForm.siteUrl.value = document.getElementById( String("no0Url") ).innerText;
			}
			else {
				objForm.siteUrl.value = document.getElementById( String("no0Url") ).textContent;
			}
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
			if ( document.getElementById( String("no" + no + "Type") ).value == "<%=URL_TYPE_INTERNAL%>" ) {
				objForm.siteDb.readOnly = true;
				objForm.siteDb.className = "readOnly";
				objForm.siteUrl.readOnly = true;
				objForm.siteUrl.className = "readOnly";
			}
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
			objForm.siteShortLabel.value = document.getElementById( String("no" + no + "ShortLabel") ).innerText;
			objForm.siteLongLabel.value = document.getElementById( String("no" + no + "LongLabel") ).innerText;
			objForm.siteUrl.value = document.getElementById( String("no" + no + "Url") ).innerText;
		}
		else {
			objForm.siteDb.value = document.getElementById( String("no" + no + "Db") ).textContent;
			objForm.siteShortLabel.value = document.getElementById( String("no" + no + "ShortLabel") ).textContent;
			objForm.siteLongLabel.value = document.getElementById( String("no" + no + "LongLabel") ).textContent;
			objForm.siteUrl.value = document.getElementById( String("no" + no + "Url") ).textContent;
		}
		
		// 内部サイトの場合
		if ( document.getElementById( String("no" + no + "Type") ).value == "<%=URL_TYPE_INTERNAL%>" ) {
			objForm.siteDb.readOnly = true;
			objForm.siteDb.className = "readOnly";
			objForm.siteType[0].disabled = false;
			objForm.siteType[1].disabled = true;
			objForm.siteType[0].checked = true;
			setClass('intLabel', 'readOnly', '');
			setClass('extLabel', '', 'readOnly');
			objForm.siteUrl.readOnly = true;
			objForm.siteUrl.className = "readOnly";
		}
		// 外部サイトの場合
		else {
			objForm.siteDb.readOnly = false;
			objForm.siteDb.className = "";
			objForm.siteType[0].disabled = true;
			objForm.siteType[1].disabled = false;
			objForm.siteType[1].checked = true;
			setClass('intLabel', '', 'readOnly');
			setClass('extLabel', 'readOnly', '');
			objForm.siteUrl.readOnly = false;
			objForm.siteUrl.className = "";
		}
		if ( no == 0 ) {
			objForm.siteUrl.readOnly = false;
			objForm.siteUrl.className = "";
			objForm.btnAdd.disabled = true;
			objForm.btnEdit.disabled = false;
			objForm.btnDelete.disabled = true;
		}
		else {
			objForm.btnAdd.disabled = true;
			objForm.btnEdit.disabled = false;
			objForm.btnDelete.disabled = false;
		}
	}
	else {
		objForm.siteDb.value = "";
		objForm.siteShortLabel.value = "";
		objForm.siteLongLabel.value = "";
		objForm.siteType[0].checked = true;
		objForm.siteType[0].disabled = false;
		objForm.siteType[1].disabled = false;
		objForm.siteType[0].checked = true;
		setClass('intLabel', 'readOnly', '');
		setClass('extLabel', 'readOnly', '');
		objForm.siteUrl.readOnly = true;
		objForm.siteUrl.className = "readOnly";
		if (navigator.userAgent.indexOf("Firefox") == -1) {
			objForm.siteUrl.value = document.getElementById( String("no0Url") ).innerText;
		}
		else {
			objForm.siteUrl.value = document.getElementById( String("no0Url") ).textContent;
		}
		
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
			objForm.siteUrl.readOnly = false;
			objForm.siteUrl.className = "";
			objForm.siteUrl.value = "http://";
		}
		else {
			objForm.siteUrl.readOnly = true;
			objForm.siteUrl.className = "readOnly";
			if (navigator.userAgent.indexOf("Firefox") == -1) {
				objForm.siteUrl.value = document.getElementById( String("no0Url") ).innerText;
			}
			else {
				objForm.siteUrl.value = document.getElementById( String("no0Url") ).textContent;
			}
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
		     (judgeHost != "" && (urlVal.indexOf("http://"+judgeHost) == -1 && urlVal.indexOf("https://"+judgeHost) == -1)) &&
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
		     (judgeHost != "" && (urlVal.indexOf("http://"+judgeHost) != -1 || urlVal.indexOf("https://"+judgeHost) != -1)) ||
		     (judgeIp != "" && urlVal.indexOf(judgeIp) != -1) ) {
			
			alert("URL of the internal cannot be specified.");
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
	var noVal = objForm.siteNo.value;
	var urlVal = objForm.siteUrl.value;
	urlVal = urlVal.toLowerCase();
	
	// ローカルサイト入力で外部は指定させない
	if ( objForm.siteType[0].checked ) {
		if ( urlVal.indexOf("localhost") == -1 &&
		     urlVal.indexOf("127.0.0.1") == -1 &&
		     (judgeBase != "" && urlVal.indexOf(judgeBase) == -1) &&
		     (judgeHost != "" && (urlVal.indexOf("http://"+judgeHost) == -1 && urlVal.indexOf("https://"+judgeHost) == -1)) &&
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
		     (judgeHost != "" && (urlVal.indexOf("http://"+judgeHost) != -1 || urlVal.indexOf("https://"+judgeHost) != -1)) ||
		     (judgeIp != "" && urlVal.indexOf(judgeIp) != -1) ) {
			
			alert("URL of the internal cannot be specified.");
			return false;
		}
	}
	
	objForm.act.value = "edit";
	if (noVal == "0") {
		if (objForm.beforeUrl.value.toLowerCase() != urlVal) {
			objForm.newBaseUrl.value = objForm.siteUrl.value;
		}
	}
	return true;
}

/**
 * 削除前処理
 * @param isAdmin 管理者権限フラグ
 */
function beforeDel(isAdmin) {
	
	// 削除許可判定
	if ( isAdmin != "true" ) {
		alert("Unauthorized user.\nPlease set \"auth_root=true\" to admin.conf.");
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

/**
 * ページロード完了時処理
 */
$(window).load(function() {
	var baseUrl = $("input[name='newBaseUrl']:hidden").val();
	if ( baseUrl != undefined && baseUrl != "" ) {
		alert ("URL on the homepage was changed to \"" + baseUrl + "\".\n\nClose the window, please access new URL.");
		window.opener = window;		// FF等でwindow.close()が機能しない現象への対応
		var win = window.open(location.href, "_self");
		win.close();
	}
});

//-->
</script>
</head>
<body onLoad="initLoad();">
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Database Manager</h2>
<%
	//----------------------------------------------------
	// リクエストパラメータ取得
	// get request type
	//----------------------------------------------------
	request.setCharacterEncoding("utf-8");
	String act = "";
	int reqNo = -1;
	String reqSiteDb = "";
	String reqShortLabel = "";
	String reqLongLabel = "";
	String reqUrlType = "";
	String reqSiteUrl = "";
	String reqSiteUrlBefore = "";
	String reqNewBaseUrl = "";
	Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( key.equals("act") ) {
			act = request.getParameter(key);
		}
		else if ( key.equals("siteNo") ) {
			reqNo = Integer.parseInt(request.getParameter(key).trim());
		}
		else if ( key.equals("siteDb") ) {
			reqSiteDb = request.getParameter(key);
		}
		else if ( key.equals("siteShortLabel") ) {
			reqShortLabel = request.getParameter(key).trim();
		}
		else if ( key.equals("siteLongLabel") ) {
			reqLongLabel = request.getParameter(key).trim();
		}
		else if ( key.equals("siteType") ) {
			reqUrlType = request.getParameter(key).trim();
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
		else if ( key.equals("newBaseUrl") ) {
			reqNewBaseUrl = request.getParameter(key).trim();
		}
	}
	
	//----------------------------------------------------
	// 各種パラメータを取得
	// fetch parameters
	//----------------------------------------------------
	final String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	final String[] dbPathes = new String[]{ MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH),
	                                        MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH),
	                                        MassBankEnv.get(MassBankEnv.KEY_PROFILE_PATH)};
	final String dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
	final String massbankConfPath = MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_PATH);
	GetConfig gtConf = new GetConfig(baseUrl);
	UpdateConfig upConf = new UpdateConfig();
	final String tomcatTmpPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_TEMP_PATH);
	final String tmpPath = (new File(tomcatTmpPath + sdf.format(new Date()))).getPath() + File.separator;
	final String baseSqlPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPADMIN_PATH) + "sql" + File.separator;
	boolean isResult = true;
	OperationManager om = OperationManager.getInstance();
	NumberFormat nf = NumberFormat.getNumberInstance();
	final String os = System.getProperty("os.name");
	AdminCommon admin = new AdminCommon();
	boolean isAdmin = admin.isAdmin();
	String hostName = "";
	String ipAddress = "";
	try {
		hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
		ipAddress = InetAddress.getLocalHost().getHostAddress();
	}
	catch (UnknownHostException uhe) {
		Logger.getLogger("global").severe( "get host name or get ip address failed." );
		uhe.printStackTrace();
		out.println( msgErr( "the host name or ip address cannot be taken.") );
		return;
	}
	
	try {
		//----------------------------------------------------
		// 排他確認
		// check for concurrent users
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
		// prepare operation
		//----------------------------------------------------
		// 一時ディレクトリを準備
		// set rights for tmp path
		(new File(tmpPath)).mkdir();
		if(os.indexOf("Windows") == -1){
			isResult = FileUtil.changeMode("777", tmpPath);
			if ( !isResult ) {
				out.println( msgErr( "[" + tmpPath + "]&nbsp;&nbsp; chmod failed.") );
				return;
			}
		}
		
		// 編集前の情報を保持
		// get infos
		String beforeServerUrl = gtConf.getServerUrl();
		String[] beforeDbList = gtConf.getDbName();
		String[] beforeUrlList = gtConf.getSiteUrl();
		
		
		//----------------------------------------------------
		// 追加処理
		// perform checks
		//----------------------------------------------------
		if ( act.equals("add") ) {
			reqNo = beforeDbList.length;
			
			// 入力値チェック
			// check database
			if ( !checkValue(out, reqSiteDb, reqShortLabel, reqLongLabel, reqSiteUrl) ) {
				out.println( msgErr("additional failure.") );
				isResult = false;
			}
			// URL種別チェック
			// check url
			else if ( !getUrlType(baseUrl, hostName, ipAddress, reqSiteUrl).equals(reqUrlType) ) {
				if ( reqUrlType.equals(URL_TYPE_INTERNAL) ) {
					out.println( msgErr("url on the external cannot be specified.") );
					isResult = false;
				}
				else if ( reqUrlType.equals(URL_TYPE_EXTERNAL) ) {
					out.println( msgErr("url of the internal cannot be specified.") );
					isResult = false;
				}
			}
			// 登録済みチェック
			// check if database is already registred
			else if ( isAlready(beforeDbList, beforeUrlList, reqSiteDb, reqSiteUrl, baseUrl, hostName, ipAddress) ) {
				isResult = false;
				out.println( msgErr("database already exists.") );
			}
			// 追加
			else {
				// 内部サイトのデータベースの追加処理を行う
				// create database
				if ( getUrlType(baseUrl, hostName, ipAddress, reqSiteUrl).equals(URL_TYPE_INTERNAL) ) {
				
					// SQLファイル準備
					File baseSql = new File(baseSqlPath + "create.sql" );
					File workSql = new File(tmpPath + "create.sql" );
					isResult = preparationSql( baseSql, workSql, reqSiteDb );
					if ( !isResult ) {
						Logger.getLogger("global").severe( "sql file create failed." + NEW_LINE +
						                                   "    file : " + workSql.getPath() );
						out.println( msgErr("add database failed.") );
						return;
					}
					
					// SQLファイル実行
					isResult = FileUtil.execSqlFile(dbHostName, "", workSql.getPath());
					if ( !isResult ) {
						Logger.getLogger("global").severe( "sql file execute failed." + NEW_LINE +
						                                   "    file : " + workSql.getPath() );
						out.println( msgErr("add database failed.") );
						return;
					}
					
					// フォルダ追加処理
					// create working directories
					for (int i=0; i<dbPathes.length; i++) {
						File workDir = new File(dbPathes[i] + reqSiteDb);
						if ( !workDir.isDirectory() ) {
							isResult = workDir.mkdir();
							if ( !isResult ) {
								Logger.getLogger("global").severe( "add derectory failed." + NEW_LINE +
								                                   "    file : " + workDir.getPath() );
							}
							if(os.indexOf("Windows") == -1){
								isResult = FileUtil.changeMode("777", workDir.getPath());
								if ( !isResult ) {
									Logger.getLogger("global").severe( "chmod derectory failed." + NEW_LINE +
									                                   "    file : " + workDir.getPath() );
								}
							}
						}
					}
				}
				
				// massbank.conf 追加処理
				// add database to config
				isResult = upConf.addConfig(reqNo, reqShortLabel, reqLongLabel, reqSiteUrl, reqSiteDb);
				if ( !isResult ) {
					Logger.getLogger("global").severe( "edit massbank.conf failed." );
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
			if ( !checkValue(out, reqSiteDb, reqShortLabel, reqLongLabel, reqSiteUrl) ) {
				out.println( msgErr("edit failure.") );
				isResult = false;
			}
			// URL種別チェック
			else if ( !getUrlType(baseUrl, hostName, ipAddress, reqSiteUrl).equals(reqUrlType) ) {
				if ( reqUrlType.equals(URL_TYPE_INTERNAL) ) {
					out.println( msgErr("url on the external cannot be specified.") );
					isResult = false;
				}
				else if ( reqUrlType.equals(URL_TYPE_EXTERNAL) ) {
					out.println( msgErr("url of the internal cannot be specified.") );
					isResult = false;
				}
			}
			// 編集
			else {
				ArrayList<Integer> internalSiteList = null;
				
				// 内部サイトの場合
				if ( reqUrlType.equals(URL_TYPE_INTERNAL) ) {
					// 内部サイト番号リスト生成
					if (reqNo == 0) {
						internalSiteList = new ArrayList<Integer>();
						for (int i=1; i<beforeUrlList.length; i++) {
							if (beforeServerUrl.equals(beforeUrlList[i])) {
								internalSiteList.add(i);
							}
						}
					}
					
					// massbank.conf 編集処理
					isResult = upConf.editConfig(reqNo, internalSiteList, reqShortLabel, reqLongLabel, reqSiteUrl, null);
					
					// TREEテーブル更新処理
					DatabaseAccess db = new DatabaseAccess(dbHostName, reqSiteDb);
					String sql = "";
					try {
						if ( db.open() ) {
							sql = "UPDATE TREE SET INFO='" + Sanitizer.sql("MassBank / " + reqLongLabel) + "' WHERE NO=1 AND PARENT=0;";
							db.executeUpdate(sql);
						}
						else {
							out.println( msgWarn( "not connect to database.") );
						}
					}
					catch (Exception e) {
						Logger.getLogger("global").severe( "SQL : " + sql );
						e.printStackTrace();
						out.println( msgWarn( "TREE table update failed." ) );
					}
					finally {
						if ( db != null ) {
							db.close();
						}
					}
				}
				else {
					// massbank.conf 編集処理
					isResult = upConf.editConfig(reqNo, internalSiteList, reqShortLabel, reqLongLabel, reqSiteUrl, reqSiteDb);
				}
				if ( !isResult ) {
					Logger.getLogger("global").severe( "edit massbank.conf failed." );
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
			else if ( !isExistDb(beforeDbList, beforeUrlList, reqNo, reqSiteDb, reqSiteUrlBefore) ) {
				reqNo = -1;
				isResult = true;
			}
			// 削除
			else {
				// massbank.conf 削除処理
				isResult = upConf.delConfig(reqNo);
				if ( !isResult ) {
					Logger.getLogger("global").severe( "edit massbank.conf failed." );
					out.println( msgErr("edit of massbank.conf failed.") );
				}
				else {
					reqNo = -1;
				}
				
				// massbank.conf 整形処理
				confFairing(massbankConfPath, tmpPath);
				
				// massbank.conf 再読み込み
				if ( isResult ) {
					gtConf = new GetConfig(baseUrl);
				}
				
				// 内部サイトのデータベースの削除処理を行う
				if ( getUrlType(baseUrl, hostName, ipAddress, reqSiteUrlBefore).equals(URL_TYPE_INTERNAL) ) {
					
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
								Logger.getLogger("global").severe( "delete derectory failed." + NEW_LINE +
								                                   "    file : " + workDir.getPath() );
							}
						}
					}
					
					// SQLファイル準備
					File baseSql = new File(baseSqlPath + "drop.sql" );
					File workSql = new File(tmpPath + "drop.sql" );
					isResult = preparationSql( baseSql, workSql, reqSiteDb );
					if ( !isResult ) {
						Logger.getLogger("global").severe( "sql file create failed." + NEW_LINE +
						                                   "    file : " + workSql.getPath() );
						out.println( msgErr("drop database failed.") );
						return;
					}
					
					// SQLファイル実行
					isResult = FileUtil.execSqlFile(dbHostName, "", workSql.getPath());
					if ( !isResult ) {
						Logger.getLogger("global").severe( "sql file execute failed." + NEW_LINE +
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
		String[] siteShortLabelList = gtConf.getSiteName();
		String[] siteLongLabelList = gtConf.getSiteLongName();
		String[] siteUrlList = gtConf.getSiteUrl();
		siteUrlList[0] = gtConf.getServerUrl();
		
		//----------------------------------------------------
		// 編集領域表示
		//----------------------------------------------------
		String selDb = "";
		String selShortLabel = "";
		String selLongLabel = "";
		String selUrlType = "";
		String selUrl = "";
		String isUrlIntChecked = "";
		String isUrlExtChecked = "";
		String isUrlIntDisabled = "";
		String isUrlExtDisabled = "";
		String intLabelClass = "";
		String extLabelClass = "";
		if ( !isResult ) {
			// エラーの場合は値を引き継ぐ
			selDb = reqSiteDb;
			selShortLabel = reqShortLabel;
			selLongLabel = reqLongLabel;
			selUrlType = reqUrlType;
			selUrl = reqSiteUrl;
			if ( !selUrlType.equals(URL_TYPE_EXTERNAL) ) {
				isUrlIntChecked = " checked";
				if ( act.equals("edit") || act.equals("del") ) {
					isUrlExtDisabled = " disabled";
					extLabelClass = "readOnly";
				}
			}
			else {
				isUrlExtChecked = " checked";
				if ( act.equals("edit") || act.equals("del") ) {
					isUrlIntDisabled = " disabled";
					intLabelClass = "readOnly";
				}
			}
		}
		else if ( act.equals("add") || act.equals("edit") ) {
			selDb = siteDbList[reqNo];
			selShortLabel = siteShortLabelList[reqNo];
			selLongLabel = siteLongLabelList[reqNo];
			selUrlType = reqUrlType;
			selUrl = siteUrlList[reqNo];
			if ( !selUrlType.equals(URL_TYPE_EXTERNAL) ) {
				isUrlIntChecked = " checked";
				isUrlExtDisabled = " disabled";
				extLabelClass = "readOnly";
			}
			else {
				isUrlExtChecked = " checked";
				isUrlIntDisabled = " disabled";
				intLabelClass = "readOnly";
			}
		}
		else {
			selDb = "";
			selShortLabel = "";
			selLongLabel = "";
			selUrlType = reqUrlType;
			isUrlIntChecked = " checked";
			if ( reqNo == -1 || !selUrlType.equals(URL_TYPE_EXTERNAL) ) {
				selUrl = "http://localhost/MassBank/";
			}
			else {
				selUrl = "http://";
			}
		}
		out.println( "<form name=\"formEdit\" method=\"post\" action=\"./Manager.jsp\" onSubmit=\"doWait();\">" );
		out.println( "<div style=\"width:980px; border: 2px Gray solid; padding:15px; background-color:WhiteSmoke;\">" );
		out.println( "<table width=\"97%\" align=\"center\" cellspacing=\"2\" cellpadding=\"2\">" );
		out.println( "<tr>" );
		out.println( "<td width=\"50\" title=\"Database No.\"><b>No.</b></td>" );
		out.println( "<td width=\"35\"></td>" ) ;
		out.println( "<td width=\"180\" title=\"Database Name\"><b>DB Name</b></td>" );
		out.println( "<td width=\"180\" title=\"Short Label\"><b>Short Label</b></td>" );
		out.println( "<td title=\"Long Label\"><b>Long Label</b></td>" );
		out.println( "</tr>" );
		out.println( "<tr height=\"10\">" );
		out.println( "<td>" );
		out.println( "<select name=\"siteNo\" style=\"width:100%;\" onChange=\"selNo();\">" );
		if ( reqNo == -1 ) {
			out.println( "<option value=\"-1\" selected>+</option>" );
		}
		else {
			out.println( "<option value=\"-1\">+</option>" );
		}
		for (int i=0; i<siteDbList.length; i++) {
			if ( reqNo == i ) {
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
		out.println( "<td><input type=\"text\" style=\"width:98%;\" name=\"siteShortLabel\" value=\"" + selShortLabel + "\"></td>" );
		out.println( "<td><input type=\"text\" style=\"width:100%;\" name=\"siteLongLabel\" value=\"" + selLongLabel + "\"></td>" );
		out.println( "</tr>" );
		
		out.println( "<tr>" );
		out.println( "<td colspan=\"2\"></td>" );
		out.println( "<td title=\"Site URL Type\"><b>URL Type</b></td>" );
		out.println( "<td title=\"URL\"><b>URL</b></td>" );
		out.println( "</tr>" );
		
		out.println( "<tr>" );
		out.println( "<td colspan=\"2\"></td>" );
		out.println( "<td>" );
		out.println( "<input type=\"radio\" name=\"siteType\" value=\"" + URL_TYPE_INTERNAL + "\" onClick=\"selType();\"" + isUrlIntChecked + isUrlIntDisabled + "> <span id=\"intLabel\" class=\"" + intLabelClass + "\">internal</span>&nbsp;&nbsp;&nbsp;&nbsp;" );
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
		out.println( "<input type=\"hidden\" name=\"newBaseUrl\" value=\"" + reqNewBaseUrl + "\">" );
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
		out.println( "\t<div class=\"count baseFont\">" + nf.format(siteDbList.length) + " database" );
		if ( externalNum > 0 ) {
			out.print(  "&nbsp;(" + externalNum + " external database)" );
		}
		out.println( "</div>" );
		out.println( "\t<table width=\"980\" cellspacing=\"1\" cellpadding=\"0\" bgcolor=\"Lavender\" class=\"fixed\">" );
		out.println( "\t\t<thead>");
		out.println( "\t\t<tr class=\"rowHeader\">");
		out.println( "\t\t\t<td width=\"30\">No.</td>" );
		out.println( "\t\t\t<td width=\"90\">DB Name</td>" );
		out.println( "\t\t\t<td>URL</td>" );
		out.println( "\t\t\t<td width=\"120\">Short Label</td>" );
		out.println( "\t\t\t<td width=\"150\">Long Label</td>" );
		out.println( "\t\t\t<td width=\"64\">Status</td>" );
		out.println( "\t\t\t<td width=\"215\">Details</td>" );
		out.println( "\t\t</tr>");
		out.println( "\t\t</thead>");
		for (int i=0; i<siteDbList.length; i++) {
			
			String status = STATUS_OK;
			StringBuilder details = new StringBuilder();
			
			// massbank.conf 取得チェック
			if ( siteDbList[i].equals("") || siteShortLabelList[i].equals("") || siteLongLabelList[i].equals("") || siteUrlList[i].equals("") ) {
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
					Logger.getLogger("global").severe( "sql file create failed." + NEW_LINE +
					                                   "    file : " + workSql.getPath() );
					out.println( msgErr("use database failed.") );
					return;
				}
				isResult = FileUtil.execSqlFile(dbHostName, "", workSql.getPath());
				if ( !isResult ) {
					status = STATUS_ERR;
					details.append( "<span class=\"errFont\">database not exist in MySQL.</span><br />" );
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
				details.append( "<span class=\"msgFont\">external database.</span><br />" );
			}
			
			out.println( "\t\t<tr class=\"rowEnable\" id=\"row" + i + "\">" );
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "No\">" + i + "</td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "Db\">" + Sanitizer.html(siteDbList[i]) + "</td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "Url\"><a href=\"" + siteUrlList[i] + "\" target=\"_blank\" class=\"urlFont\">" + Sanitizer.html(siteUrlList[i]) + "</a></td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "ShortLabel\">" + Sanitizer.html(siteShortLabelList[i]) + "</td>");
			out.println( "\t\t\t<td class=\"manage\" id=\"no" + i + "LongLabel\">" + Sanitizer.html(siteLongLabelList[i]) + "</td>");
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
