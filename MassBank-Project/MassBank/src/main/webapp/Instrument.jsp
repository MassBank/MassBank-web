<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute instIt and/or modify
 * instIt under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that instIt will be useful,
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
 * ver 1.0.6 2011.08.02
 *
 ******************************************************************************/
%>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.GetInstInfo" %>
<%@ page import="massbank.Config" %>
<%@ page import="massbank.SearchDefaults" %>
<%!
	/** Cookie名（Common） */
	private final String COOKIE_COMMON = "Common";
	/** Cookie名（PeakAdv） */
	private final String COOKIE_PEAKADV = "PeakAdv";
	
	/** Cookie情報キー（INSTRUMENTGRP） */
	private final String COOKIE_INSTGRP = "INSTGRP";
	/** Cookie情報キー（INSTRUMENT） */
	private final String COOKIE_INST = "INST";
	/** Cookie情報キー（MS） */
	private final String COOKIE_MS = "MS";
	/** Cookie情報キー（ION） */
	private final String COOKIE_ION = "ION";
	
	/** Cookie情報キー（INSTRUMENTGRPADV） */
	private final String COOKIE_INSTGRP_ADV = "INSTGRPADV";
	/** Cookie情報キー（INSTRUMENTADV） */
	private final String COOKIE_INST_ADV = "INSTADV";
	/** Cookie情報キー（MSADV） */
	private final String COOKIE_MS_ADV = "MSADV";
	/** Cookie情報キー（IONADV） */
	private final String COOKIE_ION_ADV = "IONADV";
	
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
	GetConfig conf = new GetConfig(Config.get().BASE_URL());
	
	//-------------------------------------------
	// Cookie情報取得
	//-------------------------------------------
	Cookie commonCookie = null;
	Cookie peakAdvCookie = null;
	// massbank.conf でクッキー有効の場合のみ取得
	if ( Config.get().Cookie() ) {
		final Cookie[] allCookies = request.getCookies();
		if ( allCookies != null ) {
			for (int i=0; i<allCookies.length; i++) {
				// 共通クッキーを取得
				if (allCookies[i].getName().equals(COOKIE_COMMON)) {
					commonCookie = allCookies[i];
				}
				// PeakSearchAdvance用クッキーを取得
				else if (allCookies[i].getName().equals(COOKIE_PEAKADV)) {
					peakAdvCookie = allCookies[i];
				}
			}
		}
	}
	
	//-------------------------------------------
	// パラメータ取得 ()
	//-------------------------------------------
	boolean isFirst = true;
	String first = request.getParameter("first");
	if ( first != null && first.equals("false") ) {
		isFirst = false;
	}
	boolean isPeakAdv = false;
	String peakAdv = request.getParameter("padv");
	if ( peakAdv != null && peakAdv.equals("true") ) {
		isPeakAdv = true;
	}
	String[] chkInstType = null;
	String[] chkInstGrp = null;
	String[] chkMsType = null;
	String ionMode = "";
	if ( commonCookie != null ) {
		chkInstType = getCookie(commonCookie, COOKIE_INST).split(",");
		chkInstGrp = getCookie(commonCookie, COOKIE_INSTGRP).split(",");
		chkMsType = getCookie(commonCookie, COOKIE_MS).split(",");
		ionMode = getCookie(commonCookie, COOKIE_ION);
	}
	else {
		String tmpInstType = request.getParameter("inst");
		if ( tmpInstType != null && !tmpInstType.equals("") ) {
			chkInstType = tmpInstType.split(",");
		}
		String tmpInstGrp = request.getParameter("inst_grp");
		if ( tmpInstGrp != null && !tmpInstGrp.equals("") ) {
			chkInstGrp = tmpInstGrp.split(",");
		}
		String tmpMsType = request.getParameter("ms");
		if ( tmpMsType != null && !tmpMsType.equals("") ) {
			chkMsType = tmpMsType.split(",");
		}
		String tmpIonMode = request.getParameter("ion");
		if ( tmpIonMode != null ) {
			ionMode = tmpIonMode;
		}
	}
	if ( !ionMode.equals("1") && !ionMode.equals("0") && !ionMode.equals("-1") ) {
		ionMode = SearchDefaults.ionMode;
	}
	
	String[] chkInstTypeAdv = null;
	String[] chkInstGrpAdv = null;
	String[] chkMsTypeAdv = null;
	String ionModeAdv = "";
	if ( peakAdvCookie != null ) {
		chkInstTypeAdv = getCookie(peakAdvCookie, COOKIE_INST_ADV).split(",");
		chkInstGrpAdv = getCookie(peakAdvCookie, COOKIE_INSTGRP_ADV).split(",");
		chkMsTypeAdv = getCookie(peakAdvCookie, COOKIE_MS_ADV).split(",");
		ionModeAdv = getCookie(peakAdvCookie, COOKIE_ION_ADV);
	}
	else {
		String tmpInstTypeAdv = request.getParameter("inst_adv");
		if ( tmpInstTypeAdv != null && !tmpInstTypeAdv.equals("") ) {
			chkInstTypeAdv = tmpInstTypeAdv.split(",");
		}
		String tmpInstGrpAdv = request.getParameter("inst_grp_adv");
		if ( tmpInstGrpAdv != null && !tmpInstGrpAdv.equals("") ) {
			chkInstGrpAdv = tmpInstGrpAdv.split(",");
		}
		String tmpMsTypeAdv = request.getParameter("ms_adv");
		if ( tmpMsTypeAdv != null && !tmpMsTypeAdv.equals("") ) {
			chkMsTypeAdv = tmpMsTypeAdv.split(",");
		}
		String tmpIonModeAdv = request.getParameter("ion_adv");
		if ( tmpIonModeAdv != null) {
			ionModeAdv = tmpIonModeAdv;
		}
	}
	if ( !ionModeAdv.equals("1") && !ionModeAdv.equals("0") && !ionModeAdv.equals("-1") ) {
		ionModeAdv = SearchDefaults.ionMode;
	}
	
	//-------------------------------------------
	// Aquire (を取得) Instrument Type, MS Type
	//-------------------------------------------
	GetInstInfo instInfo = new GetInstInfo(request);
