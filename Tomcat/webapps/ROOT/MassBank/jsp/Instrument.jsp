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
 * ver 1.0.1 2009.10.30
 *
 ******************************************************************************/
%>
<%@ page import="java.util.*" %>
<%@ page import="massbank.GetInstInfo" %>
<%
	//-------------------------------------------
	// パラメータ取得
	//-------------------------------------------
	String ionMode = request.getParameter("ion");
	if ( ionMode == null ) {
		ionMode = "1";
	}
	boolean isFirst = true;
	String first = request.getParameter("first");
	if ( first != null && first.equals("false") ) {
		isFirst = false;
	}
	String[] chkInstType = null;
	String[] chkInstGrp = null;
	String instType = request.getParameter("inst");
	if ( instType != null && !instType.equals("") ) {
		chkInstType = instType.split(",");
	}
	String instGrp = request.getParameter("inst_grp");
	if ( instGrp != null && !instGrp.equals("") ) {
		chkInstGrp = instGrp.split(",");
	}

	//-------------------------------------------
	// Instrument Typeを取得
	//-------------------------------------------
	String path = request.getRequestURL().toString();
	int pos = path.indexOf("/jsp");
	String baseUrl = path.substring( 0, pos+1 );
	GetInstInfo instInfo = new GetInstInfo(baseUrl);
	Map<String, List<String>> instGroup = instInfo.getTypeGroup();
	Iterator it = instGroup.keySet().iterator();

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
				   + " value=\"" + key + "\" onClick=\"selBoxGrp('" + key + "'," + list.size() + ")\"" );
		if ( isFirst ) {
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
					 	 + " value=\"" + val + "\" onClick=\"selBoxInst('" + key + "'," + list.size() + ")\"" );
				if ( isFirst ) {
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
		out.print( "\t\t\t\t\t\t\t\t\t<input type=\"radio\" name=\"ion\" value=\"" + ionVal[i] + "\"" );
		if ( ionMode.equals(ionVal[i]) ) {
			out.print( " checked" );
		}
		out.println( ">" +  ionStr[i] );
	}
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t</table>" );
%>
