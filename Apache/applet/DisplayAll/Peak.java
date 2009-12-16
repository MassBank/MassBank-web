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
 * ver 2.0.2 2009.12.16
 *
 ******************************************************************************/

import java.util.Vector;

/**
 * ピークデータ クラス
 */
public class Peak
{
	double[] mz;
	int[] intensity;

	/**
	 * 
	 * @param data
	 */
	public Peak(String[] data)
	{
		clear();

		int i;
		String[] words;
		mz = new double[data.length];
		intensity = new int[data.length];
		for(i = 0; i < data.length; i ++){
			words = data[i].split("	");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
		}
	}

	/**
	 * 
	 * @param data
	 */
	public Peak(Vector<String> data)
	{
		clear();

		int i = 0;
		String[] words;
		mz = new double[data.size()];
		intensity = new int[data.size()];
		while(data.size() > 0){
			words = data.remove(0).split("\t");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
			i ++;
		}
	}

	public void clear()
	{
		mz = null;
		intensity = null;
	}

	public double[] getMZ()
	{
		return mz;
	}

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
}
