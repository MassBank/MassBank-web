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
 * ver 2.0.1 2008.12.05
 *
 ******************************************************************************/

import java.util.Vector;

/**
 * ピークデータ クラス
 */
public class Peak
{
	float[] mz;
	int[] intensity;
	int[] top10Index;

	public Peak(String[] data)
	{
		clear();

		int i;
		String[] words;
		mz = new float[data.length];
		intensity = new int[data.length];
		for(i = 0; i < data.length; i ++){
			words = data[i].split("	");
			mz[i] = Float.parseFloat(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
		}

		searchTop10Peaks();
	}

	public Peak(Vector<String> data)
	{
		clear();

		int i = 0;
		String[] words;
		mz = new float[data.size()];
		intensity = new int[data.size()];
		while(data.size() > 0){
			words = data.remove(0).split("\t");
			mz[i] = Float.parseFloat(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
			i ++;
		}

//		searchTop10Peaks();
	}

	public void clear()
	{
		mz = null;
		intensity = null;
		top10Index = new int[10];
		for(int i = 0; i < 10; i ++){
			top10Index[i] = 0;
		}
	}

	public float[] getMZ()
	{
		return mz;
	}

	public int[] getIntensity()
	{
		return intensity;
	}

	public float getMaxMZ()
	{
		return mz[mz.length-1];
	}

	public int getMaxIntensity(float start, float end)
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

	public float getMZ(int index)
	{
		if(index < 0 || index >= mz.length)
			return -1.0f;
		return mz[index];
	}

	public int getIntensity(int index)
	{
		if(index < 0 || index >= mz.length)
			return -1;
		return intensity[index];
	}

	public int[] getTop10Index()
	{
		return top10Index;
	}

	public int getIndex(float target)
	{
		int i;
		for(i = 0; i < mz.length; i ++){
			if(mz[i] >= target)
				break;
		}
		return i;
	}

	private void searchTop10Peaks()
	{
		if (mz == null || intensity == null)
			return;

		int max, i, j, k;
		for (i = 0; i < 10; i++)
			top10Index[i] = -1;
		for (i = 0; i < 10; i++) {
			max = 0;
			for (j = 0; j < intensity.length; j++) {
				if (max < intensity[j]) {
					for (k = 0; k < i; k++) {
						if (j == top10Index[k])
							break;
					}
					if (k == i) {
						max = intensity[j];
						top10Index[i] = j;
					}
				}
			}
			if (max == 0)
				break;
		}
	}
}
