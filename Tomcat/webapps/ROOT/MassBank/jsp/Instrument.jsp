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
 * Instrument Typeパラメータ表示
 *
 * ver 1.0.2 2009.12.08
 *
 ******************************************************************************/
%>
<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.GetInstInfo" %>
<%!
	/** Cookie名 */
	private final String COOKIE_COMMON = "Common";
	/** Cookie情報キー（INSTRUMENTGRP） */
	private final String COOKIE_INSTGRP = "INSTGRP";
	/** Cookie情報キー（INSTRUMENT） */
	private final String COOKIE_INST = "INST";
	/** Cookie情報キー（ION） */
	private final String COOKIE_ION = "ION";
	
	/**
	 * Cookie情報取得
	 * 対象となるCookie情報からキーに該当する値のみを取得する
	 * @param c Cookie情報
	 * @param key 取得したいCookie情報のキー
	 * @return Cookie情報
	 */
	public String getCookie(Cookie c, String key) {
		String cookieValue = "";
		if (c != null) {
			String tmpValues = "";
			try {
				tmpValues = URLDecoder.decode(c.getValue(), "utf-8");
			}
			catch ( UnsupportedEncodingException e ) {
				e.printStackTrace();
			}
			if (tmpValues.trim().length() != 0) {
				String[] data = tmpValues.split(";");
				String[] item;
				for (int i=0; i<data.length; i++) {
					item = data[i].split("=");
					if (item[0].trim().equals(key)) {
						if (item.length == 2) {
							cookieValue = item[1].trim();
						}
						break;
					}
				}
			}
		}
		return cookieValue;
	}
