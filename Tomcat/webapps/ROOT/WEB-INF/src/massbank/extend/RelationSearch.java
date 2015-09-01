/*******************************************************************************
 *
 * Copyright (C) 2011 MassBank Project
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
 * Relationship Searchの検索処理実行クラス
 *
 * ver 1.0.0 2011.12.06
 *
 ******************************************************************************/
package massbank.extend;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import massbank.extend.ChemicalFormulaUtils;
import massbank.extend.RelationSearchResult;

public class RelationSearch {
	private String ionMode = "";
	private String precursor = "";
	private double dblExactMass = 0;
	private double peakTolerance = 0;
	private double massTolerance = 0;
	private List<String[]> ionMassList = null;
	private String[] queryIonFormulas = null;
	private RelationInfoList rInfoList = null;
	private List<Integer> hitIndexs = new ArrayList();
	private List<RelationInfo> hitInfoList = new ArrayList();
	private TreeSet showRelaNoList = new TreeSet();
	private Map<String, RelationSearchResult> resultList = new TreeMap();
	private String[] mzs = null;


	/*
	 * コンストラクタ
	 */
	public RelationSearch(String queryPeakString, String cutoff, String ionMode) {
		this.ionMode = ionMode;
		this.mzs = ChemicalFormulaUtils.getMzArray(queryPeakString, Integer.parseInt(cutoff));
	}

	/*
	 * 検索パラメータセット(Peak Tolerance)
	 */
	public void setPeakTolerance(double val) {
		this.peakTolerance = val;
	}

	/*
	 * 検索パラメータセット(Precursor)
	 */
	public void setPrecursor(String val) {
		this.precursor =val;
		double H = 1.008;
		double dblPrecursor = Double.parseDouble(val);
		if ( this.ionMode.equals("1") ) {
			this.dblExactMass = dblPrecursor - H;
		}
		else {
			this.dblExactMass = dblPrecursor + H;
		}
	}

	/*
	 * 検索パラメータセット(Mass Tolerance)
	 */
	public void setMassTolerance(double val) {
		this.massTolerance = val;
	}

