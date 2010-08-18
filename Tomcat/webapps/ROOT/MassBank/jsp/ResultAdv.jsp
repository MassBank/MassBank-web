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
 * Peak Search Advanced 検索結果を表示する
 *
 * ver 1.0.8 2010.08.18
 *
 ******************************************************************************/
%>
<%@ page import="java.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="org.apache.commons.lang.NumberUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.ResultList" %>
<%@ page import="massbank.ResultRecord" %>
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
	private final String strucOffGif = "../image/strucOff.gif";
	private final String strucOverGif = "../image/strucOver.gif";
	private final String strucOnGif = "../image/strucOn.gif";

	/**
	 *
	 */ 
	private String getPageLinkHtml(boolean isTop, int pageNo, int[] pageIndex, int totalPage) {
		StringBuffer html = new StringBuffer();
		String str1 = "";
		String str2 = "";
		String str3 = "";

		if ( isTop ) {
			str1 = "pageLinkTop";
			str2 = "top";
		}
		else {
			str1 = "pageLinkBottom";
			str2 = "bottom";
		}
		html.append( "<table width=\"" + tableWidth + "\" height=\"30\" cellpadding=\"2\" cellspacing=\"0\" class=\"" + str1 + "\">\n" );
		html.append( " <tr align=\"center\" valign=\"" + str2 + "\">\n" );

		for ( int i = pageIndex[0]; i <= pageIndex[1]; i++ ) {
			if ( i == pageIndex[0] ) {
				if ( pageNo == 1 ) {
					html.append( "  <td width=\"38\"><font color=\"#3300CC\">First</font></td>\n" );
					html.append( "  <td width=\"34\"><font color=\"#3300CC\">Prev</font></td>\n" );
				}
				else {
					html.append( "  <td width=\"38\">\n" );
		 			html.append( "   <a href=\"\" class=\"pageLink\" onClick=\"return changePage(''," + 1 + ")\">First</a></b>\n" );
					html.append( "  </td>\n" );
					html.append( "  <td width=\"34\">\n" );
					html.append( "   <a href=\"\" class=\"pageLink\" onClick=\"return changePage(''," + (pageNo-1) + ")\">Prev</a>\n" );
					html.append( "  </td>\n" );
				}
				html.append( "  <td width=\"8\">&nbsp;</td>\n" );
			}

			if ( i == pageNo ) {
				html.append( "  <td width=\"16\"><i><b>" + i + "</b></i></td>\n" );
			}
			else {
				html.append( "  <td width=\"16\">"
					+ "<a href=\"\" class=\"pageLink\" onClick=\"return changePage(''," + i + ")\">" + i + "</a></td>\n" );
			}

			if ( i == pageIndex[1] ) {
				html.append( "  <td width=\"8\">&nbsp;</td>\n" );
				if ( pageNo == totalPage ) {
					html.append( "  <td width=\"34\"><font color=\"#3300CC\">Next</font></td>\n" );
					html.append( "  <td width=\"38\"><font color=\"#3300CC\">Last</font></td>\n" );
				}
				else {
					html.append( "  <td width=\"34\">"
				 		+ "<a href=\"\" class=\"pageLink\" onClick=\"return changePage(''," + (pageNo+1) + ")\">Next</a></td>\n" );
					html.append( "  <td width=\"38\">"
						+ "<a href=\"\" class=\"pageLink\" onClick=\"return changePage(''," + totalPage + ")\">Last</a></td>\n" );
				}
			}
		}

		html.append( "  <td align=\"left\">&nbsp;&nbsp;&nbsp;( Total <i><b>" + totalPage + "</b></i> Page )</td>\n" );
		html.append( "  <td class=\"font12px\" align=\"right\" valign=\"bottom\">\n" );

		if ( isTop ) {
			str1 = "resultsEnd";
			str2 = "Results End";
			str3 = "&#9660;";
		}
		else {
			str1 = "resultsTop";
			str2 = "Results Top";
			str3 = "&#9650;";
		}

		html.append( "   <a class=\"moveDispLink\" href=\"#" + str1 + "\">&nbsp;&nbsp;"
				+ "<span class=\"font10px2\">" + str3 + "&nbsp;</span>" + str2 + "</a>\n" );
		html.append( "  </td>\n" );
		html.append( " </tr>\n" );
		html.append( "</table>\n");

		return html.toString();
	}

	/**
	 * 分子式の元素記号を並び替える
	 */
	private String swapFormula(String formula) {

		// 元素記号の順番 C, H 以降はアルファベット順
		String[] aromSequece = new String[]{
			"C", "H", "Cl", "F", "I", "N", "O", "P", "S", "Si"
		};
		HashMap<String, Integer> atomList = getAtomList(formula);
	
		String swapFormula = "";
		Set keys = atomList.keySet();
		for ( int i = 0; i < aromSequece.length; i++ ) {
			for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {

				String atom = (String)iterator.next();
				int num = atomList.get(atom);

				if ( atom.equals(aromSequece[i]) )  {
					swapFormula += atom;
					// 個数が1個の場合、個数は書かない
					if ( num > 1 ) {
						swapFormula += String.valueOf(num);
					}
					break;
				}
			}
		}
		return swapFormula;
	}

	/**
	 * 分子式を元素記号と個数に分解する
	 */
	private HashMap<String, Integer> getAtomList(String formula) {

		HashMap<String, Integer> atomList = new HashMap();
		int startPos = 0;
		int endPos = formula.length();
		for ( int pos = 1; pos <= endPos; pos++ ) {
			String chr = "";
			if ( pos < endPos ) {
				chr = formula.substring( pos, pos + 1 );
			}
			if ( pos == endPos || (!NumberUtils.isNumber(chr) && chr.equals(chr.toUpperCase())) ) {
				// 元素記号 + 個数を切り出す
				String item = formula.substring( startPos, pos );

				// 元素記号と個数を分解
				boolean isFound = false;
				int i;
				for ( i = 1; i < item.length(); i++ ) {
					chr = item.substring(i, i + 1);
					if ( NumberUtils.isNumber(chr) ) {
						isFound = true;
						break;
					}
				}
				String atom = item.substring(0, i);
				int num = 1;
				if ( isFound ) {
					num = Integer.parseInt(item.substring(i));
				}
				// 元素が同じ場合
				if ( atomList.get(atom) != null ) {
					num = num + atomList.get(atom);
				}
				// 値格納
				atomList.put(atom, num);

				startPos = pos;
			}
		}
		return atomList;
	}

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
				if ( !gifFile.equals("") ) {
					if ( siteNo == 0 ) {
						gifUrl = serverUrl + "DB/gif/" + dbNameList[siteNo] + "/" + gifFile;
					}
					else {
						gifUrl = urlList[siteNo] + "DB/gif/" + dbNameList[siteNo] + "/" + gifFile;
					}
				}
			}
			else if ( line.indexOf("---GIF_SMALL:") != -1 ) {
				String gifFile = line.replaceAll("---GIF_SMALL:", "");
				if ( !gifFile.equals("") ) {
					if ( siteNo == 0 ) {
						gifSmallUrl = serverUrl + "DB/gif_small/" + dbNameList[siteNo] + "/" + gifFile;
					}
					else {
						gifSmallUrl = urlList[siteNo] + "DB/gif_small/" + dbNameList[siteNo] + "/" + gifFile;
					}
				}
			}
			else if ( line.indexOf("---GIF_LARGE:") != -1 ) {
				String gifFile = line.replaceAll("---GIF_LARGE:", "");
				if ( !gifFile.equals("") ) {
					if ( siteNo == 0 ) {
						gifLargeUrl = serverUrl + "DB/gif_large/" + dbNameList[siteNo] + "/" + gifFile;
					}
					else {
						gifLargeUrl = urlList[siteNo] + "DB/gif_large/" + dbNameList[siteNo] + "/" + gifFile;
					}
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
	//-------------------------------------
	// リファラー（遷移元）、及びタイトル設定
	//-------------------------------------
	boolean refNeutral = false;
	String title = "";
	String type = request.getParameter("type");
	if ( type.equals("product") ) {
		refNeutral = false;
		title = "Product Ion Search Results";
	}
	else if ( type.equals("neutral") ) {
		refNeutral = true;
		title = "Neutral Loss Search Results";
	}

	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	// 全てのリクエストパラメータをハッシュに持つ
	Hashtable<String, Object> reqParams = new Hashtable<String, Object>();
	Enumeration names = request.getParameterNames();
	String exec = (request.getParameter("exec") != null) ? (String)request.getParameter("exec") : "";
	TreeMap mapFormula= new TreeMap();
	String stype = "";
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		if ( exec.equals("sort") || exec.equals("page")) {
			// ソートまたはページ変更の場合はチェックボックスの値を保持しない
			if ( key.equals("chkAll") || key.equals("pid") || key.equals("id") ) {
				continue;
			}
		}
		String val = request.getParameter( key ).trim();
		if ( !val.equals("") ) {
			reqParams.put( key, val );
			if ( key.equals("type") ) {
				stype = val;
			}
			else if ( key.indexOf("formula") == 0 ) {
				mapFormula.put( key, val );
			}
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
	String searchParam = "";
	
	// 検索実行用パラメータセット
	for ( Enumeration keys = reqParams.keys(); keys.hasMoreElements(); ) {
		String key = (String)keys.nextElement();
		if ( !key.equals("type") && key.indexOf("formula") == -1 ) {
			String val = (String)reqParams.get(key);
			searchParam += key + "=" + val + "&";
		}
	}
	searchParam = searchParam.substring( 0, searchParam.length() -1 );

	//-------------------------------------------
	// 設定ファイルから各種情報を取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	String baseUrl = path.substring( 0, (path.indexOf("/jsp")+1) );
	GetConfig conf = new GetConfig(baseUrl);
	String serverUrl = conf.getServerUrl();
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
<script type="text/javascript" src="../script/jquery_imgprev.js"></script>
<script>
<!--
function prevPeakSearchAdv() {
	document.resultForm.action = "../PeakSearchAdv.html";
	document.resultForm.target = "_self";
	document.resultForm.submit();
	return false;
}
-->
</script>
<title><%=title%></title>
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
<%
	//--------------------------------------------------------
	// 検索パラメータ表示
	//--------------------------------------------------------
	out.println( "<a name=\"resultsTop\"></a>" );
	String style = "";
	String str = "";
	if ( refNeutral ) {
		style = "bgNeutral";
		str = "Neutral&nbsp;Loss&nbsp;";
	}
	else {
		style = "bgProduct";
		str = "Product&nbsp;Ion&nbsp;";
	}
	String logic = "";
	String mode = (String)reqParams.get("mode");
	if ( mode.equals("seq") ) {
		logic = "<img src=\"../image/arrow_green.gif\">";
	}
	else {
		logic = "<b class=\"logic\">" + mode.toUpperCase() + "</b>";
	}

	//--------------------------------------------------------
	// 入力された分子式が重複していないかをチェック
	// (Product Ion検索のみ)
	//--------------------------------------------------------
	ArrayList<String> listFormula = new ArrayList();
	Iterator it = mapFormula.keySet().iterator();
	while ( it.hasNext() ) {
		String mkey = (String)it.next();
		String formula = (String)mapFormula.get(mkey);
		boolean isFound = false;
		if ( !refNeutral ) {
			for ( int i = 0; i < listFormula.size(); i++ ) {
				if ( formula.equals(listFormula.get(i)) ) {
					isFound = true;
					break;
				}
			}
		}
		if ( !isFound ) {
			listFormula.add(formula);
		}
	}

	//--------------------------------------------------------
	// Advanced用パラメータをセット
	//--------------------------------------------------------
	String advParam = "";
	if ( refNeutral ) {
		advParam = "&nloss=";
	}
	else {
		advParam = "&product=";
	}
	ArrayList<String> listSwapFormula = new ArrayList();
	for ( int i = 0; i < listFormula.size(); i++ ) {
		listSwapFormula.add( swapFormula(listFormula.get(i)) );
	}
	advParam += StringUtils.join(listSwapFormula.toArray(), ",") + "&mode=" + mode;


	out.println( "<b>Query</b><br>");
	out.println( "<table border=\"0\" cellpadding=\"0\" cellspacing=\"3\" style=\"margin:8px\">" );
	out.println( " <tr>" );
	for ( int i = 0; i < listFormula.size(); i++ ) {
		out.println( "  <td align=\"center\"><b class=\"" + style + "\">" + str + String.valueOf(i+1)
					 + "</b><br>" + listFormula.get(i) + "</td>" );
		if ( i < listFormula.size() - 1 ) {
			out.println( "  <td width=\"40\" align=\"center\"><br>" + logic + "</td>" );
		}
	}
	out.println( " </tr>" );
	out.println( "</table>" );
	out.println( "<br><a href=\"../PeakSearch2.html\" onClick=\"return prevPeakSearchAdv()\">Edit / Resubmit Query</a>" );
	out.println( "<hr size=\"1\">" );

	//--------------------------------------------------------
	// パラメータセット
	//--------------------------------------------------------
	String typeName = "";
	ResultList list = null;
	typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_PEAKADV];
	String paramFormula = "";
	for ( int i = 0; i < listFormula.size(); i++ ) {
		paramFormula += "&formula" + (i+1) + "=" + swapFormula(listFormula.get(i));
	}
	searchParam = "type=" + typeName + "&stype=" + stype + "&" + paramFormula + "&" + searchParam;

	//--------------------------------------------------------
	// 検索実行
	//--------------------------------------------------------
	MassBankCommon mbcommon = new MassBankCommon();
	list = mbcommon.execDispatcherResult( serverUrl, null, searchParam, true, null, conf );
	
	out.println( "<span id=\"menu\"></span>");
	out.println( "<form method=\"post\" action=\"Display.jsp\" name=\"resultForm\" target=\"_blank\">" );
	
	DecimalFormat numFormat = new DecimalFormat("###,###,###");
	int pageNo = Integer.parseInt((String)reqParams.get( "pageNo" ));
	int[] dispIndex = list.getDispRecordIndex(pageNo);
	int startIndex = dispIndex[0];
	int endIndex = dispIndex[1];
	int totalPage = list.getTotalPageNum();
	reqParams.put( "totalPageNo", String.valueOf(totalPage) );
	
	
	//--------------------------------------------------------
	// リクエストパラメータを隠しフィールドに保持
	//--------------------------------------------------------
	for ( Enumeration keys = reqParams.keys(); keys.hasMoreElements(); ){
		String key = (String)keys.nextElement();
		String val = (String)reqParams.get(key);
		out.println( "<input type=\"hidden\" name=\"" + key + "\" value=\"" + val + "\">" );
	}
	
	
	//--------------------------------------------------------
	// 検索結果を表示
	//--------------------------------------------------------
	out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
	out.println( " <tr>");
	out.println( "  <td>" );
	if ( list.getResultNum() > 0 ) {
		out.println( "   <b>Results : <font color=\"green\">" + numFormat.format(list.getResultNum()) 
			+ " Hit.</font>&nbsp;&nbsp;<font size=\"2\" color=\"green\">( " + numFormat.format(startIndex+1) 
			+ " - " + numFormat.format(endIndex+1) + " Displayed )</font></b>" );
//		out.println( "&nbsp;&nbsp;<font size=\"2\" color=\"tomato\"><b>" + list.getCompoundNum() + "&nbsp;Compounds</b></font>" );
	}
	else {
		out.println( "   <b>Results : <font color=\"green\">0 Hit.</font></b>" );
	}
	out.println( "  </td>" );
	if ( list.getResultNum() > 0 ) {
		out.println( "  <td align=\"right\">" );
		out.println( "   <input style=\"width:120\" type=\"button\" name=\"treeCtrl\" value=\"Open All Tree\" onClick=\"allTreeCtrl()\">" );
		out.println( "   <input style=\"width:120\" type=\"submit\" name=\"multi\" value=\"Multiple Display\" onClick=\"return submitShowSpectra();\">" );
//		out.println( "   <input style=\"width:120\" type=\"button\" name=\"search\" value=\"Spectrum Search\" onClick=\"submitSearchPage();\">" );
		out.println( "  </td>" );
	}
	out.println( " </tr>" );
	out.println( "</table>" );
	
	if ( list.getResultNum() > 0 ) {
		int[] pageIndex = list.getDispPageIndex(totalPage, pageNo);

		//-------------------------------------
		// ページリンク（上部）タグ出力
		//-------------------------------------
		out.print( getPageLinkHtml(true, pageNo, pageIndex, totalPage) );

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
		out.println( " <tr height=\"30\" bgcolor=\"silver\">" );
		out.print( "  <th width=\"" + width[0] + "\" class=\"listLayout1 cursorDefault\">" );
		out.println( "<input type=\"checkbox\" name=\"chkAll\" onClick=\"checkAll();\"></th>" );
		out.print( "  <th colspan=\"2\" width=\"" + (Integer.parseInt(width[1]) + Integer.parseInt(width[2]) - 22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_NAME + "');\">Name</th>" );
		out.print( "  <th width=\"22\" class=\"listLayout3\" onclick=\"recSort('" + sortKey + "', '" 
			+ ResultList.SORT_KEY_NAME + "');\">" );
		out.println( "<img src=\"" + nameSortImg + "\" alt=\"Name Sort\"></th>" );
		out.println( "  <th width=\"" + (Integer.parseInt(width[3]) + Integer.parseInt(width[4])-22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_FORMULA + "');\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Formula / Structure</th>" );
		out.print( "  <th width=\"22\" class=\"listLayout3\" onClick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_FORMULA + "');\">" );
		out.println( "<img src=\"" + formulaSortImg + "\" alt=\"Formula Sort\"></th>" );
		out.println( "  <th width=\"" + (Integer.parseInt(width[5]) - 22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_EMASS + "');\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ExactMass</th>" );
		out.print( "  <th width=\"22\" class=\"listLayout3\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_EMASS + "');\">" );
		out.println( "<img src=\"" + emassSortImg + "\" alt=\"ExactMass Sort\"></th>" );
		out.println( "  <th width=\"" + (Integer.parseInt(width[6]) - 22) 
			+ "\" class=\"listLayout2\" onclick=\"recSort('" + sortKey + "', '" + ResultList.SORT_KEY_ID + "');\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ID</th>" );
		out.print( "  <th width=\"22\" class=\"listLayout3\" onclick=\"recSort('" + sortKey + "', '" 
			+ ResultList.SORT_KEY_ID + "');\">" );
		out.println( "<img src=\"" + idSortImg + "\" alt=\"ID Sort\"></th>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		
		
		//-------------------------------------
		// 結果表示
		//-------------------------------------
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

			String id = rec.getId();
			String[] ids = id.split(",");
			id = ids[0];
//			String kid = "";
//			if ( ids.length > 1 ) {
//				kid =  "(" + ids[1] + ")";
//			}

			String recname = rec.getName();
			String formula = rec.getFormula();
			String site = rec.getContributor();
			String mass = rec.getDispEmass();

			// レコードページ表示用URLセット
			String url = "";
			typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
			url = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName  + "&id=" + id + "&site=" + site + advParam;

			//------------------------------------
			// ツリータグ出力
			//------------------------------------
			if ( parentTreeFlag ) {
				if ( i > startIndex ) {
					out.println( "</table>" );
				}
			}
			
			if ( parentTreeFlag ) {
				//■ ツリー(親)
				childNum = String.valueOf(nodeMap.get(rec.getNodeGroup()));
				pRowId = "of" + String.valueOf(tParentId);
				out.println( "<table width=\"" + tableWidth 
					+ "\" class=\"pTreeLayout\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
				out.println( "  <tr valign=\"top\" id=\"" + pRowId + "\" style=\"\" onmouseover=\"overBgColor(this, '#E6E6FA', '" + pRowId + "');\" onmouseout=\"outBgColor(this, '#FFFFFF', '" + pRowId + "');\">" ) ;
				out.print( "  <td class=\"treeLayout1\" width=\"" + width[0] + "\" align=\"center\">" );

					//** チェックボックス
				out.print( "<input type=\"checkbox\" name=\"pid\" id=\"" + pRowId + "check\" value=\"\" onClick=\"checkParent('" + pRowId + "', '" + parentNum + "', '" + childNum + "');\">" );
				out.println( "</td>" );
					//
				out.print(  "  <td class=\"treeLayout2\" width=\"" + (Integer.parseInt(width[1]) - 8) + "\">");
				out.print(   "&nbsp;&nbsp;<img class=\"cursorLink\" src=\"" + pluspng + "\" onclick=\"treeMenu("
					+ tParentId + ")\" name=\"" + tParentImgName + "\" alt=\"\"><br>&nbsp;&nbsp;&nbsp;<img src=\"../image/treeline0.gif\" align=\"middle\" name=\"" + tParentImgName2 + "\">" );
				out.println( "</td>" );

				//** 化合物名
				out.print(   "  <td class=\"treeLayout1\" width=\"" + (Integer.parseInt(width[2]) + 8) + "\">");
				out.print(   "<a href=\"javascript:treeMenu(" + tParentId + ")\" class=\"noLinkImg\" title=\"" + recname + "\">&nbsp;" + rec.getParentLink() + " " + "</a>" );

				// 個々のスペクトル数を表示
				String dispNum = childNum;
				if ( Integer.parseInt(childNum) == 1 ) {
				 	dispNum += " spectrum";
				}
				else {
				 	dispNum += " spectra&nbsp;&nbsp;&nbsp;";
				}
				out.println( "<div align=\"right\" style=\"font-size: 12px;\">" + dispNum + "&nbsp;&nbsp;</div>" );
				out.println( "</td>" );

				//** Formula, Structure
				out.println( "  <td class=\"treeLayout2\" width=\"" + width[3] + "\" valign=\"top\">&nbsp;<b>" + rec.getFormula() + "</b>&nbsp;</td>" );
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[4] + "\" valign=\"top\" align=\"left\">" );

				// アップレットで化学構造式を表示
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

				//** Exact Mass
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[5] + "\">&nbsp;<b>" + mass + "</b>&nbsp;</td>" );
				out.println( "  <td class=\"treeLayout2\" width=\"" + width[6] + "\">&nbsp;</td>" );
				out.println( " </tr>" );
				out.println( "</table>" );
				out.println( "<table width=\"" + tableWidth + "\" id=\"" + tChildId + "\" style=\"display:none\""
					+ " class=\"cTreeLayout\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
			}
			
			//■ ツリー(子)
			String cCheckValue = rec.getInfo() + "\t" + rec.getId() + "\t" + rec.getFormula()
							+ "\t" + rec.getDispEmass().replaceAll("&nbsp;", "") + "\t" + site;
			cRowId = String.valueOf(tChildIndex) + "of" + String.valueOf(tParentId);
			out.println( " <tr valign=\"top\" id=\"" + cRowId + "\" onmouseover=\"overBgColor(this, '#E6E6FA', '"
						+ cRowId + "');\" onmouseout=\"outBgColor(this, '#FFFFFF', '" + cRowId + "');\">" );
				//** チェックボックス
			out.print(   "  <td class=\"treeLayout1\" width=\"" + width[0] + "\" align=\"center\">" );
			out.print(   "<input type=\"checkbox\" name=\"id\" value=\"" + cCheckValue + "\" id=\"" 
				+ cRowId + "check\" onClick=\"checkChild('" + pRowId + "', '" + cRowId + "', '" + parentNum + "', '" + childNum + "');\">" );
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

				//** レコードへのリンク
			out.print(   "  <td class=\"treeLayout1\" width=\"" + width[2] + "\">" );
			out.print(   "<a href=\"" + url + "\" target=\"_blank\">&nbsp;" + rec.getChildLink() + "</a>" );
			out.println( "</td>" );

				//** Formula
			out.println( "  <td class=\"treeLayout2\" width=\"" + width[3] + "\">&nbsp;</td>" );
			out.println( "  <td class=\"treeLayout1\" width=\"" + width[4] + "\">&nbsp;</td>" );

				//** Exact Mass
			out.println( "  <td class=\"treeLayout1\" width=\"" + width[5] + "\">&nbsp;</td>" );
				//** ID
			out.println( "  <td class=\"treeLayout2\" width=\"" + width[6] + "\">&nbsp;&nbsp;&nbsp;&nbsp;" + id + "&nbsp;</td>" );
			out.println( " </tr>" );
			
			//■ 最終行
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
		out.println( "</table>" );
		out.println( "<a name=\"resultsEnd\"></a>" );
		
		
		//-------------------------------------
		// ページリンク（下部）タグ出力
		//-------------------------------------
		out.print( getPageLinkHtml(false, pageNo, pageIndex, totalPage) );
	}
%>
</form>
<hr size="1">
<iframe src="../copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
</body>
</html>
