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
 * ジョブ管理クラス
 *
 * ver 1.0.2 2013.10.28
 *
 ******************************************************************************/
package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

public class JobManager {
	private static final String DB_NAME = "MassBank_General";
	private static final String TABLE_NAME = "JOB_INFO";
	public static final String STATE_INVALID  = "Invalid query";
	public static final String STATE_WAIT     = "Waiting";
	public static final String STATE_RUN      = "Running";
	public static final String STATE_COMPLETE = "Completed";

	private JobInfo jobInfo = null;
	private Connection con;
	private Statement stmt;
	private String connectUrl;

	/**
	 * コンストラクタ
	 */
	public JobManager() {
		String dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
		if ( !MassBankEnv.get(MassBankEnv.KEY_DB_MASTER_NAME).equals("") ) {
			dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_MASTER_NAME);
		}
		this.connectUrl = "jdbc:mysql://" + dbHostName + "/" + DB_NAME;
		// DB接続
		connectDB();
	}

	/**
	 * ジョブを追加する
	 */
	public String addJobInfo(JobInfo newJobInfo) throws SQLException {
		String jobId = UUID.randomUUID().toString();
		newJobInfo.setJobId(jobId);
		newJobInfo.setStatus( STATE_WAIT );
		this.jobInfo = newJobInfo;
		insertDB();
		return jobId;
	}

	/**
	 * 指定したジョブIDの情報を取得する
	 */
	public JobInfo getJobInfo(String jobId) throws SQLException {
		JobInfo retJobInfo = null;
		String sql = "select * from " + TABLE_NAME + " where JOB_ID='" + jobId + "'";
		ResultSet rs = execQuery(sql);
		if ( rs.first() ) {
			retJobInfo = new JobInfo();
			retJobInfo.setJobId(jobId);
			retJobInfo.setStatus( rs.getString("STATUS") );
			retJobInfo.setTimeStamp( rs.getString("TIME_STAMP") );
			retJobInfo.setSessionId( rs.getString("SESSION_ID") );
			retJobInfo.setIpAddr( rs.getString("IP_ADDR") );
			retJobInfo.setMailAddr( rs.getString("MAIL_ADDR") );
			retJobInfo.setQueryFileName( rs.getString("QUERY_FILE_NAME") );
			retJobInfo.setQueryFileSize( rs.getString("QUERY_FILE_SIZE") );
			retJobInfo.setTempName( rs.getString("TEMP_FILE_NAME") );
			retJobInfo.setSearchParam( rs.getString("SEARCH_PARAM") );
			retJobInfo.setResult( rs.getString("RESULT") );
		}
		rs.close();
		return retJobInfo;
	}

	/**
	  * ジョブの状態を初期化する
	  */
	public void setInitStatus() throws SQLException {
		String sql = "update " + TABLE_NAME + " set STATUS='" + STATE_WAIT + "' where STATUS='" + STATE_RUN + "'";
		execUpdate(sql);
	}

	/**
	  * 指定されたジョブの状態を"Invalid"にする
	  */
	public void setInvalid(String jobId) throws SQLException {
		updateStatus(jobId, STATE_INVALID);
	}

	/**
	  * 指定されたジョブの状態を"Rinning"にする
	  */
	public void setRunning(String jobId) throws SQLException {
		updateStatus(jobId, STATE_RUN);
	}

	/**
	  * 指定されたジョブの状態を"Completed"にする
	  */
	public void setCompleted(String jobId) throws SQLException {
		updateStatus(jobId, STATE_COMPLETE);
	}

	/**
	 * 検索結果をセットする
	 */
	public void setResult(String jobId, String resultFilePath) throws IOException, SQLException {
		File file = new File(resultFilePath);
		BufferedReader in = new BufferedReader( new FileReader(file) );
		String line = "";
		StringBuffer result = new StringBuffer();
		while ( ( line = in.readLine() ) != null ) {
			result.append(line + "\n");
		}
		in.close();

		String sql = "update " + TABLE_NAME + " set RESULT=? where JOB_ID='" + jobId + "'";
		for ( int i = 0; i < 2; i++ ) {
			try {
				PreparedStatement pstmt = con.prepareStatement(sql);
				pstmt.setString(1, (String)result.toString());
				pstmt.executeUpdate();
				con.commit();
				break;
			}
			catch ( SQLException e) {
				// DB切断時に再接続する
				String state = e.getSQLState();
				// 08003 : Connection not open
				// 08S01 : Communication link failure
				if ( state.equals("08003") || state.equals("08S01") ) {
					connectDB();
				}
				else {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	  * 未実行ジョブのリストを取得する
	  */
	public ArrayList<JobInfo> getWaitJobList() throws SQLException {
		String sql = "select * from " + TABLE_NAME + " where STATUS='" + STATE_WAIT + "' order by TIME_STAMP";
		ResultSet rs = execQuery(sql);
		ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
		while( rs.next() ){
			JobInfo jobInfo = new JobInfo();
			jobInfo.setJobId( rs.getString("JOB_ID") );
			jobInfo.setStatus( rs.getString("STATUS") );
			jobInfo.setTimeStamp( rs.getString("TIME_STAMP") );
			jobInfo.setSessionId( rs.getString("SESSION_ID") );
			jobInfo.setIpAddr( rs.getString("IP_ADDR") );
			jobInfo.setMailAddr( rs.getString("MAIL_ADDR") );
			jobInfo.setQueryFileName( rs.getString("QUERY_FILE_NAME") );
			jobInfo.setQueryFileSize( rs.getString("QUERY_FILE_SIZE") );
			jobInfo.setTempName( rs.getString("TEMP_FILE_NAME") );
			jobInfo.setSearchParam( rs.getString("SEARCH_PARAM") );
			jobInfo.setResult( rs.getString("RESULT") );
			jobList.add(jobInfo);
		}
		rs.close();
		return jobList;
	}

	/**
	  * 実行中のジョブ数を取得する
	  */
	public int getNumRunJob() throws SQLException  {
		String sql = "select count(JOB_ID) from " + TABLE_NAME + " where STATUS='" + STATE_RUN + "'";
		ResultSet rs = execQuery(sql);
		rs.first();
		int num = rs.getInt(1);
		rs.close();
		return num;
	}

	/**
	  * 重複ジョブエントリを有無をチェックする
	  */
	public boolean checkDuplicateEntry(JobInfo newJobInfo) throws SQLException {
		ArrayList<JobInfo> entryList = getWaitJobList();
		if ( entryList == null ) {
			return true;
		}
		int cnt = 0;
		for ( int i = 0; i < entryList.size(); i++ ) {
			JobInfo jobInfo = (JobInfo)entryList.get(i);
			String sessionId = jobInfo.getSessionId();
			String timeStamp = jobInfo.getTimeStamp();
			String ipAddress = jobInfo.getIpAddr();
			String fileName  = jobInfo.getQueryFileName();
			String fileSize  = jobInfo.getQueryFileSize();
			String sParam    = jobInfo.getSearchParam();

			// 同一セッションでファイル名,ファイルサイズが同じ
			if ( sessionId.equals(newJobInfo.getSessionId())
			  && fileName.equals(newJobInfo.getQueryFileName())
			  && fileSize.equals(newJobInfo.getQueryFileSize())
			  && sParam.equals(newJobInfo.getSearchParam())    ) {
				return false;
			}

		// 同一IPアドレスで既に実行されているジョブがあるか
//			if ( ipAddress.equals(newJobInfo.getIpAddr()) ) {
//				if ( ++cnt >= 3 ) {
//					return false;
//				}
//			}
		}
		return true;
	}

	/**
	 * 終了処理
	 */
	public void end() {
		try {
			if ( stmt != null ) stmt.close();
			if ( con != null ) con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ジョブ情報をDBに書き込む
	 */
	private void insertDB() throws SQLException {
		String vals = "";
		vals += "'" + jobInfo.getJobId() + "',";
		vals += "'" + jobInfo.getStatus() + "',";
		vals += "'" + jobInfo.getTimeStamp() + "',";
		vals += "'" + jobInfo.getSessionId() + "',";
		vals += "'" + jobInfo.getIpAddr() + "',";
		vals += "'" + jobInfo.getMailAddr() + "',";
		vals += "'" + jobInfo.getQueryFileName() + "',";
		vals += jobInfo.getQueryFileSize() + ",";
		vals += "'" + jobInfo.getTempName() + "',";
		vals += "'" + jobInfo.getSearchParam() + "',";
		vals += "'" + jobInfo.getResult() + "'";
		String sql = "insert into " + TABLE_NAME + " values(" + vals + ")";
		execUpdate(sql);
	}

	/**
	 * 状態を更新する
	 */
	private void updateStatus(String jobId, String val) throws SQLException {
		String sql = "update " + TABLE_NAME + " set STATUS='" + val + "' where JOB_ID='" + jobId + "'";
		execUpdate(sql);
	}

	/**
	 * DB接続する
	 */
	private boolean connectDB() {
		// - note:massbank.admin.DatabaseAccess は排他になっているので不使用
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			this.con = DriverManager.getConnection(this.connectUrl, "bird", "bird2006");

			// 自動コミットモードを解除
			con.setAutoCommit(false);

			// トランザクション分離レベルをセット
			con.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);

			this.stmt = con.createStatement();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * SQL QUERYを実行をする
	 */
	private ResultSet execQuery(String sql) {
		for ( int i = 0; i < 2; i++ ) {
			try {
				ResultSet rs = stmt.executeQuery(sql);
				return rs;
			}
			catch (SQLException e) {
				// DB切断時に再接続する
				String state = e.getSQLState();
				// 08003 : Connection not open
				// 08S01 : Communication link failure
				if ( state.equals("08003") || state.equals("08S01") ) {
					connectDB();
				}
				else {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * SQL UPDATEを実行を実行をする
	 */
	private void execUpdate(String sql) {
		for ( int i = 0; i < 2; i++ ) {
			try {
				stmt.executeUpdate(sql);
				con.commit();
				break;
			}
			catch (SQLException e) {
				// DB切断時に再接続する
				String state = e.getSQLState();
				// 08003 : Connection not open
				// 08S01 : Communication link failure
				if ( state.equals("08003") || state.equals("08S01") ) {
					connectDB();
				}
				else {
					e.printStackTrace();
				}
			}
		}
	}
}
