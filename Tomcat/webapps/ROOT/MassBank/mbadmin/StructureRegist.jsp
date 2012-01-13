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
 * 構造式登録
 *
 * ver 1.1.11 2012.01.13
 *
 ******************************************************************************/
%>

<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.LineNumberReader" %>
<%@ page import="java.io.PrintStream" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="massbank.admin.DatabaseAccess" %>
<%@ page import="massbank.admin.FileUtil" %>
<%@ page import="massbank.admin.OperationManager" %>
<%@ page import="massbank.FileUpload" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.Sanitizer" %>
<%@ include file="../jsp/Common.jsp"%>
<%!
	/** 作業ディレクトリ用日時フォーマット */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_SSS");
	
	/** 改行文字列 */
	private final String NEW_LINE = System.getProperty("line.separator");
	
	/** アップロードMolfile名（ZIP） */
	private final String UPLOAD_MOLFILE_ZIP = "moldata.zip";
	
	/** アップロードMolfile名（MSBK） */
	private final String UPLOAD_MOLFILE_MSBK = "moldata.msbk";
	
	/** Molfileデータディレクトリ名 */
	private final String MOLDATA_DIR_NAME = "moldata";
	
	/** list.tsvファイル名 */
	private final String LIST_FILE_NAME = "list.tsv";
	
	/** Molfile拡張子 */
	private final String MOL_EXTENSION = ".mol";
	
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
	 * 構造式登録に必要となる項目の初期化
	 * 構造式用プリフィックス、次の構造式登録用ID取得
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @return プリフィックスと構造式登録時の最初のIDを格納した配列
	 * @throws IOException
	 */
	private String[] initRegist(DatabaseAccess db, JspWriter op) throws IOException {
		String[] ret = new String[2];
		String sql = "";
		ResultSet[] rs = new ResultSet[2];
		try {
			sql = "SELECT LEFT(ID, 3) PREFIX FROM SPECTRUM LIMIT 1;";	// SPECTRUMテーブルにレコードがなければ0件
			rs[0] = db.executeQuery(sql);
			if ( rs[0].next() ) {
				String tmpPrefix = rs[0].getString("PREFIX");
				try {
					Integer.parseInt(tmpPrefix.substring(2));
					ret[0] = tmpPrefix.substring(0, (tmpPrefix.length() - 1));
				}
				catch (NumberFormatException nfe) {
					ret[0] = tmpPrefix;
				}
			}
			if ( ret[0] == null || ret[0].equals("") ) {
				op.println( msgErr( "MassBank Record is not registered.") );
				ret = null;
			}
			else {
				sql = "SELECT IFNULL(MAX(SUBSTRING(FILE, " + (ret[0].length() + 1) + ")), 0) + 1 STARTID FROM MOLFILE;";
				rs[1] = db.executeQuery(sql);
				if ( rs[1].next() ) {
					ret[1] = rs[1].getString("STARTID");
				}
			}
		}
		catch (SQLException e) {
			Logger.getLogger("global").severe( "SQL : " + sql );
			e.printStackTrace();
			op.println( msgErr( "database access error.") );
			ret = null;
		}
		finally {
			try {
				for (int i=0; i<rs.length; i++) {
					if (rs[i] != null ) { rs[i].close(); }
				}
			}
			catch (SQLException e) {}
		}
		return ret;
	}
	
	/**
	 * list.tsv読み込みおよびチェック
	 * チェック警告は無視して実行（警告メッセージは画面に返す）
	 * チェックエラーはチェック処理後に中断
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param listTsvPath list.tsvファイルパス
	 * @param dataPath 構造式登録情報パス
	 * @param errMsg エラーメッセージ
	 * @return 登録に必要な情報のリスト<化合物, マスターファイル名>
	 * @throws IOException
	 */
	private TreeMap<String, String> check(DatabaseAccess db, JspWriter op, String listTsvPath, String dataPath) throws IOException {
		
		TreeMap<String, String> list = new TreeMap<String, String>();		// 登録用リスト
		TreeMap<String, String> listTmp = new TreeMap<String, String>();	// チェック作業用リスト
		HashSet<String> rsCompound = new HashSet<String>();				// レコード登録済みチェック用
		HashSet<String> rsName = new HashSet<String>();					// 構造式登録済みチェック用
		LineNumberReader r = null;
		ResultSet rs[] = new ResultSet[2];
		String sql = "";
		try {
			// DB情報取得（レコード情報）
			sql = "SELECT LEFT(NAME, INSTR(NAME, ';')-1) COMPOUND FROM SPECTRUM;";
			rs[0] = db.executeQuery(sql);
			while ( rs[0].next() ) {
				rsCompound.add(rs[0].getString("COMPOUND"));
			}
			
			// DB情報取得（化合物名）
			sql = "SELECT NAME FROM MOLFILE;";
			rs[1] = db.executeQuery(sql);
			while ( rs[1].next() ) {
				rsName.add(rs[1].getString("NAME"));
			}
			
			r = new LineNumberReader(new FileReader(new File(listTsvPath)));
			String line;
			while ((line = r.readLine()) != null) {
				boolean isWarn = false;
				
				// 1つ以上のタブ区切りがある行を登録対象とする
				if (line.indexOf("\t") != -1 && line.split("\t").length == 1) {
					op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber() + " : no file name.") );
					isWarn = true;
				}
				else if (line.indexOf("\t") != -1 && line.split("\t").length >= 2) {
					String compound = line.split("\t")[0].trim();
					String masterName = line.split("\t")[1].trim();
					
					// 化合物名チェック
					if ( compound.length() == 0 ) {
						op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber() + " : not compound name.") );
						isWarn = true;
					}
					// ファイル名チェック
					else if ( masterName.length() == 0 ) {
						op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber() + " : no file name.") );
						isWarn = true;
					}
					// list.tsv内での化合物名重複チェック
					else if ( listTmp.containsKey(compound) ) {
						op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber()
						              + " : compound name&nbsp;&nbsp;[" + compound + "]&nbsp;&nbsp;is duplicated.") );
						isWarn = true;
					}
					// list.tsv内でのファイル名重複チェック
					else if ( listTmp.containsValue(masterName) ) {
						op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber()
						              + " : file name&nbsp;&nbsp;[" + masterName + "]&nbsp;&nbsp;is duplicated.") );
						isWarn = true;
					}
					// 化合物名に対するファイル存在チェック
					else if ( !(new File(dataPath + "/" + masterName)).isFile() ) {
						op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber()
						              + " : file&nbsp;&nbsp;[" + masterName + "]&nbsp;&nbsp;is not included.") );
						isWarn = true;
					}
					// DBチェック
					else {
						// レコード情報として化合物が登録されているか
						if ( !rsCompound.contains(compound) ) {
							op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber()
							              + " : compound&nbsp;&nbsp;[" + compound + "]&nbsp;&nbsp;of unregistration in MassBank Record.") );
							isWarn = true;
						}
						// 化合物名が構造式情報として既に登録されているか
						if ( rsName.contains(compound) ) {
							op.println( msgWarn( LIST_FILE_NAME + " line " + r.getLineNumber()
							              + " : compound name&nbsp;&nbsp;[" + compound + "]&nbsp;&nbsp;exists already in database.") );
							isWarn = true;
						}
					}
					
					if ( !isWarn ) {
						list.put(compound, masterName);
					}
					listTmp.put(compound, masterName);
				}
			}
		}
		catch (SQLException e) {
			Logger.getLogger("global").severe( "SQL : " + sql );
			e.printStackTrace();
			op.println( msgErr( "database access error.") );
			return new TreeMap<String, String>();
		}
		finally {
			try {
				if ( r != null ) { r.close(); }
			}
			catch (IOException e) {}
			try {
				for (int i=0; i<rs.length; i++) {
					if ( rs[i] != null ) { rs[i].close(); }
				}
			}
			catch (SQLException e) {}
		}
		
		if (list.size() == 0) {
			if ( dataPath.indexOf(MOLDATA_DIR_NAME) != -1 ) {
				op.println( msgWarn( "no molfile for registration.") );
				op.println( msgInfo( "0 molfile registered.") );
			}
			else {
				op.println( msgWarn( "no gif for registration.") );
				op.println( msgInfo( "0 gif registered.") );
			}
		}
		
		return list;
	}
	
	/**
	 * 登録処理
	 * DBへの登録とファイルのコピーを行い、
	 * 失敗した場合はその時点で登録処理を終了して登録前の状態にロールバックする。
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param list 登録に必要な情報のリスト<化合物, マスターファイル名>
	 * @param dataPath 構造式登録情報パス
	 * @param regPath 構造式パス
	 * @param registInfo プリフィックスと構造式登録時の最初のIDを格納した配列
	 * @param cgiUrl SubstructureSearch登録用URL
	 * @param cgiParam SubstructureSearch登録用パラメータ
	 * @return 結果
	 * @throws IOException
	 */
	private boolean regist(DatabaseAccess db, JspWriter op, 
	                       TreeMap<String, String> list, String dataPath, String regPath, String[] registInfo, String cgiUrl, String cgiParam) throws IOException {
		
		boolean isSuccess = true;
		DecimalFormat idFormat = null;
		if (registInfo[0].length() == 3) {
			idFormat = new DecimalFormat("'" + registInfo[0] + "'00000");
		}
		else {
			idFormat = new DecimalFormat("'" + registInfo[0] + "'000000");
		}
		NumberFormat nf = NumberFormat.getNumberInstance();
		String sql = "";
		String id = "";
		String compound = "";
		String masterName = "";
		File masterFileName = null;
		File registFileName = null;
		int startId = Integer.parseInt(registInfo[1]);
		int nextId = startId;
		boolean isMolRegist = (dataPath.indexOf(MOLDATA_DIR_NAME) != -1);
		ArrayList<File> copiedFiles = new ArrayList<File>();
		
		// 登録処理
		try {
			db.executeUpdate( "START TRANSACTION;" );
			for (Iterator<String> i=list.keySet().iterator(); i.hasNext();) {
				// DBへの登録
				id = idFormat.format(nextId).toString();
				compound = (String)i.next();
				masterName = (String)list.get(compound);
				sql = "INSERT INTO MOLFILE (FILE, NAME) "
				    + "VALUES('" + id + "', '" + Sanitizer.sql(compound) + "');";
				db.executeUpdate(sql);
				
				// ファイルコピー
				masterFileName = new File(dataPath + File.separator + masterName);
				if ( isMolRegist ) {
					registFileName = new File(regPath + File.separator + id + MOL_EXTENSION);
				}
				FileUtils.copyFile(masterFileName, registFileName);
				copiedFiles.add(registFileName);
				nextId++;
			}
			db.executeUpdate("COMMIT;");
		}
		catch (SQLException e) {
			try { db.executeUpdate("COMMIT;"); } catch (SQLException ee) {}
			Logger.getLogger("global").severe( "SQL : " + sql );
			e.printStackTrace();
			isSuccess = false;
		}
		catch (IOException e) {
			Logger.getLogger("global").severe( "\"" + masterFileName.getPath() + "\" to \"" + registFileName.getPath() + "\" copy failed." );
			e.printStackTrace();
			isSuccess = false;
		}
		
		if ( isSuccess ) {
			// 正常登録処理
			
			// StructureSearch 登録処理
			boolean tmpRet = execCgi( cgiUrl, cgiParam );
			if ( !tmpRet ) {
				Logger.getLogger("global").severe( "cgi execute failed." + NEW_LINE +
				                                   "    url : " + cgiUrl + NEW_LINE +
				                                   "    param : " + cgiParam );
				op.println( msgWarn( "Substructure Search update failed.(inconsistent)") );
			}
			
			if ( isMolRegist ) {
				op.println( msgInfo( nf.format(list.size()) + " molfile registered.") );
			}
			else {
				op.println( msgInfo( nf.format(list.size()) + " gif registered.") );
			}
		}
		else {
			// 登録失敗処理（ロールバック）
			
			// 登録ファイル削除
			for (File delFile : copiedFiles) {
				FileUtils.deleteQuietly(delFile);
			}
			
			// ロールバック用SQL作成
			StringBuilder rollBack = new StringBuilder( "DELETE FROM MOLFILE WHERE FILE IN(\"" );
			for (int i=startId; i<=nextId; i++) {
				id = idFormat.format(nextId).toString();
				rollBack.append( id );
				if (i<nextId) {
					rollBack.append( "\", \"" );
				}
			}
			rollBack.append( "\");" );
			sql = rollBack.toString();
			try {
				db.executeUpdate(sql);
			}
			catch (SQLException e) {
				Logger.getLogger("global").severe( "SQL : " + sql );
				e.printStackTrace();
				op.println( msgErr( "rollback(sql) failed." ) );
			}
		}
		
		return isSuccess;
	}
	
	/**
	 * CGI実行
	 * @param strUrl CGIのURL
	 * @param strParam CGIに渡すパラメータ
	 * @return 結果
	 */
	private boolean execCgi( String strUrl, String strParam ) {
		boolean ret = true;
		PrintStream ps = null;
		BufferedReader in = null;
		try {
			URL url = new URL( strUrl );
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			ps = new PrintStream( con.getOutputStream() );
			ps.print( strParam );
			in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			StringBuilder retStr = new StringBuilder();
			while ( (line = in.readLine()) != null ) {
				retStr.append( line + NEW_LINE );
			}
			if (retStr.indexOf("OK") == -1) {
				ret = false;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}
		finally {
			try {
				if (in != null) { in.close(); }
				if (ps != null) { ps.close(); }
			}
			catch (IOException e) {
			}
		}
		return ret;
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
	<title>Admin | Structure Registration</title>
	<script language="javascript" type="text/javascript">
<!--
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
//-->
	</script>
</head>
<body>
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Structure Registration</h2>
<%
	//---------------------------------------------
	// 各種パラメータ取得および設定
	//---------------------------------------------
	request.setCharacterEncoding("utf-8");
	final String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	final String dbRootPath = MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
	final String molRootPath = MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH);
	final String dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
	final String tomcatTmpPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_TEMP_PATH);
	final String tmpPath = (new File(tomcatTmpPath + sdf.format(new Date()))).getPath();
	GetConfig conf = new GetConfig(baseUrl);
	OperationManager om = OperationManager.getInstance();
	String selDbName = "";
	FileUpload up = null;
	boolean isResult = true;
	String upFileName = "";
	boolean upResult = false;
	DatabaseAccess db = null;
	StringBuilder dbArgs = new StringBuilder();
	boolean isTmpRemove = true;
	
	try {
		//----------------------------------------------------
		// ファイルアップロード時の初期化処理
		//----------------------------------------------------
		if ( FileUpload.isMultipartContent(request) ) {
			(new File(tmpPath)).mkdir();
			String os = System.getProperty("os.name");
			if(os.indexOf("Windows") == -1){
				isResult = FileUtil.changeMode("777", tmpPath);
				if ( !isResult ) {
					out.println( msgErr( "[" + tmpPath + "]&nbsp;&nbsp; chmod failed.") );
					return;
				}
			}
			up = new FileUpload(request, tmpPath);
		}
		
		//----------------------------------------------------
		// 存在するDB名取得（ディレクトリによる判定）
		//----------------------------------------------------
		List<String> dbNameList = Arrays.asList(conf.getDbName());
		ArrayList<String> dbNames = new ArrayList<String>();
		dbNames.add("");
		File[] dbDirs = (new File( dbRootPath )).listFiles();
		if ( dbDirs != null ) {
			for ( File dbDir : dbDirs ) {
				if ( dbDir.isDirectory() ) {
					int pos = dbDir.getName().lastIndexOf("\\");
					String dbDirName = dbDir.getName().substring( pos + 1 );
					pos = dbDirName.lastIndexOf("/");
					dbDirName = dbDirName.substring( pos + 1 );
					if (dbNameList.contains(dbDirName)) {
						// DBディレクトリが存在し、massbank.confに記述があるDBのみ有効とする
						dbNames.add( dbDirName );
						// StructureSearch登録用
						dbArgs.append( dbDirName ).append( "," );
					}
				}
			}
			if ( dbArgs.length() > 0 ) {
				dbArgs.substring(0, dbArgs.length() - 1);
			}
		}
		if (dbDirs == null || dbNames.size() == 0) {
			out.println( msgErr( "[" + dbRootPath + "]&nbsp;&nbsp;directory not exist.") );
			return;
		}
		Collections.sort(dbNames);
		
		//----------------------------------------------------
		// DB選択状態
		//----------------------------------------------------
		if ( FileUpload.isMultipartContent(request) ) {
			HashMap<String, String[]> reqParamMap = new HashMap<String, String[]>();
			reqParamMap = up.getRequestParam();
			if (reqParamMap != null) {
				for (Map.Entry<String, String[]> req : reqParamMap.entrySet()) {
					if ( req.getKey().equals("db") ) {
						selDbName = req.getValue()[0];
						break;
					}
				}
			}
		}
		else {
			selDbName = request.getParameter("db");
		}
		if ( selDbName == null || selDbName.equals("") || !dbNames.contains(selDbName) ) {
			selDbName = dbNames.get(0);
		}
		
		//----------------------------------------------------
		// 構造式パス設定
		//----------------------------------------------------
		final String molPath = (new File(molRootPath + "/" + selDbName)).getPath();
		
		//---------------------------------------------
		// フォーム表示
		//---------------------------------------------
		out.println( "<form name=\"formMain\" action=\"./StructureRegist.jsp\" enctype=\"multipart/form-data\" method=\"post\" onSubmit=\"doWait()\">" );
		out.println( "\t<span class=\"baseFont\">Database :</span>&nbsp;" );
		out.println( "\t<select name=\"db\" class=\"db\" onChange=\"selDb()\">" );
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
		out.println( "\t</select><br><br>" );
		out.println( "\t<span class=\"baseFont\">Structure Archive :</span>&nbsp;" );
		out.println( "\t<input type=\"file\" name=\"file\" size=\"70\">&nbsp;<input type=\"submit\" value=\"Registration\"><br>" );
		out.println( "\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "\t&nbsp;<span class=\"note\">* please specify your <a href=\"" + MOLDATA_ZIP_URL + "\">" + UPLOAD_MOLFILE_ZIP + "</a> or " + UPLOAD_MOLFILE_MSBK + ".</span><br>" );
		out.println( "</form>" );
		out.println( "<hr><br>" );
		if ( !FileUpload.isMultipartContent(request) ) {
			return;
		}
		else {
			isResult = om.startOparation(om.P_STRUCTURE, om.TP_UPDATE, selDbName);
			if ( !isResult ) {
				out.println( msgWarn( "other users are updating. please access later. ") );
				return;
			}
			if ( selDbName.equals("") ) {
				out.println( msgErr( "please select database.") );
				return;
			}
		}
		
		//---------------------------------------------
		// ファイルアップロード
		//---------------------------------------------
		HashMap<String, Boolean> upFileMap = up.doUpload();
		if ( upFileMap != null ) {
			for (Map.Entry<String, Boolean> e : upFileMap.entrySet()) {
				upFileName = e.getKey();
				upResult = e.getValue();
				break;
			}
			if ( upFileName.equals("") ) {
				out.println( msgErr( "please select file.") );
				isResult = false;
			}
			else if ( !upResult ) {
				out.println( msgErr( "[" + upFileName + "]&nbsp;&nbsp;upload failed.") );
				isResult = false;
			}
			else if ( !upFileName.equals(UPLOAD_MOLFILE_ZIP) && !upFileName.equals(UPLOAD_MOLFILE_MSBK) ) {
				out.println( msgErr( "please select&nbsp;&nbsp;[" + UPLOAD_MOLFILE_ZIP + "]&nbsp;&nbsp;or&nbsp;&nbsp;[" + UPLOAD_MOLFILE_MSBK + "].") );
				up.deleteFile( upFileName );
				isResult = false;
			}
		}
		else {
			out.println( msgErr( "server error.") );
			isResult = false;
		}
		up.deleteFileItem();
		if ( !isResult ) {
			return;
		}
		
		//---------------------------------------------
		// アップロードファイルの解凍処理
		//---------------------------------------------
		final String upFilePath = (new File(tmpPath + "/" + upFileName)).getPath();
		isResult = FileUtil.unZip(upFilePath, tmpPath);
		if ( !isResult ) {
			out.println( msgErr( "[" + upFileName + "]&nbsp;&nbsp; extraction failed. possibility of time-out.") );
			return;
		}
		
		//---------------------------------------------
		// アップロードファイル格納ディレクトリ存在確認
		//---------------------------------------------
		File tmpMolDir = new File(molPath);
		if ( !tmpMolDir.isDirectory() ) {
			tmpMolDir.mkdirs();
		}
		
		//---------------------------------------------
		// アップロードファイル名を元に各パスを設定
		//---------------------------------------------
		final String molDataPath = (new File(tmpPath + "/" + MOLDATA_DIR_NAME)).getPath();
		String listTsvPath = (new File(molDataPath + "/" + LIST_FILE_NAME)).getPath();
		
		//---------------------------------------------
		// 解凍ファイルチェック処理
		//---------------------------------------------
		// dataディレクトリ存在チェック
		if ( (new File( molDataPath )).isDirectory() ) {
			// list.tsvファイル存在チェック
			if ( !(new File( listTsvPath )).isFile() ) {
				out.println( msgErr( "[" + LIST_FILE_NAME + "]&nbsp;&nbsp; not included in the up-loading file.") );
				isResult = false;
			}
			else if ( (new File( listTsvPath )).length() == 0 ) {
				out.println( msgErr( "[" + LIST_FILE_NAME + "]&nbsp;&nbsp; is empty in upload file.") );
				isResult = false;
			}
			// ファイル拡張子チェック
			for ( String fileName : (new File(molDataPath)).list() ) {
				String extType = "";
				if ( upFileName.equals(UPLOAD_MOLFILE_ZIP) || upFileName.equals(UPLOAD_MOLFILE_MSBK) ) {
					extType = MOL_EXTENSION;
				}
				
				if ( fileName.lastIndexOf(".") == -1 ) {
					isResult = false;
				}
				else {
					String suffix = fileName.substring(fileName.lastIndexOf("."));
					if ( !suffix.equals(".tsv") && !suffix.equals(extType) ) {
						isResult = false;
					}
				}
				if ( !isResult ) {
					out.println( msgErr( "file extension other than&nbsp;&nbsp;[.tsv]&nbsp;&nbsp;and"
					           + "&nbsp;&nbsp;[" + extType + "]&nbsp;&nbsp;included in upload file.") );
					break;
				}
			}
		}
		else {
			out.println( msgErr( "[" + MOLDATA_DIR_NAME + "]&nbsp;&nbsp; directory not exists in upload file.") );
			isResult = false;
		}
		if ( !isResult ) {
			return;
		}
		
		//---------------------------------------------
		// DB接続
		//---------------------------------------------
		db = new DatabaseAccess(dbHostName, selDbName);
		isResult = db.open();
		if ( !isResult ) {
			db.close();
			out.println( msgErr( "not connect to database.") );
			return;
		}
		
		//---------------------------------------------
		// 構造式登録処理（初期化）
		//---------------------------------------------
		String[] registInfo = initRegist(db, out);
		if (registInfo == null ) {
			return;
		}
		
		//---------------------------------------------
		// 構造式登録処理（list.tsv読み込みおよびチェック）
		//---------------------------------------------
		TreeMap<String, String> list = check(db, out, listTsvPath, molDataPath);
		if (list.size() == 0) {
			return;
		}
		
		//---------------------------------------------
		// StructureSearch登録用パラメータ生成
		//---------------------------------------------
		String[] urlList = conf.getSiteUrl();
		String cgiUrl = urlList[GetConfig.MYSVR_INFO_NUM] + "cgi-bin/GenSubstructure.cgi";
		String cgiParam = "db=" + dbArgs.toString();
		
		//---------------------------------------------
		// 構造式登録処理（登録）
		//---------------------------------------------
		isResult = regist(db, out, list, molDataPath, molPath, registInfo, cgiUrl, cgiParam);
		if ( !isResult ) {
			Logger.getLogger("global").severe( "registration failed." + NEW_LINE +
			                                   "    registration file path : " + tmpPath );
			out.println( msgErr( "registration failed. refer to&nbsp;&nbsp;[" + tmpPath + "]." ) );
			
			if ( molDataPath.indexOf(MOLDATA_DIR_NAME) != -1 ) {
				out.println( msgInfo( "0 molfile registered.") );
			}
			else {
				out.println( msgInfo( "0 gif registered.") );
			}
			isTmpRemove = false;
		}
	}
	finally {
		om.endOparation(om.P_STRUCTURE, om.TP_UPDATE, selDbName);
		if ( db != null ) {
			db.close();
		}
		File tmpDir = new File(tmpPath);
		if ( isTmpRemove && tmpDir.exists() ) {
			FileUtil.removeDir( tmpDir.getPath() );
		}
		if ( FileUpload.isMultipartContent(request) ) {
			out.println( msgInfo( "Done." ) );
		}
		out.println( "</body>" );
		out.println( "</html>" );
	}
%>
