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
 * 連携サーバを監視する常駐サーブレット
 *
 * ver 1.0.2 2012.11.01
 *
 ******************************************************************************/
package massbank.pserver;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import massbank.CallCgi;
import massbank.ServerStatus;
import massbank.ServerStatusInfo;

public class ServerMonitor extends HttpServlet {
	public ServletContext context = null;
	public ServerStatus svrStatus = null;
	private ServerPolling poll = null;

	/**
	 * 初期処理を行う
	 */
	public void init() throws ServletException {
		this.context = getServletContext();

		// 監視開始
		managed();
	}

	/**
	 * HTTPリクエスト処理
	 */
	public void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {

		String action = "";
		if ( req.getParameter("act") == null ) {
			return;
		}
		action = req.getParameter("act");

		// 監視開始
		if ( action.equals("Managed") ) {
			managed();
		}
		// 監視停止
		else if ( action.equals("Unmanaged") ) {
			unmanaged();
		}

		PrintWriter out = res.getWriter();
		out.println("OK");
		out.flush();
	}

	/**
	 * 終了処理を行う
	 */
	public void destroy() {
		unmanaged();
	}

	/**
	 * 監視を開始する
	 */
	private void managed() {
		Logger.global.info( "managed start" );

		// スレッドが生存している場合
		if ( this.poll != null && this.poll.isAlive() ) {
			// 終了フラグが無効の場合
			if ( !poll.isTerminated() ) {
				return;
			}

			// スレッド終了中であれば、終了するのを待つ
			do {
				try {
					poll.join(200);
				}
				catch ( Exception e ) {
				}
			} while ( poll.isAlive() );
		}

		// 状態を「監視」にセットする
		this.svrStatus = new ServerStatus();
		svrStatus.setManaged(true);

		// 連携サーバをポーリングするスレッドを起動
		this.poll = new ServerPolling();
		this.poll.start();
	}

	/**
	 * 監視を停止する
	 */
	private void unmanaged() {
		Logger.global.info( "unmanaged start" );

		// ServerPollingスレッドが生存していない場合は何もせず
		if ( !poll.isAlive() ) {
			return;
		}

		// ServerPollingスレッドに割り込む
		poll.interrupt();

		// 終了フラグを有効にする
		poll.setTerminate(true);

		// ServerPollingスレッドが終了するのを待つ
		do {
			try {
				poll.join(200);
			}
			catch ( Exception e ) {
			}
		} while ( poll.isAlive() );

		// 終了フラグを無効する
		poll.setTerminate(false);

		// 状態を「非監視」にセットする
		svrStatus.setManaged(false);
		this.svrStatus = null;

		Logger.global.info( "unmanaged end" );
	}

	/**
	 * 連携サーバをポーリングするスレッド
	 */
	class ServerPolling extends Thread {
		// タイムアウト時間
		private static final int TIMEOUT_SEC = 15;
		// 終了フラグ
		private boolean isTerminated = false;

		/**
		 * コンストラクタ
		 */
		public ServerPolling() {
		}

		/**
		 * スレッド開始
		 */
		public void run() {
			// ポーリング周期を取得
			int pollInterval = svrStatus.getPollInterval();
			Logger.global.info( "polling start" );

			try { sleep( 2000 ); }
			catch (InterruptedException ex) { ex = null; }

			//-----------------------------------------------------------------
			// ポーリングする
			//-----------------------------------------------------------------
			do {
				// 終了フラグが有効であればスレッドを終了する
				if ( isTerminated() ) {
					break;
				}

				// 管理ファイルの整合
				svrStatus.clean();

				// 監視対象のサーバがない場合は終了する
				if ( svrStatus.getServerNum() == 0 ) {
					Logger.global.info( "ServerNum=0" );
					svrStatus.setManaged(false);
					return;
				}

				// CGIのURLとパラメータのリストをセット
				ServerStatusInfo[] info = svrStatus.getStatusInfo();
				int num = info.length;
				String[] urls = new String[num];
				Hashtable[] params = new Hashtable[num];
				boolean[] isActive = new boolean[num];
				for ( int i = 0; i < num; i++ ) {
					// URLをセット
					urls[i] = info[i].getUrl() + "cgi-bin/ServerCheck.cgi";
					// DB名をセット
					params[i] = new Hashtable();
					params[i].put( "dsn", info[i].getDbName() );
					// ステータスをセット
					isActive[i] = info[i].getStatus();
				}

				// 各連携サーバ上でチェックプログラムを実行する
				CallCgi[] thread = new CallCgi[num];
				for ( int i = 0; i < num; i++ ) {
					thread[i] = new CallCgi( urls[i], params[i], TIMEOUT_SEC, context );
					thread[i].start();
				}

				// CallCgiスレッド終了待ち
				long until = System.currentTimeMillis() + TIMEOUT_SEC * 1000;
				boolean isRunning = true;
				while ( isRunning && System.currentTimeMillis() < until ) {
					isRunning = false;
					for ( int i = 0; i < num; i++ ) {
						try {
							if ( thread[i].isAlive() ) {
								// スレッドが終了するまで待機
								thread[i].join(200);
								isRunning = true;
							}
						}
						catch ( Exception e ) {
							// エラー
							Logger.global.severe( e.toString() );
						}
					}
				}

				// 結果取得
				boolean isUpate = false;
				for ( int i = 0; i < num; i++ ) {
					String res = thread[i].result;
					boolean isOK = false;
					if ( res.equals("OK") ) {
						isOK = true;
					}

					// 状態変化があれば変更する
					if ( isOK != isActive[i] ) {
//						String state = "";
//						String subject = "";
//						String contents = "";
						svrStatus.setStatus( i, isOK );
						isActive[i] = isOK;
						isUpate = true;

						// アラートメール送信
//						if ( isOK ) {
//							state = "Server recovery";
//						}
//						else {
//							state = "Server failed  ";
//						}
//						subject = state + " (" + info[i].getServerName() + ")";
//						contents = state + "  : " + info[i].getServerName() + "\n\n";
//						contents += "CGI timeout      : " + TIMEOUT_SEC + " sec.\n";
//						contents += "Polling interval : " + pollInterval + " sec.\n";
//						SendMail.send( subject, contents );
					}
				}

				// 更新があれば保存する
				if ( isUpate ) {
					svrStatus.store();
				}

				// ポーリング周期の時間待機する
				try { sleep( pollInterval * 1000 ); }
				catch (InterruptedException ex) { ex = null; }

			} while(true);
		}

		/**
		 * 終了フラグをセットする
	   * @param enable  true:有効 / false:無効
		 */
		public void setTerminate(boolean enable) {
			this.isTerminated = enable;
		}

		/**
		 * 終了フラグが有効か無効か
	   * @return true:有効 / false:無効
		 */
		public boolean isTerminated() {
			return this.isTerminated;
		}
	}
}
