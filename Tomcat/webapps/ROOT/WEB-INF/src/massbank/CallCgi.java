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
 * CGIを実行するスレッドのクラス
 *
 * ver 1.0.4 2008.12.05
 *
 ******************************************************************************/
package massbank;

import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import javax.servlet.ServletContext;
import massbank.MassBankLog;

public class CallCgi extends Thread
{
	private Hashtable m_params = null;

	public String result = "";
	private String m_url = "";
	private ServletContext m_context;
	private int m_timeout;

	/**
	 * コンストラクタ
	 */ 
	public CallCgi( String url, Hashtable params, int timeout, ServletContext context ) {
		this.m_url = url;
		this.m_timeout = timeout;
		this.m_context = context;
		this.m_params = params;
	}

	public void run() {
		String progName = "CallCgi";
		String msg = "";

		HttpClient client = new HttpClient();
		// タイムアウト値(msec)セット
		client.setTimeout( m_timeout * 1000 );
		PostMethod method = new PostMethod( this.m_url );
		String strParam = "";
		if ( m_params != null && m_params.size() > 0 ) {
			for ( Enumeration keys = m_params.keys(); keys.hasMoreElements(); ) {
				String key = (String)keys.nextElement();
				if ( !key.equals("inst") ) {
					// キーがInstrumentType以外の場合はStringパラメータ
					String val = (String)m_params.get(key);
					strParam += key + "=" + val + "&";
					method.addParameter( key, val );
				}
				else {
					// キーがInstrumentTypeの場合はString配列パラメータ
					String[] vals = (String[])m_params.get(key);
					for (int i=0; i<vals.length; i++) {
						strParam += key + "=" + vals[i] + "&";
						method.addParameter( key, vals[i] );
					}
				}
			}
			strParam = strParam.substring( 0, strParam.length()-1 );
		}

		try {
			// 実行
			int statusCode = client.executeMethod(method);
			// ステータスコードのチェック
			if ( statusCode != HttpStatus.SC_OK ){
				// エラー
				msg = method.getStatusLine().toString() + "\n" + "URL  : " + this.m_url;
				msg += "\nPARAM : " + strParam;
				MassBankLog.ErrorLog( progName, msg, m_context );
				return;
			}
			// レスポンス取得
			this.result = method.getResponseBodyAsString();
		}
		catch ( Exception e ) {
			// エラー
			msg = e.toString() + "\n" + "URL  : " + this.m_url;
			msg += "\nPARAM : " + strParam;
			MassBankLog.ErrorLog( progName, msg, m_context );
		}
		finally {
			// コネクション解放
			method.releaseConnection();
		}
	}
}