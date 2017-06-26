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
 * QPeakResult表示用モジュール
 *
 * ver 1.0.24 2011.06.16
 *
 ******************************************************************************/
%>

<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="org.apache.commons.lang3.math.NumberUtils" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ include file="./Common.jsp"%>
<%!
	// 画面内テーブルタグ幅
	private static final String tableWidth = "950";
	
	// 検索結果テーブル列幅
	private static final String[] width = { "28", "562", "142", "102", "40", "70" };
	
	// イメージファイルURL
	private final String strucOffGif = "../image/strucOff.gif";
	private final String strucOverGif = "../image/strucOver.gif";
	private final String strucOnGif = "../image/strucOn.gif";

	/**
	 * 構造式情報を一括取得する
	 * @param list
	 * @param serverUrl
	 * @param urlList
	 * @param dbNameList
	 * @return List<Map>(Map<String, String>, Map<String, String>, Map<String, String>) 画像とMolfile情報をそれぞれ格納したMapをListに格納
	 */
	private List<Map> getStructure(ArrayList list, String serverUrl, String[] urlList, String[] dbNameList) {
		List<Map> resultList = new ArrayList<Map>(4);
		
		String prevName = "";
		String param = "";
		for ( int i = 0; i < list.size(); i++ ) {
			String rec = (String)list.get(i);
			String[] fields = rec.split(";");
			String name = fields[0];  
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
		ArrayList<String> result = result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );
		
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
	MassBankCommon mbcommon = new MassBankCommon();
	
	//-------------------------------------------
	// パラメータ取得
	//-------------------------------------------
	int siteNo = -1;
	try { siteNo = Integer.parseInt(String.valueOf(request.getParameter( "site" ))); } catch (NumberFormatException nfe) {}
	String pPeak = request.getParameter("qpeak");
	String pNum  = request.getParameter( "num" );
	String pCutoff= request.getParameter("CUTOFF");
	String[] pInstGrp = request.getParameterValues("inst_grp");
	String[] pInstType = request.getParameterValues("inst");
	String[] pMsType = request.getParameterValues("ms");
	String paramCondition = "";
	if ( pInstType != null ) {
		paramCondition = "&INST=";
		for ( int i=0; i<pInstType.length; i++ ) {
			paramCondition += URLEncoder.encode(pInstType[i], "utf-8");
			if ( i < pInstType.length - 1 ) {
				paramCondition += ",";
			}
		}
	}
	if ( pMsType != null ) {
		paramCondition += "&MS=";
		for ( int i=0; i<pMsType.length; i++ ) {
			paramCondition += URLEncoder.encode(pMsType[i], "utf-8");
			if ( i < pMsType.length - 1 ) {
				paramCondition += ",";
			}
		}
	}
	
	String pIonMode = request.getParameter("ion");
	paramCondition += "&ION=" + pIonMode;
	
	if ( pPeak == null || pNum == null || pCutoff == null ) {
		out.println( "<html>" );
		out.println( "<head>" );
		out.println( " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" );
		out.println( " <meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\">" );
		out.println( " <meta http-equiv=\"Content-Style-Type\" content=\"text/css\">" );
		out.println( " <meta http-equiv=\"imagetoolbar\" content=\"no\">" );
		out.println( " <meta name=\"author\" content=\"MassBank\" />" );
		out.println( " <meta name=\"coverage\" content=\"worldwide\" />" );
		out.println( " <meta name=\"Targeted Geographic Area\" content=\"worldwide\" />" );
		out.println( " <meta name=\"rating\" content=\"general\" />" );
		out.println( " <meta name=\"copyright\" content=\"Copyright (c) 2006 MassBank Project\" />" );
		out.println( " <meta name=\"description\" content=\"Mass Spectrum Quick Search Results\">" );
		out.println( " <meta name=\"keywords\" content=\"Results\">" );
		out.println( " <meta name=\"revisit_after\" content=\"10 days\">" );
		out.println( " <link rel=\"stylesheet\" type=\"text/css\" href=\"../css/Common.css\">" );
		out.println( " <script type=\"text/javascript\" src=\"../script/Common.js\"></script>" );
		out.println( " <script type=\"text/javascript\" src=\"../script/QpeakResult.js\"></script>" );
		out.println( " <title>MassBank | Database | Quick Search Results</title>" );
		out.println( "</head>" );
		out.println( "<body class=\"msbkFont cursorDefault\">" );
		out.println( " <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">" );
		out.println( "  <tr>" );
		out.println( "   <td><h1>Quick Search Results</h1></td>" );
		out.println( "   <td align=\"right\" class=\"font12px\">" );
		out.println( "    <img src=\"../img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"text\" href=\"javascript:openMassCalc();\">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "    <img src=\"../img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"text\" href=\"" + MANUAL_URL + RESULT_PAGE + "\" target=\"_blank\">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		out.println( "   </td>" );
		out.println( "  </tr>" );
		out.println( " </table>" );
		out.println( "<iframe src=\"../menu.html\" width=\"860\" height=\"30px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		out.println( "<hr size=\"1\">" );
		out.println( "<table width=\"900\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td height=\"100\" valign=\"top\">" );
		out.println( "   <font color=\"Crimson\" size=\"+1\"><b>Error : No input data.</b></font>" );
		out.println( "  </td>" );
		out.println( "  <td align=\"right\" valign=\"bottom\">" );
		out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting()\">Edit / Resubmit Query</a>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		out.println( "<hr size=\"1\">" );
		out.println( "<form method=\"post\" action=\"Display.jsp\" name=\"resultForm\" target=\"_blank\" class=\"formStyle\">" );
		out.println( "<table width=\"900\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td>" );
		out.println( "   <b>Results : <font color=\"green\">0 Hit.</font></b>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		out.println( "<input type=\"hidden\" name=\"searchType\" value=\"peak\">" );
		out.println( "</form>" );
		out.println( "<hr size=\"1\">" );
		out.println( "<iframe src=\"../copyrightline.html\" width=\"800\" height=\"20px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		out.println( "</body>" );
		out.println( "</html>" );
		return;
	}
	
	//-------------------------------------------
	// Peak Dataを改行,セミコロン区切りで取り出す
	//-------------------------------------------
	String inpLines[] = pPeak.split("\n");
	ArrayList<String> peakList = new ArrayList<String>();
	String line = "";
	for ( int l1 = 0; l1 < inpLines.length; l1++ ) {
		line = inpLines[l1].trim().replaceAll("\\r", "");
		if ( !line.equals("") ) {
			String sublines[] = line.split(";");
			for ( int lp2 = 0 ; lp2 < sublines.length; lp2++ ) {
				peakList.add( sublines[lp2].trim() );
			}
		}
	}
	
	//-------------------------------------------
	// 入力データチェック
	//-------------------------------------------
	StringBuffer paramPeak = new StringBuffer();
	StringBuffer paramMz = new StringBuffer();
	boolean isError = false;
	String errMsg = "";
	Double maxInte = 0.0;
	
	if ( peakList.size() == 0 ) {
		isError = true;
		errMsg = "No input data.";
	}
	else {
		errMsg = "Illegal data format.";
		for ( int lp = 0; lp < peakList.size(); lp++ ) {
			line = (String)peakList.get(lp);
			int posP = line.indexOf(" ");
			if ( posP >= 0 ) {
				String peak = line.substring( 0, posP );
				String inte = line.substring( posP+1, line.length() );
				// m/z,intensity の値が数値以外の場合はエラー
				if ( !NumberUtils.isNumber(peak)
				  || !NumberUtils.isNumber(inte)  ) {
					isError = true;
					break;
				}
				// CGI,Appletのパラメータをセット
				// 強度MAX値
				if ( Double.parseDouble(inte) > maxInte ) {
					maxInte = Double.parseDouble(inte);
				}
			}
			else {
				//m/z, intensityがスペース区切りで入力されていない場合はエラー
				isError = true;
				break;
			}
		}
		if ( paramMz.length() > 0
		  && paramMz.charAt( paramMz.length()-1 ) == ',' ) {
			paramMz.deleteCharAt( paramMz.length()-1 );
		}
	}
	
	// Cutoff Threshold の値が数値以外の場合はエラー
	if ( !isError && !StringUtils.isNumeric(pCutoff) ) {
		isError = true;
		errMsg = "Illegal value of cutoff threshold.";
	}
	else {
		try {
			for ( int lp = 0; lp < peakList.size(); lp++ ) {
				line = (String)peakList.get(lp);
				int posP = line.indexOf(" ");
				String peak = line.substring( 0, posP );
				
				// 相対強度へ変換
				String inte = line.substring( posP+1, line.length() );
				Double dblInte = Double.parseDouble(inte) / maxInte * 999 + 0.5;
				int relInte = dblInte.intValue();
				
				paramPeak.append( peak + "," + String.valueOf(relInte) + "@");
				paramMz.append( peak + "," );
			}
		}
		catch (StringIndexOutOfBoundsException oe) {
			isError = true;
			errMsg = "Illegal data format.";
		}
		catch (NumberFormatException nfe) {
			isError = true;
			errMsg = "Illegal value of intensity.";
		}
	}
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="imagetoolbar" content="no">
<meta name="author" content="MassBank" />
<meta name="coverage" content="worldwide" />
<meta name="Targeted Geographic Area" content="worldwide" />
<meta name="rating" content="general" />
<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
<meta name="description" content="Mass Spectrum Quick Search Results">
<meta name="keywords" content="Results">
<meta name="revisit_after" content="10 days">
<link rel="stylesheet" type="text/css" href="../css/Common.css">
<link rel="stylesheet" type="text/css" href="../css/QpeakResult.css">
<link rel="stylesheet" type="text/css" href="../css/QpeakResultMenu.css">
<script type="text/javascript" src="../script/Common.js"></script>
<script type="text/javascript" src="../script/QpeakResult.js"></script>
<script type="text/javascript" src="../script/QpeakResultMenu.js"></script>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.8.3.min.js" ></script>
<script type="text/javascript" src="../script/StructurePreview.js"></script>
<!-- SpeckTackle dependencies-->
<script type="text/javascript" src="http://d3js.org/d3.v3.min.js"></script>
<!-- SpeckTackle library-->
<script type="text/javascript" src="../script/st.min.js" charset="utf-8"></script>
<!-- SpeckTackle style sheet-->
<link rel="stylesheet" href="../css/st.css" type="text/css" />	
<!-- SpeckTackle MassBank loading script-->
<script type="text/javascript" src="../script/massbank_specktackle.js"></script>
<title>MassBank | Database | Quick Search Results</title>
</head>
<body class="msbkFont cursorDefault">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td><h1>Quick Search Results</h1></td>
			<td align="right" class="font12px">
				<img src="../img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="../img/bullet_link.gif" width="10" height="10">&nbsp;<b><a class="text" href="<%=MANUAL_URL%><%=RESULT_PAGE%>" target="_blank">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
	</table>
<iframe src="../menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<hr size="1">
<%/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/%>
<jsp:include page="../pserver/ServerInfo.jsp" />
<%
	if ( isError ) {
		//-------------------------------------------
		// エラー表示
		//-------------------------------------------
		out.println( "<table width=\"900\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td height=\"100\" valign=\"top\">" );
		out.println( "   <font color=\"Crimson\" size=\"+1\"><b>Error : " + errMsg + "</b></font>" );
		out.println( "  </td>" );
		out.println( "  <td align=\"right\" valign=\"bottom\">" );
		out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting()\">Edit / Resubmit Query</a>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		out.println( "<hr size=\"1\">" );
		out.println( "<form method=\"post\" action=\"Display.jsp\" name=\"resultForm\" target=\"_blank\" class=\"formStyle\">" );
		out.println( "<table width=\"900\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>" );
		out.println( "  <td>" );
		out.println( "   <b>Results : <font color=\"green\">0 Hit.</font></b>" );
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( "</table>" );
		out.println( "<input type=\"hidden\" name=\"searchType\" value=\"peak\">" );
		out.println( "</form>" );
	}
	else {
		//-------------------------------------------
		// 設定ファイル内容を取得
		//-------------------------------------------
		GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		String serverUrl = conf.getServerUrl();
		String[] dbNameList = conf.getDbName();
		String[] urlList = conf.getSiteUrl();
		
		
		//-------------------------------------
		// 検索パラメータ（クエリスペクトル）表示
		//-------------------------------------
		out.println( "<a name=\"resultsTop\"></a>" );
		out.println( "<b>Query :</b><br>" );
		out.println( "<table border=\"0\" cellpadding=\"1\" cellspacing=\"5\">" );
		out.println( " <tr>" );
		out.println( "  <td>" );
		// out.println( "   <applet code=\"Display.class\" archive=\"../applet/Display2.jar\""
		//	+ " width=\"" + tableWidth + "\" height=\"150\">" );
		// out.println( "    <param name=\"qpeak\" value=\"" + paramPeak.toString() + "\">" );
		// out.println( "   </applet>" );
		out.println("<div id=\"spectrum_canvas\" peaks=\"" + paramPeak.toString() + "\" style=\"height: 200px; width: 750px; background-color: white\"></div>");
		out.println( "  </td>" );
		out.println( " </tr>" );
		out.println( " <tr>" );
		out.println( "  <td align=\"right\">" );
		out.println( "   <a href=\"\" class=\"pageLink\" onClick=\"return parameterResetting()\">Edit / Resubmit Query</a>" );
		out.println( "  </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<hr size=\"1\">" );
		
		
		//-------------------------------------
		// 検索実行・結果取得
		//-------------------------------------
		String typeName = "";
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
		String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
				 + "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
				 + "&CUTOFF=" + pCutoff + "&NUM=0&VAL=" + paramPeak.toString();
		param += paramCondition;
		ArrayList<String> result = null;
		if ( siteNo == -1 ) {
			result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );
		}
		else {
			result = mbcommon.execDispatcher( serverUrl, typeName, param, false, String.valueOf(siteNo) );
		}
		
		out.println( "<form method=\"post\" action=\"Display.jsp\" name=\"resultForm\" target=\"_blank\" class=\"formStyle\">" );
		
		//-------------------------------------
		// ヒット数表示
		//-------------------------------------
		int hitCnt = result.size();
		int iNum = Integer.parseInt(pNum);
		if ( iNum != 0 && hitCnt > iNum ) {
			hitCnt = iNum;
		}
		out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\">" );
		out.println( " <tr>");
		out.println( "  <td>" );
		out.println( "   <b>Results : <font color=\"green\">" + String.valueOf(hitCnt) + " Hit. </font></b>" );
		out.println( "  </td>" );
		if ( hitCnt > 0 ) {
			out.println( "  <td align=\"right\">" );
			out.println( "   <input style=\"width:120\" type=\"submit\" name=\"multi\" value=\"Multiple Display\" onClick=\"return submitFormCheck();\">" );
			out.println( "   <input style=\"width:120\" type=\"button\" name=\"search\" value=\"Spectrum Search\" onClick=\"submitSearchPage();\">" );
			out.println( "  </td>" );
		}
		out.println( " </tr>" );
		out.println( "</table>" );
		
		if ( hitCnt > 0 ) {
			//-------------------------------------
			// テーブルヘッダー表示
			//-------------------------------------
			out.println( "<table width=\"" + tableWidth + "\" height=\"26\" cellpadding=\"0\" cellspacing=\"0\" class=\"moveDispLinkTop\">" );
			out.println( " <tr>" );
			out.println( "  <td class=\"font12px\" align=\"right\" valign=\"bottom\">" );
			out.println( "   <a class=\"moveDispLink\" href=\"#resultsEnd\">&nbsp;&nbsp;<span class=\"font10px2\">&#9660;&nbsp;</span>Results End</a>" );
			out.println( "  </td>" );
			out.println( " </tr>" );
			out.println( "</table>" );
			out.println( "<table width=\"" + tableWidth + "\" class=\"listLayout\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
			out.println( " <tr bgcolor=\"silver\">" );
			out.print( "  <th height=\"30\" width=\"" + width[0] + "\" class=\"listLayout1\">" );
			out.println( "<input type=\"checkbox\" name=\"chkAll\" onClick=\"checkAll();\"></th>" );
			out.println( "  <th height=\"30\" width=\"" + width[1] + "\" class=\"listLayout3\">Name</th>" );
			out.println( "  <th height=\"30\" width=\"" + (Integer.parseInt(width[2]) + Integer.parseInt(width[3])) + "\" class=\"listLayout3\">Formula / Structure</th>" );
			out.println( "  <th height=\"30\" width=\"" + width[4] + "\" class=\"listLayout3\">Hit</th>" );
			out.println( "  <th height=\"30\" width=\"" + width[5] + "\" class=\"listLayout3\">Score</th>" );
			out.println( " </tr>" );
			out.println( "</table>" );
			
			// 化学構造式表示情報を一括取得する
			List<Map> structureResult = getStructure(result, serverUrl, urlList, dbNameList);
			Map<String, String> mapGifUrl = structureResult.get(0);
			Map<String, String> mapGifSmallUrl = structureResult.get(1);
			Map<String, String> mapGifLargeUrl = structureResult.get(2);
			Map<String, String> mapMolData = structureResult.get(3);
			
			//-------------------------------------
			// 結果表示
			//-------------------------------------
			out.println( "<table width=\"" + tableWidth + "\" class=\"treeLayout\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
			for ( int i = 0; i < hitCnt; i++ ) {
				// データ切り出し
				String rec = (String)result.get(i);
				String[] fields = rec.split("\t");
				String name    = fields[0];  
				String id      = fields[1];
				String ion     = fields[2];
				String formula = fields[3];
				String hitScore= fields[4];
				String site    = fields[5];
				
				// ヒットピーク数、スコア
				String score = "";
				String hit = "";
				int pos1 = hitScore.indexOf(".");
				if ( pos1 > 0 ) { 
					score = "0" + hitScore.substring(pos1);
					hit = hitScore.substring( 0, pos1 );
				}
				else {
					score = "0";
					hit = hitScore;
				}
				// スコアを小数第5位で四捨五入
				if ( score.length() > 7 ) {
					BigDecimal bdScore = new BigDecimal( score );
					score = (bdScore.setScale(4, BigDecimal.ROUND_HALF_UP)).toString();
					if ( Float.parseFloat(score) >= 1f ) {
						score = "0.9999";
					}
					else if ( Float.parseFloat(score) < 0.0001f ) {
						score = "0.0001";
					}
				}
				
				// レコードページへのリンクURLをセット
				typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
				String linkUrl = MassBankCommon.DISPATCHER_NAME + "?type=" + typeName
								 + "&id=" + id + "&site=" + site + "&qmz=" + paramMz.toString() + "&CUTOFF=" + pCutoff + "&dsn=" + conf.getDbName()[Integer.parseInt(site)];
				String valstr = name.replace("\"", "&quot;") + "\t" + id + "\t" + formula + "\t0\t" + site;
				
				String rowId = String.valueOf(i);
				out.println( " <tr id=\"" + rowId + "\"onmouseover=\"overBgColor(this, '#E6E6FA', '" + rowId + "');\" onmouseout=\"outBgColor(this, '#FFFFFF', '" + rowId + "');\">" );
				
				//** チェックボックス
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[0] + "\" align=\"center\">" );
				out.println( "   <input type=\"checkbox\" name=\"id\" value=\""
					+ valstr + "\" " + "id=\"" + rowId + "check\" onClick=\"checkNode('" + rowId + "', '" + hitCnt + "');\"></td>" );
				
				//** レコード名
				out.print( "  <td class=\"treeLayout1\" width=\"" + width[1] + "\">" );
				out.print( "&nbsp;&nbsp;<a href=\"" + linkUrl + "\" target=\"_blank\" title=\"ID:" + id + "\">" + name + "</a>&nbsp;&nbsp;" );
				out.println( "</td>" );
				
				//** Formula / Structure
				out.println( "  <td class=\"treeLayout2\" width=\"" + width[2] + "\">&nbsp;&nbsp;" + formula + "&nbsp;</td>" );
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[3] + "\" valign=\"top\" align=\"left\">" );
				
				// アップレットで化学構造式を表示
				String[] cutName = name.split(";");
				String key = cutName[0].toLowerCase();
				StringBuilder previewName = new StringBuilder(cutName[0]);
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
						// out.println( "   <applet name=\"jme_query\" code=\"JME.class\" archive=\"../applet/JME.jar\" width=\"80\" height=\"80\">");
						// out.println( "    <param name=\"options\" value=\"depict\">" );
						// out.println( "    <param name=\"mol\" value=\"");
						// out.print( moldata );
						// out.println( "\">");
						// out.println( "   </applet>\n");
						out.println("<div class=\"molecule§viewer\" id=\"molecule§viewer§" + id + "§" + conf.getDbName()[Integer.parseInt(site)] +  "\" style=\"height: 80px; width = 80px; background-color: white\"></div>");
					}
				}
				else {
					out.println( "  <a href=\"../image/not_available.gif\" class=\"preview_structure\" title=\"" + previewName.toString() + "\" onClick=\"return false\">" );
					out.println( "   <img src=\"../image/not_available_s.gif\" width=\"80\" height=\"80\" onClick=\"expandMolView('../image/not_available_l.gif')\" style=\"margin:0px; cursor:pointer\">");
					out.println( "  </a>" );
				}
				out.println( "</td>" );
				
				//** ヒットピーク数
				out.println( "  <td class=\"treeLayout1\" width=\"" + width[4] + "\">&nbsp;&nbsp;" + hit + "&nbsp;</td>" );
				
				//** スコア
				out.println( "  <td class=\"treeLayout2\" width=\"" + width[5] + "\">&nbsp;&nbsp;" + score + "&nbsp;</td>" );
				out.println( " </tr>" );
			}
			out.println( "</table>" );
			out.println( "<table width=\"" + tableWidth + "\" class=\"treeLayoutEnd\" cellpadding=\"0\" cellspacing=\"0\" onContextMenu=\"return dispRightMenu(event, true)\">" );
			out.println( " <tr>" );
			out.println( "  <td width=\"" + width[0] + "\">&nbsp;</td>" );
			out.println( "  <td width=\"" + width[1] + "\">&nbsp;</td>" );
			out.println( "  <td width=\"" + (Integer.parseInt(width[2]) + Integer.parseInt(width[3])) + "\">&nbsp;</td>" );
			out.println( "  <td width=\"" + width[4] + "\">&nbsp;</td>" );
			out.println( "  <td width=\"" + width[5] + "\">&nbsp;</td>" );
			out.println( " </tr>" );
			out.println( "</table>" );
			out.println( "<a name=\"resultsEnd\"></a>" );
			out.println( "<table width=\"" + tableWidth + "\" cellpadding=\"0\" cellspacing=\"0\" class=\"moveDispLinkBottom\">" );
			out.println( " <tr>" );
			out.println( "  <td class=\"font12px\" align=\"right\" valign=\"top\">" );
			out.println( "   <a class=\"moveDispLink\" href=\"#resultsTop\">&nbsp;&nbsp;<span class=\"font10px2\">&#9650;&nbsp;</span>Results Top</a>" );
			out.println( "  </td>" );
			out.println( " </tr>" );
			out.println( "</table>" );
		}
	}
	
	
	//-------------------------------------
	// リクエストパラメータをhiddenで持つ
	//-------------------------------------
	out.println( "<input type=\"hidden\" name=\"searchType\" value=\"peak\">" );
	out.println( "<input type=\"hidden\" name=\"qpeak\" value=\"" + pPeak + "\">" );
	out.println( "<input type=\"hidden\" name=\"num\" value=\"" + pNum + "\">" );
	out.println( "<input type=\"hidden\" name=\"CUTOFF\" value=\"" + pCutoff + "\">" );
	if ( pInstGrp != null ) {
		for ( int i = 0; i < pInstGrp.length; i++ ) {
			out.println( "<input type=\"hidden\" name=\"inst_grp\" value=\"" + pInstGrp[i] + "\">" );
		}
	}
	if ( pInstType != null ) {
		for ( int i = 0; i < pInstType.length; i++ ) {
			out.println( "<input type=\"hidden\" name=\"inst\" value=\"" + pInstType[i] + "\">" );
		}
	}
	if ( pMsType != null ) {
		for ( int i = 0; i < pMsType.length; i++ ) {
			out.println( "<input type=\"hidden\" name=\"ms\" value=\"" + pMsType[i] + "\">" );
		}
	}
	if ( pIonMode != null ) {
		out.println( "<input type=\"hidden\" name=\"ion\" value=\"" + pIonMode + "\">" );
	}
%>
</form>
<hr size="1">
<iframe src="../copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<span id="menu"></span>
</body>
</html>
