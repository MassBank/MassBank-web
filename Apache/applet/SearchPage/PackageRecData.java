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
 * レコード情報格納 クラス
 *
 * ver 1.0.4 2010.09.16
 *
 ******************************************************************************/

import java.awt.Color;

/**
 * レコード情報格納 クラス
 * 
 * スペクトル一括表示用レコード情報データクラス
 * レコード単位でPeak情報を保持するデータクラス
 */
public class PackageRecData {
	
	/** ピーク描画色種別(黒) */
	public static final int COLOR_TYPE_BLACK = 0;
	
	/** ピーク描画色種別(赤) */
	public static final int COLOR_TYPE_RED = 1;
	
	/** ピーク描画色種別(マゼンタ) */
	public static final int COLOR_TYPE_MAGENTA = 2;
	
	/** クエリーレコードフラグ */
	private boolean queryRecord = false;
	
	/** リザルトレコードフラグ */
	private boolean resultRecord = false;
	
	/** 統合レコードフラグ */
	private boolean integRecord = false;
	
	/** ID */
	private String id;
	
	/** 化合物名 */
	private String name;
	
	/** サイト */
	private String site;
	
	/** ピーク数(m/z、強度の数) */
	private int peakNum;
	
	/** m/z */
	private double[] mz;
	
	/** 強度 */
	private int[] intensity;
	
	/** ピーク選択フラグ */
	private boolean[] selectPeak;
	
	/** プリカーサー */
	private String precursor;
	
	/** ピーク描画色種別 */
	private int[] peakColorType;
	
	/** 非表示フラグ */
	private boolean disable = false;
	
	/** スコア */
	private String score = " -";
	
	/**
	 * デフォルトコンストラクタ
	 */
	public PackageRecData() {
	}
	
	/**
	 * ID取得
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * ID設定
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * ID設定
	 * @param s IDが含まれている文字列
	 * @param findStr IDを探すための文字列
	 */
	public void setId(String s, String findStr) {
		int pos = s.indexOf(findStr);
		int posNext = 0;
		if ( pos >= 0 ) { 
			posNext = s.indexOf( "\t", pos );
			this.id = s.substring( pos + findStr.length(), posNext );
		}
		else {
			this.id = "";
		}
	}

	/**
	 * ピーク描画色種別取得
	 * @param index インデックス
	 * @return ピーク描画色
	 */
	public Color getPeakColor(int index) {
		Color peakColor = Color.BLACK;
		if (peakColorType[index] == COLOR_TYPE_RED) {
			peakColor = Color.RED;
		}
		else if (peakColorType[index] == COLOR_TYPE_MAGENTA) {
			peakColor = Color.MAGENTA;
		}
		return peakColor;
	}
	
	/**
	 * ピーク描画色種別取得
	 * @param index インデックス
	 * @return ピーク描画色種別
	 */
	public int getPeakColorType(int index) {
		return peakColorType[index];
	}

	/**
	 * ピーク描画色種別設定
	 * @param index インデックス
	 * @param peakColorType ピーク描画色種別
	 */
	public void setPeakColorType(int index, int peakColorType) {
		this.peakColorType[index] = peakColorType;
	}
	
	/**
	 * ピーク描画色種別設定(一括)
	 * @param index インデックス
	 * @param peakColorType ピーク描画色種別
	 */
	public void setPeakColorType(int peakColorType) {
		for (int i=0; i<peakNum; i++) { 
			this.peakColorType[i] = peakColorType;
		}
	}
	
	/**
	 * ヒットピーク数取得
	 * @return ヒットピーク数
	 */
	public String getHitPeakNum() {
		if (!queryRecord) {
			return " -";
		}
		int hitPeakNum = 0;
		for (int i=0; i<peakNum; i++) {
			if (peakColorType[i] == COLOR_TYPE_RED) {
				hitPeakNum++;
			}
		}
		return String.valueOf(hitPeakNum);
	}
	
	/**
	 * マッチピーク数取得
	 * @return マッチピーク数
	 */
	public String getMatchPeakNum() {
		if (queryRecord) {
			return " -";
		}
		int matchPeakNum = 0;
		for (int i=0; i<peakNum; i++) {
			if (peakColorType[i] != COLOR_TYPE_BLACK) {
				matchPeakNum++;
			}
		}
		return String.valueOf(matchPeakNum);
	}
	
