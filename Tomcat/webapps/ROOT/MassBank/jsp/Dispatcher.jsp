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
 * 処理振り分け用モジュール
 *
 * ver 1.0.9 2011.07.25
 *
 ******************************************************************************/
%>

<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.httpclient.methods.PostMethod" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankCommon" %>
<%@ page import="massbank.MassBankLog" %>
<%!
	final int MYSVR_INFO_NUM = 0;
%>
<%
	ServletContext context = getServletContext();
	String progName = "Dispatcher.jsp";
	String msg = "";
	
	//-------------------------------------------
	// リクエストパラメータ取得
	//-------------------------------------------
	Hashtable<String, Object> params = new Hashtable<String, Object>();
	String type = "";
	String dbName = "";
	
	int siteNum = MYSVR_INFO_NUM;
	Enumeration names = request.getParameterNames();
	while ( names.hasMoreElements() ) {
		String key = (String)names.nextElement();
		String val = (String)request.getParameter( key );
		if ( key.equals("type") ) {
			type  = val;
		}
		else if ( key.equals("site") ) {
			siteNum = Integer.parseInt( val );
		}
		else if ( key.equals("dsn") ) {
			dbName = val;
		}
		else if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
			// キーがInstrumentType,MSType以外の場合はStringパラメータ
			params.put( key, val );
		}
		else {
			// キーがInstrumentType,MSTypeの場合はString配列パラメータ
			String[] vals = (String[])request.getParameterValues( key );
			params.put( key, vals );
		}
	}
	
	int typeNum = -1;
	for ( int i = 0; i < MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE].length; i++ ) {
		if ( type.equals( MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][i] ) ) {
			typeNum = i;
			break;
		}
	}
	
	if ( typeNum == -1 ) {
		// エラー
		msg = "パラメータtype不正";
		MassBankLog.ErrorLog( progName, msg, context );
	}
	String cgiName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_FILE][typeNum];
	
	//-------------------------------------------
	// 環境設定ファイルからURLリストを取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	int pos = path.indexOf("/jsp");
	String baseUrl = path.substring( 0, pos+1 ) ;
	GetConfig conf = new GetConfig(baseUrl);
	String[] urlList = conf.getSiteUrl();
	if ( dbName.equals("") ) {
		String[] dbNameList = conf.getDbName();
		dbName = dbNameList[siteNum];
	}
	int timeout = conf.getTimeout();
	boolean isTrace = conf.isTraceEnable();

	String reqStr = urlList[siteNum];
	//---------------------------------------------------
	// 自サーバーの場合、cgiへアクセス
	//---------------------------------------------------
	if ( siteNum == MYSVR_INFO_NUM ) {
		reqStr += "cgi-bin/" + cgiName;
		if ( typeNum == MassBankCommon.CGI_TBL_TYPE_PEAK
		  || typeNum == MassBankCommon.CGI_TBL_TYPE_PDIFF
		  || typeNum == MassBankCommon.CGI_TBL_TYPE_DISPDIFF ) {
		
			params.put( "type", type );
		}
	}
	//---------------------------------------------------
	// 連携サーバーの場合、Dispatcher.jspを介してアクセス
	//---------------------------------------------------
	else {
		reqStr += "jsp/" + MassBankCommon.DISPATCHER_NAME;
		params.put( "type", type );
		if ( typeNum != MassBankCommon.CGI_TBL_TYPE_GNAME
		  && typeNum != MassBankCommon.CGI_TBL_TYPE_GSON  ) {
			params.put( "site", "0" );
		}
		if ( typeNum == MassBankCommon.CGI_TBL_TYPE_DISP
		  || typeNum == MassBankCommon.CGI_TBL_TYPE_DISPDIFF ) {
			params.put( "src", String.valueOf(siteNum) );
		}
	}
	params.put( "dsn", dbName );
	
	HttpClient client = new HttpClient();
	// タイムアウト値(msec)セット
	client.setTimeout( timeout * 1000 );
	PostMethod method = new PostMethod( reqStr );
	String strParam = "";
	for ( Enumeration keys = params.keys(); keys.hasMoreElements(); ) {
		String key = (String)keys.nextElement();
		if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
			// キーがInstrumentType,MSType以外の場合はStringパラメータ
			String val = (String)params.get(key);
			strParam += key + "=" + val + "&";
			method.addParameter( key, val );
		}
		else {
			// キーがInstrumentType,MSTypeの場合はString配列パラメータ
			String[] vals = (String[])params.get(key);
			for (int i=0; i<vals.length; i++) {
				strParam += key + "=" + vals[i] + "&";
				method.addParameter( key, vals[i] );
			}
		}
	}
	strParam = strParam.substring( 0, strParam.length()-1 );
	
	// ログ出力
	if ( request.getQueryString() != null ) {
		msg = "Query: " + request.getQueryString() + "\n";
	}
	else {
		msg = "Query: " + strParam + "\n";
	}
	msg += "Call : ";
	if ( request.getHeader("referer") != null ) {
		msg += request.getHeader("referer") + " -->";
	}
	msg += reqStr + "?" + strParam;
	MassBankLog.TraceLog( progName, msg, context, isTrace );
	
	
	try {
		int statusCode = client.executeMethod(method);
		//** ステータスコードのチェック **
		if ( statusCode != HttpStatus.SC_OK ){
			// エラー
			msg = method.getStatusLine().toString() + "\n" + "URL : " + reqStr;
			MassBankLog.ErrorLog( progName, msg, context );
		}
		
		//** レスポンス取得 **
		String result = method.getResponseBodyAsString();
		out.print(result);
	}
	catch ( Exception e ) {
		// エラー
		msg = e.toString() + "\n" + "URL : " + reqStr;
		MassBankLog.ErrorLog( progName, msg, context );
	}
	finally {
		//** コネクション解放 **
		method.releaseConnection();
	}
%>
