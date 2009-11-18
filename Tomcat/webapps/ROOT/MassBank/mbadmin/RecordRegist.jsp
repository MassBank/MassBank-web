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
 * レコード登録
 *
 * ver 1.0.4 2009.11.18
 *
 ******************************************************************************/
%>

<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.PrintStream" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="massbank.admin.AdminCommon" %>
<%@ page import="massbank.admin.DatabaseAccess" %>
<%@ page import="massbank.admin.FileUtil" %>
<%@ page import="massbank.admin.OperationManager" %>
<%@ page import="massbank.admin.SqlFileGenerator" %>
<%@ page import="massbank.FileUpload" %>
<%@ page import="massbank.GetConfig" %>
<%!
	/** 作業ディレクトリ用日時フォーマット */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_SSS");
	
	/** 改行文字列 */
	private final String NEW_LINE = System.getProperty("line.separator");
	
	/** アップロードMolfile名（チェック用） */
	private final String UPLOAD_RECORD_NAME = "recdata.zip";
	
	/** レコードデータディレクトリ名 */
	private final String RECDATA_DIR_NAME = "recdata";
	
	/** レコード拡張子 */
	private final String REC_EXTENSION = ".txt";
	
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
	 * HTML非表示用メッセージテンプレート
	 * ブラウザタイムアウト防止用
	 * @param msg メッセージ
	 * @return 非表示用メッセージ
	 */
	private String msgHid(String msg) {
		return "<input type=\"hidden\" name=\"log\" value=\"" + msg + "\">";
	}
	
	/**
	 * チェック処理
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param dataPath 登録対象レコードパス
	 * @param registPath 登録先パス
	 * @return 登録可能ファイル名リスト
	 * @throws IOException 入出力例外
	 */
	private ArrayList<String> checkRecord(DatabaseAccess db, JspWriter op, String dataPath, String registPath) throws IOException {
		
		final String[] dataList = (new File(dataPath)).list();
		ArrayList<String> regFileList = new ArrayList<String>();
		HashMap<String, String> tmpMap = new HashMap<String, String>();
		
		if ( dataList.length == 0 ) {
			op.println( msgWarn( "no file for registration." ) );
			return regFileList;
		}
		
		//----------------------------------------------------
		// レコードファイル必須項目、必須項目値チェック処理
		//----------------------------------------------------
		final String[] requiredList = {
		    "ACCESSION: ", "RECORD_TITLE: ", "DATE: ", "AUTHORS: ", "COPYRIGHT: ", "CH$NAME: ", "CH$COMPOUND_CLASS: ", "CH$FORMULA: ",
		    "CH$EXACT_MASS: ", "CH$SMILES: ", "CH$IUPAC: ", "AC$INSTRUMENT: ", "AC$INSTRUMENT_TYPE: ",
		    "AC$ANALYTICAL_CONDITION: MODE ", "PK$NUM_PEAK: ", "PK$PEAK: m/z int. rel.int."
		};
		for (int i=0; i<dataList.length; i++) {
			String name = dataList[i];
			boolean isStatus = false;
			
			// 読み込みファイルチェック
			File file = new File(dataPath + name);
			if ( file.isDirectory() ) {
				// ディレクトリの場合
				op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is directory." ) );
				continue;
			}
			else if ( file.isHidden() ) {
				// 隠しファイルの場合
				op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is hidden." ) );
				continue;
			}
			else if ( name.lastIndexOf(REC_EXTENSION) == -1 ) {
				// ファイル拡張子不正の場合
				op.println( msgWarn( "file extension of&nbsp;&nbsp;[" + name + "]&nbsp;&nbsp;is not&nbsp;&nbsp;[" + REC_EXTENSION + "]." ) );
				continue;
			}
			
			// 読み込み
			ArrayList<String> fileContents = new ArrayList<String>();
			String line = "";
			BufferedReader br = null;
			try {
					br = new BufferedReader(new FileReader(file));
					while ((line = br.readLine()) != null) {
						fileContents.add(line);
					}
			}
			catch (IOException e) {
				Logger.global.severe( "file read failed." + NEW_LINE +
				                      "    " + file.getPath() );
				e.printStackTrace();
				op.println( msgErr( "server error." ) );
				return regFileList;
			}
			finally {
				try {
					if ( br != null ) { br.close(); }
				}
				catch (IOException e) {
				}
			}
			
			//----------------------------------------------------
			// 必須項目に対するメインチェック処理
			//----------------------------------------------------
			String idStr = "";
			for (int j=0; j<requiredList.length; j++ ) {
				isStatus = false;
				String requiredStr = requiredList[j];
				ArrayList<String> valStrs = new ArrayList<String>();	// 値
				boolean findRequired = false;							// 必須項目検出フラグ
				boolean findValue = false;								// 値検出フラグ
				boolean isPeakMode = false;								// ピーク情報検出モード
				for ( int k=0; k<fileContents.size(); k++ ) {
					String lineStr = fileContents.get(k);
					
					// 終了タグ以降は無効（必須項目検出対象としない）
					if ( lineStr.indexOf("//") != -1 ) {
						break;
					}
					// 値（ピーク情報）検出（終了タグまでを全てピーク情報とする）
					else if ( isPeakMode ) {
						findRequired = true;
						if ( !lineStr.trim().equals("") ) {
							findValue = true;
							valStrs.add(lineStr);
						}
					}
					// 必須項目が見つかった場合
					else if ( lineStr.indexOf(requiredStr) != -1 ) {
						// 必須項目検出
						findRequired = true;
						if ( requiredStr.equals("PK$PEAK: m/z int. rel.int.") ) {
							isPeakMode = true;
						}
						else {
							// 値検出
							String tmpVal = lineStr.replace(requiredStr, "");
							if ( !tmpVal.trim().equals("") ) {
								findValue = true;
								valStrs.add(tmpVal);
							}
							break;
						}
					}
				}
				if ( !findRequired ) {
					// 必須項目が見つからない場合
					op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is required item not exist." ) );
					break;
				}
				else {
					if ( !findValue ) {
						// 値が存在しない場合
						op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is required item value not exist." ) );
						break;
					}
					else {
						// 値が存在する場合
						
						//----------------------------------------------------
						// 各値チェック
						//----------------------------------------------------
						String val = valStrs.get(0);
						// ACESSION
						if ( requiredStr.equals("ACCESSION: ") ) {
							if ( !val.equals(name.replace(REC_EXTENSION, "")) ) {
								op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is file name or required item value illegal." ) );
								break;
							}
							if ( val.length() != 8 ) {
								op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is required item value illegal." ) );
								break;
							}
							idStr = val;
						}
						// PK$NUM_PEAK
						else if ( requiredStr.equals("PK$NUM_PEAK: ") ) {
							try {
								Integer.parseInt(val);
							}
							catch (NumberFormatException e) {
								op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is required item value illegal." ) );
								break;
							}
						}
						// PK$PEAK: m/z int. rel.int.
						else if ( requiredStr.equals("PK$PEAK: m/z int. rel.int.") ) {
							String peak = "";
							String mz = "";
							String intensity = "";
							boolean mzDuplication = false;
							boolean mzNotNumeric = false;
							boolean intensityNotNumeric = false;
							boolean invalidFormat = false;
							HashSet<String> mzSet = new HashSet<String>();
							for ( int l=0; l<valStrs.size(); l++ ) {
								peak = valStrs.get(l).trim();
								if ( peak.indexOf(" ") != -1 ) {
									mz = peak.split(" ")[0];
									if ( !mzSet.add(mz) ) {
										mzDuplication = true;
										break;
									}
									try {
										Double.parseDouble(mz);
									}
									catch (NumberFormatException e) {
										mzNotNumeric = true;
										break;
									}
									intensity = peak.split(" ")[1];
									try {
										Double.parseDouble(intensity);
									}
									catch (NumberFormatException e) {
										intensityNotNumeric = true;
										break;
									}
								}
								else {
									invalidFormat = true;
									break;
								}
							}
							if ( mzDuplication || mzNotNumeric || intensityNotNumeric || invalidFormat ) {
								op.println( msgWarn( "[" + name + "]&nbsp;&nbsp;is required item value illegal." ) );
								break;
							}
						}
						
						// 必須項目と値のチェック結果が正しい場合
						isStatus = true;
					}
				}
			}
			if ( isStatus ) {
				if ( !tmpMap.containsKey(idStr) ) {
					tmpMap.put(idStr, name);
				}
				else {
					op.println( msgWarn( "id&nbsp;&nbsp;[" + idStr + "]&nbsp;&nbsp;of file name&nbsp;&nbsp;[" + name + "]&nbsp;&nbsp;is duplicated." ) );
				}
			}
		}
		op.println( msgHid( "check : required item check complete." ) );
		
		//----------------------------------------------------
		// 登録済みチェック処理
		//----------------------------------------------------
		// 登録済みIDリスト生成（DB）
		HashSet<String> regIdList = new HashSet<String>();
		String[] sqls = { "SELECT ID FROM SPECTRUM ORDER BY ID",
		                  "SELECT ID FROM RECORD ORDER BY ID",
		                  "SELECT ID FROM PEAK GROUP BY ID ORDER BY ID",
		                  "SELECT ID FROM CH_NAME ID ORDER BY ID",
		                  "SELECT ID FROM CH_LINK ID ORDER BY ID",
		                  "SELECT ID FROM TREE WHERE ID IS NOT NULL AND ID<>'' ORDER BY ID" };
		for ( int i=0; i<sqls.length; i++ ) {
			String execSql = sqls[i];
			ResultSet rs = null;
			try {
				rs = db.executeQuery(execSql);
				while ( rs.next() ) {
					String idStr = rs.getString("ID");
					regIdList.add(idStr);
				}
			}
			catch (SQLException e) {
				Logger.global.severe( "    sql : " + execSql );
				e.printStackTrace();
				op.println( msgErr( "database access error." ) );
				return regFileList;
			}
			finally {
				try {
					if ( rs != null ) { rs.close(); }
				}
				catch (SQLException e) {}
			}
		}
		// 登録済みIDリスト生成（レコードファイル）
		final String[] recFileList = (new File(registPath)).list();
		for (int i=0; i<recFileList.length; i++) {
			String name = recFileList[i];
			File file = new File(registPath + File.separator + name);
			if ( !file.isFile() || file.isHidden() || name.lastIndexOf(REC_EXTENSION) == -1 ) {
				continue;
			}
			String idStr = name.replace(REC_EXTENSION, "");
			regIdList.add(idStr);
		}
		
		// 登録済みチェック
		for (Map.Entry<String, String> e : tmpMap.entrySet()) {
			String fileId = e.getKey();
			String fileName = e.getValue();
			if ( !regIdList.contains(fileId) ){
				regFileList.add(fileName);
			}
			else {
				op.println( msgWarn( "id&nbsp;&nbsp;[" + fileId + "]&nbsp;&nbsp;of file name&nbsp;&nbsp;[" + fileName + "]&nbsp;&nbsp;exists already in databese." ) );
			}
		}
		op.println( msgHid( "check : database check complete." ) );
		
		return regFileList;
	}
	
	/**
	 * 登録処理
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param conf 設定情報
	 * @param hostName ホスト名
	 * @param tmpPath 一時ディレクトリパス
	 * @param dataFileList 登録対象レコードリスト
	 * @param registPath レコード格納パス
	 * @param selDbName DB
	 * @param baseUrl ベースURL
	 * @param copiedFiles コピー済みファイルパス格納用
	 * @return 結果
	 * @throws IOException
	 */
	private boolean registRecord(DatabaseAccess db, JspWriter op, GetConfig conf, String hostName, 
	                             String tmpPath, ArrayList<String> dataFileList, String registPath, String selDbName, String baseUrl, ArrayList<File> copiedFiles) throws IOException {
		
		boolean ret = true;
		final String[] urlList = conf.getSiteUrl();
		final String cgiUrl = urlList[GetConfig.MYSVR_INFO_NUM] + "cgi-bin/";
		final String cgiPeakUrl = cgiUrl + "GenPeakSql.cgi";
		final String cgiTreeUrl = cgiUrl + "GenTreeSql.cgi";
		final String cgiHeapUrl = cgiUrl + "CreateHeap.cgi";
		final String baseParam = "src_dir=" + registPath + "&out_dir=" + tmpPath + "&db=" + selDbName;
		final String[] sqlSuffixRec = new String[]{ "_RECORD.sql", "_CH_NAME.sql", "_CH_LINK.sql", "_INSTRUMENT.sql" };
		final String[] sqlSuffixAll = new String[]{ "_PEAK.sql", sqlSuffixRec[0], sqlSuffixRec[1], sqlSuffixRec[2], sqlSuffixRec[3], "_TREE.sql" };
		
		//----------------------------------------------------
		// レコードファイルコピー処理
		//----------------------------------------------------
		File dataFile = null;
		File registFile = null;
		try {
			for (String recFile : dataFileList) {
				dataFile = new File(tmpPath + File.separator + RECDATA_DIR_NAME + File.separator + recFile);
				registFile = new File(registPath + File.separator + recFile);
				FileUtils.copyFile(dataFile, registFile);
				copiedFiles.add(registFile);
			}
		} catch (IOException e) {
			Logger.global.severe( "file copy failed." + NEW_LINE +
			                      "    " + dataFile + " to " + registFile );
			e.printStackTrace();
			return false;
		}
		op.println( msgHid( "registration : reocrd copy complete." ) );
		
		//----------------------------------------------------
		// PEAK.sql生成
		//----------------------------------------------------
		StringBuilder fname = new StringBuilder();
		for ( int i=0; i<dataFileList.size(); i++ ) {
			fname.append( dataFileList.get(i) );
			if ( i < dataFileList.size() - 1 ) {
				fname.append( "," );
			}
		}
		final String peakParam = baseParam + "&fname=" + fname.toString();
		ret = execCgi( cgiPeakUrl, peakParam );
		if ( !ret ) {
			Logger.global.severe( "cgi execute failed." + NEW_LINE +
			                      "    url : " + cgiPeakUrl + NEW_LINE +
			                      "    param : " + peakParam );
			return false;
		}
		op.println( msgHid( "registration : PEAK.sql generated." ) );
		
		//----------------------------------------------------
		// RECORD.sql, CH_NAME.sql, CH_LINK.sql, INSTRUMENT.sql生成
		//----------------------------------------------------
		PrintWriter[] pw = new PrintWriter[sqlSuffixRec.length];
		try {
			for ( int i = 0; i<sqlSuffixRec.length; i++ ) {
				String geneFileName = selDbName + sqlSuffixRec[i];
				String filePath = tmpPath + File.separator + geneFileName;
				pw[i] = new PrintWriter( new BufferedWriter(new FileWriter(filePath)) );
				pw[i].println( "START TRANSACTION;");
			}
			SqlFileGenerator sfg = new SqlFileGenerator( baseUrl, selDbName );
			for ( int i=0; i<dataFileList.size(); i++ ) {
				String filePath = registPath + File.separator + dataFileList.get(i);
				sfg.readFile( filePath );
				for ( int j=0; j<sqlSuffixRec.length; j++ ) {
					if ( sfg.isExist(j) ) {
						pw[j].println( sfg.getSql(j) );
					}
				}
			}
			for ( int i=0; i<sqlSuffixRec.length; i++ ) {
				pw[i].println( "COMMIT;");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			for ( int i=0; i<sqlSuffixRec.length; i++ ) {
				if ( pw[i] != null ) { pw[i].close(); }
			}
		}
		op.println( msgHid( "registration : RECORD.sql, CH_NAME.sql, CH_LINK.sql, INSTRUMENT.sql generated." ) );
		
		//----------------------------------------------------
		// TREE.sql生成
		//----------------------------------------------------
		final String[] tmpDbName = conf.getDbName();
		final String[] siteLongName = conf.getSiteLongName();
		String siteName = "";
		for ( int i=0; i < tmpDbName.length; i++ ) {
			if ( tmpDbName[i].equals(selDbName) ) {
				siteName = siteLongName[i];
				break;
			}
		}
		final String treeParam = baseParam + "&name=" + siteName;
		ret = execCgi( cgiTreeUrl, treeParam );
		if ( !ret ) {
			Logger.global.severe( "cgi execute failed." + NEW_LINE +
			                      "    url : " + cgiTreeUrl + NEW_LINE +
			                      "    param : " + treeParam );
			return false;
		}
		op.println( msgHid( "registration : TREE.sql generated." ) );
		
		//----------------------------------------------------
		// 生成されたすべてのSQLファイルを実行
		//----------------------------------------------------
		String insertFile = "";
		for (String suffix : sqlSuffixAll) {
			insertFile = tmpPath + selDbName + suffix;
			ret = FileUtil.execSqlFile(hostName, selDbName, insertFile);
			if ( !ret ) {
				Logger.global.severe( "sql file execute failed." + NEW_LINE +
				                      "    file : " + insertFile );
				return false;
			}
		}
		op.println( msgHid( "registration : all sql file insert complete." ) );
		
		//----------------------------------------------------
		// ヒープテーブル更新
		//----------------------------------------------------
		final String cgiParam = "dsn=" + selDbName;
		boolean tmpRet = execCgi( cgiHeapUrl, cgiParam );
		if ( !tmpRet ) {
			Logger.global.severe( "cgi execute failed." + NEW_LINE +
			                      "    url : " + cgiHeapUrl + NEW_LINE +
			                      "    param : " + cgiParam );
		}
		op.println( msgHid( "registration : heap table updated." ) );
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		op.println( msgInfo( nf.format(dataFileList.size()) + " record registered.") );
		return true;
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
	<title>Admin | Record Registration</title>
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
<h2>Record Registration</h2>
<%
	//---------------------------------------------
	// 各種パラメータ取得および設定
	//---------------------------------------------
	request.setCharacterEncoding("utf-8");
	final String reqUrl = request.getRequestURL().toString();
	final String baseUrl = reqUrl.substring( 0, (reqUrl.indexOf("/mbadmin") + 1 ) );
	final String realPath = application.getRealPath("/");
	AdminCommon admin = new AdminCommon(reqUrl, realPath);
	final String outPath = (!admin.getOutPath().equals("")) ? admin.getOutPath() : FileUpload.UPLOAD_PATH;
	final String dbRootPath = admin.getDbRootPath();
	final String dbHostName = admin.getDbHostName();
	final String tmpPath = (new File(outPath + File.separator + sdf.format(new Date()))).getPath() + File.separator;
	GetConfig conf = new GetConfig(baseUrl);
	OperationManager om = OperationManager.getInstance();
	String selDbName = "";
	FileUpload up = null;
	boolean isResult = true;
	String upFileName = "";
	boolean upResult = false;
	DatabaseAccess db = null;
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
						//DBディレクトリが存在し、massbank.confに記述があるDBのみ有効とする
						dbNames.add( dbDirName );
					}
				}
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
		
		//---------------------------------------------
		// フォーム表示
		//---------------------------------------------
		out.println( "<form name=\"formMain\" action=\"./RecordRegist.jsp\" enctype=\"multipart/form-data\" method=\"post\" onSubmit=\"doWait()\">" );
		out.println( "\t<span class=\"baseFont\">Database :</span>&nbsp;" );
		out.println( "\t<select name=\"db\" class=\"db\" onChange=\"selDb();\">" );
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
		out.println( "\t<span class=\"baseFont\">Record Archive :</span>&nbsp;" );
		out.println( "\t<input type=\"file\" name=\"file\" size=\"70\">&nbsp;<input type=\"submit\" value=\"Registration\"><br>" );
		out.println( "\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "\t&nbsp;<span class=\"note\">* please specify your <a href=\"http://www.massbank.jp/sample/recdata.zip\">recdata.zip</a>.</span><br>" );
		out.println( "</form>" );
		out.println( "<hr><br>" );
		if ( !FileUpload.isMultipartContent(request) ) {
			return;
		}
		else {
			isResult = om.startOparation(om.P_RECORD, om.TP_UPDATE, selDbName);
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
			else if ( !upFileName.equals(UPLOAD_RECORD_NAME) ) {
				out.println( msgErr( "prease select&nbsp;&nbsp;[" + UPLOAD_RECORD_NAME + "].") );
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
		final String upFilePath = (new File(tmpPath + File.separator + upFileName)).getPath();
		isResult = FileUtil.unZip(upFilePath, tmpPath);
		if ( !isResult ) {
			out.println( msgErr( "[" + upFileName + "]&nbsp;&nbsp; unzip failed. possibility of time-out.") );
			return;
		}
		
		//---------------------------------------------
		// アップロードファイル格納ディレクトリ存在確認
		//---------------------------------------------
		final String recPath = (new File(dbRootPath + File.separator + selDbName)).getPath();
		File tmpRecDir = new File(recPath);
		if ( !tmpRecDir.isDirectory() ) {
			tmpRecDir.mkdirs();
		}
		
		//---------------------------------------------
		// 解凍ファイルチェック処理
		//---------------------------------------------
		// dataディレクトリ存在チェック
		final String recDataPath = (new File(tmpPath + File.separator + RECDATA_DIR_NAME)).getPath() + File.separator;
		if ( (new File( recDataPath )).isDirectory() ) {
			// ファイル拡張子チェック
			for ( String fileName : (new File(recDataPath)).list() ) {
				if ( fileName.lastIndexOf(".") == -1 ) {
					isResult = false;
				}
				else {
					String suffix = fileName.substring(fileName.lastIndexOf("."));
					if ( !suffix.equals(REC_EXTENSION) ) {
						isResult = false;
					}
				}
				if ( !isResult ) {
					out.println( msgErr( "file extension other than&nbsp;&nbsp;[" + REC_EXTENSION + "]&nbsp;&nbsp;included in upload file.") );
					break;
				}
			}
		}
		else {
			out.println( msgErr( "[" + RECDATA_DIR_NAME + "]&nbsp;&nbsp; directory is not included in the up-loading file.") );
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
		// ロールバック用DBダンプ処理
		//---------------------------------------------
		String dumpPath = tmpPath + selDbName + ".dump";
		String[] tables = new String[]{"SPECTRUM", "RECORD", "PEAK", "CH_NAME", "CH_LINK", "TREE", "INSTRUMENT"};
		isResult = FileUtil.execSqlDump(dbHostName, selDbName, tables, dumpPath);
		if ( !isResult ) {
			Logger.global.severe( "sqldump failed." + NEW_LINE +
			                      "    dump file : " + dumpPath );
			out.println( msgErr( "dump failed." ) );
			out.println( msgInfo( "0 record registered." ) );
			return;
		}
		
		//---------------------------------------------
		// チェック処理
		//---------------------------------------------
		ArrayList<String> regFileList = checkRecord(db, out, recDataPath, recPath);
		if ( regFileList.size() == 0 ) {
			out.println( msgInfo( "0 record registered." ) );
			return;
		}
		
		//---------------------------------------------
		// 登録処理
		//---------------------------------------------
		ArrayList<File> copiedFiles = new ArrayList<File>();	// ロールバック（登録ファイル削除）用
		isResult = registRecord(db, out, conf, dbHostName, tmpPath, regFileList, recPath, selDbName, baseUrl, copiedFiles);
		if ( !isResult ) {
			Logger.global.severe( "registration failed." + NEW_LINE +
			                      "    registration file path : " + tmpPath );
			out.println( msgErr( "registration failed. refer to&nbsp;&nbsp;[" + tmpPath + "]." ) );
			out.println( msgInfo( "0 record registered." ) );
			isTmpRemove = false;
			
			//---------------------------------------------
			// ロールバック処理
			//---------------------------------------------
			
			// ロールバック（登録ファイル削除）
			try {
				for (File delFile : copiedFiles) {
					FileUtils.forceDelete(delFile);
				}
			}
			catch (IOException e) {
				Logger.global.severe( "rollback(file delete) failed." );
				e.printStackTrace();
				out.println( msgErr( "rollback failed." ) );
			}
			
			// ロールバック（mysqldump）
			isResult = FileUtil.execSqlFile(dbHostName, selDbName, dumpPath);
			if ( !isResult ) {
				Logger.global.severe( "rollback(sqldump) failed." );
				out.println( msgErr( "rollback failed." ) );
			}
			return;
		}
	}
	finally {
		if ( db != null ) {
			db.close();
		}
		File tmpDir = new File(tmpPath);
		if ( isTmpRemove && tmpDir.exists() ) {
			FileUtil.removeDir( tmpDir.getPath() );
		}
		if ( FileUpload.isMultipartContent(request) ) {
			om.endOparation(om.P_RECORD, om.TP_UPDATE, selDbName);
			out.println( msgInfo( "Done." ) );
		}
		out.println( "</body>" );
		out.println( "</html>" );
	}
%>
