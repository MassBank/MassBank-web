<%@page import="massbank.DataManagement"%>
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
 * Record Index Page表示用モジュール
 *
 * ver 1.0.30 2012.02.20
 *
 ******************************************************************************/
%>

<%@ page import="java.io.BufferedOutputStream" %>
<%@ page import="java.io.FileOutputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.Config" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="java.awt.Color" %>
<%@ page import="java.awt.Font" %>
<%@ page import="java.lang.NoClassDefFoundError" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.jfree.chart.ChartFactory" %>
<%@ page import="org.jfree.chart.ChartUtilities" %>
<%@ page import="org.jfree.chart.JFreeChart" %>
<%@ page import="org.jfree.chart.plot.PiePlot" %>
<%@ page import="org.jfree.chart.title.LegendTitle" %>
<%@ page import="org.jfree.data.general.DefaultPieDataset" %>
<%@ page import="org.jfree.ui.RectangleEdge" %>
<%@ page import="org.jfree.ui.RectangleInsets" %>
<%@ page import="org.jfree.ui.VerticalAlignment" %>
<%@ page import="org.jfree.util.SortOrder" %>
<%@ page import="massbank.web.recordindex.RecordIndexCount" %>
<%@ page import="massbank.web.SearchExecution" %>
<%@ include file="./Common.jsp"%>
<%!
	/** インデックス種別 **/
	private static final String[] indexType = { "site", "inst", "ms", "merged", "ion", "cmpd" };
	/** テーブルヘッダー **/
	private static final String[] tblName = { "Contributor", "Instrument Type", "MS Type", "Merged Type", "Ion Mode", "Compound Name" };
	/** 1行に表示する項目数 **/
	private static final int[] itemNumOfLine = { 3, 3, 5, 2, 2, 6 };
	/** 取得結果のヘッダー **/
	private static final String[] header = { "INSTRUMENT", "MS", "MERGED", "ION", "COMPOUND", "SITE" };
