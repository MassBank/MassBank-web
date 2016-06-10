/*******************************************************************************
 *
 * Copyright (C) 2011 MassBank Project
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
 * 分子式と部分構造の関係情報を格納したデータクラス
 * 
 * ver 1.0.0 2011.12.06
 *
 ******************************************************************************/
package massbank.extend;

public class RelationInfo {
	private String no = "";
	private String formula1 = "";
	private String formula2 = "";
	private String precision = "";
	private String recall = "";
	private String true_posi = "";

	public String getRelationNo() {
		return this.no;
	}
	public String getFormula1() {
		return this.formula1;
	}
	public String getFormula2() {
		return this.formula2;
	}
	public String getPrecision() {
		return this.precision;
	}
	public String getRecall() {
		return this.recall;
	}
	public String getTruePosi() {
		return this.true_posi;
	}

	public void setRelationNo(String val) {
		this.no = val;
	}
	public void setFormula1(String val) {
		this.formula1 = val;
	}
	public void setFormula2(String val) {
		this.formula2 = val;
	}
	public void setPrecision(String val) {
		this.precision = val;
	}
	public void setRecall(String val) {
		this.recall = val;
	}
	public void setTruePosi(String val) {
		this.true_posi =val;
	}
}
