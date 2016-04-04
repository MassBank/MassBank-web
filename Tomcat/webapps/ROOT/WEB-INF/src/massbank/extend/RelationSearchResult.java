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
 * Relationship Searchの検索結果を格納するデータクラス
 *
 * ver 1.0.0 2011.12.06
 *
 ******************************************************************************/
package massbank.extend;

import java.util.List;
import java.util.ArrayList;

public class RelationSearchResult {
	private CompoundInfo cInfo = null;
	private List<RelationInfo> rInfoList = new ArrayList();

	public CompoundInfo getCompoundInfo() {
		return cInfo;
	}
	public RelationInfo getRelationInfo(int index) {
		return rInfoList.get(index);
	}
	public int getCountRelationInfo() {
		return rInfoList.size();
	}
	public void setCompoundInfo(CompoundInfo info) {
		cInfo = info;
	}
	public void addRelationInfo(RelationInfo info) {
		rInfoList.add(info);
	}
	public void addRelationInfo(String[] vals) {
	 	RelationInfo rInfo = new RelationInfo();
	 	rInfo.setRelationNo(vals[0]);
		rInfo.setFormula1(vals[1]);
		rInfo.setFormula2(vals[2]);
		rInfo.setPrecision(vals[3]);
		rInfo.setRecall(vals[4]);
		rInfo.setTruePosi(vals[5]);
		rInfoList.add(rInfo);
	}

	public static class CompoundInfo {
		private List<String> ids = new ArrayList();
		private String name = "";
		private String emass = "";
		private String formula = "";

		public int getCountId() {
			return ids.size();
		}
		public String getId(int index) {
			return ids.get(index);
		}
		public String getName() {
			return name;
		}
		public String getExactMass() {
			return emass;
		}
		public String getFormula() {
			return formula;
		}
		public void addId(String val) {
			ids.add(val);
		}
		public void setName(String val) {
			name = val;
		}
		public void setExactMass(String val) {
			emass = val;
		}
		public void setFormula(String val) {
			formula = val;
		}
	}
}
