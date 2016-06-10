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
 * 検索結果レコード情報格納クラス
 * 検索にヒットした1スペクトルの情報を格納するデータクラス
 *   提供機能
 *     ・ノードグループ取得
 *     ・ノードグループ設定
 *     ・サイト取得
 *     ・サイト設定
 *     ・精密質量取得
 *     ・精密質量設定
 *     ・精密質量（ソート用）取得
 *     ・精密質量（表示用）取得
 *     ・組成式取得
 *     ・組成式設定
 *     ・組成式（ソート用）取得
 *     ・ID取得
 *     ・ID設定
 *     ・イオン取得
 *     ・イオン設定
 *     ・レコード情報取得
 *     ・レコード情報設定
 *     ・化合物名取得
 *     ・化合物名（ソート用）取得
 *     ・付加情報取得
 *     ・付加情報（ソート用）取得
 *     ・親ノード名リンク（表示用）取得
 *     ・子ノード名リンク（表示用）取得
 *
 * ver 1.0.3 2008.12.05
 *
 ******************************************************************************/
package massbank;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ResultRecord {
	
	/** ノードグループ */
	private int nodeGroup = -1;
	
	/** レコード情報 */
	private String info = "";			// セミコロン区切りレコード情報文字列
	
	/** 化合物名 */
	private String name = "";

	/** 化合物名（ソート用） */
	private String sortName = "";
	
	/** 付加情報 */
	private String addition = "";
	
	/** 付加情報（ソート用） */
	private String sortAddition = "";
	
	/** レコードID */
	private String id = "";
	
	/** イオン */
	private String ion = "";
	
	/** 組成式 */
	private String formula = "";
	
	/** 組成式（ソート用） */
	private String sortFormula = "";
	
	/** 精密質量 */
	private String emass = "";
	
	/** 精密質量（ソート用） */
	private float sortEmass = 0.0f;
	
	/** 精密質量（表示用） */
	private String dispEmass = "";
	
	/** サイト */
	private String contributor = "";
	
	/** 親ノード名リンク（表示用） */
	private String parentLink = "";
	
	/** 子ノード名リンク（表示用） */
	private String childLink = "";
	
	/** 原子数フォーマット */
	private DecimalFormat numFormat = new DecimalFormat("000");
	
	/** 精密質量小数点以下フォーマット */
	private DecimalFormat backPeriodFormat = new DecimalFormat("0.00000");
	
	
	/**
	 * コンストラクタ
	 */
	public ResultRecord() {
	}

	/**
	 * ノードグループ取得
	 * @return ノードグループ
	 */
	public int getNodeGroup() {
		return nodeGroup;
	}

	/**
	 * ノードグループ設定
	 * @param nodeGroup ノードグループ
	 */
	public void setNodeGroup(int nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	
	/**
	 * サイト取得
	 * @return サイト
	 */
	public String getContributor() {
		return contributor;
	}

	/**
	 * サイト設定
	 * @param contributor サイト
	 */
	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	/**
	 * 精密質量取得
	 * @return 精密質量
	 */
	public String getEmass() {
		return emass;
	}
	
	/**
	 * 精密質量設定
	 * @param emass 精密質量
	 */
	public void setEmass(String emass) {
		
		// 精密質量設定
		this.emass = emass;
		
		// 精密質量からソート用精密質量設定
		if (!emass.equals("") && emass.length() != 0) {
		this.sortEmass = Float.parseFloat(emass);
		}
		
		// 精密質量から表示用精密質量設定
		StringBuffer dispEmass = new StringBuffer();
		if (!emass.equals("") && emass.length() != 0) {
			
			String forePeriod = "";	// ピリオド前
			String backPeriod = "";	// ピリオド以降
			if (emass.indexOf(".") != -1) {
				forePeriod = emass.substring(0, emass.indexOf("."));
				backPeriod = emass.substring(emass.indexOf("."));
			}
			else {
				forePeriod = emass;
			}
			
			// 整数部を3桁にフォーマット（左空白詰め）
			for (int i=4; i>0; i--) {
				if (i == forePeriod.length()) {
					dispEmass.append(forePeriod);
					break;
				}
				else {
					dispEmass.append("&nbsp;&nbsp;");
				}
			}
			
			// 小数部を小数点付き5桁にフォーマット（右0詰め）
			backPeriod = "0" + backPeriod;
			backPeriod = backPeriodFormat.format(Double.parseDouble(backPeriod));
			backPeriod = backPeriod.substring(backPeriod.indexOf("."));
			
			// 整数部＋小数部（小数点付き）
			dispEmass.append(backPeriod);
		}
		this.dispEmass = dispEmass.toString();
	}
	
	/**
	 * 精密質量（ソート用）取得
	 * @return 精密質量（ソート用）
	 */
	public float getSortEmass() {
		return sortEmass;
	}
	
	/**
	 * 精密質量（表示用）取得
	 * @return 精密質量（表示用）
	 */
	public String getDispEmass() {
		return dispEmass;
	}
	
	/**
	 * 組成式取得
	 * @return 組成式
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * 組成式設定
	 * @param formula 組成式
	 */
	public void setFormula(String formula) {
		
		// 組成式設定
		this.formula = formula;
		
		// 組成式からソート用組成式設定
		StringBuffer sortFormula = new StringBuffer();
		if (!formula.equals("") && formula.length() != 0) {
			
			int current = 0;									// 現文字インデックス
			int next = 0;										// 次文字インデックス
			char[] c = formula.toCharArray();					// 組成式文字配列
			String symbol = String.valueOf(c[current]);			// 原子記号
			String num = "";									// 原子数
			
			for (current=0; current<c.length; current++) {
				next = current + 1;
				
				// 次の文字が最後の文字、または大文字英数字の場合
				if (next == c.length || Character.isUpperCase(c[next])) {
					sortFormula.append(symbol);
					if (!num.equals("")) {
						sortFormula.append(numFormat.format(Integer.parseInt(num)));
					}
					else {
						sortFormula.append(numFormat.format(1));
					}
					// 次の文字が存在する場合
					if (next != c.length) {
						symbol = String.valueOf(c[next]);
						num = "";
					}
				}
				// 次の文字が小文字英数字の場合
				else if (Character.isLowerCase(c[next])) {
					symbol += String.valueOf(c[next]);
				}
				// 次の文字が数字の場合
				else if (Character.isDigit(c[next])) {
					num += String.valueOf(c[next]);
				}
			}
		}
		this.sortFormula = sortFormula.toString();
	}

	/**
	 * 組成式（ソート用）取得
	 * @return 組成式（ソート用）  
	 */
	public String getSortFormula() {
		return sortFormula;
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
	 * イオン取得
	 * @return イオン
	 */
	public String getIon() {
		return ion;
	}
	
	/**
	 * イオン設定
	 * @param ion イオン
	 */
	public void setIon(String ion) {
		this.ion = ion;
	}
	
	/**
	 * レコード情報取得
	 * @return レコード情報
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * レコード情報設定
	 * @param info レコード情報
	 */
	public void setInfo(String info) {
		// レコード情報設定
		this.info = info;
		
		String[] tmp = info.split(";");
		
		// 化合物名設定
		this.name = tmp[0].trim();
		
		// ソート用化合物名設定
		this.sortName = tmp[0].trim();
		
		// 付加情報設定
		StringBuffer addition = new StringBuffer();
		for (int i=0; i<tmp.length; i++) {
			if (i==0) {
				continue;
			}
			addition.append(tmp[i].trim());
			if (i != (tmp.length-1)) {
				addition.append("; ");
			}
		}
		this.addition = addition.toString();
		
		// ソート用付加情報設定
		this.sortAddition = addition.toString();
		
		// リンク用親ノード名設定
		final int maxLinkStr = 50;
		if (tmp[0].trim().length() > maxLinkStr) {
			StringBuffer parentLink = new StringBuffer();
			parentLink.append(tmp[0].trim().substring(0, maxLinkStr));
			parentLink.append("...");
			this.parentLink = sanitize(parentLink.toString());
		}
		else {
			this.parentLink = sanitize(tmp[0].trim());
		}
		
		// リンク用子ノード名設定
		this.childLink = sanitize(addition.toString());
	}

	/**
	 * 化合物名取得
	 * @return 化合物名
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 化合物名（ソート用）取得
	 * @return 化合物名（ソート用）
	 */
	public String getSortName() {
		return sortName;
	}
	
	/**
	 * 付加情報取得
	 * @return 付加情報
	 */
	public String getAddition() {
		return addition;
	}
	
	/**
	 * 付加情報（ソート用）取得
	 * @return 付加情報（ソート用）
	 */
	public String getSortAddition() {
		return sortAddition;
	}
	
	/**
	 * 親ノード名リンク（表示用）取得
	 * @return 親ノード名リンク（表示用）
	 */
	public String getParentLink() {
		return parentLink;
	}
	
	/**
	 * 子ノード名リンク（表示用）取得
	 * @return 子ノード名リンク（表示用）
	 */
	public String getChildLink() {
		return childLink;
	}

	/**
	 * サニタイジング処理
	 * @param value サニタイジングする文字列
	 * @return サニタイジングした文字列
	 */
	private String sanitize(String value) {
		return value.replace("&", "&amp;")
					 .replace("<", "&lt;")
					 .replace(">", "&gt;")
					 .replace("\"", "&quot;");
		
	}
}
