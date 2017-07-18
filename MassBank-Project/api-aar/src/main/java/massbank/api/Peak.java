/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * [WEB-API] ピーク情報格納データクラス
 *
 * ver 1.0.0 2009.08.19
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;

public class Peak {

	private String id = "";
	private ArrayList<String> mzList = null;
	private ArrayList<String> inteList = null;

	/**
	 * コンストラクタ
	 */
	public Peak() {
		mzList = new ArrayList();
		inteList = new ArrayList();
	}

	/**
	 *
	 */
	public void setId(String val) {
		this.id = val;
	}

	/**
	 *
	 */
	public void addPeak(String mz, String inte) {
		this.mzList.add(mz);
		this.inteList.add(inte);
	}

	/**
	 * IDを取得する
	 */
	public String getId() {
		return this.id;
	}

	/**
	 *
	 */
	public int getNumPeaks() {
		return this.mzList.size();
	}

	/**
	 *
	 */
	public String[] getMzs() {
		String[] ret = new String[this.mzList.size()];
		this.mzList.toArray(ret);
		return ret;
	}

	/**
	 *
	 */
	public String[] getIntensities() {
		String[] ret = new String[this.inteList.size()];
		this.inteList.toArray(ret);
		return ret;
	}
}
