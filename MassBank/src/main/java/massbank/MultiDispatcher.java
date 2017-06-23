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
 * CGIをマルチスレッドで起動するサーブレット
 *
 * ver 1.0.16 2012.10.10
 *
 ******************************************************************************/
package massbank;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import massbank.svn.SVNUtils;

@SuppressWarnings("serial")
public class MultiDispatcher extends HttpServlet {
	
	private final String PROG_NAME = MassBankCommon.MULTI_DISPATCHER_NAME;

	/**
	 * HTTPリクエスト処理
	 */
	public void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {

		ServletContext context = getServletContext();
		PrintWriter out = res.getWriter();
		String msg = "";

		//---------------------------------------------------
		// ベースURLセット
		// set base URL
		//---------------------------------------------------
		String path = req.getRequestURL().toString();
		int pos = path.indexOf( MassBankCommon.MULTI_DISPATCHER_NAME );
		String baseUrl = path.substring( 0, pos );

		//---------------------------------------------------
		// 環境設定ファイル情報取得
		// fetch environment setting from file
		//---------------------------------------------------
		GetConfig conf = new GetConfig(baseUrl);
		boolean isTrace = conf.isTraceEnable();

		//---------------------------------------------------
		// リクエストパラメータ取得
		// Request parameter acquisition
		//---------------------------------------------------
		String type = "";
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		Enumeration names = req.getParameterNames();
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			String val = req.getParameter( key );
			if ( key.equals("type") ) {
				params.put( key, val );
				type = val;
			}
			else if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
				// キーがInstrumentType,MSType以外の場合はStringパラメータ
				params.put( key, val );
			}
			else {
				// キーがInstrumentType,MSTypeの場合はString配列パラメータ
				String[] vals = req.getParameterValues( key );
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
			msg = "パラメータtype不正 " + type;
			MassBankLog.ErrorLog( PROG_NAME, msg, context );
			return;
		}

		//---------------------------------------------------
		// サーバ・ステータス情報取得
		// (サーバ監視が行われていなければ未処理で返ってくる)
		//---------------------------------------------------
		ServerStatus svrStatus = new ServerStatus(baseUrl);
		
		//---------------------------------------------------
		// ディスパッチする
		//---------------------------------------------------
		Dispatcher disp = new Dispatcher(context, conf, isTrace, svrStatus, typeNum, params);
		disp.dispatch();

		//---------------------------------------------------
		// 結果取得
		//---------------------------------------------------
		if ( typeNum == MassBankCommon.CGI_TBL_TYPE_SEARCH ) {
			out.println( disp.getSortedResult() );
		}
		else {
			out.println( disp.getResult() );
		}
		out.close();

