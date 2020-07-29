<%@page import="massbank.Config" %>
<%@page import="massbank.db.DatabaseManager"%>
<%@page import="massbank.StructureToSvgStringGenerator"%>
<%@page import="massbank.StructureToSvgStringGenerator.ClickablePreviewImageData"%>

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
 * Search result page display module
 *
 * ver 2.0.36 2011.08.02
 *
 ******************************************************************************/
%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.FileUtil" %>
<%@ page import="massbank.ResultList" %>
<%@ page import="massbank.ResultRecord" %>
<%@ page import="massbank.Record" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="massbank.web.peaksearch.PeakSearchByPeakDifference" %>
<%@ page import="massbank.web.peaksearch.PeakSearchByPeak" %>
<%@ page import="massbank.web.quicksearch.QuickSearchByKeyword" %>
<%@ page import="massbank.web.quicksearch.QuickSearchByInChIKey" %>
<%@ page import="massbank.web.quicksearch.QuickSearchBySplash" %>
<%@ page import="massbank.web.recordindex.RecordIndexByCategory" %>
<%@ page import="massbank.web.SearchExecution" %>
<%@ page import="massbank.web.QueryToResultList" %>
<%@ include file="./Common.jsp"%>
<%!
	// 画面内テーブルタグ幅
	private static final String tableWidth = "950";
	
	// 検索結果テーブル列幅
	private static final String[] width = { "28", "28", "422", "142", "102", "122", "100" };
	
	// イメージファイルURL
	private final String minuspng = "image/minus.png";
	private final String pluspng = "image/plus.png";
	private final String defaultGif = "image/default.gif";
	private final String ascGif = "image/asc.gif";
	private final String descGif = "image/desc.gif";

