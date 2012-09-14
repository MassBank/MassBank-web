<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * レコード一覧
 *
 * ver 1.0.5 2012.09.14
 *
 ******************************************************************************/
%>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.PrintWriter " %>
<%@ page import="java.io.UnsupportedEncodingException " %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.GetInstInfo" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.Sanitizer" %>
<%@ page import="massbank.admin.DatabaseAccess" %>
<%@ page import="massbank.admin.FileUtil" %>
<%@ page import="massbank.admin.OperationManager" %>
<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="massbank.svn.MSDBUpdater" %>
<%!
	/** 作業ディレクトリ用日時フォーマット */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_SSS");
	
	/** 改行文字列 */
	private final String NEW_LINE = System.getProperty("line.separator");
	
	/* ステータス（OK） */
	private final String STATUS_OK = "<span class=\"msgFont\">ok</span>";
	
	/* ステータス（警告） */
	private final String STATUS_WARN = "<span class=\"warnFont\">warn</span>";
	
	/* ステータス（エラー） */
	private final String STATUS_ERR = "<span class=\"errFont\">error</span>";
	
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
	 * 装置情報削除処理
	 * @param db DBアクセスオブジェクト
	 * @param msgList メッセージ格納用
	 * @param dumpPath 作業用パス
	 * @param hostName ホスト名
	 * @param selDb 対象DB
	 * @param instInfo 現在の装置情報
	 * @param instNo 削除対象装置No.
	 * @return 削除結果
	 * @throws IOException 入出力例外
	 */
	private boolean delInstrument( DatabaseAccess db, ArrayList<String> msgList, String tmpPath, String hostName, String selDb, GetInstInfo instInfo, String instNo) throws IOException  {
		
		String dumpPath = tmpPath + selDb + ".dump";
		String[] instNoList = instInfo.getNo();
		
		//----------------------------------------------------
		// 入力値チェック処理
		//----------------------------------------------------
		boolean isCheck = true;
		if ( instNo.equals("") ) {
			msgList.add( msgErr( "[No.] is empty.") );
			isCheck = false;
		}
		else {
			try {
				Integer.parseInt(instNo);
				for (String no : instNoList) {
					if ( instNo.equals(no) ) {
						isCheck = true;
						break;
					}
					else {
						isCheck = false;
					}
				}
			}
			catch (NumberFormatException e) {
				isCheck = false;
			}
			if ( !isCheck ) {
				msgList.add( msgErr( "[No.] : \"" + instNo + "\" is illegal.") );
			}
		}
		if ( !isCheck ) {
			return false;
		}
		
		OperationManager om = OperationManager.getInstance();
		boolean isOperation = om.startOparation(om.P_INSTRUMENT, om.TP_UPDATE, selDb);
		if ( !isOperation ) {
			msgList.add( msgWarn( "other users are updating. please access later.") );
			return false;
		}
		
		boolean res = false;
		File tmpDir = new File(tmpPath);
		tmpDir.mkdir();
		String os = System.getProperty("os.name");
		if(os.indexOf("Windows") == -1){
			res = FileUtil.changeMode("777", tmpPath);
			if ( !res ) {
				msgList.add( msgErr( "[" + tmpPath + "]&nbsp;&nbsp; chmod failed.") );
				return false;
			}
		}
		try {
			//----------------------------------------------------
			// ロールバック用DBダンプ処理
			//----------------------------------------------------
			String[] tables = new String[]{"INSTRUMENT"};
			res = FileUtil.execSqlDump(hostName, selDb, tables, dumpPath);
			if ( !res ) {
				Logger.getLogger("global").severe( "sqldump failed." + NEW_LINE +
				                      "    dump file : " + dumpPath );
				msgList.add( msgErr( "dump failed." ) );
				return false;
			}
			
			ResultSet rs = null;
			String sql = "";
			try {
				//----------------------------------------------------
				// 削除対象装置の関連付けチェック
				//----------------------------------------------------
				sql = "SELECT COUNT(INSTRUMENT_NO) NO FROM RECORD WHERE INSTRUMENT_NO=" + instNo;
				rs = db.executeQuery(sql);
				int cnt = 0;
				if ( rs.next() ) {
					cnt = rs.getInt("NO");
				}
				if ( cnt > 0 ) {
					msgList.add( msgWarn( cnt + " records related to instrument [No." + instNo + "]." ) );
					return false;
				}
				
				//----------------------------------------------------
				// INSTRUMENTテーブルからの削除処理
				//----------------------------------------------------
				sql = "DELETE FROM INSTRUMENT WHERE INSTRUMENT_NO=" + instNo;
				db.executeUpdate(sql);
				msgList.add( msgInfo( "delete instrument [No.] : \"" + instNo + "\"." ) );
			}
			catch (SQLException e) {
				Logger.getLogger("global").severe( "    sql : " + sql );
				e.printStackTrace();
				msgList.add( msgErr( "database access error." ) );
				
				//----------------------------------------------------
				// ロールバック処理
				//----------------------------------------------------
				res = FileUtil.execSqlFile(hostName, selDb, dumpPath);
				if ( !res ) {
					Logger.getLogger("global").severe( "rollback(sqldump) failed." );
					msgList.add( msgErr( "rollback failed." ) );
				}
				return false;
			}
			finally {
				try {
					if ( rs != null ) { rs.close(); }
				}
				catch (SQLException e) {}
			}
		}
		finally {
			if (tmpDir.exists()) {
				FileUtil.removeDir( tmpDir.getPath() );
			}
			om.endOparation(om.P_INSTRUMENT, om.TP_UPDATE, selDb);
		}
		
		return true;
	}
	
	/**
	 * 装置情報表示処理
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param msgList メッセージ格納用
	 * @param selDbName 対象DB
	 * @param instInfo 現在の装置情報
	 * @param reqInstNo 選択装置No.
	 * @param prevInstType 更新装置種別
	 * @param prevInstName 更新装置名
	 * @return 表示結果
	 * @throws IOException 入出力例外
	 */
	private boolean dispInstrument( DatabaseAccess db, JspWriter op, ArrayList<String> msgList, String selDbName, 
	                                GetInstInfo instInfo, String reqInstNo, String prevInstType, String prevInstName) throws IOException  {
		
		OperationManager om = OperationManager.getInstance();
		boolean isOperation = om.startOparation(om.P_INSTRUMENT, om.TP_VIEW, selDbName);
		if ( !isOperation ) {
			op.println( msgWarn( "other users are updating. please access later.") );
			return false;
		}
		
		try {
			NumberFormat nf = NumberFormat.getNumberInstance();
			String[] instNoList = instInfo.getNo();
			String[] instNameList = instInfo.getName();
			String[] instTypeList = instInfo.getType();
			TreeMap<Integer, String> instInfoMap = new TreeMap<Integer, String>();
			for (int i=0; i<instNoList.length; i++) {
				Integer key = Integer.parseInt(instNoList[i]);
				String value = ((!instTypeList[i].equals("")) ? instTypeList[i] : " ") + "\t" + ((!instNameList[i].equals("")) ? instNameList[i] : " ");
				instInfoMap.put(key, value);
			}
			
			int selectNo = 0;
			String defaultType = "";
			String defaultName = "";
			if ( instInfoMap.size() != 0 ) {
				selectNo = instInfoMap.firstKey();
				try {
					int tmpNo = Integer.parseInt(reqInstNo);
					if (instInfoMap.containsKey(tmpNo)) {
						selectNo = tmpNo;
					}
				}
				catch (NumberFormatException e) {}
				defaultType = instInfoMap.get(selectNo).split("\t")[0].trim();
				defaultName = instInfoMap.get(selectNo).split("\t")[1].trim();
			}
			if ( prevInstType != null ) {
				defaultType = prevInstType;
			}
			if ( prevInstName != null ) {
				defaultName = prevInstName;
			}
			
			ResultSet[] rs = new ResultSet[instInfoMap.size()];
			String sql = "";
			try {
				int i = 0;
				for ( Map.Entry<Integer, String> e : instInfoMap.entrySet() ) {
					int key = e.getKey();
					String value = e.getValue();
					String status = "";
					String detail = "";
					int relCnt = 0;
					sql = "SELECT COUNT(INSTRUMENT_NO) RELATED FROM RECORD WHERE INSTRUMENT_NO=" + key + ";";
					rs[i] = db.executeQuery(sql);
					if ( rs[i].next() ) {
						relCnt = rs[i].getInt("RELATED");
					}
					if (relCnt > 0) {
						status = STATUS_OK;
						detail = "<span class=\"msgFont\">" + nf.format(relCnt) + " record related.</span>";
					}
					else {
						status = STATUS_WARN;
						detail = "<span class=\"warnFont\">" + nf.format(relCnt) + " record related.</span>";
					}
					value += "\t" + status + "\t" + detail;
					instInfoMap.put(key, value);
					i++;
				}
			}
			catch (SQLException e) {
				Logger.getLogger("global").severe( "    sql : " + sql );
				e.printStackTrace();
				op.println( msgErr( "database access error." ) );
				return false;
			}
			finally {
				try {
					for (int i=0; i<rs.length; i++) {
						if ( rs[i] != null ) { rs[i].close(); }
					}
				}
				catch (SQLException e) {}
			}
			
			// 編集枠表示
			String strDisable = "";
			if ( instNoList.length == 0 ) {
				msgList.add( msgInfo( "no instrument." ) );
				strDisable = " disabled";
			}
			op.println( "<form name=\"formEdit\" method=\"post\" action=\"./InstEdit.jsp\" onSubmit=\"doWait();\">" );
			op.println( "<div style=\"width:980; border: 2px Gray solid; padding:15px; background-color:WhiteSmoke;\">" );
			op.println( "<table width=\"100%\" align=\"center\" cellspacing=\"1\" cellpadding=\"1\">" );
			op.println( "<tr>" );
			op.println( "<td><b>No.</b></td>" );
			op.println( "<td><b>Instrument Type</b></td>" );
			op.println( "<td><b>Instrument Name</b></td>" );
			op.println( "</tr>" );
			op.println( "<tr>" );
			op.println( "<td>" );
			op.println( "<select name=\"instNo\" class=\"db\" onChange=\"selNo();\">" );
			for (Map.Entry<Integer, String> e : instInfoMap.entrySet()) {
				op.print( "<option value=\"" + e.getKey() + "\"" );
				if ( selectNo == e.getKey() ) {
					op.print( " selected");
				}
				op.println( ">" + e.getKey() );
			}
			op.println( "</select>" );
			op.println( "</td>" );
			op.println( "<td><input type=\"text\" name=\"instType\" size=\"33\" value=\"" + Sanitizer.html(defaultType) + "\"" + strDisable + "></td>" );
			op.println( "<td><input type=\"text\" name=\"instName\" size=\"88\" value=\"" + Sanitizer.html(defaultName) + "\"" + strDisable + "></td>" );
			op.println( "</tr>");
			op.println( "<tr>");
			op.println( "<td colspan=\"3\" align=\"right\" height=\"40px\">" );
			op.println( "<input type=\"submit\" name=\"btnDelete\" value=\"Delete\" onClick=\"return beforeEdit('del');\">" );
			op.println( "</td>" );
			op.println( "</tr>" );
			op.println( "</table>" );
			op.println( "</div>" );
			op.println( "<input type=\"hidden\" name=\"act\" value=\"\">" );
			op.println( "<input type=\"hidden\" name=\"db\" value=\"" + selDbName + "\">" );
			op.println( "</form>" );
			
			// メッセージ表示処理
			for ( String msg : msgList ) {
				op.println( msg );
			}
			
			// 一覧表示処理
			op.println( "\t<br><hr><br>" );
			op.println( "\t<div class=\"count baseFont\">" + nf.format(instInfoMap.size()) + " instruments&nbsp;</div>" );
			op.println( "\t<table width=\"980\" cellspacing=\"1\" cellpadding=\"0\" bgcolor=\"Lavender\">" );
			op.println( "\t\t<tr class=\"rowHeader\">");
			op.println( "\t\t\t<td width=\"50\">No.</td>" );
			op.println( "\t\t\t<td width=\"210\">Instrument Type</td>" );
			op.println( "\t\t\t<td width=\"470\">Instrument Name</td>" );
			op.println( "\t\t\t<td width=\"70\">Status</td>" );
			op.println( "\t\t\t<td width=\"200\">Details</td>" );
			op.println( "\t\t</tr>");
			for ( Map.Entry<Integer, String> e : instInfoMap.entrySet() ) {
				int no = e.getKey();
				String type = e.getValue().split("\t")[0].trim();
				String name = e.getValue().split("\t")[1].trim();
				String status = e.getValue().split("\t")[2];
				String detail = e.getValue().split("\t")[3];
				op.println( "<tr class=\"rowEnable\" id=\"row" + no + "\" height=\"24\">" );
				op.println( "<td align=\"center\" id=\"no" + no + "No\">" + no + "</td>" );
				op.println( "<td class=\"leftIndent\" id=\"no" + no + "Type\">" + Sanitizer.html(type) + "</td>" );
				op.println( "<td class=\"leftIndent\" id=\"no" + no + "Name\">" + Sanitizer.html(name) + "</td>" );
				op.println( "<td align=\"center\" id=\"no" + no + "Status\">" + status + "</td>" );
				op.println( "<td class=\"details\">" + detail + "</td>" );
				op.println( "</tr>" );
			}
			op.println( "</table>" );
		}
		finally {
			om.endOparation(om.P_INSTRUMENT, om.TP_VIEW, selDbName);
		}
		return true;
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<link rel="stylesheet" type="text/css" href="css/admin.css">
<script type="text/javascript" src="../script/Common.js"></script>
<title>Admin | Instrument Editor</title>
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
		objForm.instNo.focus();
		
		// 一覧背景色変更
		noList = objForm.instNo.options;
		no = objForm.instNo.value;
		noLength = noList.length;
		for (i=0; i<noLength; i++) {
			obj = document.getElementById( String("row" + noList[i].value) );
			if (noList[i].value == no) {
				obj.style.background = ON_COLOR;
			}
			else {
				obj.style.background = OFF_COLOR;
			}
		}
		
		// ボタン有効無効化
		if ( noList.length == 0 ) {
			objForm.btnUpdate.disabled = true;
			objForm.btnDelete.disabled = true;
		}
		else {
			if ( document.getElementById( String("no" + no + "Status") ).innerText == "ok" || document.getElementById( String("no" + no + "Status") ).textContent == "ok" ) {
				objForm.btnDelete.disabled = true;
			}
			else {
				objForm.btnDelete.disabled = false;
			}
		}
	}
}

