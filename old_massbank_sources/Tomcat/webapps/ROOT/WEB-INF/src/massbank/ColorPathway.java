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
 * KEGG Pathwayへの色付け
 *
 * ver 1.0.0 2010.10.13
 *
 ******************************************************************************/
package massbank;

import java.util.HashMap;
import java.util.concurrent.Callable;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;

public class ColorPathway implements Callable<HashMap<String, String>> {

	private String map;
	private String[] cpds;

	/**
	 * コンストラクタ
	 */
	public ColorPathway(String map, String[] cpds) {
		this.map = map;
		this.cpds = cpds;
	}

	/**
	 * Call
	 */
	public HashMap<String, String> call() throws Exception {
		HashMap<String, String> result = null;
		int num = this.cpds.length;
		String[] fgColors = new String[num];
		String[] bgColors = new String[num];
		String pathwayId = "path:map" + this.map;
		KEGGLocator locator = new KEGGLocator();
		KEGGPortType serv = locator.getKEGGPort();
		for ( int i = 0; i < num; i++ ) {
			if ( this.map.equals("01100") ) {
				fgColors[i] = "black";
				bgColors[i] = "black";
			}
			else if ( Integer.parseInt(this.map) > 1000 ) {
				fgColors[i] = "white";
				bgColors[i] = "red";
			}
			else {
				fgColors[i] = "red";
				bgColors[i] = "red";
			}
		}
		String url = serv.color_pathway_by_objects(pathwayId, this.cpds, fgColors, bgColors);
		result = new HashMap();
		result.put(this.map, url);
		return result;
	}
}