	/**
	 * 強度取得
	 * @param mz m/z
	 * @return 強度
	 */
	public int getIntensity(String mz) {
		// 受け取ったm/zの強度を返却する
		// レコード中にm/zが同じで強度が異なるピークが複数存在する場合は、
		// 一番最初に見つかったm/zに対応する強度を返却
		int index = -1;
		for (int i=0; i<this.mz.length; i++) {
			if (String.valueOf(this.mz[i]).equals(mz)) {
				index = i;
				break;
			}
		}
		return intensity[index];
	}
	
	/**
	 * 強度取得
	 * @param index インデックス
	 * @return 強度
	 */
	public int getIntensity(int index) {
		return intensity[index];
	}

	/**
	 * 強度設定
	 * @param index インデックス
	 * @param intensity 強度
	 */
	public void setIntensity(int index, String intensity) {
		this.intensity[index] = Integer.parseInt(intensity);
	}

	/**
	 * 最大強度取得
	 * @param start マスレンジ(m/z)開始値
	 * @param end マスレンジ(m/z)終了値
	 * @return レコード内の指定されたマスレンジ(m/z)の間で最大の強度
	 */
	public int getMaxIntensity(double start, double end) {
		int max = 0;
		for (int i=0; i<this.peakNum; i++) {
			if (this.mz[i] > end) {
				break;
			}
			if (start <= this.mz[i]) {
				max = Math.max(max, this.intensity[i]);
			}
		}
		return max;
	}
	
