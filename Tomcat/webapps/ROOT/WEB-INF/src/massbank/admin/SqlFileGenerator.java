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
 * DB登録用のSQLを生成するクラス
 *
 * ver 1.0.12 2009.11.18
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import massbank.GetConfig;
import massbank.GetInstInfo;

public class SqlFileGenerator {

	private static final int TABLE_RECORD     = 0;
	private static final int TABLE_CH_NAME    = 1;
	private static final int TABLE_CH_LINK    = 2;
	private static final int TABLE_INSTRUMENT = 3;

	private String[] instNo = null;
	private String[] instName = null;
	private String[] instType = null;
	private HashMap<String, Integer> instNew = null;
	private ArrayList<String> usedNoList = null;
	private String valInst = "";
	private String name = "";
	private String value = "";
	private String acc = "";
	private String nameReco = "";
	private String valReco  = "";
	private String nameLink = "";
	private String valLink  = "";
	private String acInst   = "";
	private String acInstType = "";
	private ArrayList<String> valNames = new ArrayList<String>();
	private Hashtable<String, String> existItem = new Hashtable<String, String>();

	/**
	 * コンストラクタ
	 * @param baseUrl ベースURL
	 * @param selDbName DB名
	 */ 
	public SqlFileGenerator( String baseUrl, String selDbName) {
		// INSTRUMENT情報を取得
		GetInstInfo instInfo = new GetInstInfo( baseUrl );
		GetConfig conf = new GetConfig(baseUrl);
		String[] dbNameList = conf.getDbName();
		int dbIndex = 0;
		for ( int i = 0; i < dbNameList.length; i++ ) {
			if ( dbNameList[i].equals(selDbName) ) {
				dbIndex = i;
				break;
			}
		}
		instInfo.setIndex(dbIndex);
		this.instNo = instInfo.getNo();
		this.instName = instInfo.getName();
		this.instType = instInfo.getType();
		this.instNew = new HashMap<String, Integer>();
		usedNoList = new ArrayList<String>();
		for (String no : this.instNo) {
			usedNoList.add(no);
		}
	}

