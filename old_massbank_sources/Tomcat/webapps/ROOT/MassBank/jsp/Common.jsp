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
 * 共通JSP（静的インクルード用）
 *
 * ver 1.0.6 2011.12.15
 *
 ******************************************************************************/
%>

<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.admin.AdminCommon" %>
<%
	//----------------------------------------------------
	// パラメータ取得
	//----------------------------------------------------
	String commonBaseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	AdminCommon commonAdmin = new AdminCommon();
	boolean isPortal = commonAdmin.isPortal();
	
	//-------------------------------------
	// 参照先ベースURL
	//-------------------------------------
	String commonRefBaseUrl = "http://www.massbank.jp/";
	if ( !isPortal ) {
		commonRefBaseUrl = commonBaseUrl;
	}
	
	//-------------------------------------
	// ブラウザ優先言語による言語判別
	//-------------------------------------
	String commonBrowserLang = (request.getHeader("accept-language") != null) ? request.getHeader("accept-language") : "";
	boolean isJp = false;
	if ( commonBrowserLang.startsWith("ja") || commonBrowserLang.equals("") ) {
		isJp = true;
	}
	
	//-------------------------------------
	// 各URL設定
	//-------------------------------------
	String MANUAL_URL = commonRefBaseUrl + "manuals/UserManual_ja.pdf";
	String SPECTRUM_PAGE = "#page=4";
	String QUICK_PAGE = "#page=8";
	String PEAK_PAGE = "#page=15";
	String STRUCTURE_PAGE = "#page=14";
	String ADVANCED_PAGE = "#page=11";
	String IDENTIFICATION_PAGE = "";
	String BROWSER_PAGE = "#page=26";
	String BATCH_PAGE = "#page=9";
	String BROWSE_PAGE = "#page=19";
	String INDEX_PAGE = "#page=18";
	String MULTI_PAGE = "#page=23";
	String RESULT_PAGE = "#page=20";
	String RESULT_PEAK_PAGE = "#page=16";
	if ( !isJp ) {
		MANUAL_URL = commonRefBaseUrl + "manuals/UserManual_en.pdf";
		SPECTRUM_PAGE = "#page=4";
		QUICK_PAGE = "#page=8";
		PEAK_PAGE = "#page=13";
		STRUCTURE_PAGE = "#page=12";
		ADVANCED_PAGE = "";
		IDENTIFICATION_PAGE = "#page=16";
		BROWSER_PAGE = "#page=28";
		BATCH_PAGE = "#page=9";
		BROWSE_PAGE = "#page=21";
		INDEX_PAGE = "#page=22";
		MULTI_PAGE = "#page=23";
		RESULT_PAGE = "#page=20";
		RESULT_PEAK_PAGE = "";
	}
	
	String SAMPLE_URL = commonRefBaseUrl + "sample/sample.txt";
	String SAMPLE_ZIP_URL = commonRefBaseUrl + "sample/sample.zip";
	String RECDATA_ZIP_URL = commonRefBaseUrl + "sample/recdata.zip";
	String MOLDATA_ZIP_URL = commonRefBaseUrl + "sample/moldata.zip";
	String GIFDATA_ZIP_URL = commonRefBaseUrl + "sample/gifdata.zip";
%>