	/*
	 *
	 */
	public String[] getMarchedPrecursorFormulas() {
		try {
			return ChemicalFormulaUtils.getMatchedFormulas(new String[]{this.precursor}, this.peakTolerance, this.ionMassList);
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * マッチした分子式を取得する
	 */
	public List<Object[]> getMatchedFormulaList() {
		List<Object[]> formulaList = new ArrayList();
		for ( String formula: this.queryIonFormulas ) {
			// マッチした分子式と質量をリストに入れる
			for ( String[] items: this.ionMassList ) {
				if ( formula.equals(items[0]) ) {
					TreeSet showNoList = new TreeSet();
					String[] showRelaNumbers = getShowRelaNumbers();
					RelationInfo[] rInfos1 = this.rInfoList.getInfos(showRelaNumbers);
					for ( RelationInfo showInfo: rInfos1 ) {
						String relaNo   = showInfo.getRelationNo();
						String formula1 = showInfo.getFormula1();
						String formula2 = showInfo.getFormula2();
						if ( formula1.equals(formula) || formula2.equals(formula) ) {
							showNoList.add(relaNo);
						}
					}

					TreeSet hiddenNoList = new TreeSet();
					String[] hiddenRelaNumbers = getHiddenRelaNumbers();
					RelationInfo[] rInfos2 = this.rInfoList.getInfos(hiddenRelaNumbers);
					for ( RelationInfo hiddenInfo: rInfos2 ) {
						String relaNo   = hiddenInfo.getRelationNo();
						String formula1 = hiddenInfo.getFormula1();
						String formula2 = hiddenInfo.getFormula2();
						if ( formula1.equals(formula) || formula2.equals(formula) ) {
							hiddenNoList.add(relaNo);
						}
					}

					Object[] newItems = new Object[4];
					newItems[0] = items[0];
					newItems[1] = new BigDecimal(items[1]).setScale(4, BigDecimal.ROUND_HALF_UP).toPlainString();
					newItems[2] = showNoList;
					newItems[3] = hiddenNoList;
					formulaList.add(newItems);
				}
			}
		}
		return formulaList;
	}

	/*
	 * 検索処理
	 */
	public Map<String, RelationSearchResult> doSearch(float threshold) {
		try {
			// イオンの分子式と質量の対応リストを取得する
			this.ionMassList = ChemicalFormulaUtils.getIonMassList();

			// m/z値が質量リストと一致した分子式を取得する
			String[] queryFormulas = ChemicalFormulaUtils.getMatchedFormulas(mzs, this.peakTolerance, this.ionMassList);

			// 
			List<String> formulaList = new ArrayList();
			String[] precFormulas = getMarchedPrecursorFormulas();
			for ( String query: queryFormulas ) {
				boolean isFound = false;
				for ( String prec: precFormulas ) {
					if ( prec.equals(query) ) {
						isFound = true;
						break;
					}
					else {
						String nloss = ChemicalFormulaUtils.getNLoss(prec, query);
						if ( !nloss.equals("") ) {
							isFound = true;
							break;
						}
					}
				}
				if ( isFound ) {
					formulaList.add(query);
				}
			}
			this.queryIonFormulas = formulaList.toArray(new String[]{});


			// 分子式と部分構造の関係情報を取得する
			this.rInfoList = new RelationInfoList(this.ionMode);

			// 関係情報の分子式リストを作成する
			int no = 0;
			List<String> singleFormulas = new ArrayList();
			List<String> pairFormulas1  = new ArrayList();
			List<String> pairFormulas2  = new ArrayList();
			List<Integer> singleArrayIndexs = new ArrayList();
			List<Integer> pairArrayIndexs   = new ArrayList();
			for ( int index = 0; index < this.rInfoList.getCount(); index++ ) {
				RelationInfo rInfo1 = this.rInfoList.getInfo(index);
				String formula1 = rInfo1.getFormula1();
				String formula2 = rInfo1.getFormula2();
				float precision = Float.valueOf(rInfo1.getPrecision());
				if ( precision >= threshold ) {
					if ( formula2.equals("") ) {
						// 分子式１つの場合の関係情報
						singleFormulas.add(formula1);
						singleArrayIndexs.add(no);
					}
					else {
						// 分子式２つの組合せの関係情報
						pairFormulas1.add(formula1);
						pairFormulas2.add(formula2);
						pairArrayIndexs.add(no);
					}
				}
				no++;
			}

			// １つの分子式が一致するものを抽出する
			for ( int lp = 0; lp < this.queryIonFormulas.length; lp++) {
				for ( int i = 0; i < singleFormulas.size(); i++ ) {
					String dbFormula = singleFormulas.get(i);
					if ( this.queryIonFormulas[lp].equals(dbFormula) ) {
						int index = singleArrayIndexs.get(i);
						this.hitIndexs.add(index);
					}
				}
			}

			// ２つの分子式の組合せが一致するものを抽出する
			for ( int lp1 = 0; lp1 < this.queryIonFormulas.length; lp1++) {
				for ( int lp2 = lp1 + 1; lp2 < this.queryIonFormulas.length; lp2++) {
					for ( int i = 0; i < pairFormulas1.size(); i++ ) {
						String dbFormula1 = pairFormulas1.get(i);
						String dbFormula2 = pairFormulas2.get(i);

						if ( (this.queryIonFormulas[lp1].equals(dbFormula1) && this.queryIonFormulas[lp2].equals(dbFormula2))
						  || (this.queryIonFormulas[lp1].equals(dbFormula2) && this.queryIonFormulas[lp2].equals(dbFormula1)) ) {
							int index = pairArrayIndexs.get(i);
							this.hitIndexs.add(index);
						}
					}
				}
			}

			if ( this.hitIndexs.size() > 0 ) {
				Class.forName("com.mysql.jdbc.Driver");
				String conUrl = "jdbc:mysql://localhost/FORMULA_STRUCTURE_RELATION";
				Connection con = DriverManager.getConnection(conUrl, "bird", "bird2006");
				Statement stmt = con.createStatement();

				RelationSearchResult result = null;
				for ( int index: this.hitIndexs ) {
					RelationInfo rInfo2 = this.rInfoList.getInfo(index);
					String relaNo    = rInfo2.getRelationNo();
					String formula1  = rInfo2.getFormula1();
					String formula2  = rInfo2.getFormula2();
					String precision = rInfo2.getPrecision();
					String recall    = rInfo2.getRecall();
					String true_posi = rInfo2.getRecall();
					String sql = "select distinct C.ID,COMPOUND_NAME,EXACT_MASS,FORMULA,INCHI "
								+ "from COMPOUND_INFO C, (select distinct ID from RELATION_NO_LIST where "
								+ "RELATION_NO='" + relaNo + "') as R where ";
					if ( this.dblExactMass > 0 ) {
						double val1 = this.dblExactMass - this.massTolerance;
						double val2 = this.dblExactMass + this.massTolerance;
						double min = new BigDecimal(val1).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
						double max = new BigDecimal(val2).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
						sql += "(EXACT_MASS between " + String.valueOf(min) + " and " + String.valueOf(max) + ") and ";
					}
					sql += "C.ID=R.ID order by INCHI";
					ResultSet rs = stmt.executeQuery(sql);
					while ( rs.next() ) {
						String id = rs.getString("ID");
						String inchi = rs.getString("INCHI");
						String[] inchiLayers = inchi.split("/");
						String inchiKey = inchiLayers[1] + "/" + inchiLayers[2] + "/" + inchiLayers[3];
						boolean isRInfoFound = false;
						if ( this.resultList.containsKey(inchiKey) ) {
							result = this.resultList.get(inchiKey);
							RelationSearchResult.CompoundInfo cInfo = result.getCompoundInfo();

							// 重複するIDがないかをチェック
							boolean isIdFound = false;
							for ( int i = 0; i < cInfo.getCountId(); i++ ) {
								if ( id.equals(cInfo.getId(i)) ) {
									isIdFound = true;
									break;
								}
							}
							// 重複するIDがない場合はリストに追加する
							if ( !isIdFound ) {
								cInfo.addId(id);
							}

							// 関係情報に重複がないかをチェック
							for ( int i = 0; i < result.getCountRelationInfo(); i++ ) {
								RelationInfo rInfo3 = result.getRelationInfo(i);
								if ( relaNo.equals(rInfo3.getRelationNo())
								  && formula1.equals(rInfo3.getFormula1())
								  && formula2.equals(rInfo3.getFormula2()) ) {
									isRInfoFound = true;
									break;
								}
							}
						}
						else {
							// 化合物情報がない場合はセットする
							result = new RelationSearchResult();
							RelationSearchResult.CompoundInfo cInfo = new RelationSearchResult.CompoundInfo();
							cInfo.addId(id);
							cInfo.setName(rs.getString("COMPOUND_NAME"));
							cInfo.setExactMass(rs.getString("EXACT_MASS"));
							cInfo.setFormula(rs.getString("FORMULA"));
							result.setCompoundInfo(cInfo);
							this.resultList.put(inchiKey, result);
						}

						// 関係情報に重複がない場合はリストに追加する
						if ( !isRInfoFound ) {
							result.addRelationInfo(rInfo2);
							this.showRelaNoList.add(relaNo);
						}
					}
					rs.close();
				}
				stmt.close();
				con.close();
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return this.resultList;
	}

	/*
	 * ヒットしたすべてのリレーション情報を取得する
	 */
	public List<RelationInfo> getHitAllInfoList() {
		for ( int index: this.hitIndexs ) {
			RelationInfo rInfo = this.rInfoList.getInfo(index);
			this.hitInfoList.add(rInfo);
		}
		return this.hitInfoList;
	}

	/*
	 * ヒットしたリレーション番号を取得する
	 */
	public String[] getShowRelaNumbers() {
		return (String[])this.showRelaNoList.toArray(new String[]{});
	}

	/*
     * ピーク（分子式）と部分構造の対応は一致したが、
	 * その部分構造を含む化合物がデータベースに存在しないものの
	 * リレーション番号を取得する
	 */
	public String[] getHiddenRelaNumbers() {
		TreeSet<String> hiddenRelaNoList = new TreeSet();
		String[] showRelaNumbers = getShowRelaNumbers();
		for ( RelationInfo rInfo: this.hitInfoList ) {
			String relaNo = rInfo.getRelationNo();
			boolean isFound = false;
			for ( String showRelaNo: showRelaNumbers ) {
				if ( showRelaNo.equals(relaNo) ) {
					isFound = true;
					break;
				}
			}
			if ( !isFound ) {
				hiddenRelaNoList.add(relaNo);
			}
		}
		return (String[])hiddenRelaNoList.toArray(new String[]{});
	}

	/*
	 * クエリのm/zの値を取得する
	 */
	public String[] getQueryMzs() {
		return this.mzs;
	}
}
