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
 * サーバ・ステータス情報データクラス
 *
 * ver 1.0.1 2012.10.10
 *
 ******************************************************************************/
package massbank;

public class ServerStatusInfo {
	private String svrName = "";
	private String url     = "";
	private String dbName  = "";
	private String db2Name  = "";
	private boolean status = true;

	/**
	 * コンストラクタ
	 * @param svrName サーバ名 
	 * @param url URL
	 * @param dbName DB名
	 */
	public ServerStatusInfo(String svrName, String url, String dbName, String db2Name) {
		this.svrName = svrName;
		this.url     = url;
		this.dbName  = dbName;
		this.db2Name = db2Name;
	}

	/**
	 * ステータスを設定する
	 * @param isActive ステータス
	 */
	public void setStatus(boolean isActive) {
		this.status = isActive;
	}

	/**
	 * サーバ名を取得する
	 * return サーバ名
	 */
	public String getServerName() {
		return this.svrName;
	}
	/**
	 * URLを取得する
	 * return URL
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * DB名を取得する
	 * return DB名
	 */
	public String getDbName() {
		return this.dbName;
	}

	/**
	 * セカンダリDB名を取得する
	 * return DB名
	 */
	public String get2ndDbName() {
		return this.db2Name;
	}

	/**
	 * ステータスを取得する
	 * return ステータス
	 */
	public boolean getStatus() {
		return this.status;
	}
}
