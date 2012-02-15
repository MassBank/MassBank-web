/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
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
 * クエリファイルのユーティリティクラス
 *
 * ver 1.0.0 2012.02.15
 *
 ******************************************************************************/
package massbank;

import java.util.ArrayList;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import org.apache.commons.lang.NumberUtils;


public class QueryFileUtil {

	private ArrayList<String> nameList = null;
	private ArrayList<String[]> mzList = null;
	private ArrayList<String[]> intensityList = null;
	private String filePath = "";

	/**
	 * コンストラクタ
	 */
	public QueryFileUtil(String filePath) {
		this.nameList = new ArrayList<String>();
		this.mzList = new ArrayList<String[]>();
		this.intensityList = new ArrayList<String[]>();
		this.filePath = filePath;

		String line = "";
		String name = "";
		String peak = "";
		int lineNo = 0;
		int cnt = 1;

		ArrayList<String[]> queryList = new ArrayList();
		try {
			File f = new File(filePath);
			BufferedReader in = new BufferedReader(new FileReader(f));
			while ( ( line = in.readLine() ) != null ) {
				line = line.trim();

				// コメント行はスキップする
				if ( line.startsWith("//") ) {
					continue;
				}
				// NAMEタグ
				else if ( line.matches("^Name:.*") ) {
					name = line.replaceFirst("^Name: *", "").trim();
				}
				else if ( line.matches(".*:.*") ) { }
				else if ( line.equals("") ) {
					if ( lineNo > 0 ) {
						addList(name, peak);
						cnt++;
						name = "";
						peak = "";
						lineNo = 0;
					}
				}
				else {
					peak += line;
					if ( !line.substring(line.length()-1).equals(";") ) {
						peak += ";";
					}
					lineNo++;
				}
			}
			in.close();
			if ( lineNo > 0 ) {
				addList(name, peak);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 名前のリストを取得する
	 */
	public String[] getNameList() {
		return this.nameList.toArray(new String[0]);
	}

	/**
	 * m/zのリストを取得する
	 */
	public String[] getMzs(int index) {
		return this.mzList.get(index);
	}

	/**
	 * 強度のリストを取得する
	 */
	public String[] getAbsIntensities(int index) {
		return this.intensityList.get(index);
	}


	/**
	 * 相対強度のリストを取得する
	 */
	public String[] getRelIntensities(int index) {

		double max = 0;
		double absInte = 0;
		String[] relIntes = intensityList.get(index);
		int num = relIntes.length;
		for ( int i = 0; i < num; i++) {
			absInte = Double.parseDouble(relIntes[i]);
			if ( absInte > max ) {
				max = absInte;
			}
		}

		String[] vals = new String[num];
		for (int i = 0; i < num; i++) {
			absInte = Double.parseDouble(relIntes[i]);
			int relInte = new Double(absInte / max * 999).intValue();
			vals[i] = String.valueOf(relInte);
		}
		return vals;
	}

	/**
	 * テンポラリファイルを削除する
	 */
	public void delete() {
		File f = new File(this.filePath);
		f.delete();
	}

	/**
	 * クエリ情報をリストに追加する
	 */
	private void addList(String name, String peak) {
		DecimalFormat df = new DecimalFormat("000000");
		int no = this.nameList.size() + 1;
		if ( name.equals("") ) {
			name = "Compound_" + df.format(no);
		}
		this.nameList.add(name);

		String[] items = peak.split(";");
		int num = items.length;
		String[] mzs = new String[num];
		String[] intes = new String[num]; 
		for ( int i = 0; i < items.length; i++) {
			String pair = items[i].trim();
			pair = pair.replaceAll(" +", ",");
			pair = pair.replaceAll("\t+", ",");
			String[] vals = pair.split(",");
			if ( vals.length < 2 || 
				 !NumberUtils.isNumber(vals[0]) || !NumberUtils.isNumber(vals[1]) ) {
				mzs[i] = ("-1");
				intes[i] = ("-1");
			}
			else {
				mzs[i] = vals[0];
				intes[i] = vals[1];
			}
			
		}
		mzList.add(mzs);
		intensityList.add(intes);
	}
}