%>
<%
	ServletContext context = getServletContext();
	int pos = -1;
	
	//-------------------------------------------
	// システム情報を取得
	//-------------------------------------------
	String osName = System.getProperty("os.name");			// OS
	
	//-------------------------------------------
	// 環境設定ファイルからURLリストを取得
	//-------------------------------------------
	String baseUrl = Config.get().BASE_URL();
	String serverUrl = Config.get().BASE_URL();

	
	//-------------------------------------
	// 検索実行・結果取得
	//-------------------------------------
	MassBankCommon mbcommon = new MassBankCommon();
	ArrayList<String> result = new SearchExecution(request).exec(new RecordIndexCount());
	
	TreeMap<String, Integer> cntSiteMap = new TreeMap<String, Integer>();
	Map<String, Integer> cntInstMap = new TreeMap<String, Integer>();
	Map<String, Integer> cntMsMap = new TreeMap<String, Integer>();
	Map<String, Integer> cntMergedMap = new LinkedHashMap<String, Integer>();
	Map<String, Integer> cntIonMap = new LinkedHashMap<String, Integer>();
	Map<String, Integer> cntCmpdMap = new LinkedHashMap<String, Integer>();
	
	// 取得結果を格納
	for ( int i=0; i<result.size(); i++ ) {
		String line = (String)result.get(i);
		if ( line.equals("") ) { continue; }
		String[] fields = line.split("\t");
		String key = fields[0].split(":")[0];
		String val = fields[0].split(":")[1];
		
		int count = Integer.parseInt( fields[1] );
		if ( key.equals(header[5]) ) {			// Contributor
			if ( !cntSiteMap.containsKey(val) ) { cntSiteMap.put(val, count); }
			else { cntSiteMap.put(val, cntSiteMap.get(val)+count); }
		}
		else if ( key.equals(header[0]) ) {	// Instrument Type
			if ( !cntInstMap.containsKey(val) ) { cntInstMap.put(val, count); }
			else { cntInstMap.put(val, cntInstMap.get(val)+count); }
		}
		else if ( key.equals(header[1]) ) {	// MS Type
			if ( !cntMsMap.containsKey(val) ) { cntMsMap.put(val, count); }
			else { cntMsMap.put(val, cntMsMap.get(val)+count); }
		}
		else if ( key.equals(header[2]) ) {	// Spectrum Type
			if ( !cntMergedMap.containsKey(val) ) { cntMergedMap.put(val, count); }
			else { cntMergedMap.put(val, cntMergedMap.get(val)+count); }
		}
		else if ( key.equals(header[3]) ) {	// Ion Mode
			if ( !cntIonMap.containsKey(val) ) { cntIonMap.put(val, count); }
			else { cntIonMap.put(val, cntIonMap.get(val)+count); }
		}
		else if ( key.equals(header[4]) ) {	// Compound Name
			if ( !cntCmpdMap.containsKey(val) ) { cntCmpdMap.put(val, count); }
			else { cntCmpdMap.put(val, cntCmpdMap.get(val)+count); }
		}
	}
	
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Style-Type" content="text/css">
	<meta http-equiv="imagetoolbar" content="no">
	<meta name="author" content="MassBank" />
	<meta name="coverage" content="worldwide" />
	<meta name="Targeted Geographic Area" content="worldwide" />
	<meta name="rating" content="general" />
	<meta name="copyright" content="Copyright (c) 2006 MassBank Project and NORMAN Association (c) 2011" />
	<meta name="description" content="Categorized list of spectra. To list up all spectra in a specific category including contributors, instrument types and ionization modes.">
	<meta name="keywords" content="APCI,ITFT,QFT,ESI,EI,LC,IT,GC,TOF,QTOF,FAB,MALDI,APPI,MS,MS/MS,MS2,MS3,MS4,CI,FI,FD,QQ,Merged,Positive,Negative,QIT,ITTOF,EB,mass spectra,MassBank,m/z">
	<meta name="hreflang" content="en">
	<meta name="robots" content="index, follow">
	<meta name="revisit_after" content="10 seconds">
	<link rel="stylesheet" type="text/css" href="./css/Common.css">
	<script type="text/javascript" src="./script/Common.js"></script>
	<title>MassBank | Database | Record Index</title>
	<script type="text/javascript" src="./script/Piwik.js"></script>
</head>
<body class="msbkFont">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Record Index</h1>
			</td>
			<td align="right" class="font12px">
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="./img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%><%=INDEX_PAGE%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<iframe src="./menu.jsp" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	<hr size="1">

	<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
<%-- 	<jsp:include page="pserver/ServerInfo.jsp" /> --%>

	<form>
