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
 * バッチ検索ジョブ管理クラス
 *
 * ver 1.0.3 2010.04.08
 *
 ******************************************************************************/
package massbank;

import java.io.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Enumeration;
import massbank.BatchJobInfo;

public class BatchJobManager {
	private static final String KEY_NAME_COUNT = "count";
	private String jobFileName = "";
	private Properties proper = new Properties();
	private BatchJobInfo jobInfo = new BatchJobInfo();

	public BatchJobManager() {
		String path = System.getProperty("catalina.home") + "/webapps/ROOT";
		this.jobFileName = path + "/MassBankBatch.job";
	}

	/**
	  * ジョブエントリ数を取得する
	  */
	public int getCount() {
		try {
			File f = new File(this.jobFileName);
			if ( !f.exists() ) {
				return 0;
			}
			FileInputStream stream = new FileInputStream(this.jobFileName);
			proper.load(stream);
			stream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		String count = proper.getProperty("count");
		int icount = 0;
		if ( count != null ) {
			icount = Integer.parseInt(count);
		}
		return icount;
	}

	/**
	  * ジョブエントリをセットする
	  */
	public void setEntry(BatchJobInfo newJobInfo) {
		this.jobInfo = newJobInfo;
		this.jobInfo.setStatus("passive");
	}

	/**
	  * ジョブエントリをチェックする
	  */
	public boolean checkEntry() {
		ArrayList<BatchJobInfo> entryList = this.getEntryList();
		if ( entryList == null ) {
			return true;
		}
		int cnt = 0;
		for ( int i = 0; i < entryList.size(); i++ ) {
			BatchJobInfo jobInfo = (BatchJobInfo)entryList.get(i);
			String sessionId = jobInfo.getSessionId();
			String timeStamp = jobInfo.getTimeStamp();
			String ipAddress = jobInfo.getIpAddr();
			String fileName  = jobInfo.getFileName();
			String fileSize  = jobInfo.getFileSize();

			BatchJobInfo newJobInfo = this.jobInfo;
			// 同一セッションでファイル名,ファイルサイズが同じ
			//   (同じファイルを実行したい場合もあるかも->終われば実行可能)
			if ( sessionId.equals(newJobInfo.getSessionId())
				&& fileName.equals(newJobInfo.getFileName())
				&& fileSize.equals(newJobInfo.getFileSize()) ) {
				return false;
			}

			// 同一IPアドレスで既に実行されているジョブがあるか
			if ( ipAddress.equals(newJobInfo.getIpAddr()) ) {
				cnt++;
			}
			if ( cnt >= 3 ) {
				return false;
			}
		}
		return true;
	}

	/**
	  * ジョブエントリを追加する
	  */
	public void addEntry() {
			this.storeEntry(true);
	}

	/**
	  * ジョブ状態をアクティブにセットする
	  */
	public void setActive() {
		this.jobInfo.setStatus("active");
		this.storeEntry(false);
	}

	/**
	  * ジョブエントリを削除する
	  */
	public void deleteEntry(String sessionId, String timeStamp) {
		synchronized (this.proper) {
			int cnt = this.getCount() - 1;
			String key = sessionId + "," + timeStamp;
			try {
				proper.remove(key);
				proper.setProperty( KEY_NAME_COUNT, String.valueOf(cnt) );
				FileOutputStream stream = new FileOutputStream(this.jobFileName);
				proper.store( stream, null );
				stream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	  * 未実行ジョブのリストを取得する
	  */
	public ArrayList<BatchJobInfo> getPassiveEntry() {
		ArrayList<BatchJobInfo> entryAll = this.getEntryList();
		if ( entryAll == null ) {
			return null;
		}
		ArrayList<BatchJobInfo> entryList = new ArrayList();
		for ( int i = 0; i < entryAll.size(); i++ ) {
			BatchJobInfo jobInfo = (BatchJobInfo)entryAll.get(i);
			String status = jobInfo.getStatus();
			if ( status.equals("passive") ) {
				entryList.add(jobInfo);
			}
		}
		return entryList;
	}

	/**
	  * 全ジョブの状態を未実行状態にする
	  */
	public void setPassiveAll() {
		synchronized (this.proper) {
			if ( this.getCount() == 0 ) {
				return;
			}
			ArrayList<BatchJobInfo> entryAll = this.getEntryList();
			ArrayList<BatchJobInfo> entryList = new ArrayList();
			for ( int i = 0; i < entryAll.size(); i++ ) {
				BatchJobInfo jobInfo = (BatchJobInfo)entryAll.get(i);
				String status = jobInfo.getStatus();
				if ( status.equals("active") ) {
					jobInfo.setStatus("passive");
					this.setEntry(jobInfo);
					this.storeEntry(false);
				}
			}
		}
	}

	/**
	  * ジョブエントリのリストを取得する
	  */
	private ArrayList<BatchJobInfo> getEntryList() {
		ArrayList<BatchJobInfo> entryList = new ArrayList();
		synchronized (this.proper) {
			try {
				File f = new File(this.jobFileName);
				if ( !f.exists() ) {
					return null;
				}
				FileInputStream stream = new FileInputStream(this.jobFileName);
				proper.load(stream);
				stream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		Enumeration names = proper.propertyNames();
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			String val = proper.getProperty( key );
			if ( !key.equals(KEY_NAME_COUNT) ) {
				String entry = key + "," + val;
				String[] items = entry.split(",");
				entryList.add(new BatchJobInfo(items));
			}
		}
		return entryList;
	}

	/**
	  * ジョブエントリを保存する
	  */
	private void storeEntry(boolean isCountup) {
		String key = jobInfo.getSessionId() + "," + jobInfo.getTimeStamp();
		String val = jobInfo.getIpAddr()    + "," + jobInfo.getMailAddr()
				 + "," + jobInfo.getFileName()  + "," + jobInfo.getFileSize()
				 + "," + jobInfo.getTempName()  + "," + jobInfo.getStatus()
				 + "," + jobInfo.getInstType(false) + "," + jobInfo.getIonMode();
		synchronized (this.proper) {
			if ( isCountup ) {
				int cnt = this.getCount() + 1;
				proper.setProperty( KEY_NAME_COUNT, String.valueOf(cnt) );
			}
			proper.setProperty( key, val );
			FileOutputStream stream = null;
			try {
				stream = new FileOutputStream(this.jobFileName);
			}
			catch (Exception e) {
			}
			try {
				proper.store( stream, null );
				stream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
