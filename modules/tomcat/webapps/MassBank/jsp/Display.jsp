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
 * スペクトル複数表示用モジュール
 *
 * ver 1.0.15 2011.09.21
 *
 ******************************************************************************/
%>

<%@ page import="java.util.*,java.io.*" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ include file="./Common.jsp"%>
<%
	//*************************************************************************
	// パラメータ
	//-----------------------------------------------------------------------
	// ● PeakSearch.jsp, QuickSearch.jspから呼ばれる場合
	// (1) multi : "Show Spectra"(空以外の文字列） name : なし
	//       ==> DisplayAllを表示するHTMLを返す
	//-----------------------------------------------------------------------
	// ● SearchApplet.jar, Browse2.jarから呼ばれる場合
	// (3) multi : なし                            name : なし
	//       ==> DisplayAllを表示するHTMLファイルを作成し、そのファイル名を返す
	// ----------------------------------------------------------------------
	// (4) multi : "Multiple Display"(なしでもOK)      name : あり
	//       ==> 指定されたHTMLファイル内容を返す
	//*************************************************************************
	
	//-------------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------------
	String type = "";
	String filename = "";
	String multi = "";
	Hashtable params = new Hashtable();
	Enumeration names = request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		String val = request.getParameter( key );
		if ( key.equals("type") ) {
			type = val;
		}
		else if ( key.equals("multi") ) {
			multi = val;
		}
		else if ( key.equals("name") ) {
			filename = val;
		}
		else {
			params.put( key, val );
		}
	}
	
	String paramName = "";
	PrintWriter pw = null;
	File temp = null;
	
	if ( filename.equals("") ) { 
		String[] ids = request.getParameterValues("id");
		int num = ids.length;
		ArrayList html = new ArrayList();
		html.add( "<html>" );
		html.add( "\t<head>" );
		html.add( "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" );
		html.add( "\t\t<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">" );
		html.add( "\t\t<meta http-equiv=\"imagetoolbar\" content=\"no\">" );
		html.add( "\t\t<meta name=\"author\" content=\"MassBank\" />" );
		html.add( "\t\t<meta name=\"coverage\" content=\"worldwide\" />" );
		html.add( "\t\t<meta name=\"Targeted Geographic Area\" content=\"worldwide\" />" );
		html.add( "\t\t<meta name=\"rating\" content=\"general\" />" );
		html.add( "\t\t<meta name=\"copyright\" content=\"Copyright (c) 2006 MassBank Project\" />" );
		html.add( "\t\t<meta name=\"description\" content=\"Multiple Display\">" );
		html.add( "\t\t<meta name=\"keywords\" content=\"Multiple,Display\">" );
		html.add( "\t\t<meta name=\"revisit_after\" content=\"30 days\">" );
		html.add( "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/Common.css\">" );
		html.add( "\t\t<script type=\"text/javascript\" src=\"../script/Common.js\"></script>" );
		html.add( "\t\t<title>MassBank | Database | Multiple Display</title>" );
		html.add( "\t</head>" );
		html.add( "\t<body class=\"msbkFont\">" );
		html.add( "\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">" );
		html.add( "\t\t\t<tr>" );
		html.add( "\t\t\t\t<td>" );
		html.add( "\t\t\t\t\t<h1>Multiple Display</h1>" );
		html.add( "\t\t\t\t</td>" );
		html.add( "\t\t\t\t<td align=\"right\" class=\"font12px\">" );
		html.add( "\t\t\t\t\t<img src=\"../img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"text\" href=\"javascript:openMassCalc();\">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		html.add( "\t\t\t\t\t<img src=\"../img/bullet_link.gif\" width=\"10\" height=\"10\">&nbsp;<b><a class=\"text\" href=\"" + MANUAL_URL + MULTI_PAGE + "\" target=\"_blank\">user manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
		html.add( "\t\t\t\t</td>" );
		html.add( "\t\t\t</tr>" );
		html.add( "\t\t</table>" );
		html.add( "\t\t<iframe src=\"../menu.html\" width=\"860\" height=\"30px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		html.add( "\t\t<hr size=\"1\">" );
		html.add( "\t\t<br>" );
		html.add( "\t\t<applet code=\"DisplayAll.class\" archive=\"../applet/DisplayAll2.jar\""
						+ " width=\"980\" height=\"" + Integer.toString(262*num) + "\">" );
		paramName = "id";
		String pnum = "";
		if ( type.equals("peak") || type.equals("diff") ) {
			if ( request.getParameter( "num" ) != null ) {
				int paramNum = Integer.parseInt( request.getParameter("num") );
				html.add( "\t\t\t<param name=\"type\" value=\"" + type + "\">" );
				String[] pname = { "mz", "op", "tol", "int" };
				String[] pval = new String[pname.length];
				
				for ( int i = 0; i < paramNum; i++ ) {
					pnum = Integer.toString(i);
					String mz = (String)params.get( "mz"  + pnum );
					if ( mz.equals("") ) {
						paramNum = i;
						break;
					}
					for ( int j = 0; j < pname.length; j++ ) {
						pval[j] = (String)params.get( pname[j] + pnum );
						html.add( "\t\t\t<param name=\"" + pname[j]  + pnum + "\" value=\"" + pval[j] + "\">" );
					}
				}
				html.add( "\t\t\t<param name=\"pnum\" value=\"" + String.valueOf(paramNum) + "\">" );
			}
		}
		
		html.add( "\t\t\t<param name=\"num\" value=\"" + Integer.toString(num) + "\">" );
		for ( int i = 0; i < num ; i++ ) {
			String[] fields = ids[i].split("\t");
			String id = fields[1];
			String name = fields[0];
			String formula = fields[2];
			String mass = fields[3];
			String ion = fields[4];
			String site = fields[fields.length-1];
			pnum = Integer.toString(i+1);
			html.add( "\t\t\t<param name=\"" + paramName + pnum + "\" value=\"" + id + "\">" );
			html.add( "\t\t\t<param name=\"name" + pnum + "\" value=\"" + name +"\">" );
			html.add( "\t\t\t<param name=\"site" + pnum + "\" value=\"" + site +"\">" );
			html.add( "\t\t\t<param name=\"formula" + pnum + "\" value=\"" + formula +"\">" );
			html.add( "\t\t\t<param name=\"mass" + pnum + "\" value=\"" + mass +"\">" );
			html.add( "\t\t\t<param name=\"ion" + pnum + "\" value=\"" + ion +"\">" );
		}
		html.add( "\t\t</applet>" );
		html.add( "\t\t<br><br>" );
		
		// 
		String path = request.getRequestURL().toString();
		String baseUrl = path.substring( 0, (path.indexOf("/jsp")+1) );
		GetConfig conf = new GetConfig(baseUrl);
		String serverUrl = conf.getServerUrl();				// サーバURL取得
		String param = "id=";
		String[] idList = new String[ids.length];
		for ( int i = 0; i < ids.length; i++ ) {
			String[] fields = ids[i].split("\t");
			String id = fields[1];
			param += id;
			idList[i] = id;
			if ( i < ids.length - 1 ) {
				param += ",";
			}
		}
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETFORMULA];
		ArrayList result = mbcommon.execMultiDispatcher( serverUrl, typeName, param );
		HashMap<String, ArrayList> annoFormulaList = new HashMap();
		TreeMap<Double, String> formulaList = new TreeMap();
		ArrayList<String[]> list = null;
		String prevId = "";
		for ( int i = 0; i < result.size(); i++ ) {
			String val = (String)result.get(i);
			String[] item = val.split("\t");
			String id      = item[0];
			String mz      = item[1];
			String formula = item[2];
			if ( !id.equals(prevId) ) {
				if ( !prevId.equals("") ) {
					annoFormulaList.put(prevId, list);
				}
				list = new ArrayList<String[]>();
			}
			list.add(new String[]{mz, formula});
			if ( !formulaList.containsKey(Double.valueOf(mz)) ) {
				formulaList.put(Double.valueOf(mz), formula);
			}
			prevId = id;
		}
		annoFormulaList.put(prevId, list);

		// 点取り表
		ArrayList html2 = new ArrayList();
		html2.add("\t\t<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		html2.add("\t\t\t<tr>");
		html2.add("\t\t\t\t<td>");
		html2.add("\t\t\t\t\t<table bgcolor=\"MidnightBlue\" border=\"0\" cellspacing=\"1\" cellpadding=\"0\" style=\"font-size:10pt\">");
		html2.add("\t\t\t\t\t\t<tr>");
		html2.add("\t\t\t\t\t\t\t<th style=\"color:white\">&nbsp;Annotated&nbsp;<br>Peak</th>");
		for ( Double mass : formulaList.keySet() ) {
			String formula = formulaList.get(mass);
			html2.add("\t\t\t\t\t\t\t<th bgcolor=\"silver\">" + formula + "<br>(" + mass + ")</th>");
		}
		html2.add("\t\t\t\t\t\t</tr>");
		int cnt = 0;
		for ( int i = 0; i < idList.length; i++ ) {
			if ( annoFormulaList.containsKey(idList[i]) ) {
				String id = idList[i];
				html2.add("\t\t\t\t\t\t<tr>");
				html2.add("\t\t\t\t\t\t\t<td bgcolor=\"LightCyan\">&nbsp;<b>" + id + "</b></td>");
				list = annoFormulaList.get(id);
				for ( Double mass : formulaList.keySet() ) {
					String formula1 = formulaList.get(mass);
					boolean isFound = false;
					for ( int j = 0; j < list.size(); j++ ) {
						String[] item = list.get(j);
						String formula2 = item[1];
						if ( formula1.equals(formula2) ) {
							isFound = true;
							break;
						}
					}
					if ( isFound ) {
						html2.add("\t\t\t\t\t\t\t<td align=\"center\" bgcolor=\"gold\">*</td>");
					}
					else {
						html2.add("\t\t\t\t\t\t\t<td align=\"center\" bgcolor=\"white\">-</td>");
					}
				}
				html2.add("\t\t\t\t\t\t</tr>");
				cnt++;
			}
		}
		html2.add("\t\t\t\t\t</table>");
		html2.add("\t\t\t\t</td>");
		html2.add("\t\t\t</tr>");
		html2.add("\t\t</table>");
		if ( cnt > 0 ) {
			html.addAll(html2);
		}

		html.add( "\t\t<hr size=\"1\">" );
		html.add( "\t\t<iframe src=\"../copyrightline.html\" width=\"800\" height=\"20px\" frameborder=\"0\" marginwidth=\"0\" scrolling=\"no\"></iframe>" );
		html.add( "\t</body>" );
		html.add( "</html>" );
		if ( multi.equals("") ) {
			//-------------------------------------------
			// テンポラリにHTMLファイルを作成
			//-------------------------------------------
			temp = File.createTempFile( "massbank", ".html" );
			pw = new PrintWriter( new FileWriter(temp) );
			for ( int i = 0; i < html.size() ; i++ ) {
				pw.println( html.get(i) );
			}
			pw.close();
			
			//-------------------------------------------
			// ファイル名をレスポンスで返す
			//-------------------------------------------
			String name = temp.getName().replaceAll( ".html", "" );
			out.println( name );
		}
		else {
			//-------------------------------------------
			// HTMLを返す
			//-------------------------------------------
			for ( int i = 0; i < html.size() ; i++ ) {
				out.println( html.get(i) );
			}
		}
	}
	else {
		//-------------------------------------------
		// テンポラリファイル読込み
		//-------------------------------------------
		String tempDir = System.getProperty("java.io.tmpdir");
		String filePath = tempDir + "/"  + filename + ".html";
		BufferedReader in = new BufferedReader( new FileReader(filePath) );
		String line;
		while ( ( line = in.readLine() ) != null ) {
			out.println( line );
		}
		in.close();
		
		//-------------------------------------------
		// ファイル削除
		//-------------------------------------------
		File f = new File( filePath );
		f.delete();
	}
%>
