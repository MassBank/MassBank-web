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
 * 登録済み構造式一覧
 *
 * ver 1.1.10  2012.09.05
 *
 ******************************************************************************/
%>

<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.PrintStream" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.admin.DatabaseAccess" %>
<%@ page import="massbank.admin.FileUtil" %>
<%@ page import="massbank.admin.OperationManager" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.Sanitizer" %>
<%@ page import="massbank.svn.MSDBUpdater" %>
<%@ page import="massbank.svn.SVNRegisterUtil" %>
<%@ page import="massbank.svn.RegistrationCommitter" %>
<%!
	/** 作業ディレクトリ用日時フォーマット */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_SSS");
	
	/** 改行文字列 */
	private final String NEW_LINE = System.getProperty("line.separator");
	
	/** Molfile拡張子 */
	private final String MOL_EXTENSION = ".mol";
	
	/** Gif拡張子 */
	private final String GIF_EXTENSION = ".gif";
	
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
	 * 登録済み構造式一覧表示処理
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param selDbName DB名
	 * @param gifPath Gif格納パス
	 * @param gifSmallPath GifSmall格納パス
	 * @param gifLargePath GifLarge格納パス
	 * @param molPath Molfile格納パス
	 * @return 結果
	 * @throws IOException
	 */
	private boolean dispStructureList( DatabaseAccess db, JspWriter op, String selDbName, String gifPath, String gifSmallPath, String gifLargePath, String molPath ) throws IOException {
		TreeMap<String, String> mainList = new TreeMap<String, String>();
		TreeMap<String, String> subList = new TreeMap<String, String>();
		HashSet<String> tmpList = new HashSet<String>();
		
		//----------------------------------------------------
		// 表示に必要な情報の退避
		//----------------------------------------------------
		TreeSet<String> rsCompound = new TreeSet<String>();						// 登録済み化合物
		HashMap<String, String> rsStructure = new HashMap<String, String>();	// 登録済み構造式
		ResultSet[] rs = new ResultSet[2];
		String sql = "";
		int registNum = 0;
		int unRelatedNum = 0;
		try {
			// DB情報取得（登録済み化合物）
			sql = "SELECT LEFT(NAME, INSTR(NAME, ';')-1) COMPOUND FROM SPECTRUM;";
			rs[0] = db.executeQuery(sql);
			while ( rs[0].next() ) {
				rsCompound.add(rs[0].getString("COMPOUND"));
			}
			
			// DB情報取得（登録済み構造式）
			sql = "SELECT NAME, FILE FROM MOLFILE;";
			rs[1] = db.executeQuery(sql);
			while ( rs[1].next() ) {
				rsStructure.put(rs[1].getString("NAME"), rs[1].getString("FILE"));
			}
			
			// １）レコード登録済の化合物の登録を検出
			for (String compound : rsCompound) {
				String idStr = "-";
				String fileStr = " ";
				String stateStr = "";
				String detailStr = " ";
				String gifFile = "";
				String molFile = "";
				if (rsStructure.get(compound) != null) {
					idStr = rsStructure.get(compound);
					gifFile = idStr + GIF_EXTENSION;
					molFile = idStr + MOL_EXTENSION;
					
					// GIF & MOLFILE
					if ( !(new File( gifPath + "/" + gifFile )).isFile() && !(new File( molPath + "/" + molFile )).isFile() ) {
						stateStr = STATUS_ERR;
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"errFont\">gif not exist.</span><br><span class=\"errFont\">molfile not exist.</span>";
					}
					else if ( !(new File( gifPath + "/" + gifFile )).isFile() ) {
						stateStr = STATUS_WARN;
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"warnFont\">gif not exist.</span>";
						fileStr = molFile;
					}
					else if ( !(new File( molPath + "/" + molFile )).isFile() ) {
						stateStr = STATUS_WARN;
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"warnFont\">molfile not exist.</span>";
						fileStr = gifFile;
					}
					else {
						fileStr = gifFile;
					}
					
					// GIF_SMALL
					if ( !(new File( gifSmallPath + "/" + gifFile )).isFile() ) {
						if ( stateStr.equals("") ) { stateStr = STATUS_WARN; }
						if ( !detailStr.equals(" ") ) { detailStr += "<br>"; }
						detailStr += "<span class=\"warnFont\">gif_small not exist.</span>";
					}
					// GIF_LARGE
					if ( !(new File( gifLargePath + "/" + gifFile )).isFile() ) {
						if ( stateStr.equals("") ) { stateStr = STATUS_WARN; }
						if ( !detailStr.equals(" ") ) { detailStr += "<br>"; }
						detailStr += "<span class=\"warnFont\">gif_large not exist.</span>";
					}
					
					if ( !stateStr.equals(STATUS_ERR) ) {
						registNum++;
					}
					if ( stateStr.equals("") ) {
						stateStr = STATUS_OK;
					}
				}
				else {
					stateStr = "-";
					detailStr = "structure data unregistered.";
				}
				
				// 表示情報退避
				mainList.put(compound, fileStr + "\t" + compound + "\t" + idStr + "\t" + stateStr + "\t" + detailStr);
				tmpList.add(idStr);
			}
			
			// ２）MOLFILEテーブル（レコード未登録）の登録を検出
			for ( Map.Entry<String, String> e : rsStructure.entrySet() ) {
				String compound = e.getKey();
				String idStr = e.getValue();
				String fileStr = " ";
				String stateStr = STATUS_WARN;
				String detailStr = "<span class=\"warnFont\">registered, but orphan compound.</span>";
				String gifFile = "";
				String molFile = "";
				if ( !mainList.containsKey(compound) ) {
					gifFile = idStr + GIF_EXTENSION;
					molFile = idStr + MOL_EXTENSION;
					
					// GIF & MOLFILE
					if ( !(new File( gifPath + "/" + gifFile )).isFile() && !(new File( molPath + "/" + molFile )).isFile() ) {
						stateStr = STATUS_WARN;
						detailStr += "<br /><span class=\"warnFont\">gif not exist.</span><br /><span class=\"warnFont\">molfile not exist.</span>";
					}
					else if ( !(new File( gifPath + "/" + gifFile )).isFile() ) {
						stateStr = STATUS_WARN;
						detailStr += "<br /><span class=\"warnFont\">gif not exist.</span>";
						fileStr = molFile;
					}
					else if ( !(new File( molPath + "/" + molFile )).isFile() ) {
						stateStr = STATUS_WARN;
						detailStr += "<br /><span class=\"warnFont\">molfile not exist.</span>";
						fileStr = gifFile;
					}
					else {
						fileStr = gifFile;
					}
					
					// GIF_SMALL
					if ( !(new File( gifSmallPath + "/" + gifFile )).isFile() ) {
						detailStr += "<br /><span class=\"warnFont\">gif_small not exist.</span>";
					}
					// GIF_LARGE
					if ( !(new File( gifLargePath + "/" + gifFile )).isFile() ) {
						detailStr += "<br /><span class=\"warnFont\">gif_large not exist.</span>";
					}
					
					unRelatedNum++;
					mainList.put(compound, fileStr + "\t" + compound + "\t" + idStr + "\t" + stateStr + "\t" + detailStr);
					tmpList.add(idStr);
				}
			}
			// ３）ファイルのみの登録を検出
			File gifDir = new File(gifPath);
			File gifSmallDir = new File(gifSmallPath);
			File gifLargeDir = new File(gifLargePath);
			File molDir = new File(molPath);
			ArrayList<String[]> workList = new ArrayList<String[]>();
			if ( gifDir.isDirectory() ) {
				workList.add(gifDir.list());
			}
			if ( gifSmallDir.isDirectory() ) {
				workList.add(gifSmallDir.list());
			}
			if ( gifLargeDir.isDirectory() ) {
				workList.add(gifLargeDir.list());
			}
			if ( molDir.isDirectory() ) {
				workList.add(molDir.list());
			}
			
			HashSet<String> fileSet = new HashSet<String>();
			for (String[] fileArray : workList) {
				for (int i=0; i<fileArray.length; i++) {
					String fileId = fileArray[i];
					int extPos = fileArray[i].lastIndexOf(".");
					if (extPos > 0) {
						fileId = fileArray[i].substring(0, extPos);
					}
					fileSet.add(fileId);
				}
			}
			
			Iterator<String> it = fileSet.iterator();
			while ( it.hasNext() ) {
				String idStr = (String)it.next();
				String fileName = " ";
				String compound = "-";
				String stateStr = STATUS_WARN;
				String detailStr = " ";
				boolean isFileOnly = false;
				
				// 既にIDが登録されている場合は保持しない
				if ( tmpList.add(idStr) ) {
					if ( (new File( gifPath + "/" + idStr + GIF_EXTENSION )).isFile() ) {
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"warnFont\">gif only. [<i>" + idStr + GIF_EXTENSION + "</i>]</span>";
						isFileOnly = true;
					}
					if ( (new File( gifSmallPath + "/" + idStr + GIF_EXTENSION )).isFile() ) {
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"warnFont\">gif_small only. [<i>" + idStr + GIF_EXTENSION + "</i>]</span>";
						isFileOnly = true;
					}
					if ( (new File( gifLargePath + "/" + idStr + GIF_EXTENSION )).isFile() ) {
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"warnFont\">gif_large only. [<i>" + idStr + GIF_EXTENSION + "</i>]</span>";
						isFileOnly = true;
					}
					if ( (new File( molPath + "/" + idStr + MOL_EXTENSION )).isFile() ) {
						if ( !detailStr.equals(" ") ) { detailStr += "<br />"; }
						detailStr += "<span class=\"warnFont\">molfile only. [<i>" + idStr + MOL_EXTENSION + "</i>]</span>";
						isFileOnly = true;
					}
					
					if ( isFileOnly ) {
						subList.put(idStr, fileName + "\t" + compound + "\t" + idStr + "\t" + stateStr + "\t" + detailStr);
					}
				}
			}
		}
		catch (SQLException e) {
			Logger.getLogger("global").severe( "SQL : " + sql );
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
		
		// 表示情報0件
		if ( mainList.size() == 0 && subList.size() == 0 ) {
			op.println( msgInfo( "no compound." ) );
			return false;
		}
		
		//----------------------------------------------------
		// テーブルヘッダー部
		//----------------------------------------------------
		NumberFormat nf = NumberFormat.getNumberInstance();
		String strNote = "";
		if ( unRelatedNum > 0 ) {
			strNote = " <span class=\"warnFont\">( " + nf.format(mainList.size() - unRelatedNum) + " compounds used )</span>";
		}
		op.println( "<form name=\"formList\" action=\"./StructureList.jsp\" method=\"post\" onSubmit=\"doWait();\">" );
		op.println( "\t<input type=\"submit\" value=\"Delete\" onClick=\"return beforeDelete();\">" );
		op.println( "\t<div class=\"count baseFont\">" + nf.format(registNum) + " structure / " + nf.format(mainList.size()) + " compounds" + strNote + "&nbsp;</div>" );
		op.println( "\t<table table width=\"980\" cellspacing=\"1\" cellpadding=\"0\" bgcolor=\"Lavender\">" );
		op.println( "\t\t<tr class=\"rowHeader\">");
		op.println( "\t\t\t<td width=\"25\"><input type=\"checkbox\" name=\"chkAll\" onClick=\"checkAll();\"></td>" );
		op.println( "\t\t\t<td width=\"430\">Compound Name</td>" );
		op.println( "\t\t\t<td width=\"95\">ID</td>" );
		op.println( "\t\t\t<td width=\"80\">Structure</td>" );
		op.println( "\t\t\t<td width=\"70\">Status</td>" );
		op.println( "\t\t\t<td width=\"200\">Details</td>" );
		op.println( "\t\t</tr>");
		
		//----------------------------------------------------
		// 一覧表示表示部生成
		//----------------------------------------------------
		int cnt = 0;
		// 化合物名に対応するリスト
		for (Iterator<String> i=mainList.keySet().iterator(); i.hasNext();) {
			String key = (String)i.next();
			String[] val = ((String)mainList.get(key)).split("\t");
			
			String strRow = "rowEnable";
			String strChkDisable = "";
			String structureUrl = "";
			String strBtnDisable = "";
			String strTitle = " title=\"" + val[0] + "\"";
			if (val[2].equals("-")) {
				strRow = "rowDisable";
				strChkDisable = " disabled";
				strBtnDisable = " disabled";
				strTitle = "";
			}
			else if (val[3].indexOf("error") == -1) {
				if (val[4].indexOf("gif not exist.") > -1 && val[4].indexOf("molfile not exist.") > -1) {
					strBtnDisable = " disabled";
					strTitle = "";
				}
				else {
					try {
						structureUrl = "./StructureView.jsp?dname=" + selDbName + "&cname=" + URLEncoder.encode(val[1] , "UTF-8");
					}
					catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				strBtnDisable = " disabled";
				strTitle = "";
			}
			
			op.println( "\t\t<tr class=\"" + strRow + "\" id=\"row" + cnt + "\">" );
			op.println( "\t\t\t<td class=\"center\">" );
			op.println( "\t\t\t\t<input type=\"checkbox\" name=\"id\" value=\"" + val[2] + "\" onClick=\"check(" + cnt + ");\"" + strChkDisable + ">" );
			op.println( "\t\t\t</td>" );
			op.println( "\t\t\t<td class=\"leftIndent\">" + Sanitizer.html(val[1]) + "</td>" );
			op.println( "\t\t\t<td class=\"center\">" + val[2] + "</td>" );
			op.println( "\t\t\t<td class=\"center\"" + strTitle + ">" );
			op.println( "\t\t\t\t<input type=\"button\" onClick=\"popupMolView('" + structureUrl + "');\" value=\"View\"" + strTitle + strBtnDisable + ">" );
			op.println( "\t\t\t</td>" );
			op.println( "\t\t\t<td align=\"center\">" + val[3] + "</td>" );
			op.println( "\t\t\t<td class=\"details\">" + val[4] + "</td>" );
			op.println( "\t\t</tr>" );
			cnt++;
		}
		
		// ファイルのみ登録済みリスト
		for (Iterator<String> i=subList.keySet().iterator(); i.hasNext();) {
			String key = (String)i.next();
			String[] val = ((String)subList.get(key)).split("\t");
			
			op.println( "\t\t<tr class=\"rowEnable\" id=\"row" + cnt + "\">" );
			op.println( "\t\t\t<td class=\"center\">" );
			op.println( "\t\t\t\t<input type=\"checkbox\" name=\"id\" value=\"" + val[2] + "\" onClick=\"check(" + cnt + ");\">" );
			op.println( "\t\t\t</td>" );
			op.println( "\t\t\t<td class=\"leftIndent\">-</td>" );
			op.println( "\t\t\t<td class=\"center\">-</td>" );
			op.println( "\t\t\t<td class=\"center\">" );
			op.println( "\t\t\t\t<input type=\"button\" value=\"View\" disabled>" );
			op.println( "\t\t\t</td>" );
			op.println( "\t\t\t<td align=\"center\">" + val[3] + "</td>" );
			op.println( "\t\t\t<td class=\"details\">" + val[4] + "</td>" );
			op.println( "\t\t</tr>");
			cnt++;
		}
		
		op.println( "\t\t</table>" );
		if ( RegistrationCommitter.isActive ) {
			op.println( "\t<br><input type=\"submit\" value=\"Update the MassBank SVN\" onClick=\"document.formList.act.value='svn';\">" );
		}
		op.println( "\t<input type=\"hidden\" name=\"act\" value=\"\">" );
		op.println( "\t<input type=\"hidden\" name=\"db\" value=\"" + selDbName + "\">" );
		op.println( "</form>" );
		return true;
	}
	
	/**
	 * 構造式登録情報削除処理
	 * @param db DBアクセスオブジェクト
	 * @param op JspWriter出力バッファ
	 * @param ids 削除対象の構造式ID
	 * @param gifPath Gif格納パス
	 * @param gifSmallPath GifSmall格納パス
	 * @param gifLargePath GifLarge格納パス
	 * @param molPath Molfile格納パス
	 * @return 結果
	 * @throws IOException
	 */
	private boolean delStructureInfo( DatabaseAccess db, JspWriter op, String[] ids, String gifPath, String gifSmallPath, String gifLargePath, String molPath ) throws IOException {
		
		if (ids == null || ids.length != 0) {
			//----------------------------------------------------
			// 構造式ファイル削除、削除用SQLパラメータ生成
			//----------------------------------------------------
			StringBuilder sqlParam = new StringBuilder();
			File file = null;
			for ( int i=0; i<ids.length; i++ ) {
				// 必ずGIFよりもMolfileを先に削除する（画像自動生成プログラムがMolfileの存在でGIFを生成するため）
				file = new File( molPath + "/" + ids[i] + MOL_EXTENSION );
				if ( file.isFile() ) { file.delete(); }
				file = new File( gifPath + "/" + ids[i] + GIF_EXTENSION );
				if ( file.isFile() ) { file.delete(); }
				file = new File( gifSmallPath + "/" + ids[i] + GIF_EXTENSION );
				if ( file.isFile() ) { file.delete(); }
				file = new File( gifLargePath + "/" + ids[i] + GIF_EXTENSION );
				if ( file.isFile() ) { file.delete(); }
				sqlParam.append( "\"" + ids[i] + "\"," );
			}
			
			//----------------------------------------------------
			// MOLFILEテーブル削除
			//----------------------------------------------------
			if (sqlParam.length() > 0) {
				sqlParam.setLength( sqlParam.length() - 1 );	// 最後のカンマを削除
				final String sql = "DELETE FROM MOLFILE WHERE FILE in(" + sqlParam.toString() + ");";
				try {
					db.executeUpdate(sql);
				}
				catch (SQLException e) {
					Logger.getLogger("global").severe( "SQL : " + sql );
					e.printStackTrace();
					op.println( msgErr( "database access error." ) );
					return false;
				}
			}
			
			NumberFormat nf = NumberFormat.getNumberInstance();
			op.println( msgInfo( nf.format(ids.length) + " structure delete." ) );
		}
		
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
	<title>Admin | Structure List</title>
	<script language="javascript" type="text/javascript">
<!--
var OFF_COLOR = "WhiteSmoke";
var ON_COLOR = "LightSteelBlue";
var prevIndex = -1;
var ev;
window.document.onkeydown = function(e){ ev = e; }

/**
 * 削除前処理
 */
function beforeDelete() {
	
	objForm = document.formList;
	
	chkFlag = false;
	idLength = objForm.id.length;
	if ( idLength != null ) {
		// 2件以上
		for (i=0; i<idLength; i++){
			if (objForm.id[i].disabled) {
				continue;
			}
			else if (objForm.id[i].checked) {
				chkFlag = true;
				break;
			}
		}
	}
	else {
		// 1件
		if (objForm.id.checked) {
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
	
	objForm = document.formList;
	idLength = objForm.id.length;
	if ( idLength == null ) {
		obj = document.getElementById( String("row0") );
		if ( objForm.id.checked ) {
			obj.style.background = ON_COLOR;
		}
		else {
			obj.style.background = OFF_COLOR;
		}
		prevIndex = -1;
		return;
	}
	
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
	
	// SHIFTキーが押されている場合は複数行選択
	if ( isShift && prevIndex != -1 ) {
		if ( index < prevIndex ) {
			start = index; end = prevIndex;
		}
		else {
			start = prevIndex;
			end = index;
		}
		isCheck = objForm.id[index].checked;
		if ( isCheck ) {
			bgcolor = ON_COLOR;
		}
		else {
			bgcolor = OFF_COLOR;
		}
		for ( i = start; i <= end; i++ ) {
			if (objForm.id[i].disabled) {
				continue;
			}
			objForm.id[i].checked = isCheck;
			obj = document.getElementById( String("row" + i) );
			obj.style.background = bgcolor;
		}
	}
	else {
		obj = document.getElementById( String("row" + index) );
		if ( objForm.id[index].checked ) {
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
	
	objForm = document.formList;
	isCheck = objForm.chkAll.checked;
	if ( isCheck ) {
		bgcolor = ON_COLOR;
	}
	else {
		bgcolor = OFF_COLOR;
	}
	
	idLength = objForm.id.length;
	if ( idLength != null ) {
		for ( i=0; i<idLength; i++ ) {
			if (objForm.id[i].disabled) {
				continue;
			}
			objForm.id[i].checked = isCheck;
			document.getElementById(String("row" + i)).style.background = bgcolor;
		}
	}
	else {
		objForm.id.checked = isCheck;
		document.getElementById(String("row0")).style.background = bgcolor;
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
 * 構造式表示ページを新規ウインドウにて開く
 * @param url ターゲットURL
 */
function popupMolView(url) {
	
	if ( ie ) {
		leftX = window.screenLeft + document.body.clientWidth - 250;
		topY =  window.screenTop;
	}
	else {
		leftX = window.screenX + document.body.clientWidth - 250;
		topY =  window.screenTop;
	}
	win = window.open(url, "MolView",
		'width=230,height=240,menubar=no,toolbar=no,scrollbars=no,status=no,left='
		 + leftX + ', top=' + topY + ',screenX=' + leftX + ',screenY=' + topY + '' );
	win.focus();
}
//-->
	</script>
</head>
<body>
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Structure List</h2>
<%
	//----------------------------------------------------
	// リクエストパラメータ取得
	//----------------------------------------------------
	request.setCharacterEncoding("utf-8");
	String act = "";
	String selDbName = "";
	String[] ids = null;
	Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( key.equals("act") ) {
			act = request.getParameter(key);
		}
		else if ( key.equals("db") ) {
			selDbName = request.getParameter(key);
		}
		else if ( key.equals("id") ) {
			ids = request.getParameterValues(key);
		}
	}
	
	//----------------------------------------------------
	// 各種パラメータ設定
	//----------------------------------------------------
	final String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	final String dbRootPath = MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
	final String molRootPath = MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH);
	final String gifRootPath = MassBankEnv.get(MassBankEnv.KEY_GIF_PATH);
	final String gifSmallRootPath = MassBankEnv.get(MassBankEnv.KEY_GIF_SMALL_PATH);
	final String gifLargeRootPath = MassBankEnv.get(MassBankEnv.KEY_GIF_LARGE_PATH);
	final String dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
	final String tomcatTmpPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_TEMP_PATH);
	final String tmpPath = (new File(tomcatTmpPath + sdf.format(new Date()))).getPath() + File.separator;
	final String backupPath = tmpPath + "backup" + File.separator;
	final String os = System.getProperty("os.name");
	GetConfig conf = new GetConfig(baseUrl);
	OperationManager om = OperationManager.getInstance();
	DatabaseAccess db = null;	
	boolean isResult = true;
	StringBuilder dbArgs = new StringBuilder();
	
	try {
		//----------------------------------------------------
		// 存在するDB名取得（ディレクトリによる判定）
		//----------------------------------------------------
		List<String> dbNameList = Arrays.asList(conf.getDbName());
		ArrayList<String> dbNames = new ArrayList<String>();
		dbNames.add("");
		File[] dbDirs = (new File( dbRootPath )).listFiles();
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
			out.println( msgErr( "[" + dbRootPath + "]&nbsp;&nbsp;directory not exist." ) );
			return;
		}
		Collections.sort(dbNames);
		
		//----------------------------------------------------
		// 構造式パス設定
		//----------------------------------------------------
		if ( selDbName.equals("") || !dbNames.contains(selDbName) ) {
			selDbName = dbNames.get(0);
		}
		final String gifPath = (new File(gifRootPath + "/" + selDbName)).getPath();
		final String gifSmallPath = (new File(gifSmallRootPath + "/" + selDbName)).getPath();
		final String gifLargePath = (new File(gifLargeRootPath + "/" + selDbName)).getPath();
		final String molPath = (new File(molRootPath + "/" + selDbName)).getPath();
		if ( !(new File(gifPath)).isDirectory() ||
		     !(new File(gifSmallPath)).isDirectory() ||
		     !(new File(gifLargePath)).isDirectory() ||
		     !(new File(molPath)).isDirectory() ) {
			
			out.println( msgErr( "[" + gifPath + "]&nbsp;&nbsp;or&nbsp;&nbsp;[" + gifSmallPath + "]&nbsp;&nbsp;or&nbsp;&nbsp;[" + gifLargePath + "]&nbsp;&nbsp;or&nbsp;&nbsp;[" + molPath + "]&nbsp;&nbsp;directory not exist." ) );
			return;
		}
		
		//----------------------------------------------------
		// フォーム表示
		//----------------------------------------------------
		out.println( "<form name=\"formMain\" action=\"./StructureList.jsp\" method=\"post\" onSubmit=\"doWait()\">" );
		out.println( "<input type=\"hidden\" name=\"act\" value=\"get\">" );
		out.println( "<span class=\"baseFont\">Database :</span>&nbsp;" );
		out.println( "<select name=\"db\" class=\"db\" onChange=\"selDb()\">" );
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
		if ( act.equals("get") || act.equals("del") || act.equals("list") ) {
			db = new DatabaseAccess(dbHostName, selDbName);
			isResult = db.open();
			if ( !isResult ) {
				out.println( msgErr( "not connect to database." ) );
				return;
			}
		}
		
		//----------------------------------------------------
		// 登録済み構造式一覧表示
		//----------------------------------------------------
		if ( act.equals("get") ) {
			isResult = om.startOparation(om.P_STRUCTURE, om.TP_VIEW, selDbName);
			if ( !isResult ) {
				out.println( msgWarn( "other users are updating. please access later. ") );
				return;
			}
			isResult = dispStructureList( db, out, selDbName, gifPath, gifSmallPath, gifLargePath, molPath );
			if ( !isResult ) {
				return;
			}
		}
		//----------------------------------------------------
		// 構造式削除
		//----------------------------------------------------
		else if ( act.equals("del") ) {
			isResult = om.startOparation(om.P_STRUCTURE, om.TP_UPDATE, selDbName);
			if ( !isResult ) {
				out.println( msgWarn( "other users are updating. please access later. ") );
				return;
			}
			
			// 作業用ディレクトリ作成
			(new File(tmpPath)).mkdir();
			(new File(backupPath)).mkdir();
			if(os.indexOf("Windows") == -1){
				isResult = FileUtil.changeMode("777", tmpPath);
				if ( !isResult ) {
					out.println( msgErr( "[" + tmpPath + "]&nbsp;&nbsp; chmod failed.") );
					return;
				}
			}
			
			// ロールバック用DBダンプ処理
			String dumpPath = tmpPath + selDbName + ".dump";
			String[] tables = new String[]{"MOLFILE"};
			isResult = FileUtil.execSqlDump(dbHostName, selDbName, tables, dumpPath);
			if ( !isResult ) {
				Logger.getLogger("global").severe( "sqldump failed." + NEW_LINE +
				                                   "    dump file : " + dumpPath );
				out.println( msgErr( "db dump failed." ) );
				out.println( msgInfo( "0 structure delete." ) );
				return;
			}
			// ファイルダンプ
			File srcFile = null;
			File destFile = null;
			try {
				for ( String structId : ids ) {
					srcFile = new File(gifPath + File.separator + structId + GIF_EXTENSION);
					destFile = new File(backupPath + structId + GIF_EXTENSION);
					if ( srcFile.isFile() ) {
						FileUtils.copyFile(srcFile, destFile);
					}
					srcFile = new File(gifSmallPath + File.separator + structId + GIF_EXTENSION);
					destFile = new File(backupPath + structId + GIF_EXTENSION + "s");
					if ( srcFile.isFile() ) {
						FileUtils.copyFile(srcFile, destFile);
					}
					srcFile = new File(gifLargePath + File.separator + structId + GIF_EXTENSION);
					destFile = new File(backupPath + structId + GIF_EXTENSION + "l");
					if ( srcFile.isFile() ) {
						FileUtils.copyFile(srcFile, destFile);
					}
					srcFile = new File(molPath + File.separator + structId + MOL_EXTENSION);
					destFile = new File(backupPath + structId + MOL_EXTENSION);
					if ( srcFile.isFile() ) {
						FileUtils.copyFile(srcFile, destFile);
					}
				}
			} catch (IOException e) {
				Logger.getLogger("global").severe( "file copy failed." + NEW_LINE +
				                                   "    " + srcFile + " to " + destFile );
				e.printStackTrace();
				out.println( msgErr( "file dump failed." ) );
				out.println( msgInfo( "0 record delete." ) );
				return;
			}
			
			// 削除
			isResult = delStructureInfo( db, out, ids, gifPath, gifSmallPath, gifLargePath, molPath );
			if ( !isResult ) {
				// ファイルロールバック
				try {
					for ( String structId : ids ) {
						srcFile = new File(backupPath + structId + GIF_EXTENSION);
						destFile = new File(gifPath + File.separator + structId + GIF_EXTENSION);
						if ( srcFile.isFile() ) {
							FileUtils.copyFile(srcFile, destFile);
						}
						srcFile = new File(backupPath + structId + GIF_EXTENSION + "s");
						destFile = new File(gifSmallPath + File.separator + structId + GIF_EXTENSION);
						if ( srcFile.isFile() ) {
							FileUtils.copyFile(srcFile, destFile);
						}
						srcFile = new File(backupPath + structId + GIF_EXTENSION + "l");
						destFile = new File(gifLargePath + File.separator + structId + GIF_EXTENSION);
						if ( srcFile.isFile() ) {
							FileUtils.copyFile(srcFile, destFile);
						}
						srcFile = new File(backupPath + structId + MOL_EXTENSION);
						destFile = new File(molPath + File.separator + structId + MOL_EXTENSION);
						if ( srcFile.isFile() ) {
							FileUtils.copyFile(srcFile, destFile);
						}
					}
				}
				catch (IOException e) {
					Logger.getLogger("global").severe( "rollback(file delete) failed." );
					e.printStackTrace();
					out.println( msgErr( "rollback failed." ) );
				}
				// SQLロールバック
				isResult = FileUtil.execSqlFile(dbHostName, selDbName, dumpPath);
				if ( !isResult ) {
					Logger.getLogger("global").severe( "rollback(sqldump) failed." );
					out.println( msgErr( "rollback failed." ) );
				}
				out.println( msgInfo( "0 record delete." ) );
			}
			
			// StructureSearch 更新処理
			String[] urlList = conf.getSiteUrl();
			String cgiUrl = urlList[GetConfig.MYSVR_INFO_NUM] + "cgi-bin/GenSubstructure.cgi";
			String cgiParam = "db=" + dbArgs.toString();
			boolean tmpRet = execCgi( cgiUrl, cgiParam );
			if ( !tmpRet ) {
				Logger.getLogger("global").severe( "cgi execute failed." + NEW_LINE +
				                                   "    url : " + cgiUrl + NEW_LINE +
				                                   "    param : " + cgiParam );
				out.println( msgWarn( "Substructure Search update failed.(inconsistent)") );
			}

			//---------------------------------------------
			// SVN削除処理
			//---------------------------------------------
			if ( RegistrationCommitter.isActive ) {
				SVNRegisterUtil.updateMolfiles(selDbName);
			}
		}
		else if ( act.equals("svn") && RegistrationCommitter.isActive  ) {
			SVNRegisterUtil.updateMolfiles(selDbName);
			out.println( msgInfo( "Done." ) );
		}
	}
	finally {
		if ( db != null ) {
			db.close();
		}
		if ( act.equals("get") ) {
			om.endOparation(om.P_STRUCTURE, om.TP_VIEW, selDbName);
		}
		else if ( act.equals("del") ) {
			File tmpDir = new File(tmpPath);
			if (tmpDir.exists()) {
				FileUtil.removeDir( tmpDir.getPath() );
			}
			om.endOparation(om.P_STRUCTURE, om.TP_UPDATE, selDbName);
			out.println( msgInfo( "Done." ) );
		}
		out.println( "</body>" );
		out.println( "</html>" );
	}
%>
