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
 * レコードバリデータ
 *
 * ver 1.0.4 2010.11.25
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.FileReader;

public class Validator {

	/** 必須項目の名称 **/
	private static final String MANDATORY_ITEM [] = {
		"ACCESSION", "RECORD_TITLE", "DATE", "AUTHORS", "COPYRIGHT", "CH$NAME", "CH$FORMULA",
		"CH$EXACT_MASS", "CH$SMILES", "CH$IUPAC", "AC$INSTRUMENT", "AC$INSTRUMENT_TYPE",
		"AC$ANALYTICAL_CONDITION: MODE", "PK$NUM_PEAK", "PK$PEAK"
	};

	private String errMsgValue = "";
	private String errMsgMandaroty = "";

	/**
	 * コンストラクタ
	 */
	public Validator( String filePath ) {

		boolean[] isExists = new boolean[MANDATORY_ITEM.length];

		try {
			// ファイル読込み
			BufferedReader in = new BufferedReader( new FileReader(filePath) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				String[] vals = this.cutItem( line );
				String name = vals[0];
				String value = vals[1];
				int index = this.getIndex( name );
				if ( index > -1 ) {
					isExists[index] = true;
				}

				// 値がないものを出力
				if ( !name.equals("") && !name.equals("MS$PROFILE") && value.equals("") ) {
					this.errMsgValue += "&nbsp;\"" + name + "\" ";
				}
			}
			in.close();

			// 必須項目の記述がないのものを出力
			for ( int i = 0; i < MANDATORY_ITEM.length; i++ ) {
				if ( !isExists[i] ) {
					this.errMsgMandaroty += "&nbsp;\"" + MANDATORY_ITEM[i] + "\" ";
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 */ 
	public String getValueErr() {
		return this.errMsgValue;
	}

	/**
	 * 
	 */
	public String getMandarotyErr() {
		return this.errMsgMandaroty;
	}

	/**
	 * 
	 */
	private String[] cutItem( String line ) {
		String name = "";
		String value = "";
		int pos = line.indexOf(":");
		if ( pos >= 0 ) {
			name = line.substring( 0, pos );
			int pos2 = pos + 2;
			if ( line.length() > pos2 ) {
				//** AC$ANALYTICAL_CONDITIONの場合
				if ( name.equals("AC$ANALYTICAL_CONDITION") ) {
					pos = line.indexOf( " ", pos2 );
					if ( pos >= 0 ) {
						name = line.substring( 0, pos );
						pos2 = pos + 1;
					}
				}
				value = line.substring( pos2 ).trim();
			}
		}
		String[] vals = new String[]{ name, value };
		return vals;
	}

	/**
	 * 
	 */ 
	private int getIndex( String name ) {
		int num = -1;
		for ( int i = 0 ; i < MANDATORY_ITEM.length; i++ ) {
			if ( name.equals( MANDATORY_ITEM[i] ) ) {
				num = i;
				break;
			}
		}
		return num;
	}
}
