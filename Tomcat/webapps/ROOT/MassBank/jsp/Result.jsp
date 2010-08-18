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
 * 検索結果ページ表示用モジュール
 *
 * ver 2.0.25 2010.08.18
 *
 ******************************************************************************/
%>
<%@ page import="java.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.ResultList" %>
<%@ page import="massbank.ResultRecord" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ include file="./Common.jsp"%>
<%!
	// 画面内テーブルタグ幅
	private static final String tableWidth = "950";
	
	// 検索結果テーブル列幅
	private static final String[] width = { "28", "28", "422", "142", "102", "122", "100" };
	
	// イメージファイルURL
	private final String minuspng = "../image/minus.png";
	private final String pluspng = "../image/plus.png";
	private final String defaultGif = "../image/default.gif";
	private final String ascGif = "../image/asc.gif";
	private final String descGif = "../image/desc.gif";

	/**
	 * 構造式情報を一括取得する
	 * @param list
	 * @param startIndex
	 * @param endIndex
	 * @param serverUrl
	 * @param urlList
	 * @param dbNameList
	 * @return List<Map>(Map<String, String>, Map<String, String>, Map<String, String>) 画像とMolfile情報をそれぞれ格納したMapをListに格納
	 */
	private List<Map> getStructure(ResultList list, int startIndex, int endIndex, String serverUrl, String[] urlList, String[] dbNameList) {
		List<Map> resultList = new ArrayList<Map>(4);
		
		String prevName = "";
		String param = "";
		for ( int i = startIndex; i <= endIndex; i++ ) {
			ResultRecord rec = list.getRecord(i);
			String name = rec.getName();
			if ( !name.equals(prevName) ) {
				String ename = "";
				try {
					ename = URLEncoder.encode( name, "utf-8" );
				}
				catch ( UnsupportedEncodingException e ) {
 					e.printStackTrace();
				}
				param += ename + "@";
			}
			prevName = name;
		}
		if ( !param.equals("") ) {
			param = param.substring(0, param.length()-1);
			param = "&names=" + param;
		}
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETSTRUCT];
		ArrayList result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );
		
		Map<String, String> gifMap = new HashMap<String, String>();
		Map<String, String> gifSmallMap = new HashMap<String, String>();
		Map<String, String> gifLargeMap = new HashMap<String, String>();
		Map<String, String> molMap = new HashMap<String, String>();
		String key = "";
		int siteNo = -1;
		String gifUrl = "";
		String gifSmallUrl = "";
		String gifLargeUrl = "";
		String molData = "";
		for ( int i = 0; i < result.size(); i++ ) {
			String temp = (String)result.get(i);
			String[] item = temp.split("\t");
			String line = item[0];
			if ( line.indexOf("---NAME:") >= 0 ) {
				if ( !key.equals("") ) {
					// GIFURL格納
					if ( !gifMap.containsKey(key) && !gifUrl.trim().equals("")) {
						gifMap.put(key, gifUrl);
					}
					// GIFSMALLURL格納
					if ( !gifSmallMap.containsKey(key) && !gifSmallUrl.trim().equals("")) {
						gifSmallMap.put(key, gifSmallUrl);
					}
					// GIFLARGEURL格納
					if ( !gifLargeMap.containsKey(key) && !gifLargeUrl.trim().equals("")) {
						gifLargeMap.put(key, gifLargeUrl);
					}
					// Molfileデータ格納
					else if ( !molMap.containsKey(key) && !molData.trim().equals("")) {
						molMap.put(key, molData);
					}
				}
				// 次のデータのキー名
				key = line.substring(8).toLowerCase();
				siteNo = Integer.parseInt(item[1]);
				gifUrl = "";
				gifSmallUrl = "";
				gifLargeUrl = "";
				molData = "";
			}
			else if ( line.indexOf("---GIF:") != -1 ) {
				String gifFile = line.replaceAll("---GIF:", "");
				if ( siteNo == 0 ) {
					gifUrl = serverUrl + "DB/gif/" + dbNameList[siteNo] + "/" + gifFile;
				}
				else {
					gifUrl = urlList[siteNo] + "DB/gif/" + dbNameList[siteNo] + "/" + gifFile;
				}
			}
			else if ( line.indexOf("---GIF_SMALL:") != -1 ) {
				String gifFile = line.replaceAll("---GIF_SMALL:", "");
				if ( siteNo == 0 ) {
					gifSmallUrl = serverUrl + "DB/gif_small/" + dbNameList[siteNo] + "/" + gifFile;
				}
				else {
					gifSmallUrl = urlList[siteNo] + "DB/gif_small/" + dbNameList[siteNo] + "/" + gifFile;
				}
			}
			else if ( line.indexOf("---GIF_LARGE:") != -1 ) {
				String gifFile = line.replaceAll("---GIF_LARGE:", "");
				if ( siteNo == 0 ) {
					gifLargeUrl = serverUrl + "DB/gif_large/" + dbNameList[siteNo] + "/" + gifFile;
				}
				else {
					gifLargeUrl = urlList[siteNo] + "DB/gif_large/" + dbNameList[siteNo] + "/" + gifFile;
				}
			}
			else {
				// JME Editor 
				if ( line.indexOf("M  CHG") >= 0 ) {
					continue;
				}
				molData += line + "|\n";
			}
		}
		if ( !gifMap.containsKey(key) && !gifUrl.trim().equals("") ) {
			gifMap.put(key, gifUrl);
		}
		if ( !gifSmallMap.containsKey(key) && !gifSmallUrl.trim().equals("") ) {
			gifSmallMap.put(key, gifSmallUrl);
		}
		if ( !gifLargeMap.containsKey(key) && !gifLargeUrl.trim().equals("") ) {
			gifLargeMap.put(key, gifLargeUrl);
		}
		if ( !molMap.containsKey(key) && !molData.trim().equals("") ) {
			molMap.put(key, molData);
		}
		resultList.add(gifMap);
		resultList.add(gifSmallMap);
		resultList.add(gifLargeMap);
		resultList.add(molMap);
		
		return resultList;
	}
