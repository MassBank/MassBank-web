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
 * ピーク情報データ クラス
 *
 * ver 1.0.1 2009.12.15
 *
 ******************************************************************************/

/**
 * ピーク情報データ クラス
 * スペクトル単位でPeak情報を保持するデータクラス
 */
public class PeakData {
	
	/** ピーク数 */
	private int peakNum = 0;
	
	/** m/z */
	private double[] mz;

	/** 強度 */
	private int[] intensity;

	/** ピーク選択フラグ */
	private boolean[] selectPeakFlag;

	/**
	 * コンストラクタ
	 * @param data m/zと強度のタブ区切り文字列を格納した配列
	 */
	public PeakData(String[] data) {
		clear();
		
		peakNum = data.length;
		if (data.length == 1) {
			if (data[0].split("\t")[0].equals("0") && data[0].split("\t")[1].equals("0")) {
				peakNum = 0;
			}
		}
		mz = new double[peakNum];
		intensity = new int[peakNum];
		selectPeakFlag = new boolean[peakNum];
		String[] words;
		for (int i=0; i<peakNum; i++) {
			words = data[i].split("\t");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
		}
	}

	/**
	 * 初期化
	 */
	public void clear() {
		mz = null;
		intensity = null;
		selectPeakFlag = null;
	}
	
	/**
	 * 最大強度取得
	 * @param start マスレンジ(m/z)開始値
	 * @param end マスレンジ(m/z)終了値
	 * @return レコード内の指定されたマスレンジ(m/z)の間で最大の強度
	 */
	public int getMaxIntensity(double start, double end) {
		int max = 0;
		for (int i = 0; i < peakNum; i++) {
			if (mz[i] > end)
				break;

			if (start <= mz[i]) {
				max = Math.max(max, intensity[i]);
			}
		}

		return max;
	}

	/**
	 * m/z取得
	 * @param index インデックス
	 * @return m/z
	 */
	public double getMz(int index) {
		if (index < 0 || index >= peakNum) {
			return -1.0d;
		}
		return mz[index];
	}

	/**
	 * 最大m/zとプリカーサーの比較
	 * @param プリカーサー
	 * @return 最大m/zとプリカーサーの大きい方
	 */
	public double compMaxMzPrecusor(String precursor) {
		double mzMax;
		if (mz.length == 0) {
			mzMax = 0d;
		}
		else {
			mzMax = mz[mz.length-1];
		}
		try {
			Double.parseDouble(precursor);
		} catch (Exception e) {
			return mzMax;
		}
		
		return Math.max(mzMax, Double.parseDouble(precursor));
	}
	
	/**
	 * 強度取得
	 * @param index インデックス
	 * @return 強度
	 */
	public int getIntensity(int index) {
		if (index < 0 || index >= peakNum) {
			return -1;
		}
		return intensity[index];
	}

	/**
	 * インデックス取得
	 * @param mz m/z
	 * @return インデックス
	 */
	public int getIndex(double mz) {
		int i;
		for (i = 0; i < peakNum; i++) {
			if (this.mz[i] >= mz)
				break;
		}
		return i;
	}
	
	/**
	 * ピーク選択状態取得
	 * @param index インデックス
	 * @return 選択状態（true：選択済, false：未選択）
	 */
	public boolean isSelectPeakFlag(int index) {
		return selectPeakFlag[index];
	}

	/**
	 * ピーク選択状態設定
	 * @param index インデックス
	 * @param flag 選択状態（true：選択済, false：未選択）
	 */
	public void setSelectPeakFlag(int index, boolean flag) {
		this.selectPeakFlag[index] = flag;
	}

	/**
	 * ピーク選択状態初期化
	 */
	public void initSelectPeakFlag() {
		this.selectPeakFlag = new boolean[peakNum];
	}
	/**
	 * 選択状態ピーク数取得 
	 * @return int 選択状態ピーク数
	 */
	public int getSelectPeakNum() {
		int num = 0;
		for (int i = 0; i < peakNum; i++) {
			if (selectPeakFlag[i]) {
				num++;
			}
		}
		return num;
	}

	/**
	 * ピーク数取得
	 * @return ピーク数
	 */
	public int getPeakNum() {
		return peakNum;
	}
}
