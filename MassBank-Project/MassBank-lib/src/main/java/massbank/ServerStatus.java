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
 * 連携サーバの状態を管理するクラス
 *
 * ver 1.0.2 2012.11.01
 *
 ******************************************************************************/
package massbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.*;

import org.apache.commons.configuration2.ex.ConfigurationException;

import java.text.DecimalFormat;
import massbank.GetConfig;
import massbank.ServerStatusInfo;

public class ServerStatus {
	private Properties proper = new Properties();
	//管理ファイルのパス
	private String filePath = "";
	// ポーリング周期
	private int pollInterval = 0;
	// 監視対象サーバ数
	private int serverNum = 0;
	// サーバ・ステータス情報
	private ServerStatusInfo[] statusList = null;
	// 管理ファイル名称
	private static final String PROF_FILE_NAME = "ServerStatus.inf";
	// プロパティのキー名
	private static final String SERVER_KEY_NAME = "server";
	private static final String MANAGED_KEY_NAME = "status";

	private DecimalFormat decFormat = new DecimalFormat("00");

	/**
	 * デフォルトコンストラクタ
	 * @throws ConfigurationException 
	 */
	public ServerStatus() throws ConfigurationException {
		// 管理ファイルのパスをセット
		//this.filePath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPPSERV_PATH) + PROF_FILE_NAME;
		this.filePath = Config.get().TOMCAT_APPPSERV_PATH() + PROF_FILE_NAME;
		setBaseInfo();
	}
	
	/**
	 * コンストラクタ
	 * @param baseUrl ベースURL
	 * @throws ConfigurationException 
	 * @deprecated 非推奨コンストラクタ
	 * @see ServerStatus#ServerStatus()
	 */
	public ServerStatus(String baseUrl) throws ConfigurationException {
//		int pos1 = baseUrl.indexOf( "/", (new String("http://")).length() );
//		int pos2 = baseUrl.lastIndexOf( "/" );
//		String subDir = "";
//		if ( pos2 > pos1 ) {
//			subDir = baseUrl.substring( pos1 + 1, pos2 );
//		}
//		String path = System.getProperty("catalina.home") + "/webapps/ROOT/";
//		this.filePath = path + subDir + "/pserver/" + PROF_FILE_NAME;
//
//		this.baseUrl = baseUrl;
//		setBaseInfo();
		// 管理ファイルのパスセットはデフォルトコンストラクタで行うことにする
		this();
	}

	/**
	 * ベース情報をセット
	 * @throws ConfigurationException 
	 */
	public void setBaseInfo() throws ConfigurationException {
		// 設定ファイル読込み
		//GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		GetConfig conf = new GetConfig(Config.get().BASE_URL());
		// URLリストを取得
		String[] urls = conf.getSiteUrl();
		// DB名リストを取得
		String[] dbNames = conf.getDbName();
		// セカンダリDB名リストを取得
		String[] db2Names = conf.getSecondaryDBName();
		// サーバ名リストを取得
		String[] svrNames = conf.getSiteName();
		// フロントサーバURLを取得
		String serverUrl = conf.getServerUrl();
		// ポーリング周期を取得
		this.pollInterval = conf.getPollInterval();

		// 監視対象サーバのURLとDB名を格納
		List<String> svrNameList = new ArrayList<String>();
		List<String> urlList = new ArrayList<String>();
		List<String> dbNameList = new ArrayList<String>();
		List<String> db2NameList = new ArrayList<String>();
		for ( int i = 0; i < urls.length; i++ ) {
			// ミドルサーバまたは、フロントサーバと同一URLの場合は対象外
			if ( i != GetConfig.MYSVR_INFO_NUM && !urls[i].equals(serverUrl) ) {
				svrNameList.add(svrNames[i]);
				urlList.add(urls[i]);
				dbNameList.add(dbNames[i]);
				db2NameList.add(db2Names[i]);
			}
		}

		// 状態管理リストをセット
		this.serverNum = urlList.size();
		if ( this.serverNum > 0 ) {
			this.statusList = new ServerStatusInfo[this.serverNum];
			for ( int i = 0; i < svrNameList.size(); i++ ) {
				String svrName = svrNameList.get(i);	// サーバ名
				String url = urlList.get(i);			// URL
				String dbName = dbNameList.get(i);		// DB名
				String db2Name = db2NameList.get(i);	// セカンダリDB名
				// ステータスは未セット
				this.statusList[i] = new ServerStatusInfo( svrName, url, dbName, db2Name );
			}
		}
	}

	/**
	 * 管理ファイルを整合する
	 * @throws ConfigurationException 
	 */
	public void clean() throws ConfigurationException {
		setBaseInfo();

		// 管理ファイルを読込み、サーバの状態を取得する
		boolean[] isActiveList = getStatusList();
		if ( isActiveList == null ) {
			return;
		}
		int listNum = isActiveList.length;

		// ステータスセット
		for ( int i = 0; i < listNum; i++ ) {
			setStatus( i, isActiveList[i] );
		}
		// 監視対象のサーバが減った場合、余分な情報を削除する
		if ( this.serverNum < listNum ) {
			for ( int i = this.serverNum; i < listNum; i++ ) {
				deleteStatus(i);
			}
		}

		// 管理ファイルに保存する
		store();
	}

	/**
	 * 監視対象サーバ数を取得する
	 * @return 監視対象サーバ数
	 */
	public int getServerNum() {
		return this.serverNum;
	}

	/**
	 * ポーリング周期を取得する
	 * @return ポーリング周期
	 */
	public int getPollInterval() {
		return this.pollInterval;
	}

