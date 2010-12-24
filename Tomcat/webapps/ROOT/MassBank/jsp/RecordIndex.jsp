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
 * ver 1.0.21 2010.12.24
 *
 ******************************************************************************/
%>

<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="java.awt.Color" %>
<%@ page import="java.awt.Font" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.DecimalFormat" %>
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
<%@ include file="./Common.jsp"%>
<%!
	/** インデックス種別 **/
	private static final String[] indexType = { "site", "inst", "ion", "cmpd" };
	/** テーブルヘッダー **/
	private static final String[] tblName = { "Contributor", "Instrument Type", "Ionization Mode", "Compound Name" };
	/** 1行に表示する項目数 **/
	private static final int[] itemNumOfLine = { 3, 3, 2, 6 };
	/** 取得結果のヘッダー **/
	private static final String[] header = { "INSTRUMENT", "ION", "COMPOUND" };
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
	String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	GetConfig conf = new GetConfig(baseUrl);
	String serverUrl = conf.getServerUrl();
	String[] siteNameList = conf.getSiteName();
	String[] siteLongNameList = conf.getSiteLongName();
	
	//-------------------------------------
	// 検索実行・結果取得
	//-------------------------------------
	MassBankCommon mbcommon = new MassBankCommon();
	String typeName = mbcommon.CGI_TBL[mbcommon.CGI_TBL_NUM_TYPE][mbcommon.CGI_TBL_TYPE_IDXCNT];
	ArrayList result = mbcommon.execMultiDispatcher( serverUrl, typeName, "" );
	
	Map<String, Integer>[] countMap = new HashMap[siteNameList.length];
	ArrayList<String> keyList = new ArrayList();
	
	for ( int siteNum = 0; siteNum < siteNameList.length; siteNum++ ) {
		countMap[siteNum] = new HashMap();
	}
	
	// 取得結果を格納
	int[] numRows = new int[header.length + 1];
	numRows[0] = siteNameList.length;
	for ( int i = 0; i < result.size(); i++ ) {
		String line = (String)result.get(i);
		if ( !line.equals("") ) {
			String[] fields = line.split("\t");
			int siteNum = Integer.parseInt(fields[fields.length - 1]);
			String key = fields[0];
			
			int count = Integer.parseInt( fields[1] );
			if ( countMap[siteNum].get( key ) != null ) {
				count = count + countMap[siteNum].get( key );
			}
			countMap[siteNum].put( key, count );

			boolean isFound = false;
			for ( int j = 0; j < keyList.size(); j++ ) {
				if ( keyList.get(j).equals(key) ) {
					isFound = true;
					break;
				}
			}
			if ( !isFound ) {
				// キーリスト追加
				keyList.add( key );
				
				// 各項目数カウント
				for ( int j = 0; j < header.length; j++ ) {
					if ( key.indexOf( header[j] ) >= 0 ) {
						numRows[j+1]++;
						break;
					}
				}
			}
		}
	}
	ArrayList<String> adjustKeyList = new ArrayList<String>();
	adjustKeyList.add( "//" );
	adjustKeyList.add( keyList.get(0) );
	adjustKeyList.add( "//" );
	for ( int i = 0; i < header.length; i++ ) {
		for ( int j = 1; j < keyList.size(); j++ ) {
			String key = keyList.get(j);
			pos = key.indexOf(":");
			String keyHead = key.substring( 0, pos );
			if ( header[i].equals( keyHead ) ) {
				adjustKeyList.add( key );
			}
		}
		adjustKeyList.add( "//" );
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
	<meta name="copyright" content="Copyright (c) since 2006 JST-BIRD MassBank" />
	<meta name="description" content="Categorized list of spectra. To list up all spectra in a specific category including contributors, instrument types and ionization modes.">
	<meta name="keywords" content="ESI,EI,LC,IT,GC,TOF,FAB,MALDI,MS,MS/MS,MSn,CI,FI,FD,QqQ">
	<meta name="revisit_after" content="30 days">
	<link rel="stylesheet" type="text/css" href="./css/Common.css">
	<script type="text/javascript" src="./script/Common.js"></script>
	<title>MassBank | Database | Record Index</title>
</head>
<body class="msbkFont">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Record Index</h1>
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

	<form>
<%
	boolean nextLine = false;
	String url = "./jsp/Result.jsp?";
	
	String linkUrl = "";
	String paramType = MassBankCommon.REQ_TYPE_RCDIDX;
	String paramIdxType = "";
	String paramSearchKey = "";
	
	String linkName = "";
	String toolTipName = "";
	String countStr = "";
	int siteItemCnt = 0;
	int instItemCnt = 0;
	int nameItemCnt = 0;
	int tblCnt = 0;
	
	double totalSiteNum = 0;
	double totalInstNum = 0;
	HashMap<String, Integer> siteData = new HashMap<String, Integer>();
	HashMap<String, Integer> instData = new HashMap<String, Integer>();
	
	DecimalFormat numFormat = new DecimalFormat("###,###,###");
	
	for ( int i = 0; i < adjustKeyList.size(); i++ ) {
		String key = adjustKeyList.get(i);
		
		//----------------------------------------------------------------------
		// 改テーブルの場合
		//----------------------------------------------------------------------
		if ( key.equals("//") ) {
			// ブロック終了タグ出力
			if ( i > 0 ) {
				out.println( "</tr>" );
				out.println( "</table>" );
				out.println( "<br>" );
				tblCnt++;
			}
			
			// ブロック開始タグ出力
			if ( i < keyList.size() - 1 ) {
				// セル縦方向連結指定
				String rowspan = "";
				int row = numRows[tblCnt] / itemNumOfLine[tblCnt] + 1;
				if ( row > 1 ) { 
					rowspan = " rowspan=\"" + String.valueOf(row) + "\"";
				}
				out.println( "<table width=\"900\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">" );
				out.println( "<tr valign=\"top\">" );
				out.println( "<td width=\"140\"" + rowspan + " nowrap><b>" + tblName[tblCnt] + "</b></td>" );
				out.println( "<td width=\"10\"" + rowspan + "><b>:</b></td>" );
			}
			continue;
		}
		
		//----------------------------------------------------------------------
		// siteの場合
		//----------------------------------------------------------------------
		int count = 0;
		if ( key.equals("site") ) {
			for ( int siteNum = 0; siteNum < siteNameList.length; siteNum++ ) {
				if ( countMap[siteNum].get( key ) == null ) {
					continue;
				}
				count = countMap[siteNum].get( key );
				
				// リンク名、件数
				linkName = siteNameList[siteNum];
				toolTipName = siteLongNameList[siteNum];
				countStr = "(" + numFormat.format(count) + ")";
				
				// パラメータ
				paramIdxType = indexType[0];
				paramSearchKey = String.valueOf(siteNum);
				
				linkUrl = url + "type=" + paramType + "&idxtype="
						   + paramIdxType + "&srchkey=" + paramSearchKey + "&sortKey=name&sortAction=1&pageNo=1&exec=";
				
				// テーブル行終了・開始
				if ( siteItemCnt != 0 && siteItemCnt % itemNumOfLine[tblCnt] == 0 ) {
					out.println( "</tr>" );
					out.println( "<tr>" );
				}
				siteItemCnt++;
				
				// テーブルデータ
				out.println( "<td>" );
				out.println( "<a href=\"" + linkUrl + "\" title=\"" + toolTipName.replaceAll(" ", "&nbsp;") + "\" target=\"_self\">" + linkName.replaceAll(" ", "&nbsp;") + "</a>"
						   + "&nbsp;&nbsp;" + countStr + "&nbsp;&nbsp;" );
				out.println( "</td>" );
				
				// グラフ用データ収集
				siteData.put(linkName, count);
				totalSiteNum += count;
			}
		}
		//----------------------------------------------------------------------
		// Instrument Type, Ionization Mode, Compound Nameの場合
		//----------------------------------------------------------------------
		else {
			nextLine = false;
			//---------------------------
			// Instrument Type
			//---------------------------
			if ( key.indexOf( header[0] ) >= 0 ) {
				// リンク名
				pos = key.indexOf(":");
				linkName = key.substring( pos + 1 );
				
				// パラメータ
				paramSearchKey = linkName;
				paramIdxType = indexType[1];
				
				// テーブル改行フラグセット
				if ( instItemCnt != 0 && instItemCnt % itemNumOfLine[tblCnt] == 0 ) {
					nextLine = true;
				}
				instItemCnt++;
			}
			//---------------------------
			// Ionization Mode
			//---------------------------
			else if ( key.indexOf( header[1] ) >= 0 ) {
				// リンク名
				pos = key.indexOf(":");
				linkName = key.substring( pos + 1 );
				
				//パラメータ
				paramSearchKey = linkName;
				paramIdxType = indexType[2];
			}
			//---------------------------
			// Compound Name
			//---------------------------
			else {
				// リンク名
				pos = key.indexOf(":");
				linkName = key.substring( pos + 1 );
				
				// パラメータ
				paramSearchKey = linkName;
				paramIdxType = indexType[3];
				
				// テーブル改行フラグセット
				if ( nameItemCnt != 0 && nameItemCnt % itemNumOfLine[tblCnt] == 0 ) {
					nextLine = true;
				}
				nameItemCnt++;
			}
			
			// 件数を合算
			for ( int siteNum = 0; siteNum < siteNameList.length; siteNum++ ) {
				if ( countMap[siteNum].get( key ) != null ) {
					count += countMap[siteNum].get( key );
				}
			}
			countStr = "(" + numFormat.format(count) + ")";
			
			// リンクURLセット
			linkUrl = url + "type=" + paramType + "&idxtype="
					   + paramIdxType + "&srchkey=" + paramSearchKey + "&sortKey=name&sortAction=1&pageNo=1&exec=";
			
			// テーブル行終了・開始
			if ( nextLine ) {
				out.println( "</tr>" );
				out.println( "<tr>" );
			}
			
			// テーブルデータ
			out.println( "<td>" );
			out.println( "<a href=\"" + linkUrl + "\" target=\"_self\">" + linkName + "</a>"
						   + "&nbsp;&nbsp;&nbsp;" + countStr );
			out.println( "</td>" );
			
			// グラフ用データ収集
			if ( key.indexOf( header[0] ) >= 0 ) {
				instData.put(linkName, count);
				totalInstNum += count;
			}
		}
	}
	out.println( "<hr size=\"1\">" );
	out.println( "<table width=\"900\" border=\"0\" cellpadding=\"12\" cellspacing=\"12\">" );
	
	// グラフデータセットオブジェクト
	DefaultPieDataset siteGraphData = new DefaultPieDataset();
	DefaultPieDataset instGraphData = new DefaultPieDataset();
	
	Set keys = null;
	String key = null;
	int val = 0;
	BigDecimal percent = null;
	String lavel = null;
	
	// Siteグラフデータセット
	if (totalSiteNum > 0) {
		keys = siteData.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			key = (String)iterator.next();
			val = siteData.get(key);
			percent = new BigDecimal(String.valueOf(val / totalSiteNum * 100));
			lavel = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			
			siteGraphData.setValue(lavel, val);
		}
	}
	else {
		siteGraphData.setValue("No Contributor Data", 0);
	}
	
	// Instrument Typeグラフデータセット
	if (totalInstNum > 0) {
		keys = instData.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			key = (String)iterator.next();
			val = instData.get(key);
			percent = new BigDecimal(String.valueOf(val / totalInstNum * 100));
			lavel = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
			
			instGraphData.setValue(lavel, val);
		}
	}
	else {
		instGraphData.setValue("No Instrument Type Data", 0);
	}
	
	
	// グラフ一括生成＆出力
	LinkedHashMap<String, DefaultPieDataset> graphDataMap = new LinkedHashMap<String, DefaultPieDataset>(2);
	graphDataMap.put("Contributor", siteGraphData);
	graphDataMap.put("Instrument Type", instGraphData);
	DefaultPieDataset data = null;
	String fileName = null;
	String filePath = null;
	
	keys = graphDataMap.keySet();
	for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
		key = (String)iterator.next();
		
		// グラフ用データ取得
		data = graphDataMap.get(key);
		
		// グラフ用データソート
		data.sortByValues(SortOrder.DESCENDING);
		
		// JFreeChartオブジェクト生成
		JFreeChart chart = ChartFactory.createPieChart(key, data, true, true, false);
		
		// グラフ全体背景色設定
		chart.setBackgroundPaint(new Color(240,255,255));
		
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
//		plot.setBackgroundAlpha(0.9f);
		
		// グラフ前景色透明度設定
		plot.setForegroundAlpha(0.9f);
		
		// グラフのファイル出力
		fileName = "massbank_" + key + "_Graph.jpg";
		filePath = MassBankEnv.get( MassBankEnv.KEY_TOMCAT_APPTEMP_PATH ) + fileName;
		BufferedOutputStream outStream = null;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(filePath));
			ChartUtilities.writeChartAsJPEG(outStream, chart, 900, 400);
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
		out.println( "<img src=\"./temp/" + fileName + "\" alt=\"\" border=\"0\">" );
		out.println( "</td>" );
		out.println( "</tr>" );
	}
	out.println( "</table>" );
%>
</form>
<hr size="1">
<iframe src="./copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
