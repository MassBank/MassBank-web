/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
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
 * バッチ検索サービス
 *
 * ver 1.0.7 2012.09.07
 *
 ******************************************************************************/
package massbank;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BatchService extends HttpServlet {
	public static final int MAX_NUM_JOB = 5;
	public int cnt = 0;
	private JobMonitor mon = null;

	/**
	 * サービス初期処理を行う
	 */
	public void init() throws ServletException {
		try {
			if (InetAddress.getLocalHost().getHostName().equals("sv21")) { 
				return;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// 定期的にジョブを監視
		mon = new JobMonitor();
		this.mon.start();
	}

	public void service(HttpServletRequest req, HttpServletResponse res) {
	}

	/**
	 * サービス終了処理を行う
	 */
	public void destroy() {
		// 終了状態セット
		this.mon.setTerminate();

		// JobMonitorスレッドに割り込む
		this.mon.interrupt();

		// JobMonitorスレッドが終了するのを待つ
		do {
			try { this.mon.join(200); }
			catch ( Exception e ) {}
		} while ( this.mon.isAlive() );
	}

	/**
	 * ジョブ監視クラス
	 */
	public class JobMonitor extends Thread {
		private boolean isTerminated = false;
		private LinkedList thList = new LinkedList();

		public JobMonitor() {
		}

		/**
		 * スレッドを起動する
		 */
		public void run() {
			try { sleep(5000); }
			catch (Exception e) {}

			JobManager jobMgr = new JobManager();
			try {
				// アクティブ状態になっているジョブを未実行状態にする
				jobMgr.setInitStatus();
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}

			do {
				// 待機
				try { sleep(10000); }
				catch (Exception e) {}

				// BatchJobWorkerスレッド終了チェック
				int size = this.thList.size();
				int n = 0;
				for ( int i = 0; i < size; i++ ) {
					BatchSearchWorker thRunning = (BatchSearchWorker)this.thList.get(n);
					if ( !thRunning.isAlive() ) {
						this.thList.remove(n);
					}
					else {
						n++;
					}
				}

				// BatchSearchWorkerスレッドを終了させる
				if ( isTerminated ) {
					for ( int i = 0; i < this.thList.size(); i++ ) {
						BatchSearchWorker thRunning = (BatchSearchWorker)this.thList.get(i);
						thRunning.setTerminate();
					}
					break;
				}

				// 未実行ジョブのリストを取得
				try {
					ArrayList<JobInfo> jobList = jobMgr.getWaitJobList();
					if ( jobList == null ) {
						continue;
					}
					int numWait = jobList.size();
					if ( numWait == 0 ) {
						continue;
					}

					// 同時に実行できるジョブ数を制限
					int numRun = jobMgr.getNumRunJob();
					int numExec = numWait;
					if ( numWait > MAX_NUM_JOB - numRun ) {
						numExec = MAX_NUM_JOB - numRun;
					}
					// ジョブ実行
					BatchSearchWorker[] thread = new BatchSearchWorker[numExec];
					for ( int i = 0; i < numExec; i++ ) {
						JobInfo jobInfo = jobList.get(i);
						String jobId = jobInfo.getJobId();
						thread[i] = new BatchSearchWorker(jobInfo);
						thread[i].start();
						this.thList.add(thread[i]);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} while(true);

			jobMgr.end();
		}

		/**
		 * 終了フラグをセットする
		 */
		public void setTerminate() {
			isTerminated = true;
		}
	}
}
