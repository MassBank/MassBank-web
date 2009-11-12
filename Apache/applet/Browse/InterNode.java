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
 * Data Model（Internal）クラス
 *
 * ver 2.0.5 2008.12.05
 *
 ******************************************************************************/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import massbank.MassBankCommon;

/**
 * Data Model（Internal）クラス
 */
@SuppressWarnings("serial")
public class InterNode extends MyTreeNode {
	
	public int	son;
	public boolean loaded;

	public InterNode(String x, String n, int s) {
		id = x;
		son = s;
		loaded = false;
		setUserObject(n);
		setAllowsChildren(true);
		add(new MyTreeNode());
	}

	public void loadSon() {
		if ( getChildCount() != 0 ) {
			removeAllChildren();
		}
		String line;
		try {
			String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GSON];
			String reqStr = BrowsePage.baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + id;
			reqStr += "&site=" + BrowsePage.site;
			
			URL url = new URL(reqStr);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			boolean isStartSpace = true;
			while ( (line = br.readLine()) != null ) {
				// 先頭スペースを読み飛ばすため
				if ( line.equals("") ) {
					if ( isStartSpace ) {
						continue;
					}
					else {
						break;
					}
				}
				else {
					isStartSpace = false;
				}
				
				String [] fld = line.split("\t");
				String son_id = fld[0];
				String son_name = fld[1];
				int son_num = Integer.valueOf(fld[2]);
				if ( son_num == 0 ) {
					add(new EmptyNode(son_id, son_name));
				}
				else if ( son_num == -1 ) {
					String spectrum_name = fld[4];
					add(new LeafNode(son_id, son_name, fld[3], spectrum_name));
				}
				else {
					add(new InterNode(son_id, son_name, son_num));
				}
			}
			br.close();
			loaded = true;
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

}
