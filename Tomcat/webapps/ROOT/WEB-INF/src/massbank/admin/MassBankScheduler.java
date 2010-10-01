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
 * MassBank用スケジューラ
 *
 * ver 1.0.0 2010.09.30
 *
 ******************************************************************************/
package massbank.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;



/**
 * MassBank用スケジューラクラス
 * 
 * 説明：
 * admin.confに記述したスケジュールタスクを指定された間隔で実行する。
 * タスクはadmin.confに以下のように複数記述できる。
 * 
 *     schedule=[タスク],[初回実行時間],[実行間隔]
 *     schedule=[タスク],[初回実行時間],[実行間隔]
 *        ：（以降複数記述可）
 * 
 * 補足：
 *  [タスク]
 *    定期的に実行したいコマンドを記述
 *  [初回実行時間]
 *    サーブレット起動後にタスクを初回実行する時間を記述（秒指定）
 *  [実行間隔]
 *    前回実行タスク終了後から次回実行タスクを開始する時間の間隔を記述（秒指定）
 *    0を指定した場合はタスクは初回のみ実行する（2回目以降は実行しない）
 *       
 * サーブレット起動後に、指定された初回実行時間で1回目のタスクを実行する。
 * 2回目以降のタスク実行は、前回のタスク実行が終了してから指定された実行間隔の時間が経過後に実行される。
 * admin.confに記述された複数のタスクは、タスクごとに別スレッドで実行される。
 */
public class MassBankScheduler extends HttpServlet {
	
	/** 最大スケジュールスレッドプールサイズ */
	private static final int MAX_THREAD_POOL_SIZE = 15;
	
	/** スケジュールオブジェクト */
	private ScheduledExecutorService sc = null;
	
	/** スケジュールタスク状態オブジェクト */
	private ScheduledFuture<?>[] futures = null;
	
	/**
	 * サーブレット初期化処理
	 */
	public void init() throws ServletException {
		
		String baseUrl = getInitParameter("baseUrl");
		if ( baseUrl == null ) {
			baseUrl = "http://localhost/MassBank/";
		}
		String realPath = this.getServletContext().getRealPath("/");
		AdminCommon admin = new AdminCommon(baseUrl, realPath);
		ArrayList<String> scheduleList = admin.getSchedule();
		
		int threadPoolSize = (( MAX_THREAD_POOL_SIZE > scheduleList.size() ) ? scheduleList.size() : MAX_THREAD_POOL_SIZE);
		sc = Executors.newScheduledThreadPool(threadPoolSize);
		futures = new ScheduledFuture<?>[scheduleList.size()];
		
		for (int i=0; i<scheduleList.size(); i++) {
			String schedule = scheduleList.get(i);
			boolean isFormatError = false;
			boolean isTimeError = false;
			
			// スケジュールフォーマット等チェック
			String taskCmd = "";
			long initial = 0L;
			long delay = 0L;
			try {
				String[] tmp = schedule.split(",");
				if ( tmp.length == 3 ) {
					taskCmd = tmp[0];
					initial = Long.parseLong(tmp[1]);
					delay = Long.parseLong(tmp[2]);
				}
				else {
					isFormatError = true;
				}
				if ( taskCmd.equals("")  ) {
					isFormatError = true;
				}
				if ( initial < 0 || delay < 0 ) {
					isTimeError = true;
				}
			}
			catch (NumberFormatException ne) {
				isTimeError = true;
			}
			
			// エラー出力とスケジュールの実行
			if ( isFormatError && isTimeError ) {
				Logger.getLogger("global").warning( 
						"<<SCHEDULE_" + i + ">> The format and time of the schedule is wrong. [schedule=" + schedule + "]" );
			}
			else if ( isFormatError ) {
				Logger.getLogger("global").warning( 
						"<<SCHEDULE_" + i + ">> The format of the schedule is wrong. [schedule=" + schedule + "]" );
			}
			else if ( isTimeError ) {
				Logger.getLogger("global").warning( 
						"  <<SCHEDULE_" + i + ">> The time of the schedule is wrong. [schedule=" + schedule + "]" );
			}
			else {
				if ( delay != 0L ) {
					// 定期的に実行
					futures[i] = sc.scheduleWithFixedDelay(new TaskExec(i, taskCmd, initial, delay), initial, delay, TimeUnit.SECONDS);
				}
				else {
					// 1回のみ実行
					futures[i] = sc.schedule(new TaskExec(i, taskCmd, initial, delay), initial, TimeUnit.SECONDS);
				}
			}
		}
	}
	
	/**
	 * サーブレット終了処理
	 */
	public void destroy() {
		// スケジュールから全てのタスクを除外し、実行中の全てのタスクを終了
		if ( futures != null ) {
			for (ScheduledFuture<?> future : futures) {
				if (future != null) {
					future.cancel(true);
				}
			}
		}
		// スケジュールの終了
		if ( sc != null ) {
			sc.shutdown();
		}
	}

	/**
	 * タスク実行クラス
	 */
	private class TaskExec implements Runnable {
		private int tskIndex = -1;
		private String taskCmd;
		private long initial;
		private long delay;
		
		/**
		 * コンストラクタ
		 * @param tskIndex タスクインデックス
		 * @param taskCmd タスクコマンド 
		 * @param initial 初回実行時間
		 * @param delay 実行間隔
		 */
		public TaskExec(int tskIndex, String taskCmd, long initial, long delay) {
			this.tskIndex = tskIndex;
			this.taskCmd = taskCmd;
			this.initial = initial;
			this.delay = delay;
		}
		
		public void run() {
			Logger.getLogger("global").info( 
					"  <<SCHEDULE_" + tskIndex + ">> Start schedule task. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
			
			Process p = null;
			boolean isError = false;
			try {
				p = Runtime.getRuntime().exec(taskCmd);
			}
			catch (IOException e) {
				e.printStackTrace();
				isError = true;
			}
			
			// タスクを実行できなかった場合はスケジュールから除外
			if ( p == null || isError ) {
				Logger.getLogger("global").severe( 
						"  <<SCHEDULE_" + tskIndex + ">> The task of failing is excluded from the schedule. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
				futures[tskIndex].cancel(true);
				return;
			}
			
			// タスクの実行に時間がかかる場合は終了まで待つ
			try {
				p.waitFor();
			} catch (InterruptedException ex) {
				// サーブレット終了時に実行中のタスクは強制終了
				Logger.getLogger("global").warning( 
						"  <<SCHEDULE_" + tskIndex + ">> Force-quit a schedule task. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
				p.destroy();
				return;
			}
			
			Logger.getLogger("global").info( 
					"  <<SCHEDULE_" + tskIndex + ">> End schedule task. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
		}
	}
}