		msg = "終了";
		MassBankLog.TraceLog( PROG_NAME, msg, context, isTrace );
	}


	/**
	 * 連携サーバにディスパッチするためのクラス
	 */
	class Dispatcher {
		
		private ServletContext context = null;
		private List<RequestInfo> reqInfoList = null;
		private CallCgi[] thread = null;
		private int typeNum = -1;
		private ServerStatus svrStatus = null;
		private boolean isTrace = false;
		private int timeout = 0;
		private String[] urlList = null;
		private String[] dbNameList = null;
		private Hashtable<String, Object> params = null;
		private String frontServerUrl = "";

		/**
		 * コンストラクタ
		 * @param context
		 * @param conf
		 * @param isTrace
		 * @param svrStatus
		 * @param typeNum
		 * @param params
		 */
		public Dispatcher(ServletContext context, GetConfig conf, boolean isTrace, 
				ServerStatus svrStatus, int typeNum, Hashtable<String, Object> params) {
			
			this.context = context;
			this.isTrace = isTrace;
			this.timeout = conf.getTimeout();
			this.frontServerUrl = conf.getServerUrl();
			this.urlList = conf.getSiteUrl();
			this.dbNameList = conf.getDbName();
			this.svrStatus = svrStatus;
			this.typeNum = typeNum;
			this.params = params;
		}
		
		/**
		 * ディスパッチする
		 */
		public void dispatch() {
			final int MYSVR_INFO_NUM = 0;

			//---------------------------------------------------
			// CGI用とJSP用のパラメータをセット
			//---------------------------------------------------
			Hashtable<String, Object> cgiParams = null;
			Hashtable<String, Object> jspParams = null;
			if ( params == null ) {
				cgiParams = new Hashtable<String, Object>();
				jspParams = new Hashtable<String, Object>();
			}
			else {
				cgiParams = new Hashtable<String, Object>(params);
				jspParams = new Hashtable<String, Object>(params);
			}
			// Peak Search, Peak Diff Search以外の場合、typeキーを削除する
			if ( typeNum != MassBankCommon.CGI_TBL_TYPE_PEAK
			  && typeNum != MassBankCommon.CGI_TBL_TYPE_PDIFF ) {
				cgiParams.remove( "type" );
			}
			
			//---------------------------------------------------
			// URLとパラメータをセット
			//---------------------------------------------------
			reqInfoList = new ArrayList<RequestInfo>();
			for ( int i = 0; i < urlList.length; i++ ) {
				String url = urlList[i];
				String dbName = dbNameList[i];

				// 連携サーバがアクティブではない場合はスキップする
				// サーバ監視が行われていなければ無条件にTrueが返ってくる
				if ( !svrStatus.isServerActive(url, dbName) ) {
					String backupDbName = svrStatus.get2ndDbName(url, dbName);
					if ( backupDbName.equals("") || !SVNUtils.checkDBExists(backupDbName) ) {
						continue;
					}
					else {
						url = frontServerUrl;
						dbName = backupDbName;
						System.out.println(url + ":" + dbName);
					}
				}

				String reqUrl = url;
				Hashtable<String, Object> reqParams = null;

				//** 自サーバーの場合、cgiへアクセス **
				if ( i == MYSVR_INFO_NUM ) {
					reqUrl += "cgi-bin/" + MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_FILE][typeNum];
					reqParams = new Hashtable<String, Object>(cgiParams);
				}
				//** 連携サーバーの場合、Dispatcher.jspを介してアクセス **
				else {
					reqUrl += "jsp/" + MassBankCommon.DISPATCHER_NAME;
					reqParams = new Hashtable<String, Object>(jspParams);
				}
				reqParams.put( "dsn", dbName );

				// データクラスにURL, パラメータ, siteNoをセット
				RequestInfo reqInfo = new RequestInfo( reqUrl, reqParams, i );
				reqInfoList.add(reqInfo);
			}
			
			//---------------------------------------------------
			// スレッド起動
			//---------------------------------------------------
			this.thread = new CallCgi[this.reqInfoList.size()];
			for ( int i = 0; i < this.reqInfoList.size(); i++ ) {
				RequestInfo reqInfo = this.reqInfoList.get(i);
				String reqUrl = reqInfo.getUrl();
				Hashtable<String, Object> reqParams = reqInfo.getParam();

				// ログ出力
				String param = "";
				for ( Enumeration<String> keys = reqParams.keys(); keys.hasMoreElements(); ) {
					String key = (String)keys.nextElement();
					if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
						// キーがInstrumentType,MSType以外の場合はStringパラメータ
						String val = (String)reqParams.get(key);
						param += key + "=" + val + "&";
					}
					else {
						// キーがInstrumentType,MSTypeの場合はString配列パラメータ
						String[] vals = (String[])reqParams.get(key);
						for ( int j = 0; j < vals.length; j++ ) {
							param += key + "=" + vals[j] + "&";
						}
					}
				}
				param = param.substring( 0, param.length()-1 );
				String msg = "Call(" + Integer.toString(i+1) + ") : " + reqUrl + "?" + param;
				MassBankLog.TraceLog( PROG_NAME, msg, context, isTrace );
				
				// 起動
				this.thread[i] = new CallCgi( reqUrl, reqParams, timeout, context );
				this.thread[i].start();
			}

			//-------------------------------------------
			// スレッド終了待ち
			//-------------------------------------------
			// HttpClient側でタイムアウトを設定しているので無限でもよい
			long until = System.currentTimeMillis() + timeout * 1000;
			boolean isRunning = true;
			while ( isRunning && System.currentTimeMillis() < until ) {
				isRunning = false;
				for ( int i = 0; i < this.thread.length; i++ ) {
					try {
						if ( this.thread[i].isAlive() ) {
							//** スレッド終了するまで待機 **
							this.thread[i].join(500);
							isRunning = true;
						}
					}
					catch ( Exception e ) {
						// エラー
						String msg = e.toString();
						MassBankLog.ErrorLog( PROG_NAME, msg, context );
					}
				}
			}
		}

		/**
		 * スコア順でソートされた結果を取得する
		 * @return 結果
		 */
		private String getSortedResult() {
			//-------------------------------------------
			// 実行結果をリストに格納
			// store result from execution as list
			//-------------------------------------------
			StringBuffer res = new StringBuffer("");
			ArrayList<String> result = new ArrayList<String>();
			ArrayList<String> scoreList = new ArrayList<String>();
			for ( int i = 0; i < this.thread.length; i++ ) {
				if ( this.thread[i].result.length() == 0 ) {
					continue;
				}
				String[] lines = this.thread[i].result.replaceAll("\r","").split("\n");
				for ( int j = 0; j < lines.length; j++ ) {
					// Internal Server Errorの場合
					if ( lines[j].indexOf("<!") >= 0 ) {
						break;
					}

					// Site No 付加
					if ( lines[j].split("\t").length > 1 ) {
						result.add( lines[j] + "\t" + this.reqInfoList.get(i).getSiteNo() );
					}
				}
			}

			//-------------------------------------------
			// スコアリストを作成
			// create score list
			//-------------------------------------------
			String line = "";
			String[] item;
			for ( int i = 0; i < result.size(); i++ ){
				line = (String) result.get(i);
				item = line.split("\t");
				int pos1 = item[2].indexOf(".");
				String score = "";
				if ( pos1 > 0 ) { 
					score = "0" + item[2].substring(pos1);
				}
				else {
					score = "0";
				}
				scoreList.add( score + "\t" + Integer.toString(i) );
			}
			//-------------------------------------------
			// スコアリストをソート
			// sort score list
			//-------------------------------------------
			Collections.sort(scoreList, new ScoreComparator());

			//-------------------------------------------
			// スコア順で結果を返す
			// return results in score-order
			//-------------------------------------------
			for ( int i = 0; i < result.size(); i++ ) {
				line = (String)scoreList.get(i);
				item = line.split("\t");
				int no = Integer.parseInt(item[1]);
				line = (String)result.get(no);
				item = line.split("\t");
				String id    = item[0];
				String name  = item[1];
				String score = item[2];
				String ion   = item[3];
				// Quick Search by Peakの結果の場合
				if ( item.length == 6 ) {
					String formula = item[4];
					String site = item[5];
					res.append( name + "\t" + id + "\t" + ion + "\t" + formula + "\t" + score + "\t" + site + "\n" );
				}
				// APIの場合
				else if ( item.length == 7 ) {
					String formula = item[4];
					String emass = item[5];
					String site = item[6];
					res.append( id + "\t" + name + "\t" + formula + "\t" + emass + "\t" + score + "\t" + site + "\n" );
				}
				// Nist Searchの結果の場合
				else {
					String site = item[4];
					res.append( id + "\t" + name + "\t" + score + "\t" + ion + "\t" + site + "\n" );
				}
			}
			return res.toString();
		}

		/**
		 * 通常のレスポンス
		 * normal (unsorted) response
		 * @return 結果 the result
		 */
		private String getResult() {
			StringBuffer res = new StringBuffer("");
			for ( int i = 0; i < this.thread.length; i++ ) {
				if ( this.thread[i].result.length() == 0 ) {
					continue;
				}
				String[] lines = this.thread[i].result.replaceAll("\r","").split("\n");
				for ( int j = 0; j < lines.length; j++ ) {
					// Internal Server Errorの場合
					if ( lines[j].indexOf("<!") >= 0 ) {
						break;
					}
					// Site No 付加
					res.append( lines[j] + "\t" + this.reqInfoList.get(i).getSiteNo() + "\n" );
				}
			}
			return res.toString();
		}
	}

	/**
	 * リクエスト情報データクラス
	 */
	class RequestInfo {
		private String url = "";
		private Hashtable<String, Object> param = null;
		private int siteNo = 0;

		/**
		 * コンストラクタ
		 */
		public RequestInfo(String url, Hashtable<String, Object> param, int siteNo) {
			this.url = url;
			this.param = param;
			this.siteNo = siteNo;
		}
		/**
		 * URL取得
		 */
		public String getUrl() {
			return this.url;
		}
		/**
		 * パラメータ取得
		 */
		public Hashtable<String, Object> getParam() {
			return this.param;
		}
		/**
		 * サイトNo取得
		 */
		public String getSiteNo() {
			return String.valueOf(this.siteNo);
		}
	}

	/**
	 * Score順でソートするためのComparator
	 */
	public class ScoreComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2){
			String[] val1 = ((String) o1).split("\t");
			String[] val2 = ((String) o2).split("\t");
			int ret = 0;
			if ( Float.parseFloat(val1[0]) < Float.parseFloat(val2[0]) ) {
				ret = 1;
			}
			else if ( Float.parseFloat(val1[0]) > Float.parseFloat(val2[0]) ) {
				ret = -1;
			}
			return ret;
		}
	}
}