%>
<%

	MassBankCommon mbcommon = new MassBankCommon();
	
	//-------------------------------------
	// リファラー（遷移元）、及びタイトル設定
	// Referrer (transition source), title setting
	//-------------------------------------
	boolean refPeak     = false;			// PeakSearch
	boolean refPeakDiff = false;			// PeakDifferenceSearch
	boolean refQuick    = false;			// QuickSearch
	boolean refRecIndex = false;			// RecordIndex
	boolean refStruct   = false;			// Substructure Search
	boolean refInchi    = false;
	boolean refSplash   = false;
	String title = "";						// タイトル
	String hTitle = "";						// ヘッダー用タイトル
	
	String type = request.getParameter("type");
	if (type == null) {
		if (request.getParameter("inchikey") != null) {
			type = "inchikey";
			/*refInchi = true;
			title = "InChIKey Search Results";
			hTitle = "InChIKey Search Results";*/
		}
		if (request.getParameter("splash") != null) {
			type = "splash";
			/*refSplash = true;
			title = "Splash Search Results";
			hTitle = "Splash Search Results";*/
		}
	}
	
	if ( type == null /*&& !refInchi && !refSplash*/) {
		out.println( "<html>" );
		out.println( "<head>" );
		out.println( " <link rel=\"stylesheet\" type=\"text/css\" href=\"css.old/Common.css\">" );
		out.println( " <title>MassBank | Database | Results</title>" );
		out.println( "</head>" );
		out.println( "<body class=\"msbkFont cursorDefault\">" );
		out.println( "<h1>Results</h1>" );
		out.println( "<jsp:include page=\"menu.html\"/>" );
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
		out.println( "<iframe src=\"copyrightline.html\" width=\"800\" height=\"20px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		out.println( "</body>" );
		out.println( "</html>" );
		return;
	}
	
	if (type != null) {
		if ( type.equals(MassBankCommon.REQ_TYPE_PEAK) ) {			// 遷移元がPeakSearch
			refPeak = true;
			title = "Peak Search Results (Peaks by m/z value)";
			hTitle = "Peak Search Results&nbsp;&nbsp;<span style=\"font-size:22px;color:OliveDrab\">(Peaks by <i>m/z</i> value)</span>";
		}
		else if ( type.equals(MassBankCommon.REQ_TYPE_PEAKDIFF) ) {	// 遷移元がPeakDifferenceSearch
			refPeakDiff = true;
			title = "Peak Search Results (Peak Differences by m/z value)";
			hTitle = "Peak Search Results&nbsp;&nbsp;<span style=\"font-size:22px;color:DarkOrchid\">(Peak Differences by <i>m/z</i> value)</span>";
		}
		else if ( type.equals(MassBankCommon.REQ_TYPE_QUICK) ) {	// 遷移元がQuickSearch
			refQuick = true;
			title = "Quick Search Results";
			hTitle = "Quick Search Results";
		}
		else if ( type.equals(MassBankCommon.REQ_TYPE_RCDIDX) ) {	// 遷移元がRecordIndex
			refRecIndex = true;
			title = "Record Index Results";
			hTitle = "Record Index Results";
		}
		else if ( type.equals(MassBankCommon.REQ_TYPE_STRUCT) ) {	// 遷移元がSubstructure Search
			refStruct = true;
			title = "Substructure Search Results";
			hTitle = "Substructure Search Results";
		}
		else if (type.equals("inchikey")) {
			refInchi = true;
			title = "InChIKey Search Results";
			hTitle = "InChIKey Search Results";
		}
		else if (type.equals("splash")) {
			refSplash = true;
			title = "Splash Search Results";
			hTitle = "Splash Search Results";
		}
	}
	
	//-------------------------------------
	// リクエストパラメータ取得
	// Request parameter acquisition
	//-------------------------------------
	// 全てのリクエストパラメータをハッシュに持つ
	// Hash all request parameters
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
		if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
			// キーがInstrumentType,MSType以外の場合はStringパラメータ
			String val = request.getParameter( key ).trim();
			reqParams.put( key, val );
		}
		else {
			// キーがInstrumentType,MSTypeの場合はString配列パラメータ
			String[] vals = request.getParameterValues( key );
			reqParams.put( key, vals );
		}
	}
	// パラメータリセット
	// parameter reset
	reqParams.put("exec", "");
	// デフォルト値設定
	// Default value setting
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
	// Request parameter processing and URL parameter generation
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
	}
	
	// URLパラメータ（検索実行用）生成
	// Parameter generation (for search execution)
	for ( Enumeration keys = reqParams.keys(); keys.hasMoreElements(); ){
		String key = (String)keys.nextElement();
		if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
			// キーがInstrumentType,MSType以外の場合はStringパラメータ
			String val = (String)reqParams.get(key);
			if ( key.indexOf("site") != -1 && val.equals("-1") ) {
				continue;
			}
			else if ( !val.equals("") ) {
				searchParam += key + "=" + URLEncoder.encode(val,"utf-8") + "&";
			}
		}
		else {
			String[] vals = null;
			try {
				vals = (String[])reqParams.get(key);
			}
			catch (ClassCastException cce) {
				vals = new String[]{ (String)reqParams.get(key) };
			}
			for ( int i=0; i<vals.length; i++ ) {
				searchParam += key + "=" + URLEncoder.encode(vals[i], "utf-8") + "&";
			}
		}
	}
	searchParam = StringUtils.chop(searchParam);
	
	
	//-------------------------------------------
	// 設定ファイルから各種情報を取得
	// Acquire various information from setting file
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	String baseUrl = path.substring( 0, (path.indexOf("/jsp")+1) );
	GetConfig conf = new GetConfig();
	//String serverUrl = Config.get().BASE_URL();
	//String [] siteLongName = conf.getSiteLongName();	// サイト名取得
	String[] dbNameList = conf.getDbName();
	//String[] urlList = conf.getSiteUrl();

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
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
	<meta name="description" content="Mass Spectrum Search Results">
	<meta name="keywords" content="Results">
	<meta name="revisit_after" content="10 days">
	<link rel="stylesheet" type="text/css" href="css.old/Common.css">
	<link rel="stylesheet" type="text/css" href="css.old/Result.css">
	<link rel="stylesheet" type="text/css" href="css.old/ResultMenu.css">
	<script type="text/javascript" src="script/Common.js"></script>
	<script type="text/javascript" src="script/Result.js"></script>
	<script type="text/javascript" src="script/ResultMenu.js"></script>
	<script type="text/javascript" src="js/jquery-3.4.1.min.js" ></script>
	<!-- SpeckTackle dependencies-->
	<script type="text/javascript" src="js/d3.v3.min.js"></script>
	<!-- SpeckTackle library-->
	<script type="text/javascript" src="js/st.min.js" charset="utf-8"></script>
	<!-- SpeckTackle style sheet-->
	<link rel="stylesheet" href="css.old/st.css" type="text/css" />	
	<!-- SpeckTackle MassBank loading script-->
	<script type="text/javascript" src="js/massbank_specktackle.js"></script>	
	<title>MassBank | Database | <%=title%></title>
