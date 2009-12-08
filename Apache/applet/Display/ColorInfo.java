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
 * ピーク色づけ情報データクラス
 *
 * ver 1.0.1 2009.12.04
 *
 ******************************************************************************/

import java.util.ArrayList;

public class ColorInfo {
	private int diffMargin = 0;
	private String[] diffMzs = null;
	private ArrayList<Double>[] hitPeaks1 = null;
	private ArrayList<Double>[] hitPeaks2 = null;
	private ArrayList<Integer>[] barColors = null;
	private ArrayList<String> formulas = null;

	public void setHitPeaks1(ArrayList<Double>[] val) {
		this.hitPeaks1 = val;
	}
	public void setHitPeaks2(ArrayList<Double>[] val) {
		this.hitPeaks2 = val;
	}
	public void setBarColors(ArrayList<Integer>[] val) {
		this.barColors = val;
	}
	public void setDiffMargin(int val) {
		this.diffMargin = val;
	}
	public void setDiffMzs(String[] val) {
		this.diffMzs = val;
	}
	public void setFormulas(ArrayList<String> val) {
		this.formulas = val;
	}

	public ArrayList<Double>[] getHitPeaks1() {
		return this.hitPeaks1;
	}
	public ArrayList<Double>[] getHitPeaks2() {
		return this.hitPeaks2;
	}
	public ArrayList<Integer>[] getBarColors() {
		return this.barColors;
	}
	public int getDiffMargin() {
		return this.diffMargin;
	}
	public String[] getDiffMzs() {
		return this.diffMzs;
	}
	public ArrayList<String> getFormulas() {
		return this.formulas;
	}
}
