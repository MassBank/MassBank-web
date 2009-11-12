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
 * レコードチェック
 *
 * ver 1.0.4 2009.09.10
 *
 ******************************************************************************/
%>

<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="massbank.admin.AdminCommon" %>
<%@ page import="massbank.admin.DatabaseAccess" %>
<%@ page import="massbank.admin.FileUtil" %>
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
	
	/** レコード値デフォルト */
	private final String DEFAULT_VALUE = "N/A";
	
	/** ステータス（OK） */
	private final String STATUS_OK = "<span class=\"msgFont\">ok</span>";
	
	/** ステータス（警告） */
	private final String STATUS_WARN = "<span class=\"warnFont\">warn</span>";
	
	/** ステータス（エラー） */
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
	 * チェック処理
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param dataPath チェック対象レコードパス
	 * @param registPath 登録先予定パス
	 * @return チェック結果Map<ファイル名, 画面表示用タブ区切り文字列>
	 * @throws IOException 入出力例外
	 */
	private TreeMap<String, String> validationRecord(DatabaseAccess db, JspWriter op, String dataPath, String registPath) throws IOException {
		
		op.println( msgInfo( "validation archive is&nbsp;&nbsp;[" + UPLOAD_RECORD_NAME + "].") );
		
		final String[] dataList = (new File(dataPath)).list();
		TreeMap<String, String> validationMap = new TreeMap<String, String>();
		
		if ( dataList.length == 0 ) {
			op.println( msgWarn( "no file for validation." ) );
			return validationMap;
		}
		
		//----------------------------------------------------
		// レコードファイル必須項目、必須項目値チェック処理
		//----------------------------------------------------
		final String[] requiredList = {
		    "ACCESSION: ", "RECORD_TITLE: ", "DATE: ", "AUTHORS: ", "COPYRIGHT: ", "CH$NAME: ", "CH$COMPOUND_CLASS: ", "CH$FORMULA: ",
		    "CH$EXACT_MASS: ", "CH$SMILES: ", "CH$IUPAC: ", "AC$INSTRUMENT: ", "AC$INSTRUMENT_TYPE: ",
		    "AC$ANALYTICAL_CONDITION: MS_TYPE ", "AC$ANALYTICAL_CONDITION: MODE ", "PK$NUM_PEAK: ", "PK$PEAK: m/z int. rel.int."
		};
		for (int i=0; i<dataList.length; i++) {
			String name = dataList[i];
			String status = "";
			StringBuilder detailsErr = new StringBuilder();
			StringBuilder detailsWarn = new StringBuilder();
			
			// 読み込み対象チェック処理
			File file = new File(dataPath + name);
			if ( file.isDirectory() ) {
				// ディレクトリの場合
				status = STATUS_ERR;
				detailsErr.append( "<span class=\"errFont\">[" + name + "]&nbsp;&nbsp;is directory.</span><br />" );
				validationMap.put(name, status + "\t" + detailsErr.toString());
				continue;
			}
			else if ( file.isHidden() ) {
				// 隠しファイルの場合
				status = STATUS_ERR;
				detailsErr.append( "<span class=\"errFont\">[" + name + "]&nbsp;&nbsp;is hidden.</span><br />" );
				validationMap.put(name, status + "\t" + detailsErr.toString());
				continue;
			}
			else if ( name.lastIndexOf(REC_EXTENSION) == -1 ) {
				// ファイル拡張子不正の場合
				status = STATUS_ERR;
				detailsErr.append( "<span class=\"errFont\">file extension of&nbsp;&nbsp;[" + name + "]&nbsp;&nbsp;is not&nbsp;&nbsp;[" + REC_EXTENSION + "].</span><br />" );
				validationMap.put(name, status + "\t" + detailsErr.toString());
				continue;
			}
			
			// 読み込み
			boolean isEndTagRead = false;
			boolean isInvalidInfo = false;
			boolean isDoubleByte = false;
			ArrayList<String> fileContents = new ArrayList<String>();
			String line = "";
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					if ( isEndTagRead ) {
						if ( !line.equals("") ) {
							isInvalidInfo = true;
						}
					}
					if ( line.indexOf("//") != -1 ) {
						// 終了タグ検出時フラグセット
						isEndTagRead = true;
					}
					fileContents.add(line);
					if ( !isDoubleByte ) {
						// 全角文字混入チェック
						byte[] bytes = line.getBytes("MS932");
						if ( bytes.length != line.length() ) {
							isDoubleByte = true;
						}
					}
				}
			}
			catch (IOException e) {
				Logger.global.severe( "file read failed." + NEW_LINE +
				                      "    " + file.getPath() );
				e.printStackTrace();
				op.println( msgErr( "server error." ) );
				validationMap.clear();
				return validationMap;
			}
			finally {
				try {
					if ( br != null ) { br.close(); }
				}
				catch (IOException e) {
				}
			}
			if ( isInvalidInfo ) {
				// 終了タグ以降の記述がある場合
				if ( status.equals("") ) status = STATUS_WARN;
				detailsWarn.append( "<span class=\"warnFont\">invalid after the end tag&nbsp;&nbsp;[//].</span><br />" );
			}
			if ( isDoubleByte ) {
				// 全角文字が混入している場合
				if ( status.equals("") ) status = STATUS_WARN;
				detailsWarn.append( "<span class=\"warnFont\">double-byte character included.</span><br />" );
			}
			
			//----------------------------------------------------
			// 必須項目に対するメインチェック処理
			//----------------------------------------------------
			int peakNum = 0;
			for (int j=0; j<requiredList.length; j++ ) {
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
								valStrs.add( tmpVal );
							}
							break;
						}
					}
				}
				if ( !findRequired ) {
					// 必須項目が見つからない場合
					status = STATUS_ERR;
					detailsErr.append( "<span class=\"errFont\">no required item&nbsp;&nbsp;[" + requiredStr + "].</span><br />" );
				}
				else {
					if ( !findValue ) {
						// 値が存在しない場合
						status = STATUS_ERR;
						detailsErr.append( "<span class=\"errFont\">no value of required item&nbsp;&nbsp;[" + requiredStr + "].</span><br />" );
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
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;not correspond to file name.</span><br />" );
							}
							if ( val.length() != 8 ) {
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is 8 digits necessary.</span><br />" );
							}
						}
						// DATE
						else if ( requiredStr.equals("DATE: ") && !val.equals(DEFAULT_VALUE) ) {
							val = val.replace(".", "/");
							val = val.replace("-", "/");
							try {
								DateFormat.getDateInstance(DateFormat.SHORT, Locale.JAPAN).parse(val);
							} catch (ParseException e) {
								if ( status.equals("") ) status = STATUS_WARN;
								detailsWarn.append( "<span class=\"warnFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not date format.</span><br />" );
							}
						}
						// CH$EXACT_MASS
						else if ( requiredStr.equals("CH$EXACT_MASS: ") && !val.equals(DEFAULT_VALUE) ) {
							try {
								Double.parseDouble(val);
							}
							catch (NumberFormatException e) {
								if ( status.equals("") ) status = STATUS_WARN;
								detailsWarn.append( "<span class=\"warnFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not numeric.</span><br />" );
							}
						}
						// AC$ANALYTICAL_CONDITION: MODE
						else if ( requiredStr.equals("AC$ANALYTICAL_CONDITION: MODE ") && !val.equals(DEFAULT_VALUE) ) {
							if ( !val.equals("POSITIVE") && !val.equals("NEGATIVE") ) {
								if ( status.equals("") ) status = STATUS_WARN;
								detailsWarn.append( "<span class=\"warnFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not \"POSITIVE\" or \"NEGATIVE\".</span><br />" );
							}
						}
						// PK$NUM_PEAK
						else if ( requiredStr.equals("PK$NUM_PEAK: ") ) {
							try {
								peakNum = Integer.parseInt(val);
							}
							catch (NumberFormatException e) {
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not numeric.</span><br />" );
							}
						}
						// PK$PEAK: m/z int. rel.int.
						else if ( requiredStr.equals("PK$PEAK: m/z int. rel.int.") ) {
							if ( peakNum != valStrs.size() ) {
								if ( status.equals("") ) status = STATUS_WARN;
								detailsWarn.append( "<span class=\"warnFont\">value of required item&nbsp;&nbsp;[PK$NUM_PEAK: ]&nbsp;&nbsp;is improper.</span><br />" );
							}
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
									}
									try {
										Double.parseDouble(mz);
									}
									catch (NumberFormatException e) {
										mzNotNumeric = true;
									}
									intensity = peak.split(" ")[1];
									try {
										Double.parseDouble(intensity);
									}
									catch (NumberFormatException e) {
										intensityNotNumeric = true;
									}
								}
								else {
									invalidFormat = true;
								}
								if ( mzDuplication && mzNotNumeric && intensityNotNumeric && invalidFormat ) {
									break;
								}
							}
							if ( mzDuplication ) {
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">mz value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is duplication.</span><br />" );
							}
							if ( mzNotNumeric ) {
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">mz value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not numeric.</span><br />" );
							}
							if ( intensityNotNumeric ) {
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">intensity value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not numeric.</span><br />" );
							}
							if ( invalidFormat ) {
								status = STATUS_ERR;
								detailsErr.append( "<span class=\"errFont\">value of required item&nbsp;&nbsp;[" + requiredStr + "]&nbsp;&nbsp;is not peak format.</span><br />" );
							}
						}
					}
				}
			}
			String details = detailsErr.toString() + detailsWarn.toString();
			if ( status.equals("") ) {
				status = STATUS_OK;
				details = " ";
			}
			validationMap.put(name, status + "\t" + details);
		}
		
		//----------------------------------------------------
		// 登録済みデータ重複チェック処理
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
				return new TreeMap<String, String>();
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
		for (Map.Entry<String, String> e : validationMap.entrySet()) {
			String statusStr = e.getValue().split("\t")[0];
			if ( statusStr.equals(STATUS_ERR) ) {
				continue;
			}
			String nameStr = e.getKey();
			String idStr = e.getKey().replace(REC_EXTENSION, "");
			String detailsStr = e.getValue().split("\t")[1];
			if ( regIdList.contains(idStr) ){
				statusStr = STATUS_WARN;
				detailsStr += "<span class=\"warnFont\">id&nbsp;&nbsp;[" + idStr + "]&nbsp;&nbsp;of file name&nbsp;&nbsp;[" + nameStr + "]&nbsp;&nbsp;already registered.</span><br />";
				validationMap.put(nameStr, statusStr + "\t" + detailsStr);
			}
		}
		
		return validationMap;
	}
	
	/**
	 * チェック結果表示処理
	 * @param op JspWriter出力バッファ
	 * @param resultMap チェック結果
	 * @return 結果
	 * @throws IOException
	 */ 
	private boolean dispResult( JspWriter op, TreeMap<String, String> resultMap ) throws IOException {
		
		//----------------------------------------------------
		// テーブルヘッダー部
		//----------------------------------------------------
		NumberFormat nf = NumberFormat.getNumberInstance();
		int okCnt = 0;
		int warnCnt = 0;
		int errCnt = 0;
		for (Map.Entry<String, String> e : resultMap.entrySet()) {
			String statusStr = e.getValue().split("\t")[0];
			if ( statusStr.equals(STATUS_OK) ) {
				okCnt++;
			}
			else if ( statusStr.equals(STATUS_WARN) ) {
				warnCnt++;
			}
			else if ( statusStr.equals(STATUS_ERR) ) {
				errCnt++;
			}
		}
		op.println( "\t<br />");
		op.println( "\t<div class=\"count baseFont\">" );
		op.println( "\t\t<span class=\"msgFont\">" + nf.format(okCnt) + " ok</span>&nbsp;,&nbsp;" );
		op.println( "\t\t<span class=\"warnFont\">" + nf.format(warnCnt) + " warn</span>&nbsp;,&nbsp;" );
		op.println( "\t\t<span class=\"errFont\">" + nf.format(errCnt) + " error</span> / " + nf.format(resultMap.size()) + " files&nbsp;" );
		op.println( "\t</div>" );
		op.println( "\t<table width=\"980\" cellspacing=\"1\" cellpadding=\"0\" bgcolor=\"Lavender\">" );
		op.println( "\t\t<tr class=\"rowHeader\">");
		op.println( "\t\t\t<td width=\"140\">Name</td>" );
		op.println( "\t\t\t<td width=\"70\">Status</td>" );
		op.println( "\t\t\t<td>Details</td>" );
		op.println( "\t\t</tr>");
		
		//----------------------------------------------------
		// 一覧表示部
		//----------------------------------------------------
		for (Map.Entry<String, String> e : resultMap.entrySet()) {
			String nameStr = e.getKey();
			String statusStr = e.getValue().split("\t")[0];
			String detailsStr = e.getValue().split("\t")[1].trim();
			op.println( "\t\t<tr class=\"rowEnable\">" );
			op.println( "\t\t\t<td class=\"leftIndent\" height=\"24\">" + nameStr+ "</td>" );
			op.println( "\t\t\t<td align=\"center\">" + statusStr + "</td>" );
			op.println( "\t\t\t<td class=\"details\">" + detailsStr + "</td>" );
			op.println( "\t\t</tr>" );
		}
		op.println( "\t</table>" );
		
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
	<title>Admin | Record Validator</title>
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
<h2>Record Validator</h2>
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
	String selDbName = "";
	FileUpload up = null;
	boolean isResult = true;
	String upFileName = "";
	boolean upResult = false;
	DatabaseAccess db = null;
	
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
		out.println( "<form name=\"formMain\" action=\"" + reqUrl + "\" enctype=\"multipart/form-data\" method=\"post\" onSubmit=\"doWait()\">" );
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
		out.println( "\t<input type=\"file\" name=\"file\" size=\"70\">&nbsp;<input type=\"submit\" value=\"Validation\"><br>" );
		out.println( "\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "\t&nbsp;<span class=\"note\">* please specify your <a href=\"http://www.massbank.jp/sample/recdata.zip\">recdata.zip</a>.</span><br>" );
		out.println( "</form>" );
		out.println( "<hr><br>" );
		if ( !FileUpload.isMultipartContent(request) ) {
			return;
		}
		else {
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
				out.println( msgErr( "please select&nbsp;&nbsp;[" + UPLOAD_RECORD_NAME + "].") );
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
		if ( !(new File( recDataPath )).isDirectory() ) {
			out.println( msgErr( "[" + RECDATA_DIR_NAME + "]&nbsp;&nbsp; directory not exists in upload file.") );
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
		// チェック処理
		//---------------------------------------------
		TreeMap<String, String> resultMap = validationRecord(db, out, recDataPath, recPath);
		if ( resultMap.size() == 0 ) {
			return;
		}
		
		//---------------------------------------------
		// 表示処理
		//---------------------------------------------
		isResult = dispResult(out, resultMap);
	}
	finally {
		if ( db != null ) {
			db.close();
		}
		File tmpDir = new File(tmpPath);
		if (tmpDir.exists()) {
			FileUtil.removeDir( tmpDir.getPath() );
		}
		out.println( "</body>" );
		out.println( "</html>" );
	}
%>
