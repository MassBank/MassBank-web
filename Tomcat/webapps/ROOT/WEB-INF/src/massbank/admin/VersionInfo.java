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
 * バージョン情報データクラス
 *
 * ver 1.0.2 2008.12.05
 *
 ******************************************************************************/
package massbank.admin;

/**
 * 
 * バージョン情報データクラス
 *
 * @ver 1.0.0 2007.12.14
 * @ver 1.0.1 2008.01.22
 */
public class VersionInfo {
	// 配列番号
	public static final int NAME    = 0;
	public static final int VERSION = 1;
	public static final int DATE    = 2;
	public static final int STATUS  = 3;
	// ステータスNO
	public static final int STATUS_NON = 0;
	public static final int STATUS_OLD = 1;
	public static final int STATUS_NEW = 2;
	public static final int STATUS_ADD = 3;
	public static final int STATUS_DEL = 4;

	private String name = "";
	private String ver  = "";
	private String date = "";
	private int status = STATUS_NON;

	public VersionInfo() {
	}

	/**
	 * コンストラクタ
	 * @param items 格納する情報
	 */
	public VersionInfo(String[] items) {
		this.name = items[NAME];
		this.ver  = items[VERSION];
		this.date = items[DATE];
	}

	/**
	 * コンストラクタ
	 * @param name ファイル名
	 * @param ver  バージョン
	 * @param name 日付
	 */
	public VersionInfo(String name, String ver, String date) {
		this.name = name;
		this.ver  = ver;
		this.date = date;
	}

	/**
	 * ファイル名情報セット
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ファイル名情報取得
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * バージョン情報取得
	 */
	public String getVersion() {
		return this.ver;
	}

	/**
	 * 日付情報取得
	 */
	public String getDate() {
		return this.date;
	}

	/**
	 * ステータス取得
	 */
	public String getStatus() {
		String ret = "";
		switch (this.status) {
		case STATUS_NON: ret = ""; break;
		case STATUS_OLD: ret = "OLD"; break;
		case STATUS_NEW: ret = "NEW"; break;
		case STATUS_ADD: ret = "ADD"; break;
		case STATUS_DEL: ret = "DEL"; break;
		}
		return ret;
	}

	/**
	 * ステータスセット
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * 更新有無判定
	 */
	public boolean isUpdate() {
		if ( this.status == STATUS_NON ) {
			return false;
		}
		return true;
	}
}