	/**
	 * m/z存在確認
	 * @param mz m/z
	 * @return 結果(m/zが存在：true、m/zが存在しない：false)
	 */
	public boolean checkMz(String mz) {
		// 受け取ったm/zがレコード中に存在するかを確認
		for (int i=0; i<this.mz.length; i++) {
			if (String.valueOf(this.mz[i]).equals(mz)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * m/z取得
	 * @param index インデックス
	 * @return m/z
	 */
	public double getMz(int index) {
		return mz[index];
	}

	/**
	 * m/z設定
	 * @param index インデックス
	 * @param mz m/z
	 */
	public void setMz(int index, String mz) {
		this.mz[index] = Double.parseDouble(mz);
	}
	
	/**
	 * 最大m/zとプリカーサーの比較
	 * @return 最大m/zとプリカーサーの大きい方
	 */
	public double compMaxMzPrecusor() {
		double mzMax;
		if (mz == null || mz.length == 0) {
			mzMax = 0f;
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
	 * ピーク選択フラグ取得
	 * @param index インデックス
	 * @return ピーク選択フラグ
	 */
	public boolean isSelectPeak(int index) {
		return selectPeak[index];
	}

	/**
	 * ピーク選択フラグ取得
	 * @param mz m/z
	 * @return ピーク選択フラグ
	 */
	public boolean isSelectPeak(String mz) {
		
		int index = -1;
		for (int i=0; i<peakNum; i++) {
			if (this.mz[i] == Double.parseDouble(mz)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			return this.selectPeak[index];
		}
		else {
			return false;
		}
	}	
	
	/**
	 * ピーク選択フラグ設定
	 * @param index インデックス
	 * @param selectPeak ピーク選択フラグ
	 */
	public void setSelectPeak(int index, boolean selectPeak) {
		this.selectPeak[index] = selectPeak;
	}
	
	/**
	 * ピーク選択フラグ変更
	 * @param mz m/z
	 * @param status ピーク選択フラグ
	 */
	public void setSelectPeak(String mz, boolean status) {
		int index = -1;
		for (int i=0; i<peakNum; i++) {
			if (this.mz[i] == Double.parseDouble(mz)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			this.selectPeak[index] = status;
		}
	}
	
	/**
	 * ピーク選択フラグ初期化
	 */
	public void initSelectPeak() {
		this.selectPeak = new boolean[peakNum];
	}
	
	/**
	 * 化合物名の取得
	 * @return 化合物名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 化合物名の設定
	 * @param name 化合物名
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 化合物名の設定
	 * @param s 化合物名が含まれている文字列
	 * @param findStr 化合物名を探すための文字列
	 */
	public void setName(String s, String findStr) {
		int pos = s.indexOf(findStr);
		int posNext = 0;
		if ( pos >= 0 ) { 
			posNext = s.indexOf( "\t", pos );
			this.name = s.substring( pos + findStr.length(), posNext );
		}
		else {
			this.name = "";
		}
	}

	/**
	 * サイト取得
	 * @return サイト
	 */
	public String getSite() {
		return site;
	}

	/**
	 * サイト設定
	 * @param site サイト
	 */
	public void setSite(String site) {
		this.site = site;
	}
	
	/**
	 * プリカーサー取得
	 * @return プリカーサー
	 */
	public String getPrecursor() {
		return precursor;
	}

	/**
	 * プリカーサー設定
	 * @param precursor プリカーサー
	 */
	public void setPrecursor(String precursor) {
		this.precursor = precursor;
	}
	
	/**
	 * プリカーサー設定
	 * @param s プリカーサーが含まれている文字列
	 * @param findStr プリカーサーを探すための文字列
	 */
	public void setPrecursor(String s, String findStr) {
		int pos = s.indexOf(findStr);
		int posNext = 0;
		if ( pos >= 0 ) { 
			posNext = s.indexOf( "\t", pos );
			// IT-MS対応
			String[] precursors = s.substring( pos + findStr.length(), posNext ).split("/");
			this.precursor = precursors[precursors.length - 1];
		}
		else {
			this.precursor = "";
		}
	}

	/**
	 * ピーク数取得
	 * @return ピーク数
	 */
	public int getPeakNum() {
		return peakNum;
	}

	/**
	 * ピーク数設定
	 * 同時にm/z、強度、ピーク選択フラグの配列を初期化
	 * @param peakNum ピーク数
	 */
	public void setPeakNum(int peakNum) {
		this.peakNum = peakNum;
		this.mz = new double[peakNum];
		this.intensity = new int[peakNum];
		this.selectPeak = new boolean[peakNum];
		this.peakColorType = new int[peakNum];
	}
	
	/**
	 * 指定m/zインデックス取得
	 * 指定されたm/z以上のm/zが格納されているインデックスを取得する
	 * @param target m/z指定値
	 * @return インデックス
	 */
	public int getIndex(double target) {
		int index;
		for (index=0; index<this.peakNum; index++) {
			if (this.mz[index] >= target) {
				break;
			}
		}
		return index;
	}

	/**
	 * クエリーレコードフラグ取得
	 * @return クエリーレコードフラグ(true：クエリー、false：クエリー以外)
	 */
	public boolean isQueryRecord() {
		return queryRecord;
	}

	/**
	 * クエリーレコードフラグ設定
	 * @param queryRecord クエリーレコードフラグ(true：クエリー、false：クエリー以外)
	 */
	public void setQueryRecord(boolean queryRecord) {
		this.queryRecord = queryRecord;
	}

	/**
	 * リザルトレコードフラグ取得
	 * @return リザルトレコードフラグ(true：リザルト、false：リザルト以外)
	 */
	public boolean isResultRecord() {
		return resultRecord;
	}

	/**
	 * リザルトレコードフラグ設定
	 * @param resultRecord リザルトレコードフラグ(true：リザルト、false：リザルト以外)
	 */
	public void setResultRecord(boolean resultRecord) {
		this.resultRecord = resultRecord;
	}
	
	/**
	 * 統合レコードフラグ取得
	 * @return 統合レコードフラグ(true：統合レコード、false：ローレコード)
	 */
	public boolean isIntegRecord() {
		return integRecord;
	}

	/**
	 * 統合レコードフラグ設定
	 * @param integRecord 統合レコードフラグ（true：統合レコード、false：統合レコードではない）
	 */
	public void setIntegRecord(boolean integRecord) {
		this.integRecord = integRecord;
	}
	
	/**
	 * 統合レコードフラグ設定
	 * @param title レコードタイトル
	 */
	public void setIntegRecord(String title) {
		// RECORD_TITLEに"MERGED"が含まれていた場合に、統合スペクトルと判断する
		if ( title.indexOf("MERGED") != -1 ) {
			this.integRecord = true;
		}
	}
	
	/**
	 * 非表示フラグ取得
	 * @return 非表示フラグ(true：非表示、false：表示)
	 */
	public boolean isDisable() {
		return disable;
	}

	/**
	 * 非表示フラグ設定
	 * @param disable 非表示フラグ(true：非表示、false：表示)
	 */
	public void setDisable(boolean disable) {
		this.disable = disable;
	}

	/**
	 * スコア取得
	 * @return スコア
	 */
	public String getScore() {
		return score;
	}

	/**
	 * スコア設定
	 * @param score スコア
	 */
	public void setScore(String score) {
		if (score.length() == 0) {
			return;
		}
		this.score = score;
	}
}