	/**
	 * サーバ・ステータス情報を取得する
	 * @return ステータス情報
	 */
	public ServerStatusInfo[] getStatusInfo() {
		// 非監視の場合は、nullを返す
		if ( !isManaged() ) {
			return null;
		}

		// 管理ファイルを読込み、サーバの状態を取得する
		boolean[] isActiveList = getStatusList();
		if ( isActiveList != null ) {
			int num = isActiveList.length;
			if ( isActiveList.length > statusList.length ) {
				num = statusList.length;
			}
			for ( int i = 0; i < num; i++ ) {
				this.statusList[i].setStatus(isActiveList[i]);
			}
		}
		return this.statusList;
	}

	/**
	 * サーバの状態をセットする
	 * @param index リストのインデックス
	 * @param isActive 状態 -- true:active / false:inactive
	 */
	public void setStatus(int index, boolean isActive) {
		String status = "";
		if ( isActive ) {
			status = "active";
		}
		else {
			status = "inactive";
		}
		String key = SERVER_KEY_NAME + decFormat.format(index);
		String val = status;
		try {
			synchronized (this.proper) {
				proper.setProperty( key, val );
			}
		}
		catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe( e.toString() );
		}
	}

	/**
	 * サーバ状態を削除する
	 * @param index リストのインデックス
	 */
	public void deleteStatus(int index) {
		
		String key = SERVER_KEY_NAME + decFormat.format(index);
		try {
			synchronized (this.proper) {
				proper.remove(key);
			}
		}
		catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe( e.toString() );
		}
	}

	/**
	  * 管理ファイルに保存する
	  */
	public void store() {
		FileOutputStream stream = null;
		try {
			synchronized (this.proper) {
				stream = new FileOutputStream(filePath);
				proper.store( stream, null );
				stream.close();
			}
		}
		catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe( e.toString() );
		}
	}

	/**
	 * 監視状態をセットする
	 * @param isManaged true:監視中 / false:非監視
	 */
	public void setManaged(boolean isManaged) {
		// 管理ファイル読込み
		read();

		// 状態セット
		String status = "";
		if ( isManaged ) {
			status = "managed";
		}
		else {
			status = "unmanaged";
		}
		try {
			synchronized (this.proper) {
				proper.setProperty( MANAGED_KEY_NAME, status );
			}
		}
		catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe( e.toString() );
		}
		store();
	}

	/**
	 * 監視状態か否か
	 * @return true:監視中 / false:非監視
	 */
	public boolean isManaged() {
		boolean isManaged = false;
		if ( read() ) {
			String status = proper.getProperty( MANAGED_KEY_NAME, "" );
			if ( status.equals("managed") ) {
				isManaged = true;
			}
		}
		return isManaged;
	}

	/**
	  * 管理ファイルを読み込む
	  */
	private boolean read() {
		File f = new File(this.filePath);
		if ( !f.exists() ) {
			return false;
		}
		try {
			synchronized (this.proper) {
				FileInputStream stream = new FileInputStream(filePath);
				proper.load(stream);
				stream.close();
			}
		}
		catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe( e.toString() );
		}
		return true;
	}

	/**
	 * 管理ファイルを読込み、サーバの状態を取得する
	 * @return サーバ状態リスト -- true:active / false:inactive / null:対象サーバなし
	 */
	private boolean[] getStatusList() {
		// 配列isActiveListを初期化
		boolean[] isActiveList = null;
		if ( this.serverNum > 0 ) {
			isActiveList = new boolean[this.serverNum];
			for ( int i = 0; i < this.serverNum; i++ ) {
				// true =「Active」をセット
				isActiveList[i] = true;
			}
			// 管理ファイル読込み
			if ( !read() ) {
				// ファイルなし
				return isActiveList;
			}
		}

		// プロパティリスト取得
		Map<String, String> list = new TreeMap<String, String>();
		Enumeration<?> names = proper.propertyNames();
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			if ( key.indexOf(SERVER_KEY_NAME) >= 0 ) {
				String val = proper.getProperty(key);
				list.put( key, val );
			}
		}

		// 管理ファイルから読み取った内容をセット
		/* - serverNum 監視対象のサーバ数       */
		/* - list.size 管理ファイル上のサーバ数 */
		int num = list.size();
		if ( num > 0 ) {
			isActiveList = new boolean[num];
			Iterator<String> it = list.keySet().iterator();
			for ( int i = 0; i < num; i++ ) {
				if ( !it.hasNext() ) {
					break;
				}
				Object okey = it.next();
				String status = (String)list.get(okey);
				if ( status.equals("active") ) {
					isActiveList[i] = true;
				}
			}
		}
		return isActiveList;
	}

	/**
	 * サーバの状態を取得する
	 * @return サーバ状態リスト -- true:active / false:inactive
	 */
	public boolean isServerActive(String url, String dbName) {
		ServerStatusInfo[] info = getStatusInfo();
		if ( info == null ) {
			return true;
		}
		for ( int i = 0; i < info.length; i++ ) {
			if ( url.equals( info[i].getUrl() ) && dbName.equals( info[i].getDbName() ) ) {
				if ( !info[i].getStatus() ) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	public String get2ndDbName(String url, String dbName) {
		ServerStatusInfo[] info = getStatusInfo();
		if ( info == null ) {
			return "";
		}
		for ( int i = 0; i < info.length; i++ ) {
			if ( url.equals( info[i].getUrl() ) && dbName.equals( info[i].getDbName() ) ) {
				return info[i].get2ndDbName();
			}
		}
		return "";
	}
}
