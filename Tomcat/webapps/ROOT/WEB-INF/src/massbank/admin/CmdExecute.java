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
 * コマンド実行クラス
 *
 * ver 1.0.4 2009.11.20
 *
 ******************************************************************************/
package massbank.admin;

import java.lang.InterruptedException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class CmdExecute {
	
	// コマンド実行中フラグ
	private boolean isRunning = false;
	
	// コマンド実行タイムアウトフラグ
	private boolean isTimeout = false;
	
	// タイムアウト時間（ミリ秒単位）
	private long timout = 60000L;

	/**
	 * デフォルトコンストラクタ
	 */
	public CmdExecute() {
	}
	
	/**
	 * コンストラクタ
	 *
	 * @param isLongTimeOut タイムアウト値延長フラグ
	 */
	public CmdExecute(boolean isLongTimeOut) {
		if ( isLongTimeOut ) {
			this.timout = 300000L;			
		}
	}
	
	/**
	 * コマンドを実行
	 *
	 * @param cmdArray コマンド
	 * @return 実行結果
	 */
	public CmdResult exec(final String[] cmd) {
		CmdResult res = new CmdResult();
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			isRunning = true;
			
			// コマンド実行監視スレッド
			WatchDog wd = new WatchDog(process);
			
			// 標準出力、エラー出力のハンドラ
			CmdOutputHandler stdoutHandler =
					new CmdOutputHandler(process.getInputStream());
			CmdOutputHandler stderrHandler =
					new CmdOutputHandler(process.getErrorStream());
			
			// スレッド開始
			stdoutHandler.start();
			stderrHandler.start();
			wd.start();
			
			// プロセス終了待ち、ステータスセット
			res.setStatus(process.waitFor());
			isRunning = false;
			
			// 割り込み処理
			if ( !stdoutHandler.isInterrupted() ) {
				stdoutHandler.interrupt();
			}
			if ( !stderrHandler.isInterrupted() ) {
				stderrHandler.interrupt();
			}
			if ( !wd.isInterrupted() ) {
				wd.interrupt();
			}
			// 標準出力、エラー出力の内容をセット
			res.setStdout(stdoutHandler.getCmdOutput());
			if (isTimeout) {
				res.setStderr(cmd[0] + " command was timeout. (" + timout + "msec.)");
			}
			else {
				res.setStderr(stderrHandler.getCmdOutput());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * コマンド出力ハンドラクラス
	 */
	private final class CmdOutputHandler extends Thread {
		// 格納バッファ
		private ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		// シェルコマンドからの出力を受け取る入力ストリ−ム
		private InputStream in;
		
		/**
		 * デフォルトコンストラクタの無効化
		 */
		private CmdOutputHandler() {
			super();
		}
		
		/**
		 * コンストラクタ
		 * @param argIn シェルコマンドからの出力を受け取るInputStream
		 */
		private CmdOutputHandler(final InputStream argIn) {
			super();
			in = argIn;
		}
		
		/**
		 * コマンドからの出力を取得します
		 * @return
		 */
		public String getCmdOutput() {
			// 念のため最後に一回読み出す
			storeBuf();
			try {
				in.close();
			}
			catch (IOException e) {
				e = null;
			}
			return buf.toString();
		}
		
		/**
		 * コマンドからの出力を読み取ります
		 */
		public void run() {
			// バッファ読み取り間隔(ms)
			final long sleepTime = 100L;
			
			while (isRunning) {
				// バッファの中身を取得する
				storeBuf();
				
				// 一定時間スリープする
				try {
					sleep(sleepTime);
				}
				catch (InterruptedException ignoreEx) {
					storeBuf();
				}
			}
		}
		
		/**
		 * 入力ストリ−ムの内容をバッファに格納します
		 */
		private void storeBuf() {
			try {
				int size = in.available();
				if ( size > 0 ) {
					byte[] cmdout = new byte[size];
					in.read(cmdout);
					buf.write(cmdout);
				}
			}
			catch (IOException ignoreEx) {
				ignoreEx = null;
			}
		}
	}
	
	/**
	 * コマンド実行監視スレッド
	 */
	private final class WatchDog extends Thread {
		// 実行中のプロセス
		private Process process = null;
		
		/**
		 * Creates a new WatchDog object.
		 *
		 * @param param 実行予定のProcess
		 */
		public WatchDog(final Process param) {
			process = param;
		}
		
		/**
		 * デフォルトコンストラクタの無効化
		 */
		@SuppressWarnings("unused")
		private WatchDog() {
			super();
		}
		
		/**
		 * タイムアウト処理
		 */
		public synchronized void run() {
			long until = System.currentTimeMillis() + timout;
			long now = 0;			
			// 一定時間待機する
			while ( isRunning && (until > (now = System.currentTimeMillis())) ) {
				try {
					wait( until - now );
				}
				catch ( InterruptedException ignoreEx ) {
					ignoreEx = null;
				}
			}
			// 一定時間経っても実行中の場合、プロセスを強制終了する
			if ( isRunning ) {
				process.destroy();
				isTimeout = true;
			}
		}
	}
}
