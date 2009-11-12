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
 * 検索結果レコード情報一括管理クラス
 * 検索にヒットしたスペクトルの情報（ResultRecordクラス）を一括で保持するデータクラス
 * Resultページに表示する全ての情報を保持するクラス
 *
 * ver 1.0.3 2008.12.17
 *
 ******************************************************************************/
package massbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ResultList {
	
	/** ソートアクション（昇順） */
	public static final int SORT_ACTION_ASC = 1;			// デフォルト
	
	/** ソートアクション（降順） */
	public static final int SORT_ACTION_DESC = -1;
	
	/** ソートキー（化合物名） */
	public static final String SORT_KEY_NAME = "name";	// デフォルト
	
	/** ソートキー（組成式） */
	public static final String SORT_KEY_FORMULA = "formula";
	
	/** ソートキー（精密質量） */
	public static final String SORT_KEY_EMASS = "emass";
	
	/** ソートキー（ID） */
	public static final String SORT_KEY_ID = "id";
	
	/** 表示する最大ページリンク数 */
	private final int DISP_LINK_NUM;
	
	/** 1ページ辺りの表示親ノード数 */
	private final int DISP_NODE_NUM;
	
	/** レコードリスト */
	private ArrayList<ResultRecord> list = new ArrayList<ResultRecord>();
	
	/**
	 * コンストラクタ（設定ファイル情報未使用）
	 * @deprecated replaced by {@link #ResultList(GetConfig conf)}
	 */
	public ResultList() {
		this.DISP_LINK_NUM = 10;							// デフォルト
		this.DISP_NODE_NUM = 25;							// デフォルト
	}
	
	/**
	 * コンストラクタ（設定ファイル情報使用）
	 * @param conf 設定ファイル情報オブジェクト
	 */
	public ResultList(GetConfig conf) {
		// ページリンク表示数
		int linkNum = 10;									// デフォルト
		try {
			if ( Integer.parseInt(conf.getDispLinkNum()) > 0 ) {
				linkNum =  Integer.parseInt(conf.getDispLinkNum());
			}
		}
		catch (NumberFormatException e) {
		}
		
		// 親ノード表示数
		int nodeNum = 25;									// デフォルト
		try {
			if ( Integer.parseInt(conf.getDispNodeNum()) > 0 ) {
				nodeNum =  Integer.parseInt(conf.getDispNodeNum());
			}
		}
		catch (NumberFormatException e) {
		}
		
		this.DISP_LINK_NUM = linkNum;
		this.DISP_NODE_NUM = nodeNum;
	}

	/**
	 * レコードリスト取得
	 * @return レコードリスト
	 */
	public ArrayList<ResultRecord> getList() {
		return list;
	}

	/**
	 * レコード取得
	 * @param index インデックス
	 * @return 結果情報一括管理リスト
	 */
	public ResultRecord getRecord(int index) {
		return list.get(index);
	}
	
	/**
	 * レコード追加
	 * @param record 結果情報レコード
	 */
	public void addRecord(ResultRecord record) {
		this.list.add(record);
	}
	
	/**
	 * レコードリストソート(ソートキー指定)
	 * @param sortKey ソートキー
	 * @param sortAction ソートアクション
	 */
	public void sortList(String sortKey, int sortAction) {
		Collections.sort(list, new ListComparator(sortKey, sortAction));
	}
	
	/**
	 * レコード数取得
	 * @return レコード数
	 */
	public int getResultNum() {
		return list.size();
	}
	
	/**
	 * 1ページに表示するレコードインデックス取得
	 * @param pageNo 表示するページ
	 * @return 表示開始と終了のインデックスを格納した配列
	 */
	public int[] getDispRecordIndex(int pageNo) {
		int[] index = new int[]{-1, -1};
		HashMap<Integer, String> pNodeMap = new HashMap<Integer, String>();
		int pageCount = 1;				// ページ数カウンタ
		int pNodeCount = 0;				// 1ページ辺りの親ノードカウンタ
		int startIndex = -1;			// 1ページに表示するレコードの開始インデックス
		int endIndex = -1;				// 1ページに表示するレコードの終了インデックス
		boolean isNextPage = false;	// 次ページフラグ
		for (int i=0; i<list.size(); i++) {
			
			// 開始インデックス設定
			if (pageNo == pageCount && startIndex == -1) {
				startIndex = i;
			}
			
			if ( (i+1) == list.size() ) {
				// 現在のレコードが最終レコードの場合
				if ( !pNodeMap.containsKey(list.get(i).getNodeGroup()) ) {
					pNodeMap.put(list.get(i).getNodeGroup(), list.get(i).getName());
					pNodeCount++;
				}
				isNextPage = true;
			}
			else if ( pNodeCount < this.DISP_NODE_NUM ) {
				// 親ノード数が表示親ノード数に達していない場合
				if ( !pNodeMap.containsKey(list.get(i).getNodeGroup()) ) {
					pNodeMap.put(list.get(i).getNodeGroup(), list.get(i).getName());
					pNodeCount++;
				}
				if ( pNodeCount == this.DISP_NODE_NUM ) {
					if ( !pNodeMap.containsKey(list.get(i+1).getNodeGroup()) ) {
						isNextPage = true;
					}
				}
			}
			else {
				// 親ノード数が表示親ノードに達した場合
				if ( !pNodeMap.containsKey(list.get(i+1).getNodeGroup()) ) {
					isNextPage = true;
				}
			}
			
			// 次ページ移行処理
			if ( isNextPage ) {
				
				// 終了インデックス設定
				if (pageCount == pageNo) {
					endIndex = i;
					break;
				}
				pNodeCount = 0;
				isNextPage = false;
				pageCount++;
			}
		}
		if (startIndex != -1 && endIndex != -1) {
			index = new int[]{startIndex, endIndex};
		}
		return index;
	}
	
	/**
	 * 総ページ数取得
	 * @return 総ページ数
	 */
	public int getTotalPageNum() {
		int num = (int)Math.ceil(((double)getCompoundNum() / this.DISP_NODE_NUM));
		return num;
	}

	/**
	 * 化合物数取得
	 * @return 化合物数
	 */
	public int getCompoundNum() {
		HashMap<Integer, String> nodeCount = new HashMap<Integer, String>();
		for (int i=0; i<list.size(); i++) {
			// 親ノード情報保持
			if (!nodeCount.containsKey(list.get(i).getNodeGroup())) {
				nodeCount.put(list.get(i).getNodeGroup(), list.get(i).getName());
			}
		}
		return nodeCount.size();
	}
	
	
	/**
	 * 表示するページリンク取得
	 * @param totalPage 総ページ数
	 * @param pageNo 現在のページ
	 * @return 表示開始と終了のページを格納した配列
	 */
	public int[] getDispPageIndex(int totalPage, int pageNo) {
		int[] index = new int[2];
		// 常に最大ページリンク数を表示するようにする
		index[0] = Math.max(1, pageNo - (int)Math.floor(this.DISP_LINK_NUM / 2.0));
		index[1] = Math.min(totalPage, pageNo + (int)Math.ceil(this.DISP_LINK_NUM / 2.0) - 1);
		
		// 表示ページの終了が最大ページリンク数より小さい場合
		if (index[1] < this.DISP_LINK_NUM) {
			index[1] = Math.min(totalPage, this.DISP_LINK_NUM);
		}
		// 表示ページ数が最大ページリンク数より小さい場合
		if ((index[1] - index[0] + 1) < this.DISP_LINK_NUM) {
			index[0] = Math.max(1, (index[1] - this.DISP_LINK_NUM + 1));
		}
		return index;
	}
	
	/**
	 * 1ページに表示する親ノードごとの子ノード数取得
	 * @param dispIndex 表示開始と終了のインデックスを格納した配列
	 * @return 1ページに表示する親ノード毎の子ノード数マップ
	 */
	public HashMap<Integer, Integer> getDispParentNodeMap(int startIndex, int endIndex) {
		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
		int nodeCnt = 0;
		for (int i=startIndex; i<list.size(); i++) {
			
			// 親ノード情報保持
			if (!nodeMap.containsKey(list.get(i).getNodeGroup())) {
				nodeCnt = 1;
				nodeMap.put(Integer.valueOf(list.get(i).getNodeGroup()), Integer.valueOf(nodeCnt));
			}
			else {
				nodeCnt = (int)nodeMap.get(list.get(i).getNodeGroup());
				nodeCnt++;
				nodeMap.put(list.get(i).getNodeGroup(), nodeCnt);
			}
			
			if ( i == endIndex ) {
				break;
			}
		}
		return nodeMap;
	}
	
	/**
	 * レコードリストソート用コンパレータ
	 * ResultListのインナークラス。
	 * ResultRecordを格納したリストのソートを行う。
	 */
	class ListComparator implements Comparator<Object> {
		
		/** ソートキー */
		private String sortKey;
		
		/** ソートアクション */
		private int sortAction;
		
		/**
		 * コンストラクタ
		 * @param sortKey ソートキー
		 */
		public ListComparator(String sortKey, int sortAction) {
			this.sortKey = sortKey;
			this.sortAction = sortAction;
		}
		
		public int compare(Object o1, Object o2){
			ResultRecord e1 =(ResultRecord)o1;
			ResultRecord e2 =(ResultRecord)o2;
			
			int ret = 0;
			
			// ソート処理
			if (sortKey == SORT_KEY_NAME) {				// Nameソート
				if (!e1.getSortName().equals(e2.getSortName())) {
					switch (sortAction) {
						case SORT_ACTION_ASC:
							ret = (e1.getSortName()).compareTo(e2.getSortName());
							break;
						case SORT_ACTION_DESC:
							ret = (e2.getSortName()).compareTo(e1.getSortName());
							break;
					}
				}
				else {
					switch (sortAction) {
						case SORT_ACTION_ASC:
							ret = (e1.getSortAddition()).compareTo(e2.getSortAddition());
							break;
						case SORT_ACTION_DESC:
							ret = (e2.getSortAddition()).compareTo(e1.getSortAddition());
							break;
					}
				}
			}
			else if (sortKey == SORT_KEY_FORMULA) {		// Formulaソート
				if (!e1.getSortFormula().equals(e2.getSortFormula())) {
					switch (sortAction) {
						case SORT_ACTION_ASC:
							ret = (e1.getSortFormula()).compareTo(e2.getSortFormula());
							break;
						case SORT_ACTION_DESC:
							ret = (e2.getSortFormula()).compareTo(e1.getSortFormula());
							break;
					}
				}
				else {
					if (!e1.getSortName().equals(e2.getSortName())) {
						ret = (e1.getSortName()).compareTo(e2.getSortName());
					}
					else {
						ret = (e1.getSortAddition()).compareTo(e2.getSortAddition());
					}
				}
			}
			else if (sortKey == SORT_KEY_EMASS) {		// ExactMassソート
				if (e1.getSortEmass() != e2.getSortEmass()) {
					switch (sortAction) {
						case SORT_ACTION_ASC:
							ret = (Float.valueOf(e1.getSortEmass())).compareTo(Float.valueOf(e2.getSortEmass()));
							break;
						case SORT_ACTION_DESC:
							ret = (Float.valueOf(e2.getSortEmass())).compareTo(Float.valueOf(e1.getSortEmass()));
							break;
					}
				}
				else {
					if (!e1.getSortName().equals(e2.getSortName())) {
						ret = (e1.getSortName()).compareTo(e2.getSortName());
					}
					else {
						ret = (e1.getSortAddition()).compareTo(e2.getSortAddition());
					}
				}
			}
			else if (sortKey == SORT_KEY_ID) {			// IDソート
				if (!e1.getSortName().equals(e2.getSortName())) {
					ret = (e1.getSortName()).compareTo(e2.getSortName());
				}
				else {
					switch (sortAction) {
						case SORT_ACTION_ASC:
							ret = e1.getId().compareTo(e2.getId());
							break;
						case SORT_ACTION_DESC:
							ret = e2.getId().compareTo(e1.getId());
							break;
					}
				}
			}
			
			return ret;
		}
	}
}
