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
 * 部分構造検索クエリ表示用モジュール
 *
 * ver 1.0.12 2009.12.09
 *
 ******************************************************************************/
%>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.commons.lang.NumberUtils" %>
<%@ include file="./Common.jsp"%>
<%!
	/**
	 * パラメータセット処理
	 * @param key リクエストパラメータのキー名
	 * @param val リクエストパラメータの値
	 * @param molData molfileデータ格納用配列
	 * @param mz mz値格納用配列
	 */ 
	private void setParams(
		String key, String val, String[] molData, String[] mz ) {

		String[] findKeys = { "moldata", "mz" };
		int numKey = -1;
		int index = 0;
		for ( int i = 0; i < findKeys.length; i++ ) {
			if ( key.indexOf(findKeys[i]) == 0 ) {
				String num = key.substring(findKeys[i].length());
				if ( NumberUtils.isNumber(num) ) {
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
			if ( NumberUtils.isNumber(numbers[i]) ) {
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
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta name="description" content="Search by substructure">
<meta name="keywords" content="Structure">
<meta name="revisit_after" content="30 days">
<link rel="stylesheet" type="text/css" href="./css/Common.css">
<title>MassBank | Database | Substructure Search</title>
<script type="text/javascript" src="script/Common.js"></script>
<script type="text/javascript">
<!--
	var MOL_HEADER = "@data=";
	var QUERY_NUM = 2;
	var PEAK_NUM = 3;
	var html = "";
	var peak = new Array(PEAK_NUM);
	var tol = "";
	var PI_CHECK_VAL = new Array( "2", "1", "0" );
	var piCheckNum = 0;
	var boxGrp;
	var boxInst;
	var ionCheckNum;

/**
 * エディタ画面を表示する
 * (クエリ→エディタに反映)
 */
function toEditor(index) {
	// 入力フィールドの内容を保持する
	storeFields();
	
	// クエリ側JME形式のデータを取得する
	var jme = document.jme_query[index].jmeFile();
	
	// クエリ側HTMLを保持する
	var ele = document.getElementById("tbl_query");
	html = ele.innerHTML;
	
	// HTMLをエディタ画面に書き換える
	ele.innerHTML =
		 "&nbsp;<b>Query" + String(index+1) + "</b><br>"
		+ "<table>"
		+ "<tr>"
		+ "<td colspan='2'>"
		+ "<applet name='jme_edit' code='JME.class' archive='./applet/JME.jar' width='516' height='400'>"
		+ "</applet><br>"
		+ "</td>"
		+ "</tr>"
		+ "<tr>"
		+ "<td>"
		+ "<input type='button' value='OK' onClick='fromEditor(" + index + ");'>"
		+ "<input type='button' value='CANCEL' onClick='cancelEditor();'>"
		+ "</td>"
		+ "<td align='right'>"
		+ "<font style='font-size:11px'><a href='http://www.molinspiration.com/jme/' target='_blank'>JME Editor</a> courtesy of Peter Ertl, Novartis</font>"
		+ "</td>"
		+ "</tr>"
		+ "</table>";
	
	// エディタ側アプレット更新する
	if ( jme != "") {
		document.jme_edit.readMolecule(jme);
	}
}

/**
 * エディタの編集を完了する
 * （エディタ→クエリに反映）
 */
function fromEditor(index) {
	// エディタ側の情報をmolfile形式で取得する
	var mol = document.jme_edit.molFile();
	html = html.replace( "&gt;&gt; invalid data", "" );
	
	// HTMLをクエリ画面に書き戻す
	document.getElementById("tbl_query").innerHTML = html;
	
	// molfile情報をmoldataフィールドにセットする
	document.form_query["moldata" + index].value = MOL_HEADER + mol;
	
	// 入力フィールドの内容を書き戻す
	restoreFields();
	
	// クエリの描画を更新する
	updateQuery();
}

/**
 * エディタの編集をキャンセルする
 */
function cancelEditor() {
	// HTMLをクエリ画面に書き戻す
	document.getElementById("tbl_query").innerHTML = html;
	
	// 入力フィールドの内容を書き戻す
	restoreFields();
	
	// クエリの描画を更新する
	updateQuery();
}

/**
 * クエリの描画を更新する
 */
function updateQuery() {
	for ( i = 0; i < QUERY_NUM; i++ ) {
		document.jme_query[i].options('depict,border');
		mol = document.form_query["moldata" + i].value;
		mol = mol.replace( MOL_HEADER, "" );
		if ( mol != "" ) {
			document.jme_query[i].readMolFile(mol);
		}
	}
}

/**
 * 入力フィールドの内容を保持する
 */
function storeFields() {
	var fq = document.form_query;

	for ( i = 0; i < PEAK_NUM; i++ ) {
		peak[i] = fq["mz" + i].value;
	}
	tol = document.form_query["tol"].value;
	piCheckNum = fq.pi_check.selectedIndex;

	// Instrument Typeチェックボックス
	var obj1 = fq["inst_grp"];
	if ( obj1.length > 1 ) {
		boxGrp = new Array(obj1.length);
		for ( i = 0; i < obj1.length; i++ ) {
			boxGrp[i] = obj1[i].checked;
		}
	}
	else {
		boxGrp = new Array(1);
		boxGrp[0] = obj1.checked;
	}

	var obj2 = fq["inst"];
	if ( obj2.length > 1 ) {
		boxInst = new Array(obj2.length);
		for ( i = 0; i < obj2.length; i++ ) {
			boxInst[i] = obj2[i].checked;
		}
	}
	else {
		boxInst = new Array(1);
		boxInst[0] = obj2.checked;
	}

	// Ionization Modeラジオボタン
	var obj3 = fq["ion"];
	for ( i = 0; i < obj3.length; i++ ) {
		if ( obj3[i].checked ) {
			ionCheckNum = i;
			break;
		}
	}
}

/**
 * 入力フィールドの内容を書き戻す
 */
function restoreFields() {
	var fq = document.form_query;

	for ( i = 0; i < PEAK_NUM; i++ ) {
		fq["mz" + i].value = peak[i];
	}
	fq["tol"].value = tol;
	fq.pi_check[piCheckNum].selected = true;

	// Instrument Typeチェックボックス
	var obj1 = fq["inst_grp"]
	if ( boxGrp.length == 1 ) {
		obj1.checked = boxGrp[0];
	}
	else {
		for ( i = 0; i < boxGrp.length; i++ ) {
			obj1[i].checked = boxGrp[i];
		}
	}
	var obj2 = fq["inst"]
	if ( boxInst.length == 1 ) {
		obj2.checked = boxInst[0];
	}
	else {
		for ( i = 0; i < boxInst.length; i++ ) {
			obj2[i].checked = boxInst[i];
		}
	}
	// Ionization Modeラジオボタン
	var obj3 = fq["ion"];
	obj3[ionCheckNum].checked = true;
}

/**
 * Molfile読み込み画面を表示する
 */
function readMolfile(index) {

	// 入力フィールドの内容を保持する
	storeFields();
	
	// クエリ側HTMLを保持する
	var ele = document.getElementById("tbl_query");
	html = ele.innerHTML;
	
	var newHtml =
			"<b>Read Molfile</b><br>"
		  + "<form action='./StructureSearch.html' enctype='multipart/form-data' method='POST'>"
		  + "<input type='file' name='file' size='50'><br><br>"
		  + "<input type='submit' value='OK'>"
		  + "<input type='button' value='CANCEL' onClick='cancelEditor()'>"
		  + "<input type='hidden' name='num' value='" + String(index) + "'>";
	
	var fq = document.form_query;

	// 入力フィールドを隠しパラメータとしてセットする
	for ( i = 0; i < QUERY_NUM; i++ ) {
		var moldata = fq["moldata" + i].value;
		newHtml += "<input type='hidden' name='moldata" + String(i) + "' value='" + moldata + "'>";
	}
	
	for ( i = 0; i < PEAK_NUM; i++ ) {
		newHtml += "<input type='hidden' name='mz" + String(i) + "' value='" + peak[i] + "'>";
	}
	newHtml += "<input type='hidden' name='tol' value='" + tol + "'>";
	newHtml += "<input type='hidden' name='pi_check' value='" + PI_CHECK_VAL[piCheckNum] + "'>";

	var obj1 = fq["inst_grp"];
	if ( obj1.length == 1 ) {
		if ( obj1.checked ) {
			newHtml += "<input type='hidden' name='inst_grp' value='" + obj1.value + "'>";
		}
	}
	else {
		for ( i = 0; i < obj1.length; i++ ) {
			if ( obj1[i].checked ) {
				newHtml += "<input type='hidden' name='inst_grp' value='" + obj1[i].value + "'>";
			}
		}
	}
	var obj2 = fq["inst"];
	if ( obj2.length == 1 ) {
		if ( obj2.checked ) {
			newHtml += "<input type='hidden' name='inst' value='" + obj2.value + "'>";
		}
	}
	else {
		for ( i = 0; i < obj2.length; i++ ) {
			if ( obj2[i].checked ) {
				newHtml += "<input type='hidden' name='inst' value='" + obj2[i].value + "'>";
			}
		}
	}
	var obj3 = fq["ion"];
	for ( i = 0; i < obj3.length; i++ ) {
		if ( obj3[i].checked ) {
			ionCheckNum = i;
			break;
		}
	}
	newHtml += "<input type='hidden' name='ion' value='" + obj3[ionCheckNum].value + "'>";
	newHtml += "</form>";
	
	// HTMLをMolfile読み込み画面に書き換える
	ele.innerHTML = newHtml;
}

/**
 * クエリをクリアする
 */
function clearQuery(index) {
	document.jme_query[index].reset();
	document.form_query["moldata" + index].value = "";
}
-->
</script>
</head>
<body class="msbkFont" onload="document.jme_query[0].reset();updateQuery();">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Substructure Search</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<iframe src="./menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	<hr size="1">

	<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
	<jsp:include page="../pserver/ServerInfo.jsp" />

	<div id="tbl_query">
		<form name="form_query" method="post" action="./jsp/Result.jsp" style="display:inline">
			<input type="hidden" name="type" value="struct">
			<table>
				<tr>
					<td>
						<table border="0" cellpadding="0" cellspacing="0" class="form-box">
							<tr>
								<td bgcolor="dimgray"><font color="white"><b>&nbsp;Substructure</b></font></td>
							</tr>
							<tr>
								<td>
									<table border="0" cellspacing="0" cellpadding="20">
<%
	boolean isRead = false;
	String readMolData = "";
	String updateIndex = "";
	String[] molData = { "", "" };
	String[] mz = { "", "", "" };
	String tol = "0.3";
	String piCheck = "2";
	boolean isInvalidQuery = false;
	List<String> instGrpList = new ArrayList<String>();
	List<String> instTypeList = new ArrayList<String>();
	String ionMode = "1";
	boolean isFirst = true;
	
	DiskFileUpload dfu = new DiskFileUpload();
	//=============================================
	// マルチパートフォームの場合（Read Molfile時)
	//=============================================
	if ( DiskFileUpload.isMultipartContent(request) ) {
		isFirst = false;
		String tempDir = System.getProperty("java.io.tmpdir");
		dfu.setSizeMax(-1);								// サイズ
		dfu.setSizeThreshold(1024);						// バッファサイズ
		dfu.setRepositoryPath(tempDir);					// 保存先フォルダ
		dfu.setHeaderEncoding("Windows-31J");			// ヘッダの文字エンコーディング
		List list = dfu.parseRequest(request);
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
				else {
					setParams( key, val, molData, mz );
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
		Enumeration names = request.getParameterNames();
		if ( names.hasMoreElements() ) {
			isFirst = false;
		}
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			String val = request.getParameter(key);
			if ( key.equals("tol") ) {
				tol = val;
			}
			else if ( key.equals("pi_check") ) {
				piCheck = val;
			}
			else if ( key.equals("inst_grp") ) {
				String[] vals = request.getParameterValues(key);
				instGrpList = Arrays.asList(vals);
			}
			else if ( key.equals("inst") ) {
				String[] vals = request.getParameterValues(key);
				instTypeList = Arrays.asList(vals);
			}
			else if ( key.equals("ion") ) {
				ionMode = val;
			}
			else {
				setParams( key, val, molData, mz );
			}
		}
	}

	if ( isRead && NumberUtils.isNumber(updateIndex) ) {
		molData[Integer.parseInt(updateIndex)] = "@data=" + readMolData;
	}

	for ( int i = 0; i < molData.length; i++ ) {
		if ( i % 2 == 0 ) {
			out.println( "\t\t\t\t\t\t\t<tr>" );
		}
		String number = String.valueOf(i);
		out.println( "\t\t\t\t\t\t\t\t<td>" );
		out.println( "\t\t\t\t\t\t\t\t\t<b>Query" + String.valueOf(i+1)  + "</b><br>" );
		out.println( "\t\t\t\t\t\t\t\t\t<applet name=\"jme_query\" code=\"JME.class\" archive=\"./applet/JME.jar\" width=\"200\" height=\"145\">" );
		out.println( "\t\t\t\t\t\t\t\t\t\t<param name=\"options\" value=\"depict,border\">" );
		out.println( "\t\t\t\t\t\t\t\t\t</applet><br>" );
		out.println( "\t\t\t\t\t\t\t\t\t<input type=\"button\" value=\"Edit\" onClick=\"toEditor(" + number + ");\">" );
		out.println( "\t\t\t\t\t\t\t\t\t<input type=\"button\" value=\"Molfile\" onClick=\"readMolfile(" + number + ");\">" );
		out.println( "\t\t\t\t\t\t\t\t\t<input type=\"button\" value=\"Clear\" onClick=\"clearQuery(" + number + ");\"><br>" );
		out.println( "\t\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"moldata" + number + "\" value=\"" + molData[i] + "\">" );
		
		if ( isInvalidQuery && Integer.parseInt(updateIndex) == i ) {
			out.println( "<font color=\"red\"><b>>> invalid data</b></font>" );
		}
		else {
			out.println( "<br>" );
		}
		
		out.println( "\t\t\t\t\t\t\t\t</td>" );
		
		if ( i % 2 == 0 ) {
			out.println( "\t\t\t\t\t\t\t\t<td width=\"30\" style=\"padding:0px;\">AND</td>" );
		}
		
		if ( i % 2 != 0 ) {
			out.println( "\t\t\t\t\t\t\t</tr>" );
		}
	}
	out.println( "\t\t\t\t\t\t</table>" );
	out.println( "\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t</tr>" );
	out.println( "\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t<td style=\"padding:10px;\"><font class=\"font12px\">Comparison of pi-electron for each atom</font>&nbsp;" );

	String[] piCheckName = { "number in query = number in target", "number in query <= number in target", "none" };
	String[] piChekNum = { "2", "1", "0" };
	out.println( "\t\t\t\t\t\t<select name=\"pi_check\">" );
	for ( int i = 0; i < piCheckName.length; i++ ) {
		out.print( "\t\t\t\t\t\t\t<option value=\"" + piChekNum[i] + "\"" );
		if ( piCheck.equals(piChekNum[i]) ) {
			out.print( " selected" );
		}
		out.println( ">" + piCheckName[i] );
	}
	out.println( "\t\t\t\t\t\t</select><br>" );
	out.println( "&nbsp;<font class=\"font12px\">* Double and triple bound is translated to pi-electrons of the bonded atoms.</font><br>" );	out.println( "\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t</tr>" );
	out.println( "\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t<td align=\"right\">" );
	out.println( "\t\t\t\t\t\t<font class=\"font10px\">Copyright 2008 by K. Tanaka and S. Kanaya, NAIST, Japan&nbsp;&nbsp;</font>" );
	out.println( "\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t<tr>" );
	out.println( "\t\t\t</table>" );
	out.println( "\t\t</td>" );

	String instGrp = "";
	for ( int i = 0; i < instGrpList.size(); i++ ) {
		instGrp += instGrpList.get(i);
		if ( i < instGrpList.size() - 1 ) {
			instGrp += ",";
		}
	}
	String instType = "";
	for ( int i = 0; i < instTypeList.size(); i++ ) {
		instType += instTypeList.get(i);
		if ( i < instTypeList.size() - 1 ) {
			instType += ",";
		}
	}
%>
					<td valign="top" style="padding:2px 15px;">
						<jsp:include page="Instrument.jsp" flush="true">
							<jsp:param name="ion" value="<%= ionMode %>" />
							<jsp:param name="first" value="<%= isFirst %>" />
							<jsp:param name="inst_grp" value="<%= instGrp %>" />
							<jsp:param name="inst" value="<%= instType %>" />
						</jsp:include>
					</td>
				</tr>
			</table>
			<table>
				<tr>
					<td colspan="2" style="padding-left:5px;">
						<b>Peak Search&nbsp;&nbsp;(Option)</b>
					</td>
				</tr>
				<tr>
					<td width="150" style="padding-left:15px;">
						<i>m/z</i>
					</td>
					<td>
<%
	for ( int i = 0; i < mz.length; i++ ) {
		String num = String.valueOf(i);
		out.print( "\t\t\t\t\t\t<input name=\"mz" + num + "\" type=\"text\" size=\"10\" value=\"" + mz[i] + "\">" );
		if ( i < mz.length - 1 ) {
			out.print( "&nbsp;,&nbsp;" );
		}
		out.println( "" );
	}
	out.print( "\t\t\t\t\t" );
%>
					</td>
				</tr>
				<tr>
					<td style="padding-left:15px;">
						Tolerance of <i>m/z</i>
					</td>
					<td>
						<input name="tol" type="text" size="10" value="<% out.print(tol); %>">
					</td>
				</tr>
			</table>
			<input type="submit" value="Search" onclick="return checkSubmit();" class="search">
		</form>
	</div>
	<br>
	<hr size="1">
	<iframe src="./copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
