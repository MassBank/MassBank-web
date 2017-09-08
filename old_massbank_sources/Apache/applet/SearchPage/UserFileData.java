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
 * UserFileデータ クラス
 *
 * ver 1.0.0 2008.12.05
 *
 ******************************************************************************/

/**
 * UserFileデータ クラス
 * 
 * UserFile読み込み情報を格納
 */
public class UserFileData {

	/** ID */
	private String id = "";
	
	/** ピーク情報 */
	private String[] peaks = new String[]{"0\t0"};

	/** 化合物名 */
	private String name = "";

	/**
	 * コンストラクタ
	 */
	public UserFileData() {
	}

	/**
	 * ピーク情報設定
	 * @param ps ピーク情報
	 */
	public void setPeaks(String[] ps) {
		if (ps == null) {
			return;
		}
		peaks = ps;
	}

	/**
	 * ピーク情報取得
	 * @return ピーク情報
	 */
	public String[] getPeaks() {
		return peaks;
	}

	/**
	 * 化合物名設定
	 * @param name 化合物名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 化合物名取得
	 * @return 化合物名
	 */
	public String getName() {
		return name;
	}

	/**
	 * ID取得
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * ID設定
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}
}
