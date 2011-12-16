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
 * Cookie情報管理 クラス
 *
 * ver 1.0.5 2011.12.16
 *
 ******************************************************************************/

import java.applet.Applet;
import java.util.ArrayList;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import netscape.javascript.JSUtil;

/**
 * Cookie情報管理 クラス
 * 
 * Cookie情報は次のように取得できる
 * [Cookie名]=[値]; [その他キー]=[その他の値];…
 * [Cookie名]には適当な名称、[値]は[[キー]=[値],[キー]=[値],…;]のように格納
 * されていることを前提としている
 * 
 * Cookie情報の例：
 *   SearchApplet=INST=CI-MS,FI-MS;ION=Positive;
 */
public class CookieManager {

	private boolean isCookie = false;		// クッキー有効フラグ
	private JSObject win;					// Javascriptのwindowオブジェクト
    private JSObject doc;					// Javascriptのdocumentオブジェクト
	private String cookieName = "Applet";	// 対象Cookie名（デフォルトApplet）
	private int expDate = 30;				// 有効期限日数（デフォルト30日）
	
	/**
	 * デフォルトコンストラクタ
	 * @deprecated 使用不可
	 */
	private CookieManager() {
	}
	
	/**
	 * コンストラクタ
	 * 対象Cookie名と有効期限日数を指定するコンストラクタ
	 * JavascriptのWindowオブジェクトが取得できない場合は
	 * このクラスを使用不可とするフラグを設定する
	 * ブラウザウィンドウオブジェクトを取得できない場合は例外を出力する
	 * @param applet アプレット
	 * @param name 対象Cookie名
	 * @param expDate 有効期限日数
	 * @param isCookie Cookie有効フラグ
	 */
	public CookieManager(Applet applet, String name, int expDate, boolean isCookie) {
		try {
			this.win = JSObject.getWindow(applet);
			this.doc = (JSObject)win.getMember("document");
			
			if (!name.trim().equals("")) {
				this.cookieName = name;
			}
			this.expDate = expDate;
			this.isCookie = isCookie;
			
			// Cookie無効時にCookie情報全削除
			if ( !this.isCookie ) {
				updateCookie("");
			}
		}
		catch (JSException e) {
			System.out.println("browser window object doesn't exist.");
			System.out.println(JSUtil.getStackTrace(e));
			return;
		}
	}
	
	/**
	 * Cookie情報設定
	 * 対象となるCookie情報にキーと値のセットを設定する
	 * 既にキーと値のセットがCookie情報に存在する場合は置換する
	 * サイズ0の値リストを引数に受け取った場合はキーに対応するCookie情報のみ削除される
	 * @param key キー
	 * @param valueList 値のリスト
	 * @return 結果
	 */
	public boolean setCookie(String key, ArrayList<String> valueList) {
		
		// Cookie無効もしくはJavascriptオブジェクトが取得できていない場合
		if ( !isCookie || win == null || doc == null ) {
			return false;
		}
		
		String param = "";
		String values = getCookie();
		if (values.trim().length() != 0) {
			String[] data = values.split(";");
			for (int i=0; i<data.length; i++) {
				// 既にCookieがある場合はkeyに該当しない情報のみを引き継ぐ
				if (!data[i].split("=")[0].trim().equals(key)) {
					param += data[i] + ";";
				}
			}
		}
		
		if (valueList.size() != 0) {
			param += key + "=";
			for (int i=0; i<valueList.size(); i++) {
				param += valueList.get(i);
				if (i+1 < valueList.size()) {
					param += ",";
				}
			}
			param += ";";
		}
		
		// Cookie情報更新
		return updateCookie(param);
	}
	
	/**
	 * Cookie情報更新
	 * Cookieに保存するパラメータがない場合はCookieを保持しない（既にある場合は削除）
	 * @param param Cookieに保存するパラメータ
	 * @return 結果
	 */
	private boolean updateCookie(String param) {
		try {
			// 現在までの経過時間取得（ミリ秒）
			JSObject date = (JSObject)win.eval("new Date()");
			Double time = Double.parseDouble(String.valueOf(date.call("getTime", null)));
			
			// 有効期限をグリニッジ標準時で算出
			if ( !param.equals("") ) {
				time += ((double)expDate * 24d * 60d * 60d * 1000d);
			} else {
				time -= (double)expDate;
			}
			try {
				time = Double.parseDouble(String.valueOf(date.call("setTime", new Object[]{time})));
			}
			catch (Exception e) {
				time = Double.parseDouble(String.valueOf(date.eval("setTime(" + String.valueOf(time) + ")")));
			}
			String gmtTime = String.valueOf(date.call("toGMTString", null));
			
			// Cookie設定情報設定処理
			String paramVal = cookieName + "=" + win.call("escape", new Object[]{param});
			String timeVal = "expires=" + gmtTime;
			String cookieVal = paramVal + "; " + timeVal;
			doc.setMember("cookie", cookieVal);
		}
		catch (JSException jse) {
			System.out.println("Unsupported javascript was used.");
			System.out.println(JSUtil.getStackTrace(jse));
			return false;
		}
		return true;
	}
	
	/**
	 * Cookie情報取得
	 * 対象となるCookie情報を全て取得する
	 * @return Cookie情報
	 */
	private String getCookie() {
		
		String values = "";	// 対象Cookie情報
		try {
			String tmpAllCookie = (String)doc.getMember("cookie");
			if (tmpAllCookie == null) {
				try {
					tmpAllCookie = (String)doc.eval("cookie");
				}
				catch (JSException jse) {				
				}
				if (tmpAllCookie == null) {
					return values;
				}
			}
			String[] allCookie = tmpAllCookie.split(";");
			String[] tmp;
			for (int i=0; i<allCookie.length; i++) {
				tmp = allCookie[i].split("=");
				// 該当するCookie情報の取得
				if (tmp[0].trim().equals(cookieName)) {
					if (tmp.length == 2) {
						try {
							values = String.valueOf(win.eval("unescape('" +  tmp[1].trim() +"')"));
						}
						catch (JSException e) {
							values = String.valueOf(win.call("unescape", new Object[]{tmp[1].trim()}));
						}
					}
					break;
				}
			}
		}
		catch (JSException e) {
			System.out.println(JSUtil.getStackTrace(e));
			values = "";
		}
		
		return values;
	}
	
	/**
	 * Cookie情報取得（キー指定）
	 * 対象となるCookie情報からキーに該当する値のみを取得する
	 * @param key 取得したいCookie情報のキー
	 * @return Cookie情報
	 */
	public ArrayList<String> getCookie(String key) {
		
		ArrayList<String> valueList = new ArrayList<String>();
		
		// Cookie無効もしくはJavascriptオブジェクトが取得できていない場合
		if ( !isCookie || win == null || doc == null ) {
			return valueList;
		}
		
		// 対象Cookie情報取得
		String values = getCookie();
		
		
		// キーに対する値の取得処理
		String val = "";
		if (values.trim().length() != 0) {
			String[] data = values.split(";");
			String[] item;
			for (int i=0; i<data.length; i++) {
				item = data[i].split("=");
				if (item[0].trim().equals(key)) {
					if (item.length == 2) {
						val = item[1].trim();
					}
					break;
				}
			}
		}
		String[] tmp = val.split(",");
		for (int i=0; i<tmp.length; i++) {
			if (!tmp[i].equals("")) {
				valueList.add(tmp[i]);
			}
		}
		
		return valueList;
	}
}