%>
<%
	MassBankCommon mbcommon = new MassBankCommon();
	
	//-------------------------------------
	// リファラー（遷移元）、及びタイトル設定
	//-------------------------------------
	boolean refPeak     = false;			// PeakSearch
	boolean refPeakDiff = false;			// PeakDifferenceSearch
	boolean refQuick    = false;			// QuickSearch
	boolean refRecIndex = false;			// RecordIndex
	boolean refStruct   = false;			// Substructure Search
	String title = "";								// タイトル
	
	String type = request.getParameter("type");
	if ( type == null ) {
		out.println( "<html>" );
		out.println( "<head>" );
		out.println( " <link rel=\"stylesheet\" type=\"text/css\" href=\"../css/Common.css\">" );
		out.println( " <title>MassBank | Database | Results</title>" );
		out.println( "</head>" );
		out.println( "<body class=\"msbkFont cursorDefault\">" );
		out.println( "<h1>Results</h1>" );
		out.println( "<iframe src=\"../menu.html\" width=\"860\" height=\"30px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		out.println( "<hr size=\"1\">" );
		out.println( "<b>Search Parameters :</b><br>" );
		out.println( "<div class=\"divSpacer9px\"></div>" );
		out.println( "<hr size=\"1\">" );

		out.println( "<table width=\"900\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td>" );
		out.println( "   <b>Results : <font color=\"green\">0 Hit.</font></b>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		out.println( "</form>" );
		out.println( "<hr size=\"1\">" );
		out.println( "<iframe src=\"../copyrightline.html\" width=\"800\" height=\"20px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		out.println( "</body>" );
		out.println( "</html>" );
		return;
	}
	
	if ( type.equals(MassBankCommon.REQ_TYPE_PEAK) ) {			// 遷移元がPeakSearch
		refPeak = true;
		title = "Peak Search Results";
	}
	else if ( type.equals(MassBankCommon.REQ_TYPE_PEAKDIFF) ) {	// 遷移元がPeakDifferenceSearch
		refPeakDiff = true;
		title = "Peak Difference Search Results";
	}
	else if ( type.equals(MassBankCommon.REQ_TYPE_QUICK) ) {	// 遷移元がQuickSearch
		refQuick = true;
		title = "Quick Search Results";
	}
	else if ( type.equals(MassBankCommon.REQ_TYPE_RCDIDX) ) {	// 遷移元がRecordIndex
		refRecIndex = true;
		title = "Record Index Results";
	}
	else if ( type.equals(MassBankCommon.REQ_TYPE_STRUCT) ) {	// 遷移元がSubstructure Search
		refStruct = true;
		title = "Substructure Search Results";
	}
	
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	// 全てのリクエストパラメータをハッシュに持つ
	Hashtable<String, Object> reqParams = new Hashtable<String, Object>();	// リクエストパラメータ格納用ハッシュ
	Enumeration names = request.getParameterNames();
	String exec = (request.getParameter("exec") != null) ? (String)request.getParameter("exec") : "";
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( exec.equals("sort") || exec.equals("page")) {
			// ソートまたはページ変更の場合はチェックボックスの値を保持しない
			if ( key.equals("chkAll") || key.equals("pid") || key.equals("id") ) {
				continue;
			}
		}
		if ( key.indexOf("inst") == -1 ) {
			// キーがInstrumentType以外の場合はStringパラメータ
			String val = request.getParameter( key ).trim();
			reqParams.put( key, val );
		}
		else {
			// キーがInstrumentTypeの場合はString配列パラメータ
			String[] vals = request.getParameterValues( key );
			reqParams.put( key, vals );
		}
	}
	// パラメータリセット
	reqParams.put("exec", "");
	// デフォルト値設定
	if ( reqParams.get("pageNo") == null ) {
		reqParams.put("pageNo", "1");
	}
	if ( reqParams.get("sortKey") == null ) {
		reqParams.put("sortKey", ResultList.SORT_KEY_NAME);
	}
	if ( reqParams.get("sortAction") == null ) {
		reqParams.put("sortAction", String.valueOf(ResultList.SORT_ACTION_ASC));
	}
	
	//-------------------------------------
	// リクエストパラメータ加工、及びURLパラメータ生成
	//-------------------------------------
	String searchParam = "";				// URLパラメータ（検索実行用）
	String recordParam = "";				// URLパラメータ（レコードページ表示用）
	
	// PeakSearch／PeakDifferenceSearch検索条件退避用
	ArrayList<String> mzArray = null;
	ArrayList<String> opArray = null;
	ArrayList<String> tolArray = null;
	ArrayList<String> valArray = null;
	
	int condFormNum = 0;					// 検索条件フォーム数
	
	// ◇ PeakSearch／PeakDifferenceSearchの場合
	if ( refPeak || refPeakDiff ) {
		// m/zが空白の検索条件トリム処理を行う
		// 6つの検索条件入力フォームのうちm/zが空白の検索条件をトリムして上につめる
		
		int condNum = 0;					// 検索条件数
		
		mzArray = new ArrayList<String>();
		opArray = new ArrayList<String>();
		tolArray = new ArrayList<String>();
		valArray = new ArrayList<String>();
		
		if ( reqParams.get("num") == null ) {
			condFormNum = MassBankCommon.PEAK_SEARCH_PARAM_NUM;
		}
		else {
			condFormNum = Integer.parseInt((String)reqParams.get("num"));
		}
		
		String tol = ((String)reqParams.get( "tol" )).replaceAll(" ", "").replaceAll("　", "");
		String val = ((String)reqParams.get( "int" )).replaceAll(" ", "").replaceAll("　", "");
		// m/z空白の検索条件を除いた検索条件を退避
		for ( int i=0; i < condFormNum; i++ ) {
			// テキストボックスからの入力値は全ての全半角スペースをトリムする
			String mz  = (String)reqParams.get( "mz"  + i );
			if ( mz == null ) {
				break;
			}
			mz = mz.replaceAll(" ", "").replaceAll("　", "").replaceAll("&#12288;", "");
			String op  = (String)reqParams.get( "op"  + i );
			
			if ( mz.equals("") ) {
				continue;
			}
			condNum++;
			recordParam += "&"
						+ "mz"  + i + "=" + mz + "&"
						+ "tol"  + i + "=" + tol + "&"
						+ "int"  + i + "=" + val;
			
			mzArray.add(mz);
			opArray.add(op);
			tolArray.add(tol);
			valArray.add(val);
		}
		recordParam += "&num=" + Integer.toString(condNum);
		
		// 検索条件数(m/zが空白の検索条件を除いた数)を設定
		reqParams.put( "num", String.valueOf(condNum) );
		
		// m/z空白の検索条件を除いた検索条件をパラメータとして再設定、及び不要リクエストパラメータ削除
		for ( int i = 0; i < condFormNum; i++ ) {
			if ( i < condNum ) {
				reqParams.put( "mz"  + i, mzArray.get(i) );
				reqParams.put( "op"  + i, opArray.get(i) );
				reqParams.put( "tol" + i, tolArray.get(i) );
				reqParams.put( "int" + i, valArray.get(i) );
			}
			else {
				reqParams.put( "mz"  + i, "" );
				reqParams.put( "op"  + i, "and" );
				reqParams.put( "tol" + i, "0.3" );
				reqParams.put( "int" + i, "100" );
			}
			reqParams.remove( "fom" + i );
		}
		reqParams.put( "op0", "or" );
	}
	
	// URLパラメータ（検索実行用）生成
	for ( Enumeration keys = reqParams.keys(); keys.hasMoreElements(); ){
		String key = (String)keys.nextElement();
		if ( key.indexOf("inst") == -1 ) {
			// キーがInstrumentType以外の場合はStringパラメータ
			String val = (String)reqParams.get(key);
			if ( !val.equals("") ) {
				searchParam += key + "=" + URLEncoder.encode(val,"utf-8") + "&";
			}
		}
		else {
			String[] vals = (String[])reqParams.get(key);
			for ( int i=0; i<vals.length; i++ ) {
				searchParam += key + "=" + URLEncoder.encode(vals[i], "utf-8") + "&";
			}
		}
	}
	searchParam = searchParam.substring( 0, searchParam.length() -1 );
	
	
	//-------------------------------------------
	// 設定ファイルから各種情報を取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	String baseUrl = path.substring( 0, (path.indexOf("/jsp")+1) );
	GetConfig conf = new GetConfig(baseUrl);
	String serverUrl = conf.getServerUrl();				// サーバURL取得
	String [] siteLongName = conf.getSiteLongName();	// サイト名取得
	String[] dbNameList = conf.getDbName();
	String[] urlList = conf.getSiteUrl();
%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Cache-Control" content="no-cache">
	<meta http-equiv="Expires" content="0">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="description" content="Mass Spectrum Search Results">
	<meta name="keywords" content="Results">
	<meta name="revisit_after" content="10 days">
	<link rel="stylesheet" type="text/css" href="../css/Common.css">
	<link rel="stylesheet" type="text/css" href="../css/Result.css">
	<link rel="stylesheet" type="text/css" href="../css/ResultMenu.css">
	<script type="text/javascript" src="../script/Common.js"></script>
	<script type="text/javascript" src="../script/Result.js"></script>
	<script type="text/javascript" src="../script/ResultMenu.js"></script>
	<script type="text/javascript" src="../script/StructSearch.js"></script>
	<script type="text/javascript" src="../script/jquery_imgprev.js"></script>
	<title>MassBank | Database | <%=title%></title>
</head>
<body class="msbkFont cursorDefault">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td><h1><%=title%></h1></td>
			<td align="right" class="font12px">
				<img src="../img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="../img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
<iframe src="../menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<hr size="1">
<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
<jsp:include page="../pserver/ServerInfo.jsp" />
<%
	//-------------------------------------
	// 検索条件パラメータ表示
	//-------------------------------------
	out.println( "<a name=\"resultsTop\"></a>" );
	
	// ◇ PeakSearch／PeakDifferenceSearch／QuickSearchの場合
	if ( refPeak || refPeakDiff || refQuick ) {
		
		out.println( "<b>Search Parameters :</b><br>" );
		
		// ◇ PeakSearch／PeakDifferenceSearchの場合
		if ( refPeak || refPeakDiff ) {
			// m/z、Rel.Int、Tol(unit)
			for (int i = 0; i < mzArray.size(); i++ ) {
				if ( i == 0 ) {
					out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
				}
				else if ( i > 0 ) {
					out.println( " <tr>" );
					out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"Crimson\"><b>" + opArray.get(i) + "</b></font></td>" );
					out.println( " </tr>" );
				}
				out.println( " <tr>" );
				out.print( "  <td>" );
				// ◇ PeakSearchの場合
				if ( refPeak ) {
					out.print( "&nbsp;&nbsp;&nbsp;<i>m/z</i>:" );
				}
				// ◇ PeakDifferenceSearchの場合
				else if ( refPeakDiff ) {
					out.print( "&nbsp;&nbsp;&nbsp;<i>m/z</i>&nbsp;Dif.:" );
				}
				out.print( "&nbsp;&nbsp;<b>" + mzArray.get(i) + "</b>" );
				out.print( "&nbsp;&nbsp;&nbsp;&nbsp;Rel.Int:&nbsp;&nbsp;<b>" + valArray.get(i) + "</b>" );
				out.print( "&nbsp;&nbsp;&nbsp;&nbsp;Tol.(unit):&nbsp;&nbsp;<b>" + tolArray.get(i) + "</b>" );
				out.println( "<td>" );
				if ( i == (mzArray.size()-1) ) {
					out.println( "</table>" );
				}
			}
		}
		// ◇ QuickSearchの場合
		else if ( refQuick ) {
			boolean isBinder = false;
			String pName = ((String)reqParams.get("compound") != null) ? (String)reqParams.get("compound") : "";
			String pMz = ((String)reqParams.get("mz") != null) ? (String)reqParams.get("mz") : "";
			String pOp1 = ((String)reqParams.get("op1") != null) ? (String)reqParams.get("op1") : "and";
			String pTol = ((String)reqParams.get("tol") != null) ? (String)reqParams.get("tol") : "";
			String pFormula = ((String)reqParams.get("formula") != null) ? (String)reqParams.get("formula") : "";
			String pOp2 = ((String)reqParams.get("op2") != null) ? (String)reqParams.get("op2") : "and";
			
			// Compound Name
			if ( !pName.equals("") ) {
				out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
				out.println( " <tr>" );
				out.println( "  <td>&nbsp;&nbsp;&nbsp;Compound Name:&nbsp;&nbsp;<b>" + pName + "</b></td>" );
				out.println( " </tr>" );
				out.println( "</table>" );
				isBinder = true;
			}
			
			// Exact Mass of Compound
			if ( !pMz.equals("") ) {
				out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
				out.println( " <tr>" );
				if ( isBinder ) {
					out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"Crimson\"><b>" + pOp1 + "</b></font></td>" );
					out.println( " </tr>" );
				}
				out.println( "  <td>&nbsp;&nbsp;&nbsp;Exact Mass of Compound:&nbsp;&nbsp;<b>" + pMz + "</b>&nbsp;&nbsp;(Tolerance:&nbsp;&nbsp;<b>" + pTol + "</b>)</td>" );
				out.println( " </tr>" );
				out.println( "</table>" );
				isBinder = true;
			}
			
			// Formula
			if ( !pFormula.equals("") ) {
				out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
				out.println( " <tr>" );
				if ( isBinder ) {
					out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"Crimson\"><b>" + pOp2 + "</b></font></td>" );
					out.println( " </tr>" );
				}
				out.println( "  <td>&nbsp;&nbsp;&nbsp;Formula:&nbsp;&nbsp;<b>" + pFormula + "</b></td>" );
				out.println( " </tr>" );
				out.println( "</table>" );
			}
		}
		
		out.println( "<div class=\"divSpacer9px\"></div>" );
		
		// Instrument Type
		String[] instrument = (reqParams.get("inst") != null) ? (String[])reqParams.get("inst") : new String[]{};
		boolean isAll = false;
		out.println( "<table width=\"" + (Integer.parseInt(tableWidth)-160) + "\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td width=\"132\">&nbsp;&nbsp;&nbsp;Instrument Type:</td>" );
		for (int i=0; i<instrument.length; i++) {
			if ( instrument[i].equals("all") ) {
				out.println( "  <td><b>All</b></td>" );
				isAll = true;
				break;
			}
		}
		if ( !isAll ) {
			int instCount = 0;
			for (int i=0; i<instrument.length; i++) {
				instCount++;
				out.print( "  <td width=\"201\"><b>" + instrument[i] + "</b>" );
				if ( i != (instrument.length-1) ) {
					if ( (instCount % 3) != 0 ) {
						out.println( " , </td>" );
					}
					else {
						out.println( "</td>" );
						out.println( " </tr>" );
						out.println( " <tr>" );
						out.println( "  <td width=\"132\"></td>" );
					}
				}
				else {
					if ( instCount < 3 ) {
						out.println( "</td><td>&nbsp;</td>" );
					}
					else {
						out.println( "</td>" );
					}
				}
			}
		}
		out.println( " </tr>" );
		out.println( "</table>" );
		
		// Ionization Mode
		String pIon = ((String)reqParams.get( "ion" ) != null) ? (String)reqParams.get( "ion" ) : "0";
		String ionMode = "";
		switch (Integer.parseInt(pIon)) {
			case 0:
				ionMode = "Both";
				break;
			case 1:
				ionMode = "Positive";
				break;
			case -1:
				ionMode = "Negative";
				break;
			default:
				ionMode = "Both";
				break;
		}
		out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td width=\"132\">&nbsp;&nbsp;&nbsp;Ionization Mode:</td>" );
		out.println( "  <td><b>" + ionMode + "</b></td>" );
		out.println( "  <td align=\"right\">" );
		out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting('" + type + "')\">Edit / Resubmit Query</a>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
	}
	// ◇ RecordIndexの場合
	else if ( refRecIndex ) {
		out.println( "<b>Index Type :</b><br>" );
		
		String pIdxtype = ((String)reqParams.get( "idxtype" ) != null) ? (String)reqParams.get( "idxtype" ) : "";
		String pSrchkey = ((String)reqParams.get( "srchkey" ) != null) ? (String)reqParams.get( "srchkey" ) : "";
		
		out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		
		if ( pIdxtype.equals("site") ) {		// Site
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Contributor: <b>"
				+ siteLongName[Integer.parseInt(pSrchkey)] + "</b></td>" );
		}
		else if ( pIdxtype.equals("inst") ) {	// Instrument Type
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Instrument Type: <b>" + pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("ion") ) {	// Ionization Mode
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Ionization Mode: <b>" + pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("cmpd") ) {	// Compound Name
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Compound Name: <b>" + pSrchkey + "</b></td>" );
		}
		
		out.println( "  <td align=\"right\">" );
		out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting('" + type + "')\">Edit / Resubmit Query</a>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
	}
	// ◇ Substructure Searchの場合
	else if ( refStruct ) {
		boolean existMoldata = false;
		
		out.println( "<table>" );
		out.println( "<tr>" );
		for ( int n = 0; n < 4; n++ ) {
			String key = "moldata" + String.valueOf(n);
			if ( reqParams.get(key) != null ) {
				String moldata = (String)reqParams.get(key);
				if ( !moldata.equals("") ) {
					existMoldata = true;
					moldata = moldata.replace( "@data=", "" );
					moldata = moldata.replace( "\n","@LF@" );
					// データ判定
					if ( StringUtils.countMatches(moldata, "@LF@") < 5 ) {
						existMoldata = false;
						break;
					}

					
					out.println( "<td>" );
					out.println( "<b>Query" + String.valueOf(n+1) + "</b><br>" );
					out.println( "<applet code=\"MolView.class\" archive=\"../applet/MolView.jar\" width=\"200\" height=\"200\">" );
					out.println( " <param name=\"moldata\" value=\"" + moldata + "\">" );
					out.println( "</applet>" );
					out.println( "</td>" );
				}
			}
		}
		if ( !existMoldata ) {
			out.println( "<td style=\"width:200px; height:224px; vertical-align:top;\">" );
			out.println( "<b>Query1</b><br>" );
			out.println( "<div style=\"width:182px; height:182px; border:1px solid #000000; margin-top:10px; margin-left:10px;\">&nbsp;</div>" );
			out.println( "</td>" );
		}

		String[] mz = new String[5];
		int cntPeak = 0;
		for ( int n = 0; n < mz.length; n++ ) {
			String key = "mz" + String.valueOf(n);
			if ( reqParams.get(key) == null ) {
				continue;
			}
			String val = (String)reqParams.get(key);
			val = val.trim();
			if ( !val.equals("") ) {
				mz[cntPeak++] = val;
			}
		}
		String tol = "";
		if ( reqParams.get("tol") != null ) {
			String val = (String)reqParams.get("tol");
			tol = val.trim();
			if ( tol.equals("") ) {
				tol = "0.3";
			}
		}
		
		if ( cntPeak > 0 ) {
			out.println( "<td valign=\"top\" style=\"padding-left:25px;\">" );
			out.println( "<b>Parameters of Peak Search</b><br>" );
			out.print( "&nbsp;&nbsp;<i>m/z:</i>&nbsp" );
			for ( int n = 0; n < cntPeak; n++ ) {
				out.print( "<b>" + mz[n] + "</b>" );
				if ( n < cntPeak - 1 ) {
					out.print( ",&nbsp;&nbsp;" );
				}
			}
			out.println( "<br>" );
			out.println( "&nbsp;&nbsp;Tolerance:&nbsp;<b>" + tol + "</b>" );
			
			out.println( "</td>" );
		}
		
		out.println( "</tr>" );
		out.println( "<tr>");
		out.println( "<td>&nbsp;&nbsp;<a href=\"../StructureSearch.html\" class=\"pageLink\" onClick=\"return prevStructSearch()\">Edit / Resubmit Query</a></td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		
		// Molfileデータなし
		if ( !existMoldata ) {
			out.println( "<hr size=\"1\">" );
			out.println( "<span id=\"menu\"></span>" );
			out.println( "<table width=\"900\" cellpadding=\"0\" cellspacing=\"0\">" );
			out.println( "<tr>" );
			out.println( "<td>" );
			out.println( "<b>Results : <font color=\"green\">0 Hit.</font></b>" );
			out.println( "</td>" );
			out.println( "</tr>" );
			out.println( "</table>" );
			out.println( "<hr size=\"1\">" );
			out.println( "<iframe src=\"../copyrightline.html\" width=\"800\" height=\"20px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
			out.println( "</body>" );
			out.println( "</html>" );
			
			return;
		}
	}
	out.println( "<hr size=\"1\">" );


	//-------------------------------------
	// 検索実行・結果取得
	//-------------------------------------
	String typeName = "";
	ResultList list = null;
	
	// ◇ PeakSearchの場合
	if ( refPeak ) {
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_PEAK];
		list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
	}
	// ◇ PeakDifferenceSearchの場合
	else if ( refPeakDiff ) {
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_PDIFF];
		list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
	}
	// ◇ QuickSearchの場合
	else if ( refQuick ) {
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_QUICK];
		list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
	}
	// ◇ RecordIndexの場合
	else if ( refRecIndex ) {
		String pIdxtype = ((String)reqParams.get( "idxtype" ) != null) ? (String)reqParams.get( "idxtype" ) : "";
		String pSrchkey = ((String)reqParams.get( "srchkey" ) != null) ? (String)reqParams.get( "srchkey" ) : "";
		boolean isSingle = false;
		if ( pIdxtype.equals("site") ) {
			isSingle = true;
			searchParam = searchParam.replaceAll( "srchkey", "site" );
		}
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_RCDIDX];
		if ( isSingle ) {
			list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, false, pSrchkey, conf );
		}
		else {
			list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
		}
	}
	// ◇ Substructure Searchの場合
	else if ( refStruct ) {
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_STRUCT];
		list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
	}
	
	out.println( "<span id=\"menu\"></span>");
	out.println( "<form method=\"post\" action=\"Display.jsp\" name=\"resultForm\" target=\"_blank\" class=\"formStyle\">" );
	
	
	DecimalFormat numFormat = new DecimalFormat("###,###,###");
	int totalPage = list.getTotalPageNum();
	int pageNo = 0;
	try {
		pageNo = Integer.parseInt((String)reqParams.get( "pageNo" ));
		if ( totalPage < pageNo ) {
			pageNo = totalPage;
		}
		else if ( 1 > pageNo ) {
			pageNo = 0;
		}
	}
	catch ( NumberFormatException nfe ) {
		nfe.printStackTrace();
	}
	int[] dispIndex = list.getDispRecordIndex(pageNo);
	int startIndex = dispIndex[0];
	int endIndex = dispIndex[1];
	reqParams.put( "totalPageNo", String.valueOf(totalPage) );
	
	
	//-------------------------------------
	// リクエストパラメータを隠しフィールドに保持
	//-------------------------------------
	for ( Enumeration keys = reqParams.keys(); keys.hasMoreElements(); ){
		String key = (String)keys.nextElement();
		if ( key.indexOf("inst") == -1 ) {
			// キーがInstrumentType以外の場合はStringパラメータ
			String val = (String)reqParams.get(key);
			out.println( "<input type=\"hidden\" name=\"" + key + "\" value=\"" + val + "\">" );
		}
		else {
			// キーがInstrumentTypeの場合はString配列パラメータ
			String[] vals = (String[])reqParams.get(key);
			for (int i=0; i<vals.length; i++) {
				out.println( "<input type=\"hidden\" name=\"" + key + "\" value=\"" + vals[i] + "\">" );
			}
		}
	}
	
	
	//-------------------------------------
	// 検索結果数、ボタン出力
	//-------------------------------------
	out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
	out.println( " <tr>");
	out.println( "  <td>" );
	if ( list.getResultNum() > 0 ) {
		out.println( "   <b>Results : <font color=\"green\">" + numFormat.format(list.getResultNum()) 
			+ " Hit.</font>&nbsp;&nbsp;<font size=\"2\" color=\"green\">( " + numFormat.format(startIndex+1) 
			+ " - " + numFormat.format(endIndex+1) + " Displayed )</font></b>" );
	}
	else {
		out.println( "   <b>Results : <font color=\"green\">0 Hit.</font></b>" );
	}
	out.println( "  </td>" );
	if ( list.getResultNum() > 0 ) {
		out.println( "  <td align=\"right\">" );
		out.println( "   <input style=\"width:120\" type=\"button\" name=\"treeCtrl\" value=\"Open All Tree\" onClick=\"allTreeCtrl()\">" );
		out.println( "   <input style=\"width:120\" type=\"submit\" name=\"multi\" value=\"Multiple Display\" onClick=\"return submitShowSpectra();\">" );
		out.println( "   <input style=\"width:120\" type=\"button\" name=\"search\" value=\"Spectrum Search\" onClick=\"submitSearchPage();\">" );
		out.println( "  </td>" );
	}
	out.println( " </tr>" );
	out.println( "</table>" );
	
	if ( list.getResultNum() > 0 ) {
		int[] pageIndex = list.getDispPageIndex(totalPage, pageNo);
		
		
		//-------------------------------------
		// ページリンク（上部）タグ出力
		//-------------------------------------
		String pageLinkUrl = "";
		// ◇ RecordIndexの場合
		if ( refRecIndex ) {
			pageLinkUrl = path
								  + "?sortAction=" + reqParams.get( "sortAction" )
								  + "&exec=page" 
								  + "&sortKey=" + reqParams.get( "sortKey" )
								  + "&totalPageNo=" + reqParams.get( "totalPageNo" )
								  + "&type=" + reqParams.get( "type" )
								  + "&srchkey=" + reqParams.get( "srchkey" )
								  + "&idxtype=" + reqParams.get( "idxtype" );
		}
		
		out.println( "<table width=\"" + tableWidth + "\" height=\"30\" cellpadding=\"2\" cellspacing=\"0\" class=\"pageLinkTop\">" );
		out.println( " <tr>" );
		for (int i=pageIndex[0]; i<=pageIndex[1]; i++) {
			if ( i == pageIndex[0] ) {
				if ( !refRecIndex && pageNo != 1 ) {
					out.println( "  <td width=\"38\" align=\"center\" valign=\"top\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + 1 + ")\">First</a></b></td>" );
					out.println( "  <td width=\"34\" align=\"center\" valign=\"top\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + (pageNo-1) + ")\">Prev</a></td>" );
				}
				else if ( refRecIndex && pageNo != 1 ) {
					out.println( "  <td width=\"38\" align=\"center\" valign=\"top\"><a href=\"" + pageLinkUrl + "&pageNo=1\" class=\"pageLink\">First</a></b></td>" );
					out.println( "  <td width=\"34\" align=\"center\" valign=\"top\"><a href=\"" + pageLinkUrl + "&pageNo=" + (pageNo-1) + "\" class=\"pageLink\">Prev</a></td>" );
				}
				else {
					out.println( "  <td width=\"38\" align=\"center\" valign=\"top\"><font color=\"#3300CC\">First</font></td>" );
					out.println( "  <td width=\"34\" align=\"center\" valign=\"top\"><font color=\"#3300CC\">Prev</font></td>" );
				}
				out.println( "  <td width=\"8\" align=\"center\" valign=\"top\">&nbsp;</td>" );
			}
			if ( !refRecIndex && i != pageNo ) {
				out.println( "  <td width=\"16\" align=\"center\" valign=\"top\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + i + ")\">" + i + "</a></td>" );
			}
			else if ( refRecIndex && i != pageNo ) {
				out.println( "  <td width=\"16\" align=\"center\" valign=\"top\"><a href=\"" + pageLinkUrl + "&pageNo=" + i + "\" class=\"pageLink\">" + i + "</a></td>" );
			}
			else {
				out.println( "  <td width=\"16\" align=\"center\" valign=\"top\"><i><b>" + i + "</b></i></td>" );
			}
			if ( i == pageIndex[1] ) {
				out.println( "  <td width=\"8\" align=\"center\" valign=\"top\">&nbsp;</td>" );
				if ( !refRecIndex && pageNo != totalPage ) {
					out.println( "  <td width=\"34\" align=\"center\" valign=\"top\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + (pageNo+1) + ")\">Next</a></td>" );
					out.println( "  <td width=\"38\" align=\"center\" valign=\"top\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + totalPage + ")\">Last</a></td>" );
				}
				else if ( refRecIndex && pageNo != totalPage ) {
					out.println( "  <td width=\"34\" align=\"center\" valign=\"top\"><a href=\"" + pageLinkUrl + "&pageNo=" + (pageNo+1) + "\" class=\"pageLink\">Next</a></td>" );
					out.println( "  <td width=\"38\" align=\"center\" valign=\"top\"><a href=\"" + pageLinkUrl + "&pageNo=" + totalPage + "\" class=\"pageLink\">Last</a></td>" );
				}
				else {
					out.println( "  <td width=\"34\" align=\"center\" valign=\"top\"><font color=\"#3300CC\">Next</font></td>" );
					out.println( "  <td width=\"38\" align=\"center\" valign=\"top\"><font color=\"#3300CC\">Last</font></td>" );
				}
			}
		}
		out.println( "  <td valign=\"top\">&nbsp;&nbsp;&nbsp;( Total <i><b>" + totalPage + "</b></i> Page )</td>" );
		out.println( "  <td class=\"font12px\" align=\"right\" valign=\"bottom\">" );
		out.println( "   <a class=\"moveDispLink\" href=\"#resultsEnd\">&nbsp;&nbsp;<span class=\"font10px2\">&#9660;&nbsp;</span>Results End</a>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>");
		
		
		//-------------------------------------
		// 検索結果テーブルラベル出力
		//-------------------------------------
		// ソートイメージ変更
		String nameSortImg = defaultGif;
		String formulaSortImg = defaultGif;
		String emassSortImg = defaultGif;
		String idSortImg = defaultGif;
		String sortKey = (String)reqParams.get( "sortKey" );
		int sortAction = Integer.parseInt((String)reqParams.get( "sortAction" ));
		switch ( sortAction ) {
			case ResultList.SORT_ACTION_ASC:
				if (sortKey.equals(ResultList.SORT_KEY_NAME)) {
					nameSortImg = ascGif;
				}
				else if (sortKey.equals(ResultList.SORT_KEY_FORMULA)) {
					formulaSortImg = ascGif;
				}
				else if (sortKey.equals(ResultList.SORT_KEY_EMASS)) {
					emassSortImg = ascGif;
				}
				else if (sortKey.equals(ResultList.SORT_KEY_ID)) {
					idSortImg = ascGif;
				}
				break;
			case ResultList.SORT_ACTION_DESC:
				if (sortKey.equals(ResultList.SORT_KEY_NAME)) {
					nameSortImg = descGif;
				}
				else if (sortKey.equals(ResultList.SORT_KEY_FORMULA)) {
					formulaSortImg = descGif;
				}
				else if (sortKey.equals(ResultList.SORT_KEY_EMASS)) {
					emassSortImg = descGif;
				}
				else if (sortKey.equals(ResultList.SORT_KEY_ID)) {
					idSortImg = descGif;
				}
				break;
		}
		out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\" class=\"cursorLink\" onContextMenu=\"return dispRightMenu(event, true)\">" );
		out.println( " <tr bgcolor=\"silver\">" );
		out.print( "  <th height=\"30\" width=\"" + width[0] + "\" class=\"listLayout1 cursorDefault\">" );
		out.println( "<input type=\"checkbox\" name=\"chkAll\" onClick=\"checkAll();\"></th>" );
		out.print( "  <th height=\"30\" colspan=\"2\" width=\"" + (Integer.parseInt(width[1]) + Integer.parseInt(width[2]) - 22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_NAME + "');\">Name</th>" );
		out.print( "  <th height=\"30\" width=\"22\" class=\"listLayout3\" onclick=\"recSort('" + sortKey + "', '" 
			+ ResultList.SORT_KEY_NAME + "');\">" );
		out.println( "<img src=\"" + nameSortImg + "\" alt=\"Name Sort\"></th>" );
		out.println( "  <th height=\"30\" width=\"" + (Integer.parseInt(width[3]) + Integer.parseInt(width[4])-22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_FORMULA + "');\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Formula / Structure</th>" );
		out.print( "  <th height=\"30\" width=\"22\" class=\"listLayout3\" onClick=\"recSort('" + sortKey + "', '" 
			+ ResultList.SORT_KEY_FORMULA + "');\">" );
		out.println( "<img src=\"" + formulaSortImg + "\" alt=\"Formula Sort\"></th>" );
		out.println( "  <th height=\"30\" width=\"" + (Integer.parseInt(width[5]) - 22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_EMASS + "');\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ExactMass</th>" );
		out.print( "  <th height=\"30\" width=\"22\" class=\"listLayout3\" onclick=\"recSort('" + sortKey + "', '" 
			+ ResultList.SORT_KEY_EMASS + "');\">" );
		out.println( "<img src=\"" + emassSortImg + "\" alt=\"ExactMass Sort\"></th>" );
		out.println( "  <th height=\"30\" width=\"" + (Integer.parseInt(width[6]) - 22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_ID + "');\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ID</th>" );
		out.print( "  <th height=\"30\" width=\"22\" class=\"listLayout3\" onclick=\"recSort('" + sortKey + "', '" 
			+ ResultList.SORT_KEY_ID + "');\">" );
		out.println( "<img src=\"" + idSortImg + "\" alt=\"ID Sort\"></th>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		
		
		//-------------------------------------
		// 結果表示
		//-------------------------------------
		if ( startIndex > -1 && endIndex > -1 ) {
			int prevNode = -1;
			
			boolean parentTreeFlag = true;
			int tParentId = -1;
			String tChildId = "";
			int tChildIndex = 0;
			HashMap<Integer, Integer> nodeMap = list.getDispParentNodeMap(startIndex, endIndex);
			String parentNum = String.valueOf(nodeMap.size());
			String childNum = "";
			String pRowId = "";
			String cRowId = "";

			// 化学構造式表示情報を一括取得する
			List<Map> structureResult = getStructure(list, startIndex, endIndex, serverUrl, urlList, dbNameList);
			Map<String, String> mapGifUrl = structureResult.get(0);
			Map<String, String> mapGifSmallUrl = structureResult.get(1);
			Map<String, String> mapGifLargeUrl = structureResult.get(2);
			Map<String, String> mapMolData = structureResult.get(3);
			
			ResultRecord rec;
			for (int i=startIndex; i<=endIndex; i++) {
				rec = list.getRecord(i);
				// ツリー表示用ID、およびイメージ名生成
				if ( prevNode != rec.getNodeGroup() ) {
					tParentId++;
					parentTreeFlag = true;
					tChildIndex = 0;
				}
				else {
					parentTreeFlag = false;
					tChildIndex++;
				}
				tChildId = String.valueOf(tParentId) + "child";
				prevNode = rec.getNodeGroup();
				String tParentImgName = tParentId + "img";
				String tParentImgName2 = tParentId + "treeimg";

				// レコードページ表示用URL生成
				String url = "";
				// ◇ PeakSearch／PeakDifferenceSearchの場合
				if ( refPeak || refPeakDiff ) {
					if ( refPeak ) {
						typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
					}
					else {
						typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISPDIFF];
					}
					url = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName  + "&id=" + rec.getId() + "&site=" + rec.getContributor() + recordParam;
				}
				// ◇ QuickSearch／RecordIndex/Substructure Searchの場合
				else if( refQuick || refRecIndex || refStruct ) {
					typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
					url = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName  + "&id=" + rec.getId() + "&site=" + rec.getContributor();
				}
				
				
				//------------------------------------
				// ツリータグ出力
				//------------------------------------
				if ( parentTreeFlag ) {
					if ( i > startIndex ) {
						out.println( "</table>" );
					}
				}
				
				if ( parentTreeFlag ) {
					// ツリー(親)
					childNum = String.valueOf(nodeMap.get(rec.getNodeGroup()));
					pRowId = "of" + String.valueOf(tParentId);
					out.println( "<table width=\"" + tableWidth 
						+ "\" class=\"pTreeLayout\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
					out.println( "  <tr id=\"" + pRowId + "\" style=\"\" onmouseover=\"overBgColor(this, '#E6E6FA', '" + pRowId + "');\" onmouseout=\"outBgColor(this, '#FFFFFF', '" + pRowId + "');\">" );
					out.print( "  <td class=\"treeLayout1\" width=\"" + width[0] + "\" valign=\"top\" align=\"center\">" );
					out.print( "<input type=\"checkbox\" name=\"pid\" id=\"" + pRowId + "check\" value=\"\" onClick=\"checkParent('" + pRowId + "', '" + parentNum + "', '" + childNum + "');\">" );
					out.println( "</td>" );
					out.print( "  <td class=\"treeLayout2\" width=\"" + (Integer.parseInt(width[1]) - 8) + "\" valign=\"top\">");
					out.print( "&nbsp;&nbsp;<img class=\"cursorLink\" src=\"" + pluspng + "\" onclick=\"treeMenu("
						+ tParentId + ")\" name=\"" + tParentImgName + "\" alt=\"\"><br>&nbsp;&nbsp;&nbsp;<img src=\"../image/treeline0.gif\" align=\"middle\" name=\"" + tParentImgName2 + "\">" );
					out.println( "</td>" );

					out.print( "  <td class=\"treeLayout1\" width=\"" + (Integer.parseInt(width[2]) + 8) + "\" valign=\"top\">");
					out.print( "<a href=\"javascript:treeMenu(" + tParentId + ")\" class=\"noLinkImg\" title=\"" + rec.getName() + "\">&nbsp;" + rec.getParentLink() + " " + "</a><br>" );

					// 個々のスペクトル数を表示
					String dispNum = childNum;
					if ( Integer.parseInt(childNum) == 1 ) {
					 	dispNum += " spectrum";
					}
					else {
					 	dispNum += " spectra&nbsp;&nbsp;&nbsp;";
					}
					out.println( "<div align=\"right\" style=\"font-size: 12px;\">" + dispNum + "&nbsp;&nbsp;</div>" );
					out.println( "  </td>" );
					out.println( "  <td class=\"treeLayout2\" width=\"" + width[3] + "\" valign=\"top\">&nbsp;<b>" + rec.getFormula() + "</b>&nbsp;</td>" );
					out.println( "  <td class=\"treeLayout1\" width=\"" + width[4] + "\" valign=\"top\" align=\"left\">" );

					// 化学構造式を表示（画像がなければアプレットで表示）
					String key = rec.getName().toLowerCase();
					StringBuilder previewName = new StringBuilder(rec.getName());
					if (previewName.length() > 17) {
						previewName.delete(17, previewName.length());
						previewName.append("...");
					}
					if ( mapGifSmallUrl.containsKey(key) ) {
						if ( mapGifUrl.containsKey(key) ) {
							out.println( "  <a href=\"" + mapGifUrl.get(key) + "\" class=\"preview_structure\" title=\"" + previewName.toString() + "\" onClick=\"return false\">" );
						}
						else {
							out.println( "  <a href=\"../image/not_available.gif\" class=\"preview_structure\" title=\"" + previewName.toString() + "\" onClick=\"return false\">" );
						}
						if ( mapGifLargeUrl.containsKey(key) ) {
							out.println( "   <img src=\"" + mapGifSmallUrl.get(key) + "\" width=\"80\" height=\"80\" onClick=\"expandMolView('" + mapGifLargeUrl.get(key) + "')\" style=\"margin:0px; cursor:pointer\">");
						}
						else {
							out.println( "   <img src=\"" + mapGifSmallUrl.get(key) + "\" width=\"80\" height=\"80\" onClick=\"expandMolView('../image/not_available_l.gif')\" style=\"margin:0px; cursor:pointer\">");
						}
						out.println( "  </a>" );
					}
					else if ( mapMolData.containsKey(key) ) {
						String moldata = mapMolData.get(key).trim();
						if ( !moldata.equals("") ) {
							out.println( "   <applet name=\"jme_query\" code=\"JME.class\" archive=\"../applet/JME.jar\" width=\"80\" height=\"80\">");
							out.println( "    <param name=\"options\" value=\"depict\">" );
							out.println( "    <param name=\"mol\" value=\"");
							out.print( moldata );
							out.println( "\">");
							out.println( "   </applet>\n");
						}
					}
					else {
						out.println( "  <a href=\"../image/not_available.gif\" class=\"preview_structure\" title=\"" + previewName.toString() + "\" onClick=\"return false\">" );
						out.println( "   <img src=\"../image/not_available_s.gif\" width=\"80\" height=\"80\" onClick=\"expandMolView('../image/not_available_l.gif')\" style=\"margin:0px; cursor:pointer\">");
						out.println( "  </a>" );
					}

					out.println( "  </td>" );
					out.println( "  <td class=\"treeLayout1\" width=\"" + width[5] + "\" valign=\"top\">&nbsp;<b>" + rec.getDispEmass() + "</b>&nbsp;</td>" );
					out.println( "  <td class=\"treeLayout2\" width=\"" + width[6] + "\" valign=\"top\">&nbsp;</td>" );
					out.println( " </tr>" );
					out.println( "</table>" );
					out.println( "<table width=\"" + tableWidth + "\" id=\"" + tChildId + "\" style=\"display:none\""
						+ " class=\"cTreeLayout\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
				}
				
				// ツリー(子)
				String cCheckValue = rec.getInfo() + "\t" + rec.getId() + "\t" + rec.getFormula()
								+ "\t" + rec.getDispEmass().replaceAll("&nbsp;", "") + "\t" + rec.getIon() + "\t" + rec.getContributor();
				cRowId = String.valueOf(tChildIndex) + "of" + String.valueOf(tParentId);
				out.println( " <tr id=\"" + cRowId + "\" onmouseover=\"overBgColor(this, '#E6E6FA', '" + cRowId + "');\" onmouseout=\"outBgColor(this, '#FFFFFF', '" + cRowId + "');\">" );
				out.print( "  <td class=\"treeLayout1\" width=\"" + width[0] + "\" valign=\"top\" align=\"center\">" );
				out.print( "<input type=\"checkbox\" name=\"id\" value=\"" + cCheckValue + "\" id=\"" + cRowId + "check\" onClick=\"checkChild('" + pRowId + "', '" + cRowId + "', '" + parentNum + "', '" + childNum + "');\">" );
				out.println( "</td>" );

				// ツリー子の罫線を表示
				String gifLine = "treeline2.gif";
				if ( i == endIndex ) {
					gifLine = "treeline3.gif";
				}
				else if ( i < endIndex ) {
					ResultRecord nextRec = list.getRecord(i+1);
					int nextNode = nextRec.getNodeGroup();
					if ( nextNode != rec.getNodeGroup() ) {
						gifLine = "treeline3.gif";
					}
				}

				out.println( "  <td class=\"treeLayout2\" width=\"" + width[1] + "\" valign=\"top\">"
					+ "&nbsp;&nbsp;&nbsp;<img src=\"../image/" + gifLine + "\" align=\"absmiddle\" border=\"0\"></td>" );
				out.print( "  <td class=\"treeLayout1\" width=\"" + width[2] + "\" valign=\"top\">" );
				out.print( "<a href=\"" + url + "\" target=\"_blank\">&nbsp;" + rec.getChildLink() + "</a>" );
				out.println( "</td>" );
				out.println( "  <td class=\"treeLayout2\" width=\"" + width[3] + "\" valign=\"top\">&nbsp;&nbsp;&nbsp;&nbsp;" /*+ rec.getFormula()*/ + "&nbsp;</td>" );
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[4] + "\" valign=\"top\">&nbsp;</td>" );
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[5] + "\" valign=\"top\">&nbsp;&nbsp;&nbsp;&nbsp;" /*+ rec.getDispEmass() */ + "&nbsp;</td>" );
				out.println( "  <td class=\"treeLayout2\" width=\"" + width[6] + "\" valign=\"top\">&nbsp;&nbsp;&nbsp;&nbsp;" + rec.getId() + "&nbsp;</td>" );
				out.println( " </tr>" );
				
				// 最終行
				if ( i == endIndex ) { 
					out.println( "</table>" );
					out.println( "<table width=\"" + tableWidth + "\" class=\"treeLayoutEnd\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
					out.println( " <tr>" );
					out.println( "  <td width=\"" + width[0] + "\">&nbsp;</td>" );
					out.println( "  <td width=\"" + (Integer.parseInt(width[1]) + Integer.parseInt(width[2])) + "\">&nbsp;</td>" );
					out.println( "  <td width=\"" + (Integer.parseInt(width[3]) + Integer.parseInt(width[4])) + "\">&nbsp;</td>" );
					out.println( "  <td width=\"" + width[5] + "\">&nbsp;</td>" );
					out.println( "  <td width=\"" + width[6] + "\">&nbsp;</td>" );
					out.println( " </tr>" );
					out.println( "</table>" );
				}
			}
			out.println( "<a name=\"resultsEnd\"></a>" );
			
			
			//-------------------------------------
			// ページリンク（下部）タグ出力
			//-------------------------------------
			out.println( "<table width=\"" + tableWidth + "\" height=\"30\" cellpadding=\"2\" cellspacing=\"0\" class=\"pageLinkBottom\">" );
			out.println( " <tr>" );
			for (int i=pageIndex[0]; i<=pageIndex[1]; i++) {
				if ( i == pageIndex[0] ) {
					if ( !refRecIndex && pageNo != 1 ) {
						out.println( "  <td width=\"38\" align=\"center\" valign=\"bottom\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + 1 + ")\">First</a></b></td>" );
						out.println( "  <td width=\"34\" align=\"center\" valign=\"bottom\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + (pageNo-1) + ")\">Prev</a></td>" );
					}
					else if ( refRecIndex && pageNo != 1 ) {
						out.println( "  <td width=\"38\" align=\"center\" valign=\"bottom\"><a href=\"" + pageLinkUrl + "&pageNo=1\" class=\"pageLink\">First</a></b></td>" );
						out.println( "  <td width=\"34\" align=\"center\" valign=\"bottom\"><a href=\"" + pageLinkUrl + "&pageNo=" + (pageNo-1) + "\" class=\"pageLink\">Prev</a></td>" );
					}
					else {
						out.println( "  <td width=\"38\" align=\"center\" valign=\"bottom\"><font color=\"#3300CC\">First</font></td>" );
						out.println( "  <td width=\"34\" align=\"center\" valign=\"bottom\"><font color=\"#3300CC\">Prev</font></td>" );
					}
					out.println( "  <td width=\"8\" align=\"center\" valign=\"bottom\">&nbsp;</td>" );
				}
				if ( !refRecIndex && i != pageNo ) {
					out.println( "  <td width=\"16\" align=\"center\" valign=\"bottom\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + i + ")\">" + i + "</a></td>" );
				}
				else if ( refRecIndex && i != pageNo ) {
					out.println( "  <td width=\"16\" align=\"center\" valign=\"bottom\"><a href=\"" + pageLinkUrl + "&pageNo=" + i + "\" class=\"pageLink\">" + i + "</a></td>" );
				}
				else {
					out.println( "  <td width=\"16\" align=\"center\" valign=\"bottom\"><i><b>" + i + "</b></i></td>" );
				}
				if ( i == pageIndex[1] ) {
					out.println( "  <td width=\"8\" align=\"center\" valign=\"bottom\">&nbsp;</td>" );
					if ( !refRecIndex && pageNo != totalPage ) {
						out.println( "  <td width=\"34\" align=\"center\" valign=\"bottom\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + (pageNo+1) + ")\">Next</a></td>" );
						out.println( "  <td width=\"38\" align=\"center\" valign=\"bottom\"><a href=\"\" class=\"pageLink\" onClick=\"return changePage('" + type + "', " + totalPage + ")\">Last</a></td>" );
					}
					else if ( refRecIndex && pageNo != totalPage ) {
						out.println( "  <td width=\"34\" align=\"center\" valign=\"bottom\"><a href=\"" + pageLinkUrl + "&pageNo=" + (pageNo+1) + "\" class=\"pageLink\">Next</a></td>" );
						out.println( "  <td width=\"38\" align=\"center\" valign=\"bottom\"><a href=\"" + pageLinkUrl + "&pageNo=" + totalPage + "\" class=\"pageLink\">Last</a></td>" );
					}
					else {
						out.println( "  <td width=\"34\" align=\"center\" valign=\"bottom\"><font color=\"#3300CC\">Next</font></td>" );
						out.println( "  <td width=\"38\" align=\"center\" valign=\"bottom\"><font color=\"#3300CC\">Last</font></td>" );
					}
				}
			}
			out.println( "  <td valign=\"bottom\">&nbsp;&nbsp;&nbsp;( Total <i><b>" + totalPage + "</b></i> Page )</td>" );
			out.println( "  <td class=\"font12px\" align=\"right\" valign=\"top\">" );
			out.println( "   <a class=\"moveDispLink\" href=\"#resultsTop\">&nbsp;&nbsp;<span class=\"font10px2\">&#9650;&nbsp;</span>Results Top</a>" );
			out.println( "  </td>" );
			out.println( " </tr>" );
			out.println( "</table>");
		}
		out.println( "<br>" );
	}
%>
</form>

<hr size="1">
<iframe src="../copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