/**
 * 編集前処理
 */
function beforeEdit(action) {
	
	objForm = document.formEdit;
	
	if ( confirm("Are you sure?") ) {
		objForm.act.value = action;
		return true;
	}
	else {
		return false;
	}
}

/**
 * DB選択
 */
function selDb() {
	url = location.href;
	url = url.split("?")[0];
	url = url.split("#")[0];
	dbVal = document.formMain.db.value;
	if ( dbVal != "" ) {
		url += "?db=" + dbVal;
	}
	location.href = url;
}

/**
 * DB選択チェック
 */
function checkDb() {
	if ( document.formMain.db.value == "" ) {
		return false;
	}
	return true;
}

/**
 * No.選択
 */
function selNo() {
	if ( noList.length == 0 ) {
		return;
	}
	
	objForm = document.formEdit;
	no = objForm.instNo.value;
	
	// デフォルト値設定
	if (typeof document.getElementById( String("no" + no + "Type") ).innerText != "undefined") {
		objForm.instType.value = document.getElementById( String("no" + no + "Type") ).innerText;
		objForm.instName.value = document.getElementById( String("no" + no + "Name") ).innerText;
	}
	else {
		objForm.instType.value = document.getElementById( String("no" + no + "Type") ).textContent;
		objForm.instName.value = document.getElementById( String("no" + no + "Name") ).textContent;
	}
	
	// 一覧背景色変更
	noList = document.formEdit.instNo.options;
	noLength = noList.length;
	for (i=0; i<noLength; i++) {
		obj = document.getElementById( String("row" + noList[i].value) );
		if (noList[i].value == no) {
			obj.style.background = ON_COLOR;
		}
		else {
			obj.style.background = OFF_COLOR;
		}
	}
	
	// ボタン有効無効化
	if ( document.getElementById( String("no" + no + "Status") ).innerText == "ok" || document.getElementById( String("no" + no + "Status") ).textContent == "ok" ) {
		objForm.btnDelete.disabled = true;
	}
	else {
		objForm.btnDelete.disabled = false;
	}
}
//-->
</script>
</head>
<body onLoad="initLoad();">
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Instrument Editor</h2>
<%
	//----------------------------------------------------
	// リクエストパラメータ取得
	//----------------------------------------------------
	request.setCharacterEncoding("utf-8");
	String act = "";
	String selDbName = "";
	String reqInstNo = "";
	String reqInstType = "";
	String reqInstName = "";
	Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( key.equals("act") ) {
			act = request.getParameter(key);
		}
		else if ( key.equals("db") ) {
			selDbName = request.getParameter(key);
		}
		else if ( key.equals("instNo") ) {
			reqInstNo = request.getParameter(key).trim();
		}
		else if ( key.equals("instType") ) {
			reqInstType = request.getParameter(key).trim();
		}
		else if ( key.equals("instName") ) {
			reqInstName = request.getParameter(key).trim();
		}
	}
	
	//----------------------------------------------------
	// 各種パラメータを取得
	//----------------------------------------------------
	final String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	final String tomcatTmpPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_TEMP_PATH);
	final String annotationPath = MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
	final String dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
	final String tmpPath = (new File(tomcatTmpPath + sdf.format(new Date()))).getPath() + File.separator;
	GetConfig conf = new GetConfig(baseUrl);
	GetInstInfo instInfo = null;
	DatabaseAccess db = null;
	boolean isResult = true;
	ArrayList<String> msgList = new ArrayList<String>();
	
	try {
		//----------------------------------------------------
		// 存在するDB名を取得
		//----------------------------------------------------
		List<String> dbNameList = Arrays.asList(conf.getDbName());
		ArrayList<String> dbNames = new ArrayList<String>();
		dbNames.add("");
		File[] dbDirs = (new File( annotationPath )).listFiles();
		if ( dbDirs != null ) {
			for ( File dbDir : dbDirs ) {
				if ( dbDir.getName().indexOf(MSDBUpdater.BACKUP_IDENTIFIER) != -1 ) {
					continue;
				}
				if ( dbDir.isDirectory() ) {
					int pos = dbDir.getName().lastIndexOf("\\");
					String dbDirName = dbDir.getName().substring( pos + 1 );
					pos = dbDirName.lastIndexOf("/");
					dbDirName = dbDirName.substring( pos + 1 );
					if (dbNameList.contains(dbDirName)) {
						//DBディレクトリが存在し、massbank.confに記述があるDBのみ有効とする
						dbNames.add( dbDirName );
					}
				}
			}
		}
		if (dbDirs == null || dbNames.size() == 0) {
			out.println( msgErr( "[" + annotationPath + "]&nbsp;&nbsp;directory not exist." ) );
			return;
		}
		Collections.sort(dbNames);
		
		if ( selDbName.equals("") || !dbNames.contains(selDbName) ) {
			selDbName = dbNames.get(0);
		}
		int dbIndex = dbNameList.indexOf(selDbName);
		
		//----------------------------------------------------
		// フォーム表示
		//----------------------------------------------------
		out.println( "<form name=\"formMain\" action=\"./InstEdit.jsp\" method=\"post\" onSubmit=\"doWait();\">" );
		out.println( "<input type=\"hidden\" name=\"act\" value=\"get\">" );
		out.println( "<span class=\"baseFont\">Database :</span>&nbsp;" );
		out.println( "<select name=\"db\" class=\"db\" onChange=\"selDb();\">" );
		for ( int i=0; i<dbNames.size(); i++ ) {
			String dbName = dbNames.get(i);
			out.print( "<option value=\"" + dbName + "\"" );
			if ( dbName.equals(selDbName) ) {
				out.print( " selected" );
			}
			if ( i == 0 ) {
				out.println( ">------------------</option>" );
			}
			else {
				out.println( ">" + dbName + "</option>" );
			}
		}
		out.println( "</select>" );
		out.println( "<input type=\"submit\" value=\"Get\" onClick=\"return checkDb();\">" );
		out.println( "</form>" );
		out.println( "<hr><br>" );
		
		//----------------------------------------------------
		// DB接続
		//----------------------------------------------------
		if ( act.equals("get") || act.equals("del") ) {
			db = new DatabaseAccess(dbHostName, selDbName);
			isResult = db.open();
			if ( !isResult ) {
				out.println( msgErr( "not connect to database.") );
				return;
			}
		}
		
		//----------------------------------------------------
		// 削除処理
		//----------------------------------------------------
		if ( act.equals("del") ) {
			instInfo = new GetInstInfo( baseUrl );
			instInfo.setIndex(dbIndex);
			isResult = delInstrument( db, msgList, tmpPath, dbHostName, selDbName, instInfo, reqInstNo );
		}
		
		//----------------------------------------------------
		// 一覧表示
		//----------------------------------------------------
		if ( act.equals("get") || act.equals("mod") || act.equals("del") ) {
			instInfo = new GetInstInfo( baseUrl );
			instInfo.setIndex(dbIndex);
			String prevInstType = (act.equals("mod")) ? reqInstType : null;
			String prevInstName = (act.equals("mod")) ? reqInstName : null;
			isResult = dispInstrument( db, out, msgList, selDbName, instInfo, reqInstNo, prevInstType, prevInstName );
		}
	}
	finally {
		if ( db != null ) {
			db.close();
		}
		out.println( "</body>" );
		out.println( "</html>" );
	}
%>
