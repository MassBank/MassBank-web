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
 * 化学式を扱うユーティリティクラス
 *
 * ver 1.0.1 2012.11.01
 *
 ******************************************************************************/
package massbank.extend;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.commons.lang3.math.NumberUtils;

public class ChemicalFormulaUtils {

	/**
	 * 分子式を元素記号と個数に分解する
	 */
	public static Map<String, Integer> getAtomList(String formula) {
		Map<String, Integer> atomList = new HashMap();
		int startPos = 0;
		int endPos = formula.length();
		int i = 0;
		for ( int pos = 1; pos <= endPos; pos++ ) {
			String chr = "";
			if ( pos < endPos ) {
				chr = formula.substring( pos, pos + 1 );
			}
			if ( pos == endPos || (!NumberUtils.isCreatable(chr) && chr.equals(chr.toUpperCase())) ) {
				// 元素記号 + 個数を切り出す
				String item = formula.substring( startPos, pos );

				// 元素記号と個数を分解
				boolean isFound = false;
				for ( i = 1; i < item.length(); i++ ) {
					chr = item.substring(i, i + 1);
					if ( NumberUtils.isCreatable(chr) ) {
						isFound = true;
						break;
					}
				}
				String atom = item.substring(0, i);
				int num = 1;
				if ( isFound ) {
					num = Integer.parseInt(item.substring(i));
				}
				// 元素が同じ場合
				if ( atomList.get(atom) != null ) {
					num = num + atomList.get(atom);
				}
				// 値格納
				atomList.put(atom, num);

				startPos = pos;
			}
		}
		return atomList;
	}

	/**
	 * 分子式の元素記号を並び替える
	 */
	public static String swapFormula(String formula) {

		// 元素記号の順番 C, H 以降はアルファベット順
		String[] atomSequece = new String[]{
			"C", "H", "Cl", "F", "I", "N", "O", "P", "S", "Si"
		};
		Map<String, Integer> atomList = getAtomList(formula);
		String swapFormula = "";
		Set keys = atomList.keySet();
		for ( int i = 0; i < atomSequece.length; i++ ) {
			for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {

				String atom = (String)iterator.next();
				int num = atomList.get(atom);

				if ( atom.equals(atomSequece[i]) )  {
					swapFormula += atom;
					// 個数が1個の場合、個数は書かない
					if ( num > 1 ) {
						swapFormula += String.valueOf(num);
					}
					break;
				}
			}
		}
		return swapFormula;
	}

	/**
	 * イオンの分子式と質量の対応リストを取得する
	 */
	public static List<String[]> getIonMassList() throws IOException {
		List<String[]> massList = new ArrayList();
		try {
	
			Class.forName("org.mariadb.jdbc.Driver");
			String conUrl = "jdbc:mysql://127.0.0.1/FORMULA_STRUCTURE_RELATION";
			Connection con = DriverManager.getConnection(conUrl, "bird", "bird2006");
			Statement stmt = con.createStatement();
			String sql = "SELECT FORMULA, MASS FROM ION_MASS order by MASS";
			ResultSet rs = stmt.executeQuery(sql);
			while ( rs.next() ) {
				String formula = rs.getString("FORMULA");
				String mass = rs.getString("MASS");
				massList.add(new String[]{formula,mass});
			}
			rs.close();
			stmt.close();
			con.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return massList;
	}

	/**
	 * m/zを配列に格納した値を取得する
	 */
	public static String[] getMzArray(String peakString, int cutoff) {
		List<String> mzList = new ArrayList();
		String[] peaks = peakString.split(";");
		for ( String peak: peaks ) {
			String val = peak.trim();
			String[] pair = val.split(",");
			String mz = pair[0];
			int inte = Integer.parseInt(pair[1]);
			if ( inte >= cutoff ) {
				mzList.add(mz);
			}
		}
		Collections.sort(mzList, new Comparator(){
			public int compare(Object obj1, Object obj2){
				String s1 = (String) obj1;
				String s2 = (String) obj2;
				return Double.valueOf(s1).compareTo(Double.valueOf(s2));
			}
		});
		return mzList.toArray(new String[]{});
	}

	/**
	 * m/z値が質量リストと一致した分子式を取得する
	 */
	public static String[] getMatchedFormulas(
		String[] mzs, double peakTolerance, List<String[]>massList) throws IOException {
		List<String> formulaList = new ArrayList();
		for ( String mz: mzs ) {
			double dblMz = Double.parseDouble(mz);
			double min = dblMz - peakTolerance;
			double max = dblMz + peakTolerance;
			for ( String[] items: massList ) {
				String formula = items[0];
				String mass = items[1];
				double dblMass = Double.parseDouble(mass);
				if ( min <= dblMass && dblMass <= max ) {
					if ( !formulaList.contains(formula) ) {
						formulaList.add(formula);
					}
				}
				else if ( max < dblMass ) {
					break;
				}
			}
		}
		return formulaList.toArray(new String[]{});
	}

	/*
	 *
	 */
	public static String getNLoss(String formula1, String formula2) {
		Map<String, Integer> atomList1 = getAtomList(formula1);
		Map<String, Integer> atomList2 = getAtomList(formula2);
		String nloss = "";
		int foundCnt = 0;
		for ( Map.Entry<String, Integer> e1 : atomList1.entrySet()) {
			String atom1 = e1.getKey();
			int num1 = e1.getValue();
			boolean isFound = false;
			for ( Map.Entry<String, Integer> e2 : atomList2.entrySet()) {
				String atom2 = e2.getKey();
				int num2 = e2.getValue();
				if ( atom1.equals(atom2) ) {
					if ( num1 >= num2 ) {
						if ( num1 - num2 == 1 ) {
							nloss += atom1;
						}
						else if ( num1 - num2 > 1 ) {
							nloss += atom1 + String.valueOf(num1 - num2);
						}
						isFound = true;
						break;
					}
					else {
						return "";
					}
				}
			}
			if ( isFound ) {
				foundCnt++;
			}
			else {
				nloss += atom1;
				if ( num1 > 1 ) {
					nloss += String.valueOf(num1);
				}
			}
		}
		if ( foundCnt < atomList2.size() ) {
			return "";
		}
		return swapFormula(nloss);
	}
}