%>
<%
	//-------------------------------------------
	// Instrument Typeを取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	int pos = path.indexOf("/jsp");
	String baseUrl = path.substring( 0, pos+1 );
	GetConfig conf = new GetConfig(baseUrl);
	GetInstInfo instInfo = new GetInstInfo(baseUrl);
	Map<String, List<String>> instGroup = instInfo.getTypeGroup();
	Iterator it = instGroup.keySet().iterator();
	
	//-------------------------------------------
	// Cookie情報取得
	//-------------------------------------------
	Cookie commonCookie = null;
	// massbank.conf でクッキー有効の場合のみ取得
	if ( conf.isCookie() ) {
		final Cookie[] allCookies = request.getCookies();
		if ( allCookies != null ) {
			for (int i=0; i<allCookies.length; i++) {
				// 共通クッキーを取得
				if (allCookies[i].getName().equals(COOKIE_COMMON)) {
					commonCookie = allCookies[i];
				}
			}
		}
	}
	
	//-------------------------------------------
	// パラメータ取得
	//-------------------------------------------
	boolean isFirst = true;
	String first = request.getParameter("first");
	if ( first != null && first.equals("false") ) {
		isFirst = false;
	}
	String ionMode = "";
	String[] chkInstType = null;
	String[] chkInstGrp = null;
	if ( commonCookie != null ) {
		ionMode = getCookie(commonCookie, COOKIE_ION);
		chkInstType = getCookie(commonCookie, COOKIE_INST).split(",");
		chkInstGrp = getCookie(commonCookie, COOKIE_INSTGRP).split(",");
	}
	else {
		String tmpIonMode = request.getParameter("ion");
		if ( tmpIonMode != null ) {
			ionMode = tmpIonMode;
		}
		String tmpInstType = request.getParameter("inst");
		if ( tmpInstType != null && !tmpInstType.equals("") ) {
			chkInstType = tmpInstType.split(",");
		}
		String tmpInstGrp = request.getParameter("inst_grp");
		if ( tmpInstGrp != null && !tmpInstGrp.equals("") ) {
			chkInstGrp = tmpInstGrp.split(",");
		}
	}
	if ( !ionMode.equals("1") && !ionMode.equals("0") && !ionMode.equals("-1") ) {
		ionMode = "1";
	}
	
	//--------------------------------------------
	// Instrument Type チェックボックス表示
	//--------------------------------------------
	out.println( "\t\t\t\t\t\t<table width=\"340\" class=\"inst\">" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" class=\"inst-title\">" );
	out.println( "\t\t\t\t\t\t\t\t\t<b>Instrument&nbsp;Type</b>" );
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t\t</table>" );
	
	out.println( "\t\t\t\t\t\t<div class=\"inst-scroll\">" );
	out.println( "\t\t\t\t\t\t\t<table width=\"310\">" );
	while ( it.hasNext() ) {
		String key = (String)it.next();
		List<String> list = instGroup.get(key);
		out.println( "\t\t\t\t\t\t\t\t<tr valign=\"top\">" );
		out.println( "\t\t\t\t\t\t\t\t\t<td width=\"80\" style=\"padding:5px;\">" );
		out.print( "\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"inst_grp\" id=\"inst_grp_" + key + "\""
				   + " value=\"" + key + "\" onClick=\"selBoxGrp('" + key + "', " + list.size() + "); setCookie('" + conf.isCookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '"+ COOKIE_ION + "');\"" );
		if ( isFirst && commonCookie == null ) {
			if ( key.equals("ESI") ) {
				out.print( " checked" );
			}
		}
		else {
			if ( chkInstGrp != null ) {
				for ( int lp = 0; lp < chkInstGrp.length; lp++ ) {
					if ( key.equals(chkInstGrp[lp]) ) {
						out.print( " checked" );
						break;
					}
				}
			}
		}
		out.print( ">" + key );
		out.println( "\t\t\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t\t\t\t<td style=\"padding:5px;\">" );
		for ( int j = 0; j < list.size(); j++ ) {
				String val = list.get(j);
				out.print( "\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"inst\" id=\"inst_" + key + j + "\""
					 	 + " value=\"" + val + "\" onClick=\"selBoxInst('" + key + "'," + list.size() + "); setCookie('" + conf.isCookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '"+ COOKIE_ION + "');\"" );
				if ( isFirst && commonCookie == null ) {
					if ( key.equals("ESI") ) {
						out.print( " checked" );
					}
				}
				else {
					if ( chkInstType != null ) {
						for ( int lp = 0; lp < chkInstType.length; lp++ ) {
							if ( val.equals(chkInstType[lp]) ) {
								out.print( " checked" );
								break;
							}
						}
					}
				}
				out.println( ">" + val + "<br>" );
		}
		out.println( "\t\t\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t\t\t</tr>" );
		if ( it.hasNext() ) {
			out.println( "\t\t\t\t\t\t\t\t<tr>" );
			out.println( "\t\t\t\t\t\t\t\t\t<td colspan=\"2\"><hr width=\"96%\" size=\"1\" color=\"silver\" align=\"center\"></td>" );
			out.println( "\t\t\t\t\t\t\t\t</tr>" );
		}
	}
	out.println( "\t\t\t\t\t\t\t</table>" );
	out.println( "\t\t\t\t\t\t</div><br>" );
	
	//--------------------------------------------
	// Ionization Mode ラジオボタン表示
	//--------------------------------------------
	out.println( "\t\t\t\t\t\t<table width=\"340\" class=\"inst\">" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" class=\"inst-title\">" );
	out.println( "\t\t\t\t\t\t\t\t\t<b>Ionization&nbsp;Mode</b>" );
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" class=\"inst-item\">" );
	String[] ionVal = { "1", "-1", "0" };
	String[] ionStr = { "Positive&nbsp;&nbsp;", "Negative&nbsp;&nbsp;&nbsp;&nbsp;", "Both" };
	for ( int i = 0; i < ionVal.length; i++ ) {
		out.print( "\t\t\t\t\t\t\t\t\t<input type=\"radio\" name=\"ion\" value=\"" + ionVal[i] + "\" onClick=\"setCookie('" + conf.isCookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '"+ COOKIE_ION + "');\"" );
		if ( ionMode.equals(ionVal[i]) ) {
			out.print( " checked" );
		}
		out.println( ">" +  ionStr[i] );
	}
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t</table>" );
%>
