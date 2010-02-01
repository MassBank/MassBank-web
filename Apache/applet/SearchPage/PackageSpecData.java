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
 * スペクトル情報格納 クラス
 *
 * ver 1.0.4 2010.02.01
 *
 ******************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.JTable;

/**
 * スペクトル情報格納 クラス
 * 
 * スペクトル一括表示用データクラス
 * レコード情報を一括で保持するデータクラス
 */
public class PackageSpecData {
	
	/** レコードソートキー(ソート無し) */
	public static final int SORT_KEY_NONE = -1;
	
	/** レコードソートキー(スコア) */
	public static final int SORT_KEY_SCORE = 0;
	
	/** レコードソートキー(化合物名) */
	public static final int SORT_KEY_NAME = 1;
	
	/** レコード情報 */
	private ArrayList<PackageRecData> recInfo = new ArrayList<PackageRecData>();
	
	/** レコード数 */
	private int recNum = 0;
	
	/** 選択済みピークm/zリスト */
	private TreeSet<Double> selectedPeakList = new TreeSet<Double>();
	
	/**
	 * デフォルトコンストラクタ
	 */
	public PackageSpecData() {
	}
	
	/**
	 * レコード情報初期化(全レコード)
	 */
	public void initAllData() {
		recInfo = new ArrayList<PackageRecData>();
		recNum = 0;
		selectedPeakList = new TreeSet<Double>();
	}
	
	/**
	 * レコード情報初期化(検索結果レコードのみ)
	 */
	public void initResultData() {
		PackageRecData queryRecData = null;
		for (int i=0; i<recInfo.size(); i++) {
			// クエリーレコード退避
			if(recInfo.get(i).isQueryRecord()) {
				queryRecData = recInfo.get(i);
				break;
			}
		}
		recInfo = new ArrayList<PackageRecData>();
		if (queryRecData != null) {
			queryRecData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
			recInfo.add(queryRecData);
		}
		recNum = recInfo.size();
		selectedPeakList = new TreeSet<Double>();
	}
	
	/**
	 * レコード数取得
	 * @return レコード数
	 */
	public int getRecNum() {
		return recNum;
	}

	/**
	 * レコード情報取得
	 * @return レコード情報
	 */
	public ArrayList<PackageRecData> getRecInfo() {
		return recInfo;
	}
	
	/**
	 * レコード情報取得(インデックス指定)
	 * @param index インデックス
	 * @return レコードデータ
	 */
	public PackageRecData getRecInfo(int index) {
		return recInfo.get(index);
	}
	
	/**
	 * レコード情報追加
	 * @param recData レコードデータ
	 */
	public void addRecInfo(PackageRecData recData) {
		this.recInfo.add(recData);
		this.recNum = recInfo.size();			// レコード数の設定も同時に行う
	}
	
	/**
	 * レコード情報ソート(ソートキー指定)
	 * @param sortKey ソートキー
	 */
	public void sortRecInfo(int sortKey) {
		Collections.sort(recInfo, new RecInfoComparator(sortKey));
	}
	
