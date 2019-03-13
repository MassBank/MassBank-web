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

<%@ page import="massbank.Config" %>
<%
	//-------------------------------------
	// 各URL設定
	//-------------------------------------
// 	String commonRefBaseUrl = Config.get().BASE_URL();
//	String MANUAL_URL = commonRefBaseUrl + "manuals/UserManual_en.pdf";
	String SPECTRUM_PAGE = "";
	String QUICK_PAGE = "";
	String PEAK_PAGE = "";
	String STRUCTURE_PAGE = "";
	String ADVANCED_PAGE = "";
	String IDENTIFICATION_PAGE = "";
	String BROWSER_PAGE = "";
	String BATCH_PAGE = "";
	String BROWSE_PAGE = "";
	String INDEX_PAGE = "";
	String MULTI_PAGE = "";
	String RESULT_PAGE = "";
	String RESULT_PEAK_PAGE = "";
// 	String SAMPLE_URL = commonRefBaseUrl + "sample/sample.txt";
// 	String SAMPLE_ZIP_URL = commonRefBaseUrl + "sample/sample.zip";
// 	String RECDATA_ZIP_URL = commonRefBaseUrl + "sample/recdata.zip";
// 	String MOLDATA_ZIP_URL = commonRefBaseUrl + "sample/moldata.zip";
%>
