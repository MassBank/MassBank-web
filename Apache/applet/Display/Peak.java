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
 * ピークデータクラス
 *
 * ver 2.0.3 2009.12.07
 *
 ******************************************************************************/
import java.util.Vector;

public class Peak
{
	/** ピーク数 */
	private int peakNum = 0;
	
	/** m/z */
	private double[] mz;
	
	/** 強度 */
	private int[] intensity;

	/** ピーク選択フラグ */
	private boolean[] selectPeakFlag;
	
	/**
	 * コンストラクタ1
	 * @parama data ピークデータ
	 */
	public Peak(String[] data)
	{
		clear();
		
		peakNum = data.length;
		if (data.length == 1) {
			if (data[0].split(" ")[0].equals("0") && data[0].split(" ")[1].equals("0")) {
				peakNum = 0;
			}
		}
		mz = new double[peakNum];
		intensity = new int[peakNum];
		selectPeakFlag = new boolean[peakNum];
		String[] words;
		for(int i=0; i<peakNum; i++){
			words = data[i].split("	");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
		}
	}

	/**
	 * コンストラクタ2
	 * @parama data ピークデータ
	 */
	public Peak(Vector<String> data)
	{
		clear();
		
		peakNum = data.size();
		if (data.size() == 1) {
			if (data.get(0).split("\t")[0].equals("0") && data.get(0).split("\t")[1].equals("0")) {
				peakNum = 0;
			}
		}
		mz = new double[peakNum];
		intensity = new int[peakNum];
		selectPeakFlag = new boolean[peakNum];
		int i = 0;
		String[] words;
		while ( data.size() > 0 ) {
			words = data.remove(0).split("\t");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
			i++;
		}
	}

	/**
	 * 初期化
	 * @
	 */
	public void clear()
	{
		mz = null;
		intensity = null;
		selectPeakFlag = null;
	}

	/**
	 * m/zを取得する
	 * @return m/z値
	 */
	public double[] getMZ()
	{
		return mz;
	}

	/**
	 * intensityを取得する
	 * @return intensity値
	 */
	public int[] getIntensity()
	{
		return intensity;
	}

	/**
	 * 最大m/zとプリカーサーの比較
	 * @param プリカーサー
	 * @return 最大m/zとプリカーサーの大きい方
	 */
	public double compMaxMzPrecusor(String precursor) {
		double mzMax;
		if (mz.length == 0) {
			mzMax = 0f;
		}
		else {
			mzMax = mz[mz.length-1];
		}
		try {
			Float.parseFloat(precursor);
		} catch (Exception e) {
			return mzMax;
		}
		
		return Math.max(mzMax, Double.parseDouble(precursor));
	}
	
	/**
	 * intensityの最大値を取得する
	 * @param start m/zの範囲1
	 * @param end   m/zの範囲2
	 * @return intensityの最大値
	 */
	public int getMaxIntensity(double start, double end)
	{
		int max = 0;
		for ( int i = 0; i < intensity.length; i++ ) {
			if ( mz[i] > end ) {
				break;
			}
			if ( start <= mz[i] ) {
				if ( max < intensity[i] ) {
					max = intensity[i];
				}
			}
		}
		return max;
	}

	/**
	 * ピーク数を取得する
	 * @return ピーク数
	 */
	public int getCount()
	{
		return peakNum;
	}

	/**
	 * 指定されたインデックスのm/zを取得する
	 * @param index インデックス
	 * @return m/z値
	 */
	public double getMz(int index)
	{
		if ( index < 0 || index >= peakNum ) {
			return -1.0f;
		}
		return mz[index];
	}

	/**
	 * 指定されたインデックスのintensityを取得する
	 * @param index インデックス
	 * @return intensity値
	 */
	public int getIntensity(int index)
	{
		if ( index < 0 || index >= peakNum ) {
			return -1;
		}
		return intensity[index];
	}

	/**
	 * 指定されたm/zのインデックスを取得する
	 * @param target m/z値
	 * @return インデックス
	 */
	public int getIndex(double target)
	{
		int i;
		for ( i = 0; i < peakNum; i++ ) {
			if ( mz[i] >= target ) {
				break;
			}
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
}