<%
	String url = "Result.jsp?";
	String linkUrl = "";
	String linkName = "";
	String toolTipName = "";
	String countStr = "";
	int siteItemCnt = 0;
	int instItemCnt = 0;
	int msItemCnt = 0;
	int cmpdItemCnt = 0;
	
	double totalSiteNum = 0;
	double totalInstNum = 0;
	double totalMsNum = 0;
	HashMap<String, Integer> siteData = new HashMap<String, Integer>();
	HashMap<String, Integer> instData = new HashMap<String, Integer>();
	HashMap<String, Integer> msData = new HashMap<String, Integer>();
	
	DecimalFormat numFormat = new DecimalFormat("###,###,###");
	
	String rowspan = "";
	int row = 0;
	
	//----------------------------------------------------------------------
	// Contributor
	//----------------------------------------------------------------------
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
	out.println( "<tr valign=\"top\">" );
	row = (cntSiteMap.size() % itemNumOfLine[0] == 0) ? (cntSiteMap.size() / itemNumOfLine[0]) : (cntSiteMap.size() / itemNumOfLine[0] + 1);
	if ( row > 1 ) { rowspan = " rowspan=\"" + String.valueOf(row) + "\""; }
	else { rowspan = ""; }
	out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[0] + "</b></td>" );
	out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
	Set<String> siteKeys = cntSiteMap.keySet();
	
	for (Iterator i = siteKeys.iterator(); i.hasNext();) {
		String val = (String)i.next();
		//int siteNum = Integer.parseInt(val.split("\t")[1]);
		int count = cntSiteMap.get(val);
		
		// テーブル行終了・開始
		if ( siteItemCnt != 0 && siteItemCnt % itemNumOfLine[0] == 0 ) {
			out.println( "</tr>" );
			out.println( "<tr>" );
		}
		siteItemCnt++;
		
		// テーブルデータ
		// linkName = siteNameList[siteNum];
		linkUrl = url + "type=" + MassBankCommon.REQ_TYPE_RCDIDX + "&idxtype="
				   + indexType[0] + "&srchkey=" + val + "&sortKey=name&sortAction=1&pageNo=1&exec=";
		// toolTipName = siteLongNameList[siteNum];
		countStr = "(" + numFormat.format(count) + ")";
		//countStr = "(" + "<a href=\"" + DataManagement.toMsp(DataManagement.search(indexType[0], String.valueOf(siteNum), "name", "1", ""), toolTipName + ".msp") + "\" title=\"Download " + numFormat.format(count) + " record(s) in NIST *.msp format\"><img src=\"./image/download_icon.png\" title=\"Download " + numFormat.format(count) + " record(s) in NIST *.msp format\" width=\"16\" height=\"16\" />" + numFormat.format(count) + "</a>" + ")";
		out.println( "<td>" );
		//out.println( "<a href=\"" + linkUrl + "\" title=\"" + toolTipName.replaceAll(" ", "&nbsp;") + "\" target=\"_self\">" + linkName.replaceAll(" ", "&nbsp;") + "</a>"
		//		   + "&nbsp;&nbsp;" + countStr + "&nbsp;&nbsp;" );
		out.println( "<a href=\"" + linkUrl + "\" title=\"" + val.replaceAll(" ", "&nbsp;") + "\" target=\"_self\">" + val.replaceAll(" ", "&nbsp;") + "</a>"
				+ "&nbsp;&nbsp;" + countStr + "&nbsp;&nbsp;" );
		out.println( "</td>" );
		
		// グラフ用データ収集
		siteData.put(val, count);
		totalSiteNum += count;
	}
	
	if ( cntSiteMap.size() == 0 ) { out.println( "<td>&nbsp;</td>" ); }
	out.println( "</tr>" );
	out.println( "</table>" );
	out.println( "<br>" );
	
	//---------------------------
	// Instrument Type
	//---------------------------
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
	out.println( "<tr valign=\"top\">" );
	row = (cntInstMap.size() % itemNumOfLine[1] == 0) ? (cntInstMap.size() / itemNumOfLine[1]) : (cntInstMap.size() / itemNumOfLine[1] + 1);
	if ( row > 1 ) { rowspan = " rowspan=\"" + String.valueOf(row) + "\""; }
	else { rowspan = ""; }
	out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[1] + "</b></td>" );
	out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
	Set<String> instKeys = cntInstMap.keySet();
	for (Iterator i = instKeys.iterator(); i.hasNext();) {
		String val = (String)i.next();
		int count = cntInstMap.get(val);
		
		// テーブル行終了・開始
		if ( instItemCnt != 0 && instItemCnt % itemNumOfLine[1] == 0 ) {
			out.println( "</tr>" );
			out.println( "<tr>" );
		}
		instItemCnt++;
		
		// テーブルデータ
		linkUrl = url + "type=" + MassBankCommon.REQ_TYPE_RCDIDX + "&idxtype="
				   + indexType[1] + "&srchkey=" + val + "&sortKey=name&sortAction=1&pageNo=1&exec=";
		countStr = "(" + numFormat.format(count) + ")";
		out.println( "<td>" );
		out.println( "<a href=\"" + linkUrl + "\" target=\"_self\">" + val + "</a>" + "&nbsp;&nbsp;&nbsp;" + countStr );
		out.println( "</td>" );
		
		// グラフ用データ収集
		instData.put(val, count);
		totalInstNum += count;
	}
	if ( cntInstMap.size() == 0 ) { out.println( "<td>&nbsp;</td>" ); }
	out.println( "</tr>" );
	out.println( "</table>" );
	out.println( "<br>" );
	
	//---------------------------
	// MS Type
	//---------------------------
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
	out.println( "<tr valign=\"top\">" );
	row = (cntMsMap.size() % itemNumOfLine[2] == 0) ? (cntMsMap.size() / itemNumOfLine[2]) : (cntMsMap.size() / itemNumOfLine[2] + 1);
	if ( row > 1 ) { rowspan = " rowspan=\"" + String.valueOf(row) + "\""; }
	else { rowspan = ""; }
	out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[2] + "</b></td>" );
	out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
	Set<String> msKeys = cntMsMap.keySet();
	for (Iterator i = msKeys.iterator(); i.hasNext();) {
		String val = (String)i.next();
		int count = cntMsMap.get(val);
		
		// テーブル行終了・開始
		if ( msItemCnt != 0 && msItemCnt % itemNumOfLine[2] == 0 ) {
			out.println( "</tr>" );
			out.println( "<tr>" );
		}
		msItemCnt++;
		
		// テーブルデータ
		linkUrl = url + "type=" + MassBankCommon.REQ_TYPE_RCDIDX + "&idxtype="
				   + indexType[2] + "&srchkey=" + val + "&sortKey=name&sortAction=1&pageNo=1&exec=";
		countStr = "(" + numFormat.format(count) + ")";
		out.println( "<td>" );
		out.println( "<a href=\"" + linkUrl + "\" target=\"_self\">" + val + "</a>" + "&nbsp;&nbsp;&nbsp;" + countStr );
		out.println( "</td>" );
		
		// グラフ用データ収集
		msData.put(val, count);
		totalMsNum += count;
	}
	if ( cntMsMap.size() == 0 ) { out.println( "<td>&nbsp;</td>" ); }
	out.println( "</tr>" );
	out.println( "</table>" );
	out.println( "<br>" );
	
	
	//---------------------------
	// Merged Type
	//---------------------------
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
	out.println( "<tr valign=\"top\">" );
	row = (cntMergedMap.size() % itemNumOfLine[3] == 0) ? (cntMergedMap.size() / itemNumOfLine[3]) : (cntMergedMap.size() / itemNumOfLine[3] + 1);
	if ( row > 1 ) { rowspan = " rowspan=\"" + String.valueOf(row) + "\""; }
	else { rowspan = ""; }
	out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[3] + "</b></td>" );
	out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
	Set<String> mergedKeys = cntMergedMap.keySet();
	for (Iterator i = mergedKeys.iterator(); i.hasNext();) {
		String val = (String)i.next();
		int count = cntMergedMap.get(val);
		
		// テーブルデータ
		linkUrl = url + "type=" + MassBankCommon.REQ_TYPE_RCDIDX + "&idxtype="
				   + indexType[3] + "&srchkey=" + val + "&sortKey=name&sortAction=1&pageNo=1&exec=";
		countStr = "(" + numFormat.format(count) + ")";
		if ( val.equals("Normal") ) { out.println( "<td width=\"220\">" ); } else { out.println( "<td>" ); }
		out.println( "<a href=\"" + linkUrl + "\" target=\"_self\">" + val + "</a>" + "&nbsp;&nbsp;&nbsp;" + countStr );
		out.println( "</td>" );
	}
	if ( cntMergedMap.size() == 0 ) { out.println( "<td>&nbsp;</td>" ); }
	out.println( "</tr>" );
	out.println( "</table>" );
	out.println( "<br>" );
	
	
	//---------------------------
	// Ion Mode
	//---------------------------
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
	out.println( "<tr valign=\"top\">" );
	row = (cntIonMap.size() % itemNumOfLine[4] == 0) ? (cntIonMap.size() / itemNumOfLine[4]) : (cntIonMap.size() / itemNumOfLine[4] + 1);
	if ( row > 1 ) { rowspan = " rowspan=\"" + String.valueOf(row) + "\""; }
	else { rowspan = ""; }
	out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[4] + "</b></td>" );
	out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
	Set<String> ionKeys = cntIonMap.keySet();
	for (Iterator i = ionKeys.iterator(); i.hasNext();) {
		String val = (String)i.next();
		int count = cntIonMap.get(val);
		
		// テーブルデータ
		linkUrl = url + "type=" + MassBankCommon.REQ_TYPE_RCDIDX + "&idxtype="
				   + indexType[4] + "&srchkey=" + val + "&sortKey=name&sortAction=1&pageNo=1&exec=";
		countStr = "(" + numFormat.format(count) + ")";
		if ( val.equals("Positive") ) { out.println( "<td width=\"220\">" ); } else { out.println( "<td>" ); }
		out.println( "<a href=\"" + linkUrl + "\" target=\"_self\">" + val + "</a>" + "&nbsp;&nbsp;&nbsp;" + countStr );
		out.println( "</td>" );
	}
	if ( cntIonMap.size() == 0 ) { out.println( "<td>&nbsp;</td>" ); }
	out.println( "</tr>" );
	out.println( "</table>" );
	out.println( "<br>" );
	
	
	//---------------------------
	// Compound Name
	//---------------------------
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
	out.println( "<tr valign=\"top\">" );
	row = (cntCmpdMap.size() % itemNumOfLine[5] == 0) ? (cntCmpdMap.size() / itemNumOfLine[5]) : (cntCmpdMap.size() / itemNumOfLine[5] + 1);
	if ( row > 1 ) { rowspan = " rowspan=\"" + String.valueOf(row) + "\""; }
	else { rowspan = ""; }
	out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[5] + "</b></td>" );
	out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
	Set<String> cmpdKeys = cntCmpdMap.keySet();
	for (Iterator i = cmpdKeys.iterator(); i.hasNext();) {
		String val = (String)i.next();
		int count = cntCmpdMap.get(val);
		
		// テーブル行終了・開始
		if ( cmpdItemCnt != 0 && cmpdItemCnt % itemNumOfLine[5] == 0 ) {
			out.println( "</tr>" );
			out.println( "<tr>" );
		}
		cmpdItemCnt++;
		
		// テーブルデータ
		linkUrl = url + "type=" + MassBankCommon.REQ_TYPE_RCDIDX + "&idxtype="
				   + indexType[5] + "&srchkey=" + val + "&sortKey=name&sortAction=1&pageNo=1&exec=";
		countStr = "(" + numFormat.format(count) + ")";
		out.println( "<td>" );
		out.println( "<a href=\"" + linkUrl + "\" target=\"_self\">" + val + "</a>" + "&nbsp;&nbsp;&nbsp;" + countStr );
		out.println( "</td>" );
	}
	if ( cntCmpdMap.size() == 0 ) { out.println( "<td>&nbsp;</td>" ); }
	out.println( "</tr>" );
	out.println( "</table>" );
	out.println( "<br>" );
	
	
	out.println( "<hr size=\"1\">" );
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"12\" cellspacing=\"12\">" );
	
	//---------------------------
	// construct and show pie charts
	//---------------------------
	// グラフデータセットオブジェクト
	DefaultPieDataset siteGraphData = new DefaultPieDataset();
	DefaultPieDataset instGraphData = new DefaultPieDataset();
	DefaultPieDataset msGraphData = new DefaultPieDataset();
	
	// グラフデータ表示件数
	final int MAX_DISP_DATA = 10;
	Set<String> keys = null;
	String key = null;
	int val = 0;
	BigDecimal percent = null;
	String label = null;
	
	// Contributorグラフデータセット
	if (totalSiteNum > 0) {
		keys = siteData.keySet();
		int siteNum = 0;
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			key = (String)iterator.next();
			val = siteData.get(key);
			percent = new BigDecimal(String.valueOf(val / totalSiteNum * 100));
			label = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			siteNum++;
			siteGraphData.setValue(label, val);
		}
		siteGraphData.sortByValues(SortOrder.DESCENDING);
		
		// グラフデータが多い場合は表示データを省略
		long etcVal = 0;
		int siteCount = siteGraphData.getItemCount();
		ArrayList<Comparable> etcList = new ArrayList<Comparable>();
		if ( siteCount > MAX_DISP_DATA) {
			for (int index=MAX_DISP_DATA; index<siteCount; index++) {
				etcList.add(siteGraphData.getKey(index));
			}
			for (Comparable etcKey:etcList) {
				etcVal += siteGraphData.getValue(etcKey).longValue();
				siteGraphData.remove(etcKey);
			}
			percent = new BigDecimal(String.valueOf(etcVal / totalSiteNum * 100));
			label = "etc. : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			siteGraphData.setValue(label, etcVal);
		}
	}
	else {
		siteGraphData.setValue("No Contributor Data", 0);
	}
	
	// Instrument Typeグラフデータセット
	if (totalInstNum > 0) {
		keys = instData.keySet();
		int instNum = 0;
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			key = (String)iterator.next();
			val = instData.get(key);
			percent = new BigDecimal(String.valueOf(val / totalInstNum * 100));
			label = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			instNum++;
			instGraphData.setValue(label, val);
		}
		instGraphData.sortByValues(SortOrder.DESCENDING);
		
		// グラフデータが多い場合は表示データを省略
		long etcVal = 0;
		int instCount = instGraphData.getItemCount();
		ArrayList<Comparable> etcList = new ArrayList<Comparable>();
		if ( instCount > MAX_DISP_DATA) {
			for (int index=MAX_DISP_DATA; index<instCount; index++) {
				etcList.add(instGraphData.getKey(index));
			}
			for (Comparable etcKey:etcList) {
				etcVal += instGraphData.getValue(etcKey).longValue();
				instGraphData.remove(etcKey);
			}
			percent = new BigDecimal(String.valueOf(etcVal / totalInstNum * 100));
			label = "etc. : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			instGraphData.setValue(label, etcVal);
		}
	}
	else {
		instGraphData.setValue("No Instrument Type Data", 0);
	}
	
	
	// MS Typeグラフデータセット
	if (totalMsNum > 0) {
		keys = msData.keySet();
		int msNum = 0;
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			key = (String)iterator.next();
			val = msData.get(key);
			percent = new BigDecimal(String.valueOf(val / totalMsNum * 100));
			label = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			msNum++;
			msGraphData.setValue(label, val);
		}
		msGraphData.sortByValues(SortOrder.DESCENDING);
		
		// グラフデータが多い場合は表示データを省略
		long etcVal = 0;
		int msCount = msGraphData.getItemCount();
		ArrayList<Comparable> etcList = new ArrayList<Comparable>();
		if ( msCount > MAX_DISP_DATA) {
			for (int index=MAX_DISP_DATA; index<msCount; index++) {
				etcList.add(msGraphData.getKey(index));
			}
			for (Comparable etcKey:etcList) {
				etcVal += msGraphData.getValue(etcKey).longValue();
				msGraphData.remove(etcKey);
			}
			percent = new BigDecimal(String.valueOf(etcVal / totalMsNum * 100));
			label = "etc. : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			msGraphData.setValue(label, etcVal);
		}
	}
	else {
		msGraphData.setValue("No MS Type Data", 0);
	}
	
	// グラフ一括生成＆出力
	try {
		LinkedHashMap<String, DefaultPieDataset> graphDataMap = new LinkedHashMap<String, DefaultPieDataset>(2);
		int siteTopNum = (siteGraphData.getItemCount() < MAX_DISP_DATA) ? siteGraphData.getItemCount() : MAX_DISP_DATA;
		int instTopNum = (instGraphData.getItemCount() < MAX_DISP_DATA) ? instGraphData.getItemCount() : MAX_DISP_DATA;
		int msTopNum = (msGraphData.getItemCount() < MAX_DISP_DATA) ? msGraphData.getItemCount() : MAX_DISP_DATA;
		graphDataMap.put("Contributor  top " + siteTopNum, siteGraphData);
		graphDataMap.put("Instrument Type  top " + instTopNum, instGraphData);
		graphDataMap.put("MS Type  top " + msTopNum, msGraphData);
		DefaultPieDataset data = null;
		String fileName = null;
		String filePath = null;
		
		keys = graphDataMap.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			key = (String)iterator.next();
			
			// グラフ用データ取得
			data = graphDataMap.get(key);
			
			// JFreeChartオブジェクト生成
			JFreeChart chart = ChartFactory.createPieChart(key, data, true, true, false);
			
			// グラフ全体背景色設定
			chart.setBackgroundPaint(new Color(242,246,255));
			
			// グラフ全体境界線設定
			chart.setBorderVisible(true);
			
			// グラフエリアパディング設定
			chart.setPadding(new RectangleInsets(10,10,10,10));
			
			// グラフタイトルフォント設定
			chart.getTitle().setFont(new Font("Arial", Font.BOLD, 22));
			
			// 凡例の表示位置設定
			LegendTitle legend = chart.getLegend();
			legend.setPosition(RectangleEdge.RIGHT);
			
			// 凡例パディング設定
			legend.setPadding(10,10,10,10);
			
			// 凡例マージン設定
			legend.setMargin(0,10,0,5);
			
			// 凡例表示位置上寄せ
			legend.setVerticalAlignment(VerticalAlignment.TOP);
			
			// グラフの描画領域を取得
			PiePlot plot = (PiePlot)chart.getPlot();
			
			// グラフの楕円表示を許可する
			plot.setCircular(true);
			
			// グラフ境界線色設定
			plot.setBaseSectionOutlinePaint(Color.BLACK);
			
			// グラフラベル背景色設定
			plot.setLabelBackgroundPaint(new Color(240,255,255));
			
			// グラフラベルフォント設定
			plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
			
			// グラフラベル幅設定
			plot.setMaximumLabelWidth(0.3);
			
			// グラフラベル間距離設定
			plot.setLabelGap(0.05);
			
			// グラフ背景色透明度設定
//			plot.setBackgroundAlpha(0.9f);
			
			// グラフ前景色透明度設定
			plot.setForegroundAlpha(0.9f);
			
			// グラフのファイル出力
			fileName = "massbank_" + key + "_Graph.jpg";
			fileName = Pattern.compile("[ ]*top[ ]*[0-9]*").matcher(fileName).replaceAll("");
			
			filePath = Config.get().TOMCAT_TEMP_PATH(context) + fileName;
			BufferedOutputStream outStream = null;
			try {
				outStream = new BufferedOutputStream(new FileOutputStream(filePath));
				ChartUtilities.writeChartAsJPEG(outStream, chart, 900, 350);
			}
			catch ( IOException ie ) {
				ie.printStackTrace();
			}
			finally {
				if ( outStream != null ) {
					try {
						outStream.flush();
						outStream.close();
					} catch (IOException ie) {
						ie.printStackTrace();
					}
				}
			}
			
			// グラフの表示
			out.println( "<tr>" );
			out.println( "<td>" );
			//out.println( "<img src=\"" + Config.get().BASE_URL() + "temp/" + fileName + "\" alt=\"\" border=\"0\">" );
			out.println( "<img src=\"" + Config.get().TOMCAT_TEMP_URL() + fileName + "\" alt=\"\" border=\"0\">" );
			out.println( "</td>" );
			out.println( "</tr>" );
		}
	}
	catch ( NoClassDefFoundError nc ) {	// for linux...(a transitional program)
		// "java.lang.NoClassDefFoundError: Could not initialize class sun.awt.X11GraphicsEnvironment" by ChartFactory.createPieChart
		// Linuxでグラフを描画しようとした際に上記のエラーが発生する環境ではグラフを表示しない（暫定対処）
		out.println( "<tr><td>Graph can not be rendered by the influence of environment on the server....</td></tr>" );
		Logger.getLogger("global").warning("Graph can not be rendered by the influence of environment on the server....");
		nc.printStackTrace();
	}
	out.println( "</table>" );
%>
</form>
<hr size="1">
<iframe src="./copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
