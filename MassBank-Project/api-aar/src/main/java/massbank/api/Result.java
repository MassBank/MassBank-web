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
 * [WEB-API] 検索結果詳細格納データクラス
 *
 * ver 1.0.1 2010.04.15
 *
 ******************************************************************************/
package massbank.api;

public class Result {

	private String id = "";
	private String title = "";
	private String formula = "";
	private String exactMass = "";
	private String score = "";

	/**
	 * コンストラクタ
	 */
	public Result() {
	}

	//--  setterメソッド ----------------------------------
	/**
	 * IDをセットする
	 */
	public void setId(String val) {
		this.id = val;
	}
	/**
	 * レコードタイトルをセットする
	 */
	public void setTitle(String val) {
		this.title = val;
	}
	/**
	 * 分子式をセットする
	 */
	public void setFormula(String val) {
		this.formula = val;
	}
	/**
	 * 精密質量をセットする
	 */
	public void setExactMass(String val) {
		this.exactMass = val;
	}
	/**
	 * スコアをセットする
	 */
	public void setScore(String val) {
		this.score = "0";
		int pos = val.indexOf(".");
		if ( pos > 0 ) {
			this.score += val.substring(pos);
		}
	}

	//--  getterメソッド ----------------------------------
	/**
	 * IDを取得する
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * レコードタイトルを取得する
	 */
	public String getTitle() {
		return this.title;
	}
	/**
	 * 分子式を取得する
	 */
	public String getFormula() {
		return this.formula;
	}
	/**
	 * 精密質量を取得する
	 */
	public String getExactMass() {
		return this.exactMass;
	}
	/**
	 * スコアを取得する
	 */
	public String getScore() {
		return this.score;
	}
}