//	GetInstInfo instInfo = null;
//	if ( !isPeakAdv ) {
//		instInfo = new GetInstInfo(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
//	}
//	else {
//		instInfo = new GetInstInfo(MassBankEnv.get(MassBankEnv.KEY_BASE_URL), 2, true);
//	}
	Map<String, List<String>> instGroup = instInfo.getTypeGroup();
	Iterator instIt = instGroup.keySet().iterator();
	String[] msInfo = instInfo.getMsAll();
	
	//--------------------------------------------
	// Instrument Type check box display (チェックボックス表示)
	//--------------------------------------------
	out.println( "\t\t\t\t\t\t<table width=\"340\" class=\"cond\">" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	String condInstTitle = "condInstTitle";
	if ( isPeakAdv ) { condInstTitle = "condInstTitleAdv"; }
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" id=\"" + condInstTitle + "\" class=\"cond-title\">" );
	out.println( "\t\t\t\t\t\t\t\t\t<b>Instrument&nbsp;Type</b>" );
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t\t</table>" );
	out.println( "\t\t\t\t\t\t<div class=\"inst-scroll\">" );
	out.println( "\t\t\t\t\t\t\t<table width=\"310\">" );
	while ( instIt.hasNext() ) {
		String key = (String)instIt.next();
		List<String> grpList = instGroup.get(key);
		out.println( "\t\t\t\t\t\t\t\t<tr valign=\"top\">" );
		out.println( "\t\t\t\t\t\t\t\t\t<td width=\"80\" style=\"padding:5px;\">" );
		if ( !isPeakAdv ) {
			out.print( "\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"inst_grp\" id=\"inst_grp_" + key + "\""
					   + " value=\"" + key + "\" onClick=\"selBoxGrp('" + key + "', " + grpList.size() + ", 0); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '" + COOKIE_MS + "', '" + COOKIE_ION + "', 0);\"" );
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
		}
		else {
			out.print( "\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"inst_grp_adv\" id=\"inst_grp_adv_" + key + "\""
					   + " value=\"" + key + "\" onClick=\"selBoxGrp('" + key + "', " + grpList.size() + ", 1); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_PEAKADV + "', '" + COOKIE_INSTGRP_ADV + "', '" + COOKIE_INST_ADV + "', '" + COOKIE_MS_ADV + "', '" + COOKIE_ION_ADV + "', 1);\"" );
			if ( isFirst && peakAdvCookie == null ) {
				if ( key.equals("ESI") ) {
					out.print( " checked" );
				}
			}
			else {
				if ( chkInstGrpAdv != null ) {
					for ( int lp = 0; lp < chkInstGrpAdv.length; lp++ ) {
						if ( key.equals(chkInstGrpAdv[lp]) ) {
							out.print( " checked" );
							break;
						}
					}
				}
			}
		}
		out.print( ">" + key );
		out.println( "\t\t\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t\t\t\t<td style=\"padding:5px;\">" );
		for ( int j = 0; j < grpList.size(); j++ ) {
				String val = grpList.get(j);
				if ( !isPeakAdv ) {
					out.print( "\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"inst\" id=\"inst_" + key + j + "\""
							 + " value=\"" + val + "\" onClick=\"selBoxInst('" + key + "'," + grpList.size() + ", 0); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '" + COOKIE_MS + "', '" + COOKIE_ION + "', 0);\"" );
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
				}
				else {
					out.print( "\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"inst_adv\" id=\"inst_adv_" + key + j + "\""
							 + " value=\"" + val + "\" onClick=\"selBoxInst('" + key + "'," + grpList.size() + ", 1); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_PEAKADV + "', '" + COOKIE_INSTGRP_ADV + "', '" + COOKIE_INST_ADV + "', '" + COOKIE_MS_ADV + "', '" + COOKIE_ION_ADV + "', 1);\"" );
					if ( isFirst && peakAdvCookie == null ) {
						if ( key.equals("ESI") ) {
							out.print( " checked" );
						}
					}
					else {
						if ( chkInstTypeAdv != null ) {
							for ( int lp = 0; lp < chkInstTypeAdv.length; lp++ ) {
								if ( val.equals(chkInstTypeAdv[lp]) ) {
									out.print( " checked" );
									break;
								}
							}
						}
					}
				}
				out.println( ">" + val + "<br>" );
		}
		out.println( "\t\t\t\t\t\t\t\t\t</td>" );
		out.println( "\t\t\t\t\t\t\t\t</tr>" );
		if ( instIt.hasNext() ) {
			out.println( "\t\t\t\t\t\t\t\t<tr>" );
			out.println( "\t\t\t\t\t\t\t\t\t<td colspan=\"2\"><hr width=\"96%\" size=\"1\" color=\"silver\" align=\"center\"></td>" );
			out.println( "\t\t\t\t\t\t\t\t</tr>" );
		}
	}
	out.println( "\t\t\t\t\t\t\t</table>" );
	out.println( "\t\t\t\t\t\t</div><br>" );
	
	//--------------------------------------------
	// MS Type チェックボックス表示
	//--------------------------------------------
	out.println( "\t\t\t\t\t\t<table width=\"340\" class=\"cond\">" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	String condMsTitle = "condMsTitle";
	if ( isPeakAdv ) { condMsTitle = "condMsTitleAdv"; }
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" id=\"" + condMsTitle + "\" class=\"cond-title\">" );
	out.println( "\t\t\t\t\t\t\t\t\t<b>MS&nbsp;Type</b>" );
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" class=\"cond-item\">" );
	if ( msInfo.length > 0 ) {
		String allCheked = "";
		if ( !isPeakAdv ) {
			if ( isFirst && commonCookie == null ) {
				allCheked = " checked";
			}
			else {
				if ( chkMsType != null ) {
					for ( int lp = 0; lp < chkMsType.length; lp++ ) {
						if ( "all".equals(chkMsType[lp]) ) {
							allCheked = " checked";
							break;
						}
					}
				}
			}
			out.println( "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"ms\" id=\"ms_MS0\" value=\"all\" onClick=\"selAllMs(" + msInfo.length + ", 0); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '" + COOKIE_MS + "', '" + COOKIE_ION + "', 0);\"" + allCheked + ">All&nbsp;&nbsp;&nbsp;" );
		}
		else {
			if ( isFirst && peakAdvCookie == null ) {
				allCheked = " checked";
			}
			else {
				if ( chkMsTypeAdv != null ) {
					for ( int lp = 0; lp < chkMsTypeAdv.length; lp++ ) {
						if ( "all".equals(chkMsTypeAdv[lp]) ) {
							allCheked = " checked";
							break;
						}
					}
				}
			}
			out.println( "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"ms_adv\" id=\"ms_adv_MS0\" value=\"all\" onClick=\"selAllMs(" + msInfo.length + ", 1); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_PEAKADV + "', '" + COOKIE_INSTGRP_ADV + "', '" + COOKIE_INST_ADV + "', '" + COOKIE_MS_ADV + "', '" + COOKIE_ION_ADV + "', 1);\"" + allCheked + ">All&nbsp;&nbsp;&nbsp;" );
		}
	}
	else {
		out.println( "\t\t\t\t\t\t\t\t\t&nbsp;" );
	}
	for ( int i=0; i<msInfo.length; i++ ) {
		if ( !isPeakAdv ) {
			out.print( "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"ms\" id=\"ms_MS" + (i+1) + "\""
					 + " value=\"" + msInfo[i] + "\" onClick=\"selMs(" + msInfo.length + ", 0); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '" + COOKIE_MS + "', '" + COOKIE_ION + "', 0);\"" );
			if ( isFirst && commonCookie == null ) {
				out.print( " checked" );
			}
			else {
				if ( chkMsType != null ) {
					for ( int lp = 0; lp < chkMsType.length; lp++ ) {
						if ( msInfo[i].equals(chkMsType[lp]) || chkMsType[lp].equals("all") ) {
							out.print( " checked" );
							break;
						}
					}
				}
			}
		}
		else {
			out.print( "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"ms_adv\" id=\"ms_adv_MS" + (i+1) + "\""
					 + " value=\"" + msInfo[i] + "\" onClick=\"selMs(" + msInfo.length + ", 1); setCookie('" + Config.get().Cookie() + "', '" + COOKIE_PEAKADV + "', '" + COOKIE_INSTGRP_ADV + "', '" + COOKIE_INST_ADV + "', '" + COOKIE_MS_ADV + "', '" + COOKIE_ION_ADV + "', 1);\"" );
			if ( isFirst && chkMsTypeAdv == null ) {
				out.print( " checked" );
			}
			else {
				if ( chkMsTypeAdv != null ) {
					for ( int lp = 0; lp < chkMsTypeAdv.length; lp++ ) {
						if ( msInfo[i].equals(chkMsTypeAdv[lp]) || chkMsTypeAdv[lp].equals("all") ) {
							out.print( " checked" );
							break;
						}
					}
				}
			}
		}
		out.println( ">" + msInfo[i] + "&nbsp;" );
	}
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t</table><br>" );
	
	//--------------------------------------------
	// Ion Mode ラジオボタン表示
	//--------------------------------------------
	out.println( "\t\t\t\t\t\t<table width=\"340\" class=\"cond\">" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	String condIonTitle = "condIonTitle";
	if ( isPeakAdv ) { condIonTitle = "condIonTitleAdv"; }
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" id=\"" + condIonTitle + "\" class=\"cond-title\">" );
	out.println( "\t\t\t\t\t\t\t\t\t<b>Ion&nbsp;Mode</b>" );
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t\t<tr>" );
	out.println( "\t\t\t\t\t\t\t\t<td colspan=\"2\" class=\"cond-item\">" );
	String[] ionVal = { "1", "-1", "0" };
	String[] ionStr = { "Positive&nbsp;&nbsp;", "Negative&nbsp;&nbsp;&nbsp;&nbsp;", "Both" };
	for ( int i = 0; i < ionVal.length; i++ ) {
		if ( !isPeakAdv ) {
			out.print( "\t\t\t\t\t\t\t\t\t<input type=\"radio\" name=\"ion\" value=\"" + ionVal[i] + "\" onClick=\"setCookie('" + Config.get().Cookie() + "', '" + COOKIE_COMMON + "', '" + COOKIE_INSTGRP + "', '" + COOKIE_INST + "', '" + COOKIE_MS + "', '" + COOKIE_ION + "', 0);\"" );
			if ( ionMode.equals(ionVal[i]) ) {
				out.print( " checked" );
			}
		}
		else {
			out.print( "\t\t\t\t\t\t\t\t\t<input type=\"radio\" name=\"ion_adv\" value=\"" + ionVal[i] + "\" onClick=\"setCookie('" + Config.get().Cookie() + "', '" + COOKIE_PEAKADV + "', '" + COOKIE_INSTGRP_ADV + "', '" + COOKIE_INST_ADV + "', '" + COOKIE_MS_ADV + "', '" + COOKIE_ION_ADV + "', 1);\"" );
			if ( ionModeAdv.equals(ionVal[i]) ) {
				out.print( " checked" );
			}
		}
		out.println( ">" +  ionStr[i] );
	}
	out.println( "\t\t\t\t\t\t\t\t</td>" );
	out.println( "\t\t\t\t\t\t\t</tr>" );
	out.println( "\t\t\t\t\t\t</table>" );
%>