	/**
	 * レコード情報ソート(テーブル順)
	 * @param t レコード情報ソートの元となるテーブル
	 */
	public void sortRecInfo(JTable t) {
		if(recNum == 0) {
			return;
		}
		
		int idColumn = t.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_ID);
		int typeColumn = t.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_TYPE);
		String idVal = "";
		String typeVal = "";
		
		// 現在のレコード情報を退避
		ArrayList tmpRecInfo = (ArrayList)recInfo.clone();
		recInfo.clear();
		
		// 指定されたインデックスリスト順で並び替え
		PackageRecData recData;
		for (int i=0; i<t.getRowCount(); i++) {
			idVal = String.valueOf(t.getValueAt(i, idColumn));
			typeVal = String.valueOf(t.getValueAt(i, typeColumn));
			for (int j=0; j<tmpRecInfo.size(); j++) {
				recData = (PackageRecData)tmpRecInfo.get(j);
				if ( idVal.equals(recData.getId()) ) {
					if ( (typeVal.indexOf(PackageViewPanel.INTEGRATE_RECORD) == -1 
							&& typeVal.equals(PackageViewPanel.QUERY_RECORD) == recData.isQueryRecord()) 
							|| (typeVal.indexOf(PackageViewPanel.INTEGRATE_RECORD) != -1
								&& typeVal.equals(PackageViewPanel.QUERY_RECORD + PackageViewPanel.INTEGRATE_RECORD) == recData.isQueryRecord() ))
					{
						recInfo.add(recData);
						tmpRecInfo.remove(j);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 全レコード情報内のピーク選択フラグ初期化
	 */
	public void initAllSelectedPeak() {
		// 各レコード情報内のピーク選択フラグ初期化メソッド呼び出し
		for (int i=0; i<recNum; i++) {
			recInfo.get(i).initSelectPeak();
		}
	}
	
	/**
	 * 選択済みピークの確認
	 * @param mz 確認したいm/z
	 * @return 結果(選択済：true、未選択：false)
	 */
	public boolean containsSelectedPeak(String mz) {
		return selectedPeakList.contains(Double.valueOf(mz));
	}
	
	/**
	 * 選択済みピークリストの初期化
	 */
	public void clearSelectedPeakList() {
		selectedPeakList.clear();
	}
	
	/**
	 * 選択済みピークリストへの登録
	 * @param mz 選択済みとするm/z
	 */
	public void addSelectedPeakList(String mz) {
		selectedPeakList.add(Double.parseDouble(mz));
	}
	
	/**
	 * 選択済みピークリストからの削除
	 * @param mz 選択解除するm/z
	 */
	public void removeSelectedPeakList(String mz) {
		selectedPeakList.remove(Double.valueOf(mz));
	}
	
	/**
	 * 選択済みピークリストの取得
	 * @return 選択済みピークリスト
	 */
	public TreeSet<Double> getSelectedPeakList() {
		return selectedPeakList;
	}
	
	/**
	 * 選択済みピーク数取得
	 * @retrun 選択済ピーク数
	 */
	public int getSelectedPeakNum() {
		return selectedPeakList.size();
	}
	
	/**
	 * マッチピーク情報設定
	 * クエリーレコードのピークに対して合致したピークを色づけするため、
	 * PackeageRecDataクラスにピーク色情報を設定する。
	 *  クエリーレコードのピークに完全一致の場合
	 *   ・クエリーレコードのピーク：赤
	 *   ・比較レコードのピーク：赤
	 *  クエリーレコードのピークに範囲内一致の場合
	 *   ・クエリーレコードのピーク：赤
	 *   ・比較レコードのピーク：マゼンタ
	 * @param tolVal Tolerance入力値
	 * @param tolUnit Tolerance単位(true：unit、false：ppm)
	 */
	public void setMatchPeakInfo(float tolVal, boolean tolUnit) {
		
		// クエリーレコード取得とピーク色初期化
		PackageRecData queryRecData = null;
		for (int i=0; i<recNum; i++) {
			if ( recInfo.get(i).isQueryRecord() ) {
				queryRecData = recInfo.get(i);
				queryRecData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
				break;
			}
		}
		
		// クエリーレコードとの比較
		long qMz;
		long cMz;
		int qIts = 0;
		int cIts = 0;
		long minusRange;
		long plusRange;
		final int TO_INTEGER_VAL = 100000;	// 丸め誤差が生じるため整数化するのに使用
		PackageRecData compRecData = null;
		for (int i=0; i<recNum; i++) {
			if ( !recInfo.get(i).isQueryRecord() ) {
				// 比較用レコード取得とピーク色初期化
				compRecData = recInfo.get(i);
				compRecData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
				
				if (queryRecData == null) {
					continue;
				}
				// 非表示中レコードは比較対象としない
				if (queryRecData.isDisable() || compRecData.isDisable()) {
					continue;
				}
				
				for (int queryPeakI=0; queryPeakI<queryRecData.getPeakNum(); queryPeakI++) {
					qMz = (long)(queryRecData.getMz(queryPeakI) * TO_INTEGER_VAL);
					qIts = queryRecData.getIntensity(queryPeakI);
					
					if (qIts < SearchPage.CUTOFF_THRESHOLD) {
						continue;
					}
					
					// unitの場合
					if (tolUnit) {
						minusRange = qMz - (int)(tolVal * TO_INTEGER_VAL);
						plusRange = qMz + (int)(tolVal * TO_INTEGER_VAL);
					}
					// ppmの場合
					else {
						minusRange = (long)(qMz * (1 - tolVal / 1000000));
						plusRange = (long)(qMz * (1 + tolVal / 1000000));
					}
					
					for (int compPeakI=0; compPeakI<compRecData.getPeakNum(); compPeakI++) {
						cMz = (long)(compRecData.getMz(compPeakI) * TO_INTEGER_VAL);
						cIts = compRecData.getIntensity(compPeakI);
						
						if (cIts < SearchPage.CUTOFF_THRESHOLD) {
							continue;
						}
						
						if (minusRange <= cMz && cMz <= plusRange) {
							if (qMz == cMz) {
								compRecData.setPeakColorType(compPeakI, PackageRecData.COLOR_TYPE_RED);
							}
							else if(compRecData.getPeakColorType(compPeakI) != PackageRecData.COLOR_TYPE_RED) {
								compRecData.setPeakColorType(compPeakI, PackageRecData.COLOR_TYPE_MAGENTA);
							}
							queryRecData.setPeakColorType(queryPeakI, PackageRecData.COLOR_TYPE_RED);
						}
					}
				}
			}
		}
	}
	
	/**
	 * レコード情報ソート用コンパレータ
	 * PeckageSpecDataのインナークラス。
	 * PackageRecDataを格納したリストのソートを行う。
	 * ソート処理を行うが、どのようなソートを行った場合でも
	 * 必ず最後尾からクエリーレコード、統合レコード、実測レコードの順に並ぶ。
	 * ソートキーが指定された場合は実測レコードの順が並び変わる。
	 */
	class RecInfoComparator implements Comparator<Object> {
		
		/** ソートキー */
		private int sortKey = PackageSpecData.SORT_KEY_NONE;
		
		/**
		 * コンストラクタ
		 * @param sortKey ソートキー
		 */
		public RecInfoComparator(int sortKey) {
			this.sortKey = sortKey;
		}
		
		public int compare(Object o1, Object o2){
			PackageRecData e1 =(PackageRecData)o1;
			PackageRecData e2 =(PackageRecData)o2;
			
			int ret = 0;
			
			// ソートキーソート処理
			if (sortKey == PackageSpecData.SORT_KEY_NAME) {
				ret = (e2.getName()).compareTo(e1.getName());
			}
			else if (sortKey == PackageSpecData.SORT_KEY_SCORE) {
				// スコアによるソートは文字列も含まれる場合があるので考慮する
	    		boolean e1String = false;
	    		boolean e2String = false;
	    		try {
	    			Double.parseDouble(e1.getScore());
	    		} catch (NumberFormatException e) {
	    			e1String = true;
	    		}
	    		try {
	    			Double.parseDouble(e2.getScore());
	    		} catch (NumberFormatException e) {
	    			e2String = true;
	    		}
	    		if (e1String && e2String) {
	    			ret = o1.toString().compareTo(o2.toString());
	    		}
	    		else if (e1String && !e2String) {
	    			ret = 1;
	    		}
	    		else if (!e1String && e2String) {
	    			ret = -1;
	    		}
	    		else {
	    			ret = Double.valueOf((e1.getScore())).compareTo(Double.valueOf(e2.getScore()));
	    		}
			}
			else {
				// ソートキー指定なしの場合はIDでソート
				ret = (e1.getId()).compareTo(e2.getId());
			}
			
			// 固定ソート処理
			if (e1.isQueryRecord() && !e2.isQueryRecord()) {
				ret = 1;
			}
			else if (e2.isQueryRecord() && !e1.isQueryRecord()) {
				ret = -1;
			}
			else if (e1.isIntegRecord() && !e2.isIntegRecord()) {
				ret = 1;
			}
			else if (e2.isIntegRecord() && !e1.isIntegRecord()) {
				ret = -1;
			}
			return ret;
		}
	}
}
