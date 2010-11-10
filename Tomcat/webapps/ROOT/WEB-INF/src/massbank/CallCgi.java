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
 * CGI�����s����X���b�h�̃N���X
 *
 * ver 1.0.4 2008.12.05
 *
 ******************************************************************************/
package massbank;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	 * �R���X�g���N�^
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
		// �^�C���A�E�g�l(msec)�Z�b�g
		client.setTimeout( m_timeout * 1000 );
		PostMethod method = new PostMethod( this.m_url );
		String strParam = "";
		if ( m_params != null && m_params.size() > 0 ) {
			for ( Enumeration keys = m_params.keys(); keys.hasMoreElements(); ) {
				String key = (String)keys.nextElement();
				if ( !key.equals("inst") ) {
					// �L�[��InstrumentType�ȊO�̏ꍇ��String�p�����[�^
					String val = (String)m_params.get(key);
					strParam += key + "=" + val + "&";
					method.addParameter( key, val );
				}
				else {
					// �L�[��InstrumentType�̏ꍇ��String�z��p�����[�^
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
			// ���s
			int statusCode = client.executeMethod(method);
			// �X�e�[�^�X�R�[�h�̃`�F�b�N
			if ( statusCode != HttpStatus.SC_OK ){
				// �G���[
				msg = method.getStatusLine().toString() + "\n" + "URL  : " + this.m_url;
				msg += "\nPARAM : " + strParam;
				MassBankLog.ErrorLog( progName, msg, m_context );
				return;
			}
			// ���X�|���X�擾
			//this.result = method.getResponseBodyAsString();
			
			/**
			 * modification start
			 * Use method.getResponseBodyAsStream() rather 
			 * than method.getResponseBodyAsString() (marked as deprecated) for updated HttpClient library.
			 * Prevents logging of message:
			 * "Going to buffer response body of large or unknown size. Using getResponseBodyAsStream instead is recommended."
			 */
			String charset = method.getResponseCharSet();
			InputStream is = method.getResponseBodyAsStream();
			StringBuilder sb = new StringBuilder();
			String line = "";
			if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
                while ((line = reader.readLine()) != null) {
                	reader.mark(2000);
                	String forward = reader.readLine();
                	if((line.equals("") || line.equals("\n") || line.equals("OK")) && forward == null)
                		sb.append(line);				// append last line to StringBuilder
                	else if(forward != null)
                		sb.append(line).append("\n");	// append current line with explicit line break
                	else sb.append(line);
                	
                	reader.reset();
                }
                reader.close();
                is.close();
                
	            this.result = sb.toString().trim();
	            if(this.result.endsWith("\n"))			// remove trailing line break
	            {
	            	int pos = this.result.lastIndexOf("\n");
	            	this.result = this.result.substring(0, pos);
	            }
	        } else {        
	        	this.result = "";
	        }
			/**
			 * modification end
			 */
		}
		catch ( Exception e ) {
			// �G���[
			msg = e.toString() + "\n" + "URL  : " + this.m_url;
			msg += "\nPARAM : " + strParam;
			MassBankLog.ErrorLog( progName, msg, m_context );
		}
		finally {
			// �R�l�N�V�������
			method.releaseConnection();
		}
	}
}