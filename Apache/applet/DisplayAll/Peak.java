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
 * ピークデータ クラス
 *
 * ver 2.0.3 2010.01.08
 *
 ******************************************************************************/

import java.math.BigDecimal;
import java.util.Vector;

/**
 * ピークデータ クラス
 */
public class Peak {
	
	/** m/z */
	private double[] mz;
	
	/** 強度 */
	private int[] intensity;
	
	/** ピーク差表示用 */
	private String[] diff;

	/** ピーク差表示基準値 */
	private String base = null;
	
	
	/**
	 * コンストラクタ
	 * @param data
	 * @param emass
	 * @param ion
	 */
	public Peak(String[] data, String emass, String ion) {
		clear();

		// 差表示用ベース値算出
		if ( !emass.equals("") && !emass.equals("0") ) {
			if ( ion.equals("1") ) {
				emass = String.valueOf(Double.parseDouble(emass) + 1.0078250321);
			}
			else if ( ion.equals("-1") ) {
				emass = String.valueOf(Double.parseDouble(emass) - 1.0078250321);
			}
			base = new BigDecimal(Double.parseDouble(emass)).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
		}
		
		int i;
		String[] words;
		mz = new double[data.length];
		intensity = new int[data.length];
		diff = new String[data.length];
		for(i = 0; i < data.length; i ++){
			words = data[i].split("	");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
			if ( base != null ) {
				diff[i] =  new BigDecimal(mz[i] - Double.parseDouble(base)).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
			}
		}
	}

	/**
	 * コンストラクタ
	 * @param data
	 * @param emass
	 * @param ion
	 */
	public Peak(Vector<String> data, String emass, String ion) {
		clear();
		
		// 差表示用ベース値算出
		if ( !emass.equals("") && !emass.equals("0") ) {
			if ( ion.equals("1") ) {
				emass = String.valueOf(Double.parseDouble(emass) + 1.0078250321);
			}
			else if ( ion.equals("-1") ) {
				emass = String.valueOf(Double.parseDouble(emass) - 1.0078250321);
			}
			base = new BigDecimal(Double.parseDouble(emass)).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
		}
		
		int i = 0;
		String[] words;
		mz = new double[data.size()];
		intensity = new int[data.size()];
		diff = new String[data.size()];
		while(data.size() > 0){
			words = data.remove(0).split("\t");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
			if ( base != null ) {
				diff[i] =  new BigDecimal(mz[i] - Double.parseDouble(base)).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
			}
			i ++;
		}
	}

	public void clear() {
		mz = null;
		intensity = null;
	}

	public double[] getMZ() {
		return mz;
	}

	public int[] getIntensity() {
		return intensity;
	}
	
	public String[] getDiff() {
		return diff;
	}
	
	public String getBase() {
		return base;
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

	public int getMaxIntensity(double start, double end)
	{
		int max = 0;
		for(int i = 0; i < intensity.length; i ++){
			if(mz[i] > end)
				break;

			if(start <= mz[i]){
				if(max < intensity[i])
					max = intensity[i];
			}
		}

		return max;
	}

	public int getCount()
	{
		return mz.length;
	}

	public double getMZ(int index)
	{
		if(index < 0 || index >= mz.length)
			return -1.0d;
		return mz[index];
	}

	public int getIntensity(int index)
	{
		if(index < 0 || index >= mz.length)
			return -1;
		return intensity[index];
	}

	public int getIndex(double target)
	{
		int i;
		for(i = 0; i < mz.length; i ++){
			if(mz[i] >= target)
				break;
		}
		return i;
	}
	
	public String getDiff(int index) {
		return diff[index];
	}
}
