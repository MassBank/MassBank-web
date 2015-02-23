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
 * ヒットピーク情報格納 クラス
 *
 * ver 2.0.5 2008.12.16
 *
 ******************************************************************************/

import java.util.ArrayList;

/**
 * ヒットピーク情報格納 クラス
 *-------------------------------------------------
 * [構造]
 *  ○スペクトル1の情報 : mzInfoList[0]
 *        ｜
 *        ├ ヒットピーク1の情報 : MzInfo(0)
 *        ｜        ｜
 *        ｜        ├ diffmz         ピーク差 
 *        ｜        ├ mz1Ary(0..n)   m/z1
 *        ｜        ├ mz2Ary(0..n)   m/z2
 *        ｜        └ barColor(0..n) 描画色
 *        ｜  ・
 *        ｜  ・
 *        ｜
 *        └ ヒットピークnの情報 MzInfo(n)
 *
 *
 *  ○スペクトル2の情報 mzInfoList[1]
 *      ・
 *      ・
 *  ○スペクトルnの情報 mzInfoList[n]
 *-------------------------------------------------
 */
public class HitPeaks
{
	/* ヒットしたピークの格納リスト */
	public ArrayList<MzInfo>[] mzInfoList = null;
	/* 格納リストの順番 */
	private int pnum = 1;
	
	/**
	 * 格納リストの順番をセットする
	 * @param pnum 格納リストの順番
	 */
	public void setListNum(int pnum) {
		this.pnum = pnum;
	}

	/**
	 * 格納リストの順番をセットする
	 * @return pnum 格納リストの順番
	 */
	public int getListNum() {
		return this.pnum ;
	}

	/**
	 * ピークバーの描画色をセットする
	 * @param idNum 表示するスペクトルの順番
	 * @param 格納リストの順番
	 */
	public void setBarColor( int idNum, int index, int colotTblNum ) {
		ArrayList<MzInfo> mzInfo = mzInfoList[idNum];
		MzInfo mzs = mzInfo.get( this.pnum - 1 );
		mzs.barColor.set( index, colotTblNum );
	}

	/**
	 * ピークバーの描画色を取得する
	 * @param  idNum 表示するスペクトルの順番
	 * @return 格納リストの順番
	 */
	public ArrayList<Integer> getBarColor( int idNum ) {
		ArrayList<MzInfo> mzInfo = mzInfoList[idNum];
		MzInfo mzs = mzInfo.get( this.pnum - 1 );
		return mzs.barColor;
	}

	/**
	 * ヒットしたピークを格納する
	  * @param  hitMzInfo cgiからの返された内容
	  * @param  isDiff ture:ピーク差検索 / false:ピーク検索
	  * @return 格納したリスト
	 */
	public ArrayList<MzInfo> setMz( String[] hitMzInfo, boolean isDiff ) {
		ArrayList<MzInfo> mzInfoList = new ArrayList<MzInfo>();
		MzInfo mzInfo = new MzInfo();
		// ピーク差検索の場合
		if ( isDiff ) {
			double diffmz = 0;
			double diffmz2 = 0;
			for ( int i = 0; i < hitMzInfo.length; i++ ) {
				String[] val = hitMzInfo[i].split(",");
				diffmz = Double.parseDouble(val[0]);
				if ( diffmz != diffmz2 ) {
					if ( i > 0 ) {
						mzInfoList.add(mzInfo);
					}
					mzInfo = new MzInfo();
					mzInfo.diffmz = val[0];
				}
				if ( val.length > 1  ) {
					double mz1 = Double.parseDouble(val[1]);
					double mz2 = Double.parseDouble(val[2]);
					mzInfo.mz1Ary.add(mz1);
					mzInfo.mz2Ary.add(mz2);
				}
				mzInfo.barColor.add(null);
				diffmz2 = diffmz;
			}
		}
		// ピーク検索の場合
		else {
			for ( int i = 0; i < hitMzInfo.length; i++ ) {
				double mz1 = Double.parseDouble(hitMzInfo[i]);
				mzInfo.mz1Ary.add(mz1);
				mzInfo.barColor.add(0);
			}
		}
		mzInfoList.add(mzInfo);
		return mzInfoList;
	}

	/**
	 * ピーク差（検索条件のm/z）の値を取得する
	  * @return ピーク差値のリスト
	 */
	public String[] getDiffMz( int idNum ) {
		String[] diffmzs = new String[ mzInfoList[idNum].size() ]; 
		for ( int i = 0; i < mzInfoList[idNum].size(); i++) {
			MzInfo MzInfo = mzInfoList[idNum].get(i);
			diffmzs[i] = String.valueOf(MzInfo.diffmz);
		}
		return diffmzs;
	}

	/**
	 * ヒットしたピークを取得する(1)
	  * @param  idNum 表示するスペクトルの順番
	  * @return ヒットしたピークのリスト
	 */
	public ArrayList<Double> getMz1( int idNum ) {
		return getMzList( idNum, this.pnum, 1 );
	}

	/**
	 * ヒットしたピークを取得する(2)
	 * ※ピーク差検索の場合のみ使用
	 * @param  idNum 表示するスペクトルの順番
	 * @return ヒットしたピークのリスト
	 */
	public ArrayList<Double> getMz2( int idNum ) {
		return getMzList( idNum, this.pnum, 2 );
	}

	/**
	 * ピークのリストを取得する
	 */
	private ArrayList<Double> getMzList( int idNum, int pnum, int flg ) {
		ArrayList<MzInfo> mzInfo = mzInfoList[idNum];
		MzInfo mzs = mzInfo.get( pnum - 1 );
		ArrayList<Double> mzAry = new ArrayList<Double>();
		if ( flg == 0 || flg == 1 ) {
			mzAry.addAll( mzs.mz1Ary );
		}
		if ( flg == 0 || flg == 2 ) {
			mzAry.addAll( mzs.mz2Ary );
		}
		return mzAry;
	}

	/**
	 * ヒットしたピークのm/zを格納するクラス
	 */
	public class MzInfo 
	{
		String diffmz = "";
		ArrayList<Double> mz1Ary = new ArrayList<Double>();
		ArrayList<Double> mz2Ary = new ArrayList<Double>();
		ArrayList<Integer> barColor = new ArrayList<Integer>();
	}
}

