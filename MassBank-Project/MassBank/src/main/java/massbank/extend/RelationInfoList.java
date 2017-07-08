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
 * 分子式と部分構造の関係情報を格納したデータクラス
 * 
 * ver 1.0.0 2011.12.06
 *
 ******************************************************************************/
package massbank.extend;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;

public class RelationInfoList {
	 private List<RelationInfo> infoList = new ArrayList();

	/**
	 * コンストラクタ
	 */
	public RelationInfoList(String ionMode) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		String conUrl = "jdbc:mysql://localhost/FORMULA_STRUCTURE_RELATION";
		Connection con = DriverManager.getConnection(conUrl, "bird", "bird2006");
		Statement stmt = con.createStatement();
		String sql = "select RELATION_NO,FORMULA1,FORMULA2,S_PRECISION,S_RECALL,S_TRUE_POSI "
					+ "from RELATION_INFO where ION_MODE=" + ionMode + " order by RELATION_NO";
		ResultSet rs = stmt.executeQuery(sql);
		while ( rs.next() ) {
			RelationInfo rInfo = new RelationInfo();
			rInfo.setRelationNo(rs.getString("RELATION_NO"));
			rInfo.setFormula1(rs.getString("FORMULA1"));
			rInfo.setFormula2(rs.getString("FORMULA2"));
			rInfo.setPrecision(rs.getString("S_PRECISION"));
			rInfo.setRecall(rs.getString("S_RECALL"));
			rInfo.setTruePosi(rs.getString("S_TRUE_POSI"));
			this.infoList.add(rInfo);
		}
		rs.close();
		stmt.close();
		con.close();
	}

	/**
	 * 関係情報の数を取得する
	 */
	public int getCount() {
		return infoList.size();
	}

	/**
	 * 指定されたインデックスの関係情報を取得する
	 */
	public RelationInfo getInfo(int index) {
		return this.infoList.get(index);
	}

	/**
	 * 指定されたリレーション番号の情報を取得する
	 */
	public RelationInfo[] getInfos(String[] relaNumbers) {
		List<RelationInfo> relaNoList = new ArrayList();
		for ( int i = 0; i < getCount(); i++ ) {
			RelationInfo rInfo = getInfo(i);
			String getRelaNo = rInfo.getRelationNo();
			for ( String number: relaNumbers ) {
				if ( number.equals(getRelaNo) ) {
					relaNoList.add(rInfo);
				}
			}
		}
		RelationInfo[] relaNoAry = (RelationInfo[])relaNoList.toArray(new RelationInfo[]{});
		return relaNoAry;
	}
}