</head>
<body class="msbkFont cursorDefault">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td><h1><%=hTitle%></h1></td>
			<td align="right" class="font12px">
				<img src="image/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
<jsp:include page="menu.html"/>
<hr size="1">
<%
	/*
		↓ServerInfo.jsp
		はプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)
		Exists only on the primary server (it does not cause an error even if there is no file)
	*/
%>
<%-- <jsp:include page="pserver/ServerInfo.jsp" /> --%>
<%
	//-------------------------------------
	// 検索条件パラメータ表示
	// display search parameters
	//-------------------------------------
	out.println( "<a name=\"resultsTop\"></a>" );
	
	// ◇ PeakSearch／PeakDifferenceSearch／QuickSearchの場合
	if ( refPeak || refPeakDiff || refQuick || refInchi || refSplash) {
		
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
				out.println( "</td>" );
				out.println( "</tr>" );
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
		else if ( refInchi ) {
			boolean isBinder = false;
			String inchikey = ((String)request.getParameter("inchikey") != null) ? (String)request.getParameter("inchikey") : ""; 
			// InChIKey
			if ( !inchikey.equals("") ) {
				out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
				out.println( " <tr>" );
				out.println( "  <td>&nbsp;&nbsp;&nbsp;InChIKey:&nbsp;&nbsp;<b>" + inchikey + "</b></td>" );
				out.println( " </tr>" );
				out.println( "</table>" );
				isBinder = true;
			}
		}
		else if ( refSplash ) {
			boolean isBinder = false;
			String splash = ((String)request.getParameter("splash") != null) ? (String)request.getParameter("splash") : ""; 
			// Splash
			if ( !splash.equals("") ) {
				out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
				out.println( " <tr>" );
				out.println( "  <td>&nbsp;&nbsp;&nbsp;Splash:&nbsp;&nbsp;<b>" + splash + "</b></td>" );
				out.println( " </tr>" );
				out.println( "</table>" );
				isBinder = true;
			}
		}
		
		out.println( "<div class=\"divSpacer9px\"></div>" );
		
		// Instrument Type
		String[] instrument = null;
		try {
			instrument = (reqParams.get("inst") != null) ? (String[])reqParams.get("inst") : new String[]{};
		}
		catch (ClassCastException cce) {
			instrument = new String[]{ (String)reqParams.get("inst") };
		}
		boolean isInstAll = false;
		out.println( "<table width=\"" + (Integer.parseInt(tableWidth)-160) + "\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td width=\"132\">&nbsp;&nbsp;&nbsp;Instrument Type:</td>" );
		for (int i=0; i<instrument.length; i++) {
			if ( instrument[i].equals("all") ) {
				out.println( "  <td><b>All</b></td>" );
				isInstAll = true;
				break;
			}
		}
		if (type == null && (refInchi || refSplash)) {
			out.println( "  <td><b>All</b></td>" );
			isInstAll = true;
		}
		if ( !isInstAll ) {
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
		
		// MS Type
		String[] msType = null;
		try {
			msType = (reqParams.get("ms") != null) ? (String[])reqParams.get("ms") : new String[]{};
		}
		catch (ClassCastException cce) {
			msType = new String[]{ (String)reqParams.get("ms") };
		}
		boolean isMsAll = false;
		out.println( "<table width=\"" + (Integer.parseInt(tableWidth)-160) + "\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td width=\"132\">&nbsp;&nbsp;&nbsp;MS Type:</td>" );
		for (int i=0; i<msType.length; i++) {
			if ( msType[i].equals("all") ) {
				out.println( "  <td><b>All</b></td>" );
				isMsAll = true;
				break;
			}
		}
		if (type == null && (refInchi || refSplash)) {
			out.println( "  <td><b>All</b></td>" );
			isMsAll = true;
		}
		if ( !isMsAll ) {
			out.print( "  <td>" );
			for (int i=0; i<msType.length; i++) {
				out.print( "<b>" + msType[i] + "</b>" );
				if ( i != (msType.length-1) ) {
					out.println( " ,&nbsp;&nbsp;&nbsp;" );
				}
			}
			out.println( "  </td>" );
		}
		out.println( " </tr>" );
		out.println( "</table>" );
		
		// Ion Mode
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
		out.println( "  <td width=\"132\">&nbsp;&nbsp;&nbsp;Ion Mode:</td>" );
		out.println( "  <td><b>" + ionMode + "</b></td>" );
		out.println( "  <td align=\"right\">" );
		out.println( "   <button onclick=\"window.history.back()\">Edit / Resubmit Query</button>" );
		//out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting('" + type + "')\">Edit / Resubmit Query</a>" );
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
			//out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Contributor: <b>"
			//	+ siteLongName[Integer.parseInt(pSrchkey)] + "</b></td>" );
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Contributor: <b>"
					+ pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("inst") ) {	// Instrument Type
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Instrument Type: <b>" + pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("ms") ) {	// MS Type
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;MS Type: <b>" + pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("merged") ) {	// Merged Type
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Merged Type: <b>" + pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("ion") ) {	// Ion Mode
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Ion Mode: <b>" + pSrchkey + "</b></td>" );
		}
		else if ( pIdxtype.equals("cmpd") ) {	// Compound Name
			out.println( "  <td>&nbsp;&nbsp;&nbsp;&nbsp;Compound Name: <b>" + pSrchkey + "</b></td>" );
		}
		
		out.println( "  <td align=\"right\">" );
		out.println( "   <button onclick=\"window.history.back()\">Back to Record Index</button>" );
		//out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting('" + type + "')\">Edit / Resubmit Query</a>" );
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
					out.println( "<applet code=\"MolView.class\" archive=\"applet/MolView.jar\" width=\"200\" height=\"200\">" );
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
		out.println( "<td>&nbsp;&nbsp;<a href=\"StructureSearch.jsp\" class=\"pageLink\" onClick=\"return prevStructSearch()\">Edit / Resubmit Query</a></td>" );
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
			out.println( "<iframe src=\"copyrightline.html\" width=\"800\" height=\"20px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
			out.println( "</body>" );
			out.println( "</html>" );
			
			return;
		}
	}
	out.println( "<hr size=\"1\">" );


	//-------------------------------------
	// 検索実行・結果取得
	// execute search and acquire results
	//-------------------------------------
	String typeName = "";
	ResultList list = null;
	boolean isMulti = true;
	int siteNo = -1;
	
	// ◇ RecordIndexの場合
	if ( refRecIndex ) {
		try { siteNo = Integer.parseInt(String.valueOf(reqParams.get( "srchkey" ))); } catch (NumberFormatException nfe) {}
		String pIdxtype = ((String)reqParams.get( "idxtype" ) != null) ? (String)reqParams.get( "idxtype" ) : "";
		if ( pIdxtype.equals("site") ) {
			isMulti = false;
			searchParam = searchParam.replaceAll( "srchkey", "site" );
		}
		typeName = MassBankCommon.TYPE_TBL[MassBankCommon.CGI_TBL_TYPE_RCDIDX];
	}
	else {
		try { siteNo = Integer.parseInt(String.valueOf(reqParams.get( "site" ))); } catch (NumberFormatException nfe) {}
		if ( siteNo != -1 ) {
			isMulti = false;
		}
		// ◇ PeakSearchの場合
		if ( refPeak ) {
			typeName = MassBankCommon.TYPE_TBL[MassBankCommon.CGI_TBL_TYPE_PEAK];
		}
		// ◇ PeakDifferenceSearchの場合
		else if ( refPeakDiff ) {
			typeName = MassBankCommon.TYPE_TBL[MassBankCommon.CGI_TBL_TYPE_PDIFF];
		}
		// ◇ QuickSearchの場合
		else if ( refQuick ) {
			typeName = MassBankCommon.TYPE_TBL[MassBankCommon.CGI_TBL_TYPE_QUICK];
		}
		// ◇ Substructure Searchの場合
		else if ( refStruct ) {
			typeName = MassBankCommon.TYPE_TBL[MassBankCommon.CGI_TBL_TYPE_STRUCT];
		}
		else if ( refInchi ) {
			typeName = "inchikey";
		}
		else if ( refSplash ) {
			typeName = "splash";
		}
	}
	
	// 検索実行
	// execute search
	ResultRecord[] records	= null;
	if (typeName.compareTo("quick") == 0) {
		//list = mbcommon.execDispatcherResult(typeName, request, conf);
		records = new SearchExecution(request).exec(new QuickSearchByKeyword());
	} else if (typeName.compareTo("rcdidx") == 0) {
		//list = mbcommon.execDispatcherResult(typeName, request, conf);
		records = new SearchExecution(request).exec(new RecordIndexByCategory());
	} else if (typeName.compareTo("peak") == 0) {
		//list = mbcommon.execDispatcherResult(typeName, request, conf);	
		records = new SearchExecution(request).exec(new PeakSearchByPeak());
	} else if (typeName.compareTo("diff") == 0) {
		//list = mbcommon.execDispatcherResult(typeName, request, conf);
		records = new SearchExecution(request).exec(new PeakSearchByPeakDifference());
	} else if (typeName.compareTo("inchikey") == 0) {
		records = new SearchExecution(request).exec(new QuickSearchByInChIKey());
	} else if (typeName.compareTo("splash") == 0) {
		records = new SearchExecution(request).exec(new QuickSearchBySplash());
	}
	else {
		throw new IllegalArgumentException(typeName);
		// if ( isMulti ) {
		// 	list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null);
		// }
		// else {
		// 	list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, false, String.valueOf(siteNo));
		// }
	}
	
	if (records != null) 
		list = QueryToResultList.toResultList(records, request);
	
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
	// Keep request parameters in hidden field
	//-------------------------------------
	for ( Enumeration keys = reqParams.keys(); keys.hasMoreElements(); ){
		String key = (String)keys.nextElement();
		if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
			// キーがInstrumentType,MSType以外の場合はStringパラメータ
			String val = (String)reqParams.get(key);
			out.println( "<input type=\"hidden\" name=\"" + key + "\" value=\"" + val + "\">" );
		}
		else {
			// キーがInstrumentType,MSTypeの場合はString配列パラメータ
			String[] vals = null;
			try {
				vals = (String[])reqParams.get(key);
			}
			catch (ClassCastException cce) {
				vals = new String[]{ (String)reqParams.get(key) };
			}
			for (int i=0; i<vals.length; i++) {
				out.println( "<input type=\"hidden\" name=\"" + key + "\" value=\"" + vals[i] + "\">" );
			}
		}
	}
	
	
	//-------------------------------------
	// 検索結果数、ボタン出力
	// number of search results, buttons
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
			pageLinkUrl = "./Result.jsp"
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
		// Search result table output
		//-------------------------------------
		// ソートイメージ変更
		// change sort image
		String nameSortImg = defaultGif;
		String formulaSortImg = defaultGif;
		String emassSortImg = defaultGif;
		String idSortImg = defaultGif;
		String notSortImg = defaultGif;
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
				else if (sortKey.equals(ResultList.SORT_NOT)) {
					notSortImg = ascGif;
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
				else if (sortKey.equals(ResultList.SORT_NOT)) {
					notSortImg = descGif;
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
		// display results
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
			ResultRecord rec;
			DatabaseManager dbManager	= new DatabaseManager(Config.get().dbName());
			for (int i=startIndex; i<=endIndex; i++) {
				rec = list.getRecord(i);
				// ツリー表示用ID、およびイメージ名生成
				// Generation of display name and image name
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
				String contributor	= dbManager.getContributorFromAccession(rec.getId()).SHORT_NAME;
				
				// if ( refPeak || refPeakDiff ) {
				// 		if ( refPeak ) {
				// 			typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
				// 		}
				// 		else {
				//	 		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISPDIFF];
				// 		}
				// 		url = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName  + "&id=" + rec.getId() + "&dsn=" + contributor + recordParam;
				// }
				// // ◇ QuickSearch／RecordIndex/Substructure Searchの場合
				// else if( refQuick || refRecIndex || refStruct || refInchi || refSplash) {
				// 		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
				//	 	//url = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName  + "&id=" + rec.getId() + "&site=" + rec.getContributor() + "&dsn=" + conf.getDbName()[Integer.parseInt(rec.getContributor())];
				// 	url = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName  + "&id=" + rec.getId() + "&dsn=" + contributor;
				// }
				if ( refPeak ) {
					url = "RecordDisplay" + "?id=" + rec.getId() + "&dsn=" + contributor + recordParam;
				}
				if ( refPeakDiff ) {
					typeName = MassBankCommon.TYPE_TBL[MassBankCommon.CGI_TBL_TYPE_DISP];
					url = "RecordDisplay" + "?type=" + typeName  + "&id=" + rec.getId() + "&dsn=" + contributor + recordParam;
				}
				if( refQuick || refRecIndex || refStruct || refInchi || refSplash) {
					url = "RecordDisplay" + "?id=" + rec.getId() + "&dsn=" + contributor;
				}
				
				//------------------------------------
				// ツリータグ出力
				// print tree output
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
						+ tParentId + ")\" name=\"" + tParentImgName + "\" alt=\"\"><br>&nbsp;&nbsp;&nbsp;<img src=\"image/treeline0.gif\" align=\"middle\" name=\"" + tParentImgName2 + "\">" );
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
					
					// get data for svg image generation
					String databaseName		= conf.getDbName()[Integer.parseInt(rec.getContributor())];
					String accession		= rec.getId();
					
					String tmpUrlFolder		= Config.get().TOMCAT_TEMP_URL();
					String tmpFileFolder	= Config.get().TOMCAT_TEMP_PATH(getServletContext());
					
					Record.Structure structure	= dbManager.getStructureOfAccession(accession);
					ClickablePreviewImageData clickablePreviewImageData	= StructureToSvgStringGenerator.createClickablePreviewImage(
							accession, structure.CH_IUPAC, structure.CH_SMILES, tmpFileFolder, tmpUrlFolder,
							80, 250, 436
					);
					
					// display svg
					if(clickablePreviewImageData != null){
						// write big image and medium image as temp file
						FileUtil.writeToFile(clickablePreviewImageData.svgMedium,	clickablePreviewImageData.tmpFileMedium);
						FileUtil.writeToFile(clickablePreviewImageData.svgBig,		clickablePreviewImageData.tmpFileBig);
						
						// add expandMolView on click for small image
						String svgSmall	= clickablePreviewImageData.svgSmall.replaceAll(
								"</g>\\n</svg>", 
								"<rect class=\"btn\" x=\"0\" y=\"0\" width=\"80\" height=\"80\" onclick=\"expandMolView('" + clickablePreviewImageData.tmpUrlBig + "')\" fill-opacity=\"0.0\" stroke-width=\"0\" /> </g>\\\\n</svg>"
						);
						// cursor for small image
						svgSmall	= StructureToSvgStringGenerator.setSvgStyle(svgSmall, "cursor:pointer");
						
						// paste small image to web site
						out.println( "  <a href=\"" + clickablePreviewImageData.tmpUrlMedium + "\" class=\"preview_structure\" title=\"" + previewName.toString() + "\" onClick=\"return false\">" );
						out.println( "   " + svgSmall);
						out.println( "  </a>" );
					} else {
						// no structure there or svg generation failed
						out.println( "   <img src=\"image/not_available_s.gif\" width=\"80\" height=\"80\" style=\"margin:0px;\">");
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
					+ "&nbsp;&nbsp;&nbsp;<img src=\"image/" + gifLine + "\" align=\"absmiddle\" border=\"0\"></td>" );
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
			dbManager.closeConnection();
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
<jsp:include page="copyrightline.html"/>
</body>
</html>
