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
 * ver 1.0.8 2011.07.25
 *
 ******************************************************************************/
package massbank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


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

		CloseableHttpClient httpclient = HttpClients.createDefault();
		// タイムアウト値(msec)セット
		int CONNECTION_TIMEOUT_MS = m_timeout * 1000; // Timeout in millis.
		RequestConfig requestConfig = RequestConfig.custom()
		    .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
		    .setConnectTimeout(CONNECTION_TIMEOUT_MS)
		    .setSocketTimeout(CONNECTION_TIMEOUT_MS)
		    .build();
		
		HttpPost httpPost = new HttpPost(this.m_url);
		httpPost.setConfig(requestConfig);
		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		String strParam = "";
		if ( m_params != null && m_params.size() > 0 ) {
			for ( Enumeration keys = m_params.keys(); keys.hasMoreElements(); ) {
				String key = (String)keys.nextElement();
				if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
					// キーがInstrumentType,MSType以外の場合はStringパラメータ
					String val = (String)m_params.get(key);
					strParam += key + "=" + val + "&";
					nvps.add(new BasicNameValuePair( key, val ));
				}
				else {
					// キーがInstrumentType,MSTypeの場合はString配列パラメータ
					String[] vals = (String[])m_params.get(key);
					for (int i=0; i<vals.length; i++) {
						strParam += key + "=" + vals[i] + "&";
						nvps.add(new BasicNameValuePair( key, vals[i] ));
					}
				}
			}
			strParam = strParam.substring( 0, strParam.length()-1 );
		}

		CloseableHttpResponse response = null;
		try {
			// set parameters and execute
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httpPost);
			
			// 実行
			int statusCode = response.getStatusLine().getStatusCode();
			// ステータスコードのチェック
			if ( statusCode != HttpStatus.SC_OK ){
				// エラー
				msg = response.getStatusLine().toString() + "\n" + "URL  : " + this.m_url;
				msg += "\nPARAM : " + strParam;
				MassBankLog.ErrorLog( progName, msg, m_context );
				return;
			}
			
			/**
			 * get result
			 */

			HttpEntity entity = response.getEntity();
			String responseString	= EntityUtils.toString(entity);
			
			// remove trailing and tailing blank lines
			if(responseString.length() > 0){
				int numberOfTrailingBlankLines	= 0;
				while(numberOfTrailingBlankLines < responseString.length() && (responseString.charAt(numberOfTrailingBlankLines) == 13 || responseString.charAt(numberOfTrailingBlankLines) == 10))
					numberOfTrailingBlankLines++;
				responseString	= responseString.substring(numberOfTrailingBlankLines, responseString.length());
			}
			if(responseString.length() > 0){
				int numberOfTailingBlankLines	= 0;
				while(numberOfTailingBlankLines < responseString.length() && (responseString.charAt(responseString.length() - 1 - numberOfTailingBlankLines) == 13 || responseString.charAt(responseString.length() - 1 - numberOfTailingBlankLines) == 10))
					numberOfTailingBlankLines++;
				responseString	= responseString.substring(0, responseString.length() - numberOfTailingBlankLines);
			}
			
			this.result	= responseString;
		}
		catch ( Exception e ) {
			// エラー
			msg = e.toString() + "\n" + "URL  : " + this.m_url;
			msg += "\nPARAM : " + strParam;
			MassBankLog.ErrorLog( progName, msg, m_context );
		}
		finally {
			// コネクション解放
			if(response != null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
