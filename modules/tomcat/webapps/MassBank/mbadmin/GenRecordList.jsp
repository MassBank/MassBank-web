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
 * 化合物名リスト作成処理
 *
 * ver 1.0.3 2011.08.18
 *
 ******************************************************************************/
%>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.FileOutputStream" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.PrintStream" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFCell" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFCellStyle" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFClientAnchor" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFComment" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFFont" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFPatriarch" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFRichTextString" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFRow" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFSheet" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFWorkbook" %>
<%@ page import="org.apache.poi.hssf.util.HSSFColor" %>
<%@ page import="org.apache.poi.hssf.util.Region" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.MassBankEnv" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<link rel="stylesheet" type="text/css" href="css/admin.css">
<title>Record List Generator</title>
</head>
<body>
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no" title=""></iframe>
<h2>Record List Generator</h2>
<%
	// ベースUrl, JSP名をセット
	String reqUrl = request.getRequestURL().toString();
	String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	String jspName = reqUrl.substring( reqUrl.lastIndexOf("/")+1 );
	
	// 環境設定ファイルからURLリスト、DB名リストを取得
	GetConfig conf = new GetConfig(baseUrl);
	String[] dbNameList = conf.getDbName();
	String serverUrl = conf.getServerUrl();
	
	// リクエストパラメータ取得
	int siteNum = 0;
	if ( request.getParameter("site_num") != null ) {
		siteNum = Integer.parseInt(request.getParameter("site_num"));
	}
	
	String act = "";
	if ( request.getParameter("act") != null ) {
		act = request.getParameter("act");
	}
	
	if ( !act.equals("") ) {
		// CGI経由で化合物名リストを取得
		String strUrl = serverUrl + "jsp/" + MassBankCommon.DISPATCHER_NAME;
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GETLIST];
		String param = "type=" + typeName + "&site=" + String.valueOf(siteNum);
		URL url = new URL( strUrl );
		URLConnection con = url.openConnection();
		con.setDoOutput(true);
		PrintStream psm = new PrintStream( con.getOutputStream() );
		psm.print( param );
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String line = "";
		ArrayList<String> list = new ArrayList<String>();
		while ( ( line = in.readLine() ) != null ) {
			list.add( line );
		}
		in.close();
		
		// ワークブック、ワークシートを作成
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("list of compounds");
		
		// 見出し用スタイルをセット
		HSSFCellStyle style1 = wb.createCellStyle();
			// フォント - 太字、白色
		HSSFFont font1 = wb.createFont();
		font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font1.setColor(HSSFColor.WHITE.index);
		style1.setFont(font1);
			// 背景色
		style1.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
		style1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		// リンク用スタイルをセット
		HSSFCellStyle style2 = wb.createCellStyle();
			// フォント - 青字で下線アリ
		HSSFFont font2 = wb.createFont();
		font2.setColor(HSSFColor.BLUE.index);
		font2.setUnderline(HSSFFont.U_SINGLE);
		style2.setFont(font2);
		style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		// コメント用
		HSSFPatriarch patr = sheet.createDrawingPatriarch();
		HSSFFont font3 = wb.createFont();
		font3.setFontHeight((short)(20*9));	// 9px
		
		// 化合物リストを書き込む
		HSSFRow hsRow = null;
		int colspan = 0;
		for ( int row = 0; row < list.size(); row++ ){
			line = list.get(row);
			String[] item = line.split("\t");
			hsRow = sheet.createRow(row + 1);
			
			// 化合物名、Formnulaをセット
			for ( short col = 0; col < item.length - 1; col++ ) {
				HSSFCell cell = hsRow.createCell(col);
				String val = item[col];
				cell.setCellValue(new HSSFRichTextString(val));
			}
			
			// Recored IDをセット
			boolean isColReSize = false;
			String partId = item[item.length - 1];
			String[] idList = partId.split("@");
			if ( idList.length > colspan ) {
				isColReSize = true;
				colspan = idList.length;
			}
			for ( short n = 0; n < idList.length; n++ ) {
				short col2 = (short)(item.length - 1 + n);
				HSSFCell cell = hsRow.createCell(col2);
				int pos = idList[n].indexOf(" NAME=");
				String id = idList[n].substring( 0, pos );
				String recordName = idList[n].substring( pos + 6 );
				
				cell.setCellValue(new HSSFRichTextString(id));
				if ( isColReSize ) {
					sheet.autoSizeColumn(col2);
				}
				// ハイパーリンクをセット
				String linkUrl = serverUrl + "jsp/Dispatcher.jsp?type=disp&id="
												 + id + "&site=" + String.valueOf(siteNum);
				cell.setCellFormula("HYPERLINK(\"" + linkUrl + "\",\"" + id + "\")");
				
				// スタイルをセット
				cell.setCellStyle(style2);
				
				// コメントをセット
				HSSFComment comment = patr.createComment(new HSSFClientAnchor(0,0,0,150, (short)0,0,(short)2,1)); 
				cell.setCellComment(comment);
				HSSFRichTextString richText = new HSSFRichTextString(recordName);
				richText.applyFont(font3);
				comment.setString(richText);
			}
		}
		
		//** 見出しを書き込む
		hsRow = sheet.createRow(0);
		String[] headline = { "Compound Name", "Formula", "Recored ID" };
		for ( short col = 0; col < headline.length; col++ ){
				HSSFCell cell = hsRow.createCell(col);
				cell.setCellValue(new HSSFRichTextString(headline[col]));
				cell.setCellStyle(style1);
				sheet.autoSizeColumn(col);
		}
		// Recored IDヘッダのセルを結合
		sheet.addMergedRegion( new Region(0,(short)(headline.length-1), 0,(short)(colspan+1)) );
		
		//** 保存
		String outPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH) + dbNameList[siteNum] + "_list.xls";
		FileOutputStream fso = new FileOutputStream(outPath);
		wb.write(fso);
		fso.close();
		
		String downloadUrl = baseUrl + "temp/" + dbNameList[siteNum] + "_list.xls";
		out.println( "<b>Download :</b>&nbsp;<a href=\"" + downloadUrl + "\" target=\"_blank\">" + downloadUrl + "</a><br><br>" );
	}

%>
<form name="form1" method="post" action="<%out.print(jspName);%>">
<b>Contributor :</b>&nbsp;
<select name="site_num">
<%
	for ( int i = 0; i < dbNameList.length; i++ ) {
		out.print( "<option value=\"" + String.valueOf(i) + "\"" );
		if ( i == siteNum ) {
			out.print( " selected" );
		}
		out.println( ">" + dbNameList[i] );
	}
%>
</select>
<input type="submit" value="Execute">
<input type="hidden" name="act" value="gene">
</form>
</body>
</html>