	/**
	 * レコードファイル読込み
	 * @param filePath レコードファイルのパス
	 */ 
	public void readFile( String filePath ) {
		try {
			this.valInst = "";
			this.name = "";
			this.value = "";
			this.acc = "";
			this.nameReco = "";
			this.valReco  = "";
			this.nameLink = "";
			this.valLink  = "";
			this.valNames.clear();
			String line = "";

			existItem.clear();

			// ファイル読込み
			BufferedReader in = new BufferedReader( new FileReader(filePath) );
			while ( ( line = in.readLine() ) != null ) {
				this.cutItem( line );
				if ( name.equals("ACCESSION") ) {
					this.acc = value;
				}
				//***********************************************
				// テーブル:RECORD,INSTRUMENT
				//***********************************************
				else if (name.equals("CH$FORMULA")
					  || name.equals("CH$EXACT_MASS")
					  || name.equals("CH$SMILES")
					  || name.equals("CH$IUPAC")
					  || name.equals("DATE")
					  || name.equals("MS$FOCUSED_ION")
					  || name.equals("AC$INSTRUMENT")
					  || name.equals("AC$INSTRUMENT_TYPE") ) {
					this.setRecord();
				}
				//***********************************************
				// テーブル:CH_NAME
				//***********************************************
				else if ( name.equals("CH$NAME") ) {
					this.setChName();
				}
				//***********************************************
				// テーブル:CH_LINK
				//***********************************************
				else if ( name.equals("CH$LINK") ) {
					this.setChLink();
				}
			}
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 各情報有無チェック
	 * @param type レコードファイルのパス
	 * @return true:あり/false:なし
	 */ 
	public boolean isExist( int type ) {
		boolean ret = true;
		switch ( type ) {
		case TABLE_RECORD:
			if ( nameReco.equals("") )  { ret = false; } break;
		case TABLE_CH_NAME:
			if ( valNames.size() == 0 ) { ret = false; } break;
		case TABLE_CH_LINK:
			if ( nameLink.equals("") )  { ret = false; } break;
		case TABLE_INSTRUMENT:
			if ( valInst.equals("") )  { ret = false; } break;
		default: break;
		}
		return ret;
	}

	/**
	 * 生成したSQL文を取得
	 * @param type 種別
	 * @return SQL文
	 */ 
	public String getSql( int type ) {
		String sql = "";
		switch ( type ) {
		case TABLE_RECORD:
			sql = "insert into RECORD(ID" + nameReco + ") values('"
					+ acc + "'" + valReco + ");";
			break;
		case TABLE_CH_NAME:
			for ( int i = 0; i < valNames.size(); i++ ) {
				sql += "insert into CH_NAME values('" + acc + "', '" + valNames.get(i) + "');";
				if ( i < valNames.size() - 1 ) {
					sql += "\n";
				}
			}
			break;
		case TABLE_CH_LINK:
			sql = "insert into CH_LINK(ID" + nameLink + ") values('"
					+ acc + "'" + valLink + ");";
			break;
		case TABLE_INSTRUMENT:
			sql = "insert into INSTRUMENT(INSTRUMENT_NO, INSTRUMENT_TYPE, INSTRUMENT_NAME) values(" + valInst + ");";
			break;
		default:
			break;
		}
		return sql;
	}

	/**
	 * RECORD,INSTRUMENTテーブルSQL文セット
	 */ 
	private void setRecord() {
		if ( name.equals("CH$FORMULA")
	 	  || name.equals("CH$EXACT_MASS")
	 	  || name.equals("CH$SMILES")
	 	  || name.equals("CH$IUPAC")  ) {
			String exist = (String)this.existItem.get(name);
			if ( exist == null ) {
				existItem.put(name, "true");
			}
			else {
				return;
			}
			nameReco += ", " + name.substring(3);
			valReco += ", ";
			if ( name.equals("CH$EXACT_MASS") ) {
				try {
					double emass = Double.parseDouble(value);
					if ( emass > 0.0d ) {
						valReco += value;
					}
					else {
						valReco += "0";
					}
				}
				catch ( NumberFormatException e ) {
					valReco += "0";
				}
			}
			else {
				valReco += "'" + value + "'";
			}
		}
		else if ( name.equals("DATE") ) {
			nameReco += ", " + name;
			valReco += ", '" + value + "'";
		}
		else if ( name.equals("AC$INSTRUMENT") ) {
			this.acInst = value;
		}
		else if ( name.equals("AC$INSTRUMENT_TYPE") ) {
			this.acInstType = value.trim();
			boolean isFound = false;
			int i = 0;
			for ( i = 0; i < this.instNo.length; i++ ) {
				if (  this.acInstType.equals(this.instType[i]) && this.acInst.equals(this.instName[i]) ) {
					isFound = true;
					break;
				}
			}
			if ( isFound ) {
				// DBに登録済みINSTRUMENTの場合
				valReco += ", " + this.instNo[i];
			}
			else {
				// DBに未登録INSTRUMENTの場合
				int instNo = 1;
				String keyStr = this.acInstType + "\t" + this.acInst;
				if ( this.instNew.containsKey(keyStr) ) {
					// 既に新規登録処理を行っているINSTRUMENTの場合
					instNo = this.instNew.get(keyStr);
				}
				else {
					// まだ新規登録処理を行っていないINSTRUMENTの場合
					for (int tmpNo=instNo; tmpNo<Integer.MAX_VALUE; tmpNo++) {
						if (!usedNoList.contains(String.valueOf(tmpNo))) {
							usedNoList.add(String.valueOf(tmpNo));
							instNo = tmpNo;
							break;
						}
					}
					this.instNew.put(keyStr, instNo);
					valInst = "'" + instNo + "', '" + this.acInstType + "', '" + this.acInst + "'";
				}
				valReco += ", " + String.valueOf(instNo);
			}
			nameReco += ", INSTRUMENT_NO";
		}
	}

	/**
	 * CH_NAMEテーブルSQL文セット
	 */ 
	private void setChName() {
		value = value.replaceAll( "'", "\\\\'" );
		valNames.add( value );
	}

	/**
	 * CH_LINKテーブルSQL文セット
	 */ 
	private void setChLink() {
		int pos = value.indexOf(" ");
		if ( pos > 0 ) {
			String linkName = value.substring( 0, pos );
			if ( !  (linkName.equals("CAS")
						|| linkName.equals("CHEBI")
						|| linkName.equals("CHEMPDB")
						|| linkName.equals("KEGG")
						|| linkName.equals("NIKKAJI")
						|| linkName.equals("PUBCHEM")
						|| linkName.equals("KNAPSACK")
						|| linkName.equals("KAPPAVIEW")
						|| linkName.equals("LIPIDBANK")
						|| linkName.equals("FLAVONOIDVIEW")
						|| linkName.equals("KEIO")
						|| linkName.equals("PRIME")) ) {
				return;
			}
			String linkVal = "";
			// KIEO ID or PRIME ID
			if ( linkName.equals("KEIO")
				|| linkName.equals("PRIME") ) {
				linkName = "SITE_ID";
				linkVal = value;
			}
			else {
				linkVal = value.substring( pos + 1 );
			}
			nameLink += ", " + linkName;
			valLink += ", '" + linkVal + "'";
		}
	}


	/**
	 * 項目名と値の分割
	 * @param line 分割対象文字列
	 */ 
	private void cutItem( String line ) {
		this.name = "";
		this.value = "";
		int pos = line.indexOf(":");
		if ( pos >= 0 ) {
			this.name = line.substring( 0, pos );
			if ( line.length() > pos + 2 ) {
				this.value = line.substring( pos + 2 );
			}
		}
	}
}
