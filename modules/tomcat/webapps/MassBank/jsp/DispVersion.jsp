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
 * バージョン情報表示用モジュール
 *
 * ver 1.0.8 2009.02.06
 *
 ******************************************************************************/
%>

<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="massbank.admin.VersionManager" %>
<%@ page import="massbank.admin.VersionInfo" %>
<%!
	// 処理種別
	private static final int ACTION_DISP    = 0;
	private static final int ACTION_GET     = 1;
	private static final int ACTION_CHECK   = 2;
	private static final int ACTION_UPDATE  = 3;
	private static final int ACTION_ARCHIVE = 4;
%>
<%
	boolean isCheck = false;
	//---------------------------------------------
	// リクエストパラメータ取得
	//---------------------------------------------
	int action = ACTION_DISP;
	if ( request.getParameter( "act" ) != null ) {
		String param = request.getParameter( "act" );
		if ( param.equals("get") ) {
			action = ACTION_GET;
		}
		else if ( param.equals("check") ) {
			action = ACTION_CHECK;
			isCheck = true;
		}
		else if ( param.equals("update") ) {
			action = ACTION_UPDATE;
		}
		else if ( param.equals("archive") ) {
			action = ACTION_ARCHIVE;
		}
	}
	if ( action != ACTION_GET && action != ACTION_ARCHIVE ) {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<link rel="stylesheet" type="text/css" href="../mbadmin/css/admin.css">
<title>Version Information</title>
<script language="javascript" type="text/javascript">
<!--
/**
 * チェックボックス制御
 */
function CheckAll(num) {
	itemName = new Array( "OLD", "ADD", "DEL", "NEW" );
	elements1 = document.getElementsByName( itemName[num] + "_ALL" );
	elements2 = document.getElementsByName( itemName[num] );
	check = elements1[0].checked;
	for ( i = 0; i < elements2.length; i++ ) {
		elements2[i].checked = check;
	}
}
//-->
</script>
</head>
<body>
<iframe src="../mbadmin/menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Version Information</h2>
<%
	}
	//リクエストURL取得
	String reqUrl = request.getRequestURL().toString();
	// JSPファイル名取得
	String jspName = reqUrl.substring( reqUrl.lastIndexOf("/") + 1 );
	// JSPの絶対パス取得
	String realPath = application.getRealPath("/");
	// バージョン管理クラスのインスタンス生成
	VersionManager verMgr = new VersionManager(reqUrl, realPath);
	
	//-----------------------------------------------------
	// action：アーカイブ作成（プライマリサーバ側の処理）
	//-----------------------------------------------------
	if ( action == ACTION_ARCHIVE ) {
		String res = "OK";
		if ( !verMgr.doArchive() ) {
			res = "NG";
		}
		out.println( res );
		// ここで終了
		return;
	}
	//-----------------------------------------------------
	// action：アップデート
	//-----------------------------------------------------
	else if ( action == ACTION_UPDATE ) {
		String[] paramNames = { "OLD", "ADD", "NEW" };
		// コピーファイルのリストを作成
		List<String	> copyFiles = new ArrayList();
		for ( int i = 0; i < paramNames.length; i++ ) {
			String[] copyFileNames = request.getParameterValues(paramNames[i]);
			if ( copyFileNames != null ) {
				for ( int j = 0; j < copyFileNames.length; j++ ) {
					copyFiles.add(copyFileNames[j]);
				}
			}
		}
		// 削除ファイルのリストを作成
		List<String> removeFiles = new ArrayList();
		String[] delFileNames = request.getParameterValues("DEL");
		if ( delFileNames != null ) {
			for ( int i = 0; i < delFileNames.length; i++ ) {
				removeFiles.add(delFileNames[i]);
			}
		}
		String msg = "";
		if ( removeFiles.size() + copyFiles.size() == 0 ) {
			// 更新対象なし
			msg = "<font color=\"blue\"><b>No update file.</b></font><br>";
		}
		else {
			// アップデート処理
			boolean isOK = verMgr.doUpdate( copyFiles, removeFiles );
			if ( isOK ) {
				// アップデート成功メッセージ
				msg = "<font color=\"blue\"><b>Update completed.</b></font><br>";
			}
			else {
				// アップデート失敗メッセージ
				msg = "<font color=\"red\"><b>Update failed.</b></font><br>";
			}
		}
		out.println( msg );
	}
	
	//-----------------------------------------------------
	// 自サーバのバージョン情報を設定する
	//-----------------------------------------------------
	List<VersionInfo>[] verInfoMyServer = verMgr.getVerMyServer();
	
	//-----------------------------------------------------
	// action：バージョン情報取得（プライマリサーバ側の処理）
	//-----------------------------------------------------
	if ( action == ACTION_GET ) {
		String res = verMgr.doGetVerPServer(verInfoMyServer);
		out.println( res );
		// ここで終了
		return;
	}
	
	//-----------------------------------------------------
	// action：バージョンチェック
	//-----------------------------------------------------
	if ( action == ACTION_CHECK ) {
		if ( !verMgr.doCheckVersion(verInfoMyServer) ) {
			out.println( "<font color=\"red\"><b>Can't connect to massbank.jp server.</b></font><br>" );
			isCheck = false;
		}
	}
	
	//-----------------------------------------------------
	// バージョン情報表示
	//-----------------------------------------------------
	out.println( "<form action=\"" + jspName + "\" method=\"post\">" );
	if ( isCheck )  {
		// 更新あり
		if ( verMgr.isUpdate() ) {
			out.println( "<table>" );
			out.println( "<tr>" );
			out.println( "<td width=\"300\">" );
			out.println( "<input type=\"submit\" value=\"Update\" style=\"background:gold;\">" );
			out.println( "</td>" );
			out.println( "<td width=\"300\" align=\"right\">" );
			// OLDチェックボックス
			if ( verMgr.getOldCnt() > 0 ) {
				out.println( "<input type=\"checkbox\" name=\"OLD_ALL\" value=\"OLD\""
									+ " checked onClick=\"CheckAll(0);\">OLD&nbsp;&nbsp;" );
			}
			// ADDチェックボックス
			if ( verMgr.getAddCnt() > 0 ) {
				out.println( "<input type=\"checkbox\" name=\"ADD_ALL\" value=\"ADD\""
									+ " checked onClick=\"CheckAll(1);\">ADD&nbsp;&nbsp;" );
			}
			// DELチェックボックス
			if ( verMgr.getDelCnt() > 0 ) {
				out.println( "<input type=\"checkbox\" name=\"DEL_ALL\" value=\"DEL\""
									+ " onClick=\"CheckAll(2);\">DEL&nbsp;&nbsp;" );
			}
			// NEWチェックボックス
			if ( verMgr.getNewCnt() > 0 ) {
				out.println( "<input type=\"checkbox\" name=\"NEW_ALL\" value=\"NEW\""
														+ " onClick=\"CheckAll(3);\">NEW" );
			}
			out.println( "</td>" );
			out.println( "</tr>" );
			out.println( "</table>" );
			out.println( "<input type=\"hidden\" name=\"act\" value=\"update\">" );
		}
		// 更新なし
		else {
			out.println("<font color=\"blue\"><b>You have the latest version.</b></font><br>");
		}
	}
	else {
		// バージョンチェックボタン表示
		out.println( "<input type=\"submit\" value=\"Verison Check\">" );
		out.println( "<input type=\"hidden\" name=\"act\" value=\"check\">" );
	}
	
	for ( int i = 0; i < VersionManager.COMPONENT_NAMES.length; i++ ) {
		out.println( "<table>" );
		out.println( "<tr class=\"tbTitle\" align=\"center\">" );
		out.println( "<td width=\"300\">" + VersionManager.COMPONENT_NAMES[i] + " Files</td>" );
		out.println( "<td width=\"200\">Version</td>" );
		out.println( "<td width=\"150\">Date</td>" );
		if ( isCheck ) {
			out.println( "<td width=\"100\">Status</td>" );
		}
		out.println( "</tr>" );
		for ( int j = 0; j < verInfoMyServer[i].size(); j++ ) {
			VersionInfo verInfo = verInfoMyServer[i].get(j);
			String className = "";
			if ( isCheck && verInfo.isUpdate() ) {
				className = "tbUpdate";
			}
			else {
				if ( j % 2 == 0 ) { className = "tb1"; }
				else              { className = "tb2"; }
			}
			out.println( "<tr class=\"" + className + "\">" );
			out.println( "<td><img src=\"../mbadmin/image/file.gif\">&nbsp;" + verInfo.getName() + "</td>" );
			out.println( "<td align=\"center\">" + verInfo.getVersion() + "</td>" );
			out.println( "<td align=\"center\">" + verInfo.getDate() + "</td>" );
			if ( isCheck ) {
				out.println( "<td width=\"100\" align=\"center\">" + verInfo.getStatus() );
				if ( verInfo.isUpdate() ) {
					String path = VersionManager.COMPONENT_DIR[i] + "/" + verInfo.getName();
					out.println( "<input type=\"checkbox\" name=\"" + verInfo.getStatus()
						+  "\" value=\"" + path + "\"" );
					if ( !verInfo.getStatus().equals("NEW")
					  && !verInfo.getStatus().equals("DEL") ) {
						out.println(" checked");
					}
					out.println(">");
				}
				out.println( "</td>" );
			}
			out.println( "</tr>" );
		}
		out.println( "</table>" );
		out.println( "</br>" );
	}
	out.println( "</form>" );
%>
</body>
</html>
