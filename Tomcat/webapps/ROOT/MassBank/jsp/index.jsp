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
 * トップページ表示用モジュール
 *
 * ver 2.0.26 2012.12.05
 *
 ******************************************************************************/
%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%!
	/** ニュース表示数 */
	private final int DISP_NEWS_NUM = 5;
	
	/** イベント表示数 */
	private final int DISP_EVENT_NUM = 3;
%>
<%
	//-------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------
	String reqLang = (request.getParameter("lang") != null) ? request.getParameter("lang") : "";
	
	
	//-------------------------------------
	// ブラウザ優先言語取得
	//-------------------------------------
	String browserLang = (request.getHeader("accept-language") != null) ? request.getHeader("accept-language") : "";
	
	
	//-------------------------------------
	// 表示HTML判別フラグ設定
	//-------------------------------------
	boolean isJpTop = true;
	if ( reqLang.equals("") ) {
		// リクエストパラメータがない場合はブラウザ優先言語で判別
		if ( browserLang.startsWith("ja") || browserLang.equals("") ) {
			isJpTop = true;
		}
		else {
			isJpTop = false;
		}
	}
	else if ( reqLang.equals("ja") ) {
		isJpTop = true;
	}
	else {
		isJpTop = false;
	}
	
	
	//-------------------------------------
	// 表示用の外部情報読み込み
	//-------------------------------------
	ArrayList<String> readHtmlList = new ArrayList<String>() {{add("/news.html"); add("/event.html");}};
	ArrayList<String> newsList = new ArrayList<String>(DISP_NEWS_NUM);
	ArrayList<String> eventList = new ArrayList<String>(DISP_EVENT_NUM);
	BufferedReader br = null;
	final String reqUrl = request.getRequestURL().toString();
	String langStr = "ja";
	if ( !isJpTop ) {
		langStr = "en";
	}
	for (String readHtml : readHtmlList) {
		try {
			URL url = new URL( reqUrl.substring(0, reqUrl.indexOf("jsp")) + langStr + readHtml );
			URLConnection con = url.openConnection();
			br = new BufferedReader( new InputStreamReader(con.getInputStream(), "UTF-8") );
			String line;
			String newLine = "";
			boolean readFlag = false;
			int eventDetail = 0;
			while ((line = br.readLine()) != null) {
				//-------------------------------------
				// ニュース読み込み
				//-------------------------------------
				if ( readHtml.equals("/news.html") ) {
					if ( !readFlag && line.indexOf("<h2>") != -1 ) {
						readFlag = true;
					}
					if ( readFlag ) {
						if ( line.indexOf("<em>") != -1 ) {
							// パスを修正後に表示用リストに追加
							newLine = line.replaceAll("\"./", "\"./" + langStr + "/");
							newLine = newLine.replaceAll("\"../", "\"./");
							newLine = newLine.replaceAll("<br />", "&nbsp;&nbsp;<img src=\"./img/new.gif\"><br />");
							newsList.add(newLine);
						}
						if ( newsList.size() == DISP_NEWS_NUM ) {
							break;
						}
					}
				}
				//-------------------------------------
				// イベント読み込み
				//-------------------------------------
				else if ( readHtml.equals("/event.html") ) {
					if ( !readFlag && line.indexOf("<h2>") != -1 ) {
						readFlag = true;
					}
					if ( readFlag ) {
						if ( line.startsWith("<a ") ) {
							newLine = line.replaceAll("\"../", "\"./");
							newLine = newLine.substring(0, newLine.indexOf("</a>")+4);
							eventDetail += 1;
						}
						else if (eventDetail == 1) {
							newLine += line.substring(line.indexOf("&nbsp;:"), line.indexOf("</span>"));
							newLine += "<br />";
							eventList.add(newLine);
							eventDetail = 0;
						}
						if ( eventList.size() == DISP_EVENT_NUM ) {
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if ( br != null ) {
				br.close();
			}
		}
	}
	
	//-------------------------------------
	// HTML出力
	//-------------------------------------
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
	if ( isJpTop ) {
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja" lang="ja">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-Script-Type" content="text/javascript" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<meta name="author" content="MassBank" />
<meta name="coverage" content="japan" />
<meta name="Targeted Geographic Area" content="japan" />
<meta name="classification" content="general,computers,internet,miscellaneous" />
<meta name="rating" content="general" />
<meta name="copyright" content="Copyright &copy; 2006 MassBank Project" />
<meta name="description" content="MassBank は、研究者がマススペクトルを共有することを目的とした、世界で最初の public database です。これらのマススペクトルは、メタボローム解析をはじめとする生物科学研究において、質量分析で検出した化合物の同定や推定に利用することができます。" />
<meta name="keywords" content="MassBank, massbank, マスバンク, データベース, マススペクトル, Quality, spectral, database" />
<meta name="revisit_after" content="10 days">
<meta name="google-site-verification" content="gNuKmu49uDWeEwk8Y545khNlKbOHgijB8hW31fdMoDw" />
<link rel="stylesheet" href="./css/import.css" type="text/css" media="all" />
<script type="text/javascript" src="./script/Common.js"></script>
<title>MassBank | High Quality Mass Spectral Database</title>
</head>

<body id="home">

<!--ここから▼ヘッダー-->
<div id="header"><h1 class="hide_text"><a href="./index.html?lang=ja" name="pagetop" id="pagetop">MassBank | High Quality Mass Spectral Database</a></h1></div>
<!--ここまで▲ヘッダー-->


<!--ここから▼コンテンツ-->
<div id="wrap" class="clr">

<div id="content" class="clr">

<!--ここから▼右カラムメインコンテンツ-->
<div id="main" class="fr clr">

<!--ここから▼JS OFF対応-->
<noscript>
<p id="js_use" class="clr">
<em class="e16">MassBankではJavascriptを使用しています。</em><br />
&nbsp;&nbsp;MassBank では Javascript を使用しています。 Javascript が Off になっていると正常にご利用いただけません。お手数ですが Javascript を使用可能にした後、再度ページを読み込んでください。
</p>
</noscript>
<!--ここまで▲JS OFF対応-->

<!--ここから▼ニュース-->
<h2 id="h_news" class="hide_text">ニュース</h2>
<p id="headline">
<%
	//-------------------------------------
	// ニュース表示
	//-------------------------------------
	for (String outStr : newsList) {
		out.print(outStr + System.getProperty("line.separator"));
	}
%>
</p>
<div id="home_news" class="text_right"><a href="./ja/news.html" class="bullet_link">過去のニュース</a></div>
<!--ここまで▲ニュース-->

<!--ここから▼お知らせ-->
<!--
<p id="home_notice2" class="clr">
<span class="title clr">&lt;&lt;データ公開・マススペクトルデータベース構築マニュアルに関するお知らせ&gt;&gt;</span>
<span class="contents clr">
MassBank システムインストーラ 1.8、レコード編集ツール 2.1、Mass++ 2.1 を使用してマススペクトルデータベースを構築するための、新しい <a href="./ja/manual.html">マニュアル</a> を公開しました。
</span>
</p>
-->
<!--ここまで▲お知らせ-->

<div class="separate"></div>

<!--ここから▼データベースサービス　ショートカットボタン一覧-->
<h2 id="h_home_massdb" class="hide_text">データベースサービス</h2>
<div class="massdb_bg clr">
<ul id="line1" class="hide_text">
<li id="home_btn1"><a href="./SearchPage.html" title="Spectrum Search">Spectrum Search</a></li>
<li id="home_btn2"><a href="./QuickSearch.html" title="Quick Search">Quick Search</a></li>
<li id="home_btn3"><a href="./PeakSearch.html" title="Peak Search">Peak Search</a></li>
<li id="home_btn4"><a href="./StructureSearch.html" title="Substructure Search">Substructure Search</a></li>
<li id="home_btn5"><a href="./MetaboPrediction.html" title="Metabolite Prediction">Metabolite Prediction</a></li>
</ul>
<ul id="line2" class="hide_text">
<li id="home_btn6"><a href="./PackageView.html" title="Spectral Browser">Spectral Browser</a></li>
<li id="home_btn7"><a href="./BatchSearch.html" title="Batch Service">Batch Service</a></li>
<li id="home_btn8"><a href="./BrowsePage.html" title="Browse Page">Browse Page</a></li>
<li id="home_btn9"><a href="./RecordIndex.html" title="Record Index">Record Index</a></li>
</ul>
</div><!--div class="massdb_bg"-->
<p class="p_dbsammary separate">
MassBank は、<a href="http://biosciencedbc.jp/" target="_blank">NBDC-JST</a> によるライフサイエンスデータベース統合化推進プログラムの支援を受けています（2011－2013）。<br />
MassBank は、<a href="http://www.mssj.jp/index-jp.html" target="_blank">日本質量分析学会</a> の公式データベースです。<br />
MassBank をご利用された方は論文 (<a href="http://dx.doi.org/10.1002/jms.1777" target="_blank">DOI</a>) を引用してください。
</p>
<p />
<!--ここまで▲データベースサービス　ショートカットボタン一覧-->

<!--ここから▼イベント-->
<h2 id="h_event" class="hide_text">イベント</h2>
<p id="headline">
<%
	//-------------------------------------
	// イベント表示
	//-------------------------------------
	for (String outStr : eventList) {
		out.print(outStr + System.getProperty("line.separator"));
	}
%>
</p>
<div id="home_event" class="text_right"><a href="./ja/event.html" class="bullet_link">関連イベント一覧</a></div>
<!--ここまで▲イベント-->

</div><!--div id="main" class="fr clr"-->
<!--ここまで▲右カラムメインコンテンツ-->


<!--ここから▼左カラムナビゲーション-->
<div id="navi" class="fl clr">
<ul id="navi_global" class="hide_text clr">
<li id="navi_g01" ><a href="./ja/database.html" title="データベースサービス">データベースサービス</a></li>
<li id="navi_g02" ><a href="./ja/statistics.html" title="公開データ">公開データ</a></li>
<li id="navi_g03" ><a href="./ja/publication.html" title="パブリケーション">パブリケーション</a></li>
<li id="navi_g04" ><a href="./ja/download.html" title="ダウンロード">ダウンロード</a></li>
<li id="navi_g05" ><a href="./ja/manual.html" title="マニュアル">マニュアル</a></li>
<li id="navi_g06" ><a href="./ja/about.html" title="MassBankについて">MassBankについて</a></li>
<li id="navi_g07" ><a href="./ja/contact.html" title="お問い合わせ">お問い合わせ</a></li>
<li id="navi_g08" ><a href="./ja/group.html" title="研究協力グループ">研究協力グループ</a></li>
</ul>
<!--ここから▼左カラム機能ナビ-->
<ul id="navi_function" class="hide_text clr">
<li id="navi_f01" ><a href="./ja/sitemap.html" title="サイトマップ">サイトマップ</a></li>
<li id="navi_f02" ><a href="./ja/regulation.html" title="利用規定">利用規定</a></li>
<li id="navi_f03" ><a href="./ja/copyright.html" title="著作権・免責事項">著作権・免責事項</a></li>
<li id="navi_f04" ><a href="./index.html?lang=en" title="English Site">English Site</a></li>
</ul>
<!--ここまで▲左カラム機能ナビ-->



<!--ここから▼Mass++動画リンク-->
<p id="home_masspp">
<span class="title clr">How to Use Mass++: Identification&nbsp;and MassBank Search</span>
<a href="http://www.youtube.com/embed/XhNdD82Eo78" target="_blank"><img src="./img/how-to-use-mass++.jpg" width="223" height="167" class="img_line"></a><br />
<a href="http://www.first-ms3d.jp/achievement/software" target="_blank">What&#x27;s Mass++ ?</a>
</p>
<!--ここまで▲Mass++動画リンク-->

</div>
<!--ここまで▲左カラムナビゲーション-->

</div><!--div id="content"-->



<!--ここから▼フッター-->
<div id="footer" class="clr">
<iframe src="./copyrightline.html" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe>
<div class="fr above"><a href="./index.html?lang=ja#pagetop" title="最上部へ" class="text_right bullet_up">最上部へ</a></div>
</div>
<!--ここまで▲フッター-->


</div><!--div id="wrap" class="clr"-->
<!--ここまで▲コンテンツ-->

</body>
</html>

<%
	}
	else {
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-Script-Type" content="text/javascript" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<meta name="author" content="MassBank" />
<meta name="coverage" content="worldwide" />
<meta name="Targeted Geographic Area" content="worldwide" />
<meta name="classification" content="general,computers,internet,miscellaneous" />
<meta name="rating" content="general" />
<meta name="copyright" content="Copyright &copy; MassBank Project" />
<meta name="description" content="MassBank is the first public repository of mass spectral data for sharing them among scientific research community. MassBank data are useful for the chemical identification and structure elucidation of chemical comounds detected by mass spectrometry." />
<meta name="keywords" content="MassBank, massbank, Quality, mass, spectral, database" />
<meta name="revisit_after" content="10 days">
<meta name="google-site-verification" content="gNuKmu49uDWeEwk8Y545khNlKbOHgijB8hW31fdMoDw" />
<link rel="stylesheet" href="./css/import.css" type="text/css" media="all" />
<script type="text/javascript" src="./script/Common.js"></script>
<title>MassBank | High Quality Mass Spectral Database</title>
</head>

<body id="home">

<!--st▼header-->
<div id="header"><h1 class="h1_en hide_text"><a href="./index.html?lang=en" name="pagetop" id="pagetop">MassBank | High Quality Mass Spectral Database</a></h1></div>
<!--ed▲header-->


<!--st▼contents-->
<div id="wrap" class="clr">

<div id="content" class="clr">

<!--st▼right column main-->
<div id="main" class="fr clr">

<!--st▼JS OFF support-->
<noscript>
<p id="js_use" class="clr">
<em class="e16">Javascript is used in this site</em><br />
&nbsp;&nbsp;Javascript is used in this site. If Javascript cannot be used, it is not correctly displayed. Please enable the use of Javascript. And reload.
</p>
</noscript>
<!--ed▲JS OFF support-->

<!--st▼news-->
<h2 id="h_news_en" class="hide_text">News</h2>
<p id="headline">
<%
	//-------------------------------------
	// show news
	//-------------------------------------
	for (String outStr : newsList) {
		out.print(outStr + System.getProperty("line.separator"));
	}
%>
</p>
<div id="home_news" class="text_right"><a href="./en/news.html" class="bullet_link">All news</a></div>
<!--ed▲news-->

<!--st▼notice-->
<!--
<p id="home_notice" class="clr">
<span class="title clr">&lt;&lt;News on Record Editor&gt;&gt;</span>
<span class="contents clr">
We have just released a new version of <a href="./en/download.html">Record Editor</a> with enhanced MassBank record editing!
All the reported problems in the previous version are fixed in the new editor.
</span>
</p>
-->
<!--ed▲notice-->

<div class="separate"></div>

<!--st▼mass spectrum database shortcut button list-->
<h2 id="h_home_massdb_en" class="hide_text">Database Service</h2>
<div class="massdb_bg clr">
<ul id="line1" class="hide_text">
<li id="home_btn1"><a href="./SearchPage.html" title="Spectrum Search">Spectrum Search</a></li>
<li id="home_btn2"><a href="./QuickSearch.html" title="Quick Search">Quick Search</a></li>
<li id="home_btn3"><a href="./PeakSearch.html" title="Peak Search">Peak Search</a></li>
<li id="home_btn4"><a href="./StructureSearch.html" title="Substructure Search">Substructure Search</a></li>
<li id="home_btn5"><a href="./MetaboPrediction.html" title="Metabolite Prediction">Metabolite Prediction</a></li>
</ul>
<ul id="line2" class="hide_text">
<li id="home_btn6"><a href="./PackageView.html" title="Spectral Browser">Spectral Browser</a></li>
<li id="home_btn7"><a href="./BatchSearch.html" title="Batch Service">Batch Service</a></li>
<li id="home_btn8"><a href="./BrowsePage.html" title="Browse Page">Browse Page</a></li>
<li id="home_btn9"><a href="./RecordIndex.html" title="Record Index">Record Index</a></li>
</ul>
</div><!--div class="massdb_bg"-->
<p class="p_dbsammary separate">
MassBank is financially suported from <a href="http://biosciencedbc.jp/?lng=en" target="_blank">National Bioscience Database Center, Japan Science and Technology Agency</a> (2011-2013).<br />
<a href="http://www.mssj.jp/index.html" target="_blank">The Mass Spectorometry Society of Japan</a> officially supports MassBank.<br />
Please cite the article (<a href="http://dx.doi.org/10.1002/jms.1777" target="_blank">DOI</a>) when using MassBank.
</p>
<p />
<!--ed▲mass spectrum database shortcut button list-->

<!--st▼event-->
<h2 id="h_event_en" class="hide_text">Event</h2>
<p id="headline">
<%
	//-------------------------------------
	// show event
	//-------------------------------------
	for (String outStr : eventList) {
		out.print(outStr + System.getProperty("line.separator"));
	}
%>
</p>
<div id="home_event" class="text_right"><a href="./en/event.html" class="bullet_link">All related events</a></div>
<!--ed▲event-->

</div><!--div id="main" class="fr clr"-->
<!--ed▲right column main-->


<!--st▼left column navi-->
<div id="navi" class="fl clr">
<ul id="navi_global_en" class="hide_text clr">
<li id="navi_g01_en" ><a href="./en/database.html" title="Database Service">Database Service</a></li>
<li id="navi_g02_en" ><a href="./en/statistics.html" title="Statistics">Statistics </a></li>
<li id="navi_g03_en" ><a href="./en/publication.html" title="Publications">Publications</a></li>
<li id="navi_g04_en" ><a href="./en/download.html" title="Download">Download</a></li>
<li id="navi_g05_en" ><a href="./en/manual.html" title="Manual">Manual</a></li>
<li id="navi_g06_en" ><a href="./en/about.html" title="About MassBank">About MassBank</a></li>
<li id="navi_g07_en" ><a href="./en/contact.html" title="Contact">Contact</a></li>
<li id="navi_g08_en" ><a href="./en/group.html" title="Consortium Members">Consortium Members</a></li>
</ul>
<!--st▼left column function navi-->
<ul id="navi_function_en" class="hide_text clr">
<li id="navi_f01_en" ><a href="./en/sitemap.html" title="Site Map">Site Map</a></li>
<li id="navi_f02_en" ><a href="./en/regulation.html" title="Use Restrictions">Use Restrictions</a></li>
<li id="navi_f03_en" ><a href="./en/copyright.html" title="Copyright">Copyright</a></li>
<li id="navi_f04_en" ><a href="./index.html?lang=ja" title="Japanese Site">Japanese Site</a></li>
</ul>
<!--ed▲left column function navi-->

<!--st▼Mass++ link-->
<p id="home_masspp" class="clr">
<span class="title clr">How to Use Mass++: Identification&nbsp;and MassBank Search</span>
<a href="http://www.youtube.com/embed/XhNdD82Eo78" target="_blank"><img src="./img/how-to-use-mass++.jpg" width="223" height="167" class="img_line"></a><br />
<a href="http://www.first-ms3d.jp/achievement/software" target="_blank">What&#x27;s Mass++ ?</a>
</p>
<!--ed▲Mass++ link-->

</div>
<!--ed▲left column navi-->

</div><!--div id="content"-->



<!--st▼footer-->
<div id="footer" class="clr">
<iframe src="./copyrightline.html" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe>
<div class="fr above"><a href="./index.html?lang=en#pagetop" title="To top" class="text_right bullet_up">To top</a></div>
</div>
<!--ed▲footer-->


</div><!--div id="wrap" class="clr"-->
<!--ed▲contents-->

</body>
</html>

<%
	}
%>

