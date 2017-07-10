/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * サニタイジングユーティリティ
 *
 * ver 1.0.0 2009.07.10
 *
 ******************************************************************************/
package massbank;

/**
 * サニタイジングユーティリティクラス
 */
public class Sanitizer {

	/**
	 * HTMLサニタイジング
	 * @param val 文字列
	 * @return サニタイズ済み文字列
	 */
	public static String html( String val ) {
		if( val == null || val.equals("")) {
			return "";
		}
		val = val.replaceAll( "&" , "&amp;"  );
		val = val.replaceAll( "<" , "&lt;"   );
		val = val.replaceAll( ">" , "&gt;"   );
		val = val.replaceAll( "\"", "&quot;" );
		val = val.replaceAll( "'" , "&#39;"  );
		return val;
	}
	
	/**
	 * HTMLアンサニタイジング
	 * @param val 文字列
	 * @return サニタイズ済み文字列
	 */
	public static String unhtml( String val ) {
		if( val == null || val.equals("")) {
			return "";
		}
		val = val.replaceAll( "&amp;" , "&"  );
		val = val.replaceAll( "&lt;"  , "<"  );
		val = val.replaceAll( "&gt;"  , ">"  );
		val = val.replaceAll( "&quot;", "\"" );
		val = val.replaceAll( "&#39;" , "'"  );
		return val;
	}

	/**
	 * SQLサニタイジング
	 * @param val 文字列
	 * @return サニタイズ済み文字列
	 */
	public static String sql( String val ) {
		if( val == null || val.equals("")) {
			return "";
		}
		val = val.replaceAll( "'" , "''"  );
		return val;
	}
	
	/**
	 * SQLサニタイジング
	 * @param val 文字列
	 * @return サニタイズ済み文字列
	 */
	public static String unsql( String val ) {
		if( val == null || val.equals("")) {
			return "";
		}
		val = val.replaceAll( "''" , "'"  );
		return val;
	}
}
