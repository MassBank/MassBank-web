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
 * バッチ検索処理クラス
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/
package massbank;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import massbank.BatchJobManager;
import massbank.GetConfig;
import java.util.Date;

public class BatchJobWorker extends Thread { 
	private String baseUrl;
	private String sessionId;
	private String timeStamp;
	private String mailAddress;
	private String fileName;
	private String name = "";
	private String peak = "";
	private int sendLen = 0;
	private int LIMIT = 33554432; // 32 GB
	private String serverUrl = "";
	private boolean isTerminated = false;

	public BatchJobWorker(String baseUrl, BatchJobInfo jobInfo) {
		this.baseUrl = baseUrl;
		this.sessionId   = jobInfo.getSessionId();
		this.timeStamp   = jobInfo.getTimeStamp();
		this.mailAddress = jobInfo.getMailAddr();
		this.fileName    = jobInfo.getTempName();
	}

	public void run() {
		try {
			GetConfig conf = new GetConfig(this.baseUrl);
			this.serverUrl = conf.getServerUrl();
			String[] urlList = conf.getSiteUrl();
			String midServerUrl = urlList[GetConfig.MYSVR_INFO_NUM];

			String tempDir = System.getProperty("java.io.tmpdir");
			File temp = File.createTempFile("batchRes", ".txt");
			String queryFilePath = tempDir + "/" + this.fileName;
			String resultFilePath = tempDir + "/" + temp.getName();

			// ** open temporary file
			File f1 = new File(queryFilePath);
			File f2 = new File(resultFilePath);
			BufferedReader in = new BufferedReader(new FileReader(f1));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
			String line;
			name = "";
			peak = "";
			sendLen = 0;

			writer.println(mailAddress);
			sendLen += mailAddress.length() + 1;
			String year   = this.timeStamp.substring(0,4);
			String month  = this.timeStamp.substring(4,6);
			String day    = this.timeStamp.substring(6,8);
			String hour   = this.timeStamp.substring(8,10);
			String minute = this.timeStamp.substring(10,12);
			String second = this.timeStamp.substring(12,14);
			String time = year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second + " JST";
			writer.println(time);
			sendLen += time.length() + 1;
			writer.println();
			sendLen ++;
			name = "(none)";
			int flag = 0;
			while ( ( line = in.readLine() ) != null ) {
				if ( line.matches("^Name:.*") ) {
					name = line;
					name = line.replaceFirst("^Name: *", "").replaceFirst("^ *$", "");
					peak = "";
					flag = 1;
				}
				else if ( line.matches(".*:.*") ) { }
				else if ( line.matches("^$") ) {
					if ( flag == 0 ) {
						continue;
					}
					flag = 0;
					doSearch(writer);
					if ( sendLen >= LIMIT ) {
						break;
					}
				}
				else {
					peak += "  " + line;
				}

				// スレッド終了
				if ( isTerminated ) {
//					System.out.println( new Date().toString() + " isTerminated-1" );
					break;
				}
			}
			in.close();

			if ( flag == 1 && sendLen < LIMIT ) {
				doSearch(writer);
			}
			writer.flush();
			writer.close();

			if ( isTerminated ) {
				f2.delete();
				return;
			}

			in = new BufferedReader(new FileReader(f2));
			URL url = new URL( midServerUrl + "cgi-bin/BatchSender.cgi");
			URLConnection urlc = (URLConnection)url.openConnection(); 
			urlc.setUseCaches(false);
			urlc.setRequestProperty("Content-type", "text/plain");
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setAllowUserInteraction(false);
			writer = new PrintWriter(urlc.getOutputStream());
			while ( ( line = in.readLine() ) != null ) {
				writer.println(line);
			}
			in.close();
			writer.flush();
			writer.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			while ( reader.readLine() != null ) { }
			reader.close();

			// ジョブエントリ削除
			BatchJobManager job = new BatchJobManager();
			job.deleteEntry(this.sessionId, this.timeStamp);

			// ** delete temporary file
			f1.delete();
			f2.delete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void doSearch(PrintWriter writer) {
		// ** format peak data
		peak = peak.replaceAll("[^0-9.]", " ").replaceAll(" +", " ").replaceFirst("^ *", "").replaceFirst(" *$", " ");
		while ( peak.indexOf(" ") >= 0 ) {
			peak = peak.replaceFirst(" ", ",").replaceFirst(" ", "@");
		}
	
		// ** search spectra
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
		String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
	 			+ "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
	 			+ "&CUTOFF=5" + "&NUM=0&VAL=" + peak;
		ArrayList result = mbcommon.execMultiDispatcher( this.serverUrl, typeName, param );
		int hitCnt = result.size();
		int sendCnt = hitCnt;
		if ( sendCnt > 20 ) {
			sendCnt = 20;
		}
	
		// ** calculate size
		sendLen += name.length() + 1;
		sendLen += String.valueOf(hitCnt).length() + 1;
		sendLen += String.valueOf(sendCnt).length() + 1;
		for ( int i = 0; i < sendCnt; i ++ ) {
			String rec = (String)result.get(i);
			sendLen += rec.length() + 1;
		}
		sendLen ++;
		if ( sendLen >= LIMIT ) {
			return;
		}
	
		// ** save search results
		writer.println(name);
		writer.println(hitCnt);
		writer.println(sendCnt);
		for ( int i = 0; i < sendCnt; i ++ ) {
			String rec = (String)result.get(i);
			writer.println(rec);
		}
		writer.println("");
		writer.flush();
		name = "(none)";
		peak = "";
	}

	public void setTerminate() {
		isTerminated = true;
	}
}
