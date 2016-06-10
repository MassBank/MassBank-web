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
 * MassBank共通クラス(Applet, JSP, Servlet 全てにおいて使用される)
 *
 * ver 1.0.23 2011.09.21
 *
 ******************************************************************************/
package massbank;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class MassBankCommon {
	public static final String DISPATCHER_NAME = "Dispatcher.jsp";
	public static final String MULTI_DISPATCHER_NAME = "MultiDispatcher";

	// ＜検索条件数＞
	public static final int PEAK_SEARCH_PARAM_NUM     = 6;
	
	// ＜リクエスト種別＞
	public static final String REQ_TYPE_PEAK      = "peak";
	public static final String REQ_TYPE_PEAKDIFF  = "diff";
	public static final String REQ_TYPE_DISP      = "disp";
	public static final String REQ_TYPE_GDATA     = "gdata";
	public static final String REQ_TYPE_GDATA2    = "gdata2";
	public static final String REQ_TYPE_SEARCH    = "search";
	public static final String REQ_TYPE_GNAME     = "gname";
	public static final String REQ_TYPE_GPEAK     = "gpeak";
	public static final String REQ_TYPE_QSTAR     = "qstar";
	public static final String REQ_TYPE_GSON      = "gson";
	public static final String REQ_TYPE_DISPDIFF  = "dispdiff";
	public static final String REQ_TYPE_QUICK     = "quick";
	public static final String REQ_TYPE_PEAK2     = "peak2";		// ver 1.0.11 未使用
	public static final String REQ_TYPE_PEAKDIFF2 = "diff2";		// ver 1.0.11 未使用
	public static final String REQ_TYPE_RECORD    = "record";
	public static final String REQ_TYPE_IDXCNT    = "idxcnt";
	public static final String REQ_TYPE_RCDIDX    = "rcdidx";
	public static final String REQ_TYPE_INST      = "inst";
	public static final String REQ_TYPE_MOL       = "mol";
	public static final String REQ_TYPE_GSDATA    = "gsdata";
	public static final String REQ_TYPE_GDATA3    = "gdata3";
	public static final String REQ_TYPE_GETLIST   = "getlist";
	public static final String REQ_TYPE_STRUCT    = "struct";
	public static final String REQ_TYPE_PEAKADV   = "peakadv";
	public static final String REQ_TYPE_GETRECORD = "grecord";
	public static final String REQ_TYPE_GETMOL    = "gmol";
	public static final String REQ_TYPE_GETSTRUCT = "gstrct";
	public static final String REQ_TYPE_ADVSEARCH = "advsearch";
	public static final String REQ_TYPE_GETCINFO  = "gcinfo";
	public static final String REQ_TYPE_GETFORMULA = "gform";
	
	// ＜CGIテーブルインデックス＞
	public static final int CGI_TBL_NUM_TYPE = 0;		// CGI種別指定
	public static final int CGI_TBL_NUM_FILE = 1;		// CGIファイル指定

	// ＜CGIテーブル番号＞
	public static final int CGI_TBL_TYPE_PEAK     = 0;
	public static final int CGI_TBL_TYPE_PDIFF    = 1;
	public static final int CGI_TBL_TYPE_DISP     = 2;
	public static final int CGI_TBL_TYPE_GDATA    = 3;
	public static final int CGI_TBL_TYPE_GDATA2   = 4;
	public static final int CGI_TBL_TYPE_SEARCH   = 5;
	public static final int CGI_TBL_TYPE_GNAME    = 6;
	public static final int CGI_TBL_TYPE_GPEAK    = 7;
	public static final int CGI_TBL_TYPE_QSTAR    = 8;
	public static final int CGI_TBL_TYPE_GSON     = 9;
	public static final int CGI_TBL_TYPE_DISPDIFF = 10;
	public static final int CGI_TBL_TYPE_QUICK    = 11;
	public static final int CGI_TBL_TYPE_PEAK2    = 12;		// ver 1.0.11 未使用
	public static final int CGI_TBL_TYPE_PDIFF2   = 13;		// ver 1.0.11 未使用
	public static final int CGI_TBL_TYPE_RECORD   = 14;
	public static final int CGI_TBL_TYPE_IDXCNT   = 15;
	public static final int CGI_TBL_TYPE_RCDIDX   = 16;
	public static final int CGI_TBL_TYPE_INST     = 17;
	public static final int CGI_TBL_TYPE_MOL      = 18;
	public static final int CGI_TBL_TYPE_GSDATA   = 19;
	public static final int CGI_TBL_TYPE_GDATA3   = 20;
	public static final int CGI_TBL_TYPE_GETLIST  = 21;
	public static final int CGI_TBL_TYPE_STRUCT   = 22;
	public static final int CGI_TBL_TYPE_PEAKADV  = 23;
	public static final int CGI_TBL_TYPE_GETRECORD= 24;
	public static final int CGI_TBL_TYPE_GETMOL   = 25;
	public static final int CGI_TBL_TYPE_GETSTRUCT= 26;
	public static final int CGI_TBL_TYPE_ADVSEARCH= 27;
	public static final int CGI_TBL_TYPE_GETCINFO  = 28;
	public static final int CGI_TBL_TYPE_GETFORMULA = 29;
	

	// ＜CGIテーブル＞
	public static final String[][] CGI_TBL = {
		{ REQ_TYPE_PEAK,     REQ_TYPE_PEAKDIFF,  REQ_TYPE_DISP,     REQ_TYPE_GDATA,
		  REQ_TYPE_GDATA2,   REQ_TYPE_SEARCH,    REQ_TYPE_GNAME,    REQ_TYPE_GPEAK,
		  REQ_TYPE_QSTAR,    REQ_TYPE_GSON,      REQ_TYPE_DISPDIFF, REQ_TYPE_QUICK,
		  REQ_TYPE_PEAK2,    REQ_TYPE_PEAKDIFF2, REQ_TYPE_RECORD,   REQ_TYPE_IDXCNT,
		  REQ_TYPE_RCDIDX,   REQ_TYPE_INST,
		  REQ_TYPE_MOL,      REQ_TYPE_GSDATA,    REQ_TYPE_GDATA3,
		  REQ_TYPE_GETLIST,  REQ_TYPE_STRUCT,    REQ_TYPE_PEAKADV,  REQ_TYPE_GETRECORD,
		  REQ_TYPE_GETMOL,   REQ_TYPE_GETSTRUCT, REQ_TYPE_ADVSEARCH, REQ_TYPE_GETCINFO,
		  REQ_TYPE_GETFORMULA
		} ,
		{ "PeakSearch2.cgi",   "PeakSearch2.cgi",     "Disp.cgi",         "GetData.cgi",
		  "GetData2.cgi",      "Search.cgi",          "GetName.cgi",      "GetPeakById.cgi",
		  "QstarTable.cgi",    "GetSon.cgi",          "Disp.cgi",         "QuickSearch.cgi",
		  "PeakSearch2.cgi",   "PeakSearch2.cgi",     "ExistRecord.cgi",  "IndexCount.cgi",
		  "RecordIndex.cgi",   "GetInstInfo.cgi",
		  "MolfileAPI.cgi",    "GetSpectrumData.cgi", "GetData3.cgi",
		  "GetRecordList.cgi", "StructureSearch.cgi", "PeakSearchAdv.cgi", "GetRecordInfo.cgi",
		  "GetMolfile.cgi",    "GetStructure.cgi",    "AdvancedSearch.cgi", "GetCompoudInfo.cgi",
		  "GetFormula.cgi"
		}
	};

	
	/**
	 * サーブレットMultiDispatcherを実行する
	 * @param serverUrl 	ベースURL
	 * @param type		リクエスト種別
	 * @param param	URLパラメータ（リクエスト種別を含まない）
	 * @return
	 * @deprecated 非推奨メソッド
	 * @see execDispatcher(String serverUrl, String type, String param, boolean isMulti, String siteNo)
	 */
	public ArrayList<String> execMultiDispatcher( String serverUrl, String type, String param ) {
		return execDispatcher( serverUrl, type, param, true, null );
	}
	
	
	/**
	 * サーブレットMultiDispatcher または、Dispatcher.jspを実行する
	 * @param serverUrl		サーバーURL
	 * @param type			リクエスト種別
	 * @param param			URLパラメータ（リクエスト種別を含まない）
	 * @param isMulti		マルチフラグ
	 * @param siteNo		サイトNo.
	 * @return 
	 */
	public ArrayList<String> execDispatcher(
			String serverUrl,
			String type,
			String param,
			boolean isMulti,
			String siteNo ) {
		
		String reqUrl = "";
		int site = 0;
		if ( isMulti ) {
			reqUrl = serverUrl + MULTI_DISPATCHER_NAME;
		}
		else {
			reqUrl = serverUrl + "jsp/" + DISPATCHER_NAME;
			site = Integer.parseInt(siteNo);
		}
		
		// URLパラメータ生成
		String reqParam = "type=" + type;
		if ( !param.equals("") ) {
			reqParam += "&" + param;
		}
		ArrayList<String> result = new ArrayList<String>();
		try {
			URL url = new URL( reqUrl );
			URLConnection con = url.openConnection();
			if ( !reqParam.equals("") ) {
				con.setDoOutput(true);
				PrintStream psm = new PrintStream( con.getOutputStream() );
				psm.print( reqParam );
			}
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			boolean isStartSpace = true;
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				// 先頭スペースを読み飛ばすため
				if ( isStartSpace ) {
					if ( line.equals("") ) {
						continue;
					}
					else {
						isStartSpace = false;
					}
				}
				if ( !line.equals("") ) {
					if ( isMulti ) {
						result.add(line);
					}
					else {
						result.add(line + "\t" + Integer.toString(site) );
					}
				}
			}
			in.close();
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return result;
	 }
	

	/**
	 * サーブレットMultiDispatcher または、Dispatcher.jspを実行する（検索結果ページ表示用）
	 * @param serverUrl		サーバーURL
	 * @param type			リクエスト種別
	 * @param reqParam		URLパラメータ（リクエスト種別を含まない）
	 * @param isMulti		マルチフラグ
	 * @param siteNo		サイトNo.
	 * @param conf			設定ファイル情報オブジェクト
	 * @return レコード情報リスト
	 */
	public ResultList execDispatcherResult(
			String serverUrl,
			String type,
			String reqParam,
			boolean isMulti,
			String siteNo,
			GetConfig conf ) {
		
		String reqUrl = "";
		int site = 0;
		if ( isMulti ) {
			reqUrl = serverUrl + MULTI_DISPATCHER_NAME;
		}
		else {
			reqUrl = serverUrl + "jsp/" + DISPATCHER_NAME;
			site = Integer.parseInt(siteNo);
		}
		
		// 結果取得
		ArrayList<String> allLine = new ArrayList<String>();
		try {
			URL url = new URL( reqUrl );
			URLConnection con = url.openConnection();
			if ( !reqParam.equals("") ) {
				con.setDoOutput(true);
				PrintStream psm = new PrintStream( con.getOutputStream() );
				psm.print( reqParam );
			}

			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			boolean isStartSpace = true;
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				// 先頭スペースを読み飛ばすため
				if ( isStartSpace ) {
					if ( line.equals("") ) {
						continue;
					}
					else {
						isStartSpace = false;
					}
				}
				
				if ( !line.equals("") ) {
					if ( isMulti ) {
						allLine.add(line);
					}
					else {
						allLine.add(line + "\t" + Integer.toString(site) );
					}
				}
			}
			in.close();
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		
		// 結果情報レコード生成
		ResultList list = new ResultList(conf);
		ResultRecord record;
		int nodeGroup = -1;
		HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
		String[] fields;
		for (int i=0; i<allLine.size(); i++) {
			fields = allLine.get(i).split("\t");
			
			record = new ResultRecord();
			record.setInfo(fields[0]);
			record.setId(fields[1]);
			record.setIon(fields[2]);
			record.setFormula(fields[3]);
			record.setEmass(fields[4]);
			record.setContributor(fields[fields.length-1]);
			// ノードグループ設定
			if (!nodeCount.containsKey(record.getName())) {
				nodeGroup++;
				nodeCount.put(record.getName(), nodeGroup);
				record.setNodeGroup(nodeGroup);
			}
			else {
				record.setNodeGroup(nodeCount.get(record.getName()));
			}
			list.addRecord(record);
		}
		
		// ソートキー取得
		String sortKey = ResultList.SORT_KEY_NAME;
		if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_FORMULA) != -1) {
			sortKey = ResultList.SORT_KEY_FORMULA;
		}
		else if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_EMASS) != -1) {
			sortKey = ResultList.SORT_KEY_EMASS;
		}
		else if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_ID) != -1) {
			sortKey = ResultList.SORT_KEY_ID;
		}
		
		// ソートアクション取得
		int sortAction = ResultList.SORT_ACTION_ASC;
		if (reqParam.indexOf("sortAction=" + ResultList.SORT_ACTION_DESC) != -1) {
			sortAction = ResultList.SORT_ACTION_DESC;
		}
		
		// レコードソート
		list.sortList(sortKey, sortAction);
		
		
		return list;
	 }
}
