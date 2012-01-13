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
 * ver 1.0.10 2012.01.13
 *
 ******************************************************************************/
package massbank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import massbank.admin.FileUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.RandomStringUtils;


public class BatchSearchWorker extends Thread { 
	private String jobId;
	private String time;
	private String mailAddress;
	private String fileName;
	private String inst = "All";
	private String ms = "All";
	private String ion = "1";
	private String serverUrl = "";
	private boolean isTerminated = false;
	PrintWriter writer = null;

	/**
	 * コンストラクタ
	 * @param jobInfo
	 */
	public BatchSearchWorker(JobInfo jobInfo) {
		this.jobId       = jobInfo.getJobId();
		this.time        = jobInfo.getTimeStamp() + " JST";
		this.mailAddress = jobInfo.getMailAddr();
		this.fileName    = jobInfo.getTempName();
		String sParam    = jobInfo.getSearchParam();
		String[] params = sParam.split("&");
		for ( int i = 0; i < params.length; i++ ) {
			String[] items = params[i].split("=");
			if ( items.length == 2 ) {
				String name = items[0];
				String val  = items[1];
				if ( name.equals("inst") ) {
					this.inst = val;
				}
				else if ( name.equals("ms") ) {
					if ( val.indexOf("all") != -1 ) {
						this.ms = "All";
					}
					else {
						this.ms = val;
					}
				}
				else if ( name.equals("ion") ) {
					this.ion = val;
				}
			}
		}
	}

	public void run() {
		File attacheDir = null;
		try {
			// 指定されたジョブの状態を"Rinning"にする
			JobManager jobMgr = new JobManager();
			jobMgr.setRunning(this.jobId);

			GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
			this.serverUrl = conf.getServerUrl();

			String tempDir = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_TEMP_PATH);
			File temp = File.createTempFile("batchRes", ".txt");
			String queryFilePath = ( !this.fileName.equals("") ) ? tempDir + this.fileName : "";
			String resultFilePath = ( !temp.getName().equals("") ) ? tempDir + temp.getName() : "";

			// ** open temporary file
			File f1 = new File(queryFilePath);
			File f2 = new File(resultFilePath);
			BufferedReader in = new BufferedReader(new FileReader(f1));
			this.writer = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
			String line = "";
			String name = "";
			String peak = "";
			int peakLineCnt = 0;
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<String> peaks = new ArrayList<String>();
			while ( ( line = in.readLine() ) != null ) {
				line = line.trim();

				// コメント行はスキップする
				if ( line.startsWith("//") ) {
					continue;
				}
				// NAMEタグ
				else if ( line.matches("^Name:.*") ) {
					name = line.replaceFirst("^Name: *", "").trim();
				}
				else if ( line.matches(".*:.*") ) { }
				else if ( line.equals("") ) {
					if ( peakLineCnt > 0 ) {
						names.add(name);
						peaks.add(peak);
						name = "";
						peak = "";
						peakLineCnt = 0;
					}
				}
				else {
					peak += line;
					if ( !line.substring(line.length()-1).equals(";") ) {
						peak += ";";
					}
					peakLineCnt++;
				}
			}
			in.close();
			if ( peakLineCnt > 0) {
				names.add(name);
				peaks.add(peak);
			}

			// 検索処理
			for ( int i = 0; i < names.size(); i++) {
				boolean ret = doSearch( names.get(i), peaks.get(i), i );
				// スレッド終了
				if ( isTerminated ) {
					break;
				}
			}
			writer.flush();
			writer.close();

			if ( isTerminated ) {
				f2.delete();
				return;
			}

			if ( !this.mailAddress.equals("") ) {
				// メール送信情報生成
				SendMailInfo info = new SendMailInfo(MassBankEnv.get(MassBankEnv.KEY_BATCH_SMTP), MassBankEnv.get(MassBankEnv.KEY_BATCH_FROM), this.mailAddress);
				info.setFromName(MassBankEnv.get(MassBankEnv.KEY_BATCH_NAME));
				info.setSubject("MassBank Batch Service Results");
				info.setContents( "Dear Users,\n\nThank you for using MassBank Batch Service.\n"
								+ "\n"
								+ "The results for your request dated '" + this.time + "' are attached to this e-mail.\n"
								+ "\n"
								+ "----------------------------------------------\n"
								+ "MassBank - High Quality Mass Spectral Database\n"
								+ "  URL: " + serverUrl + "\n"
								+ "  E-mail: " + MassBankEnv.get(MassBankEnv.KEY_BATCH_FROM));
				
				
				// 添付ファイル生成一時ディレクトリ
				attacheDir = new File(tempDir + "batch_" + RandomStringUtils.randomAlphanumeric(9));
				while (attacheDir.exists()) {
					attacheDir = new File(tempDir + "batch_" + RandomStringUtils.randomAlphanumeric(9));
				}
				attacheDir.mkdir();
				
				// 添付ファイル生成（テキスト形式）
				File textFile = new File(attacheDir.getPath() + "/results.txt");
				textFile.createNewFile();
				createTextFile(f2, textFile);

				// 添付ファイル生成（HTML形式）
//				File htmlFile = new File(attacheDir.getPath() + "/MassBankResults.html");
//				htmlFile.createNewFile();
//				createHtmlFile(f2, htmlFile);

				// サマリ作成
				File summaryFile = new File(attacheDir.getPath() + "/summary.html");
				summaryFile.createNewFile();
				createSummary(f2, summaryFile);
				info.setFiles(new File[]{ summaryFile, textFile });

				// メール送信
				SendMail.send(info);
			}

			// 検索結果をセット
			jobMgr.setResult(this.jobId, resultFilePath);

			// 指定されたジョブの状態を"Completed"にする
			jobMgr.setCompleted(this.jobId);

			// クエリファイルと検索結果ファイルを削除
			f1.delete();
			f2.delete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if ( attacheDir != null && attacheDir.isDirectory() ) {
				try { FileUtils.forceDelete(attacheDir); } catch(IOException e) { e.printStackTrace(); }
			}
		}
	}
	
	/**
	 * 添付ファイル生成（テキスト形式）
	 * @param resultFile 結果ファイル
	 * @param textFile 添付用テキストファイル
	 */
	private void createTextFile(File resultFile, File textFile) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		LineNumberReader in = null;
		PrintWriter out = null;
		try {
			in = new LineNumberReader(new FileReader(resultFile));
			out = new PrintWriter(new BufferedWriter(new FileWriter(textFile)));
			
			// ヘッダー出力
			String reqIonStr = "Both";
			try {
				if (Integer.parseInt(this.ion) > 0) {
					reqIonStr = "Positive";
				}
				else if(Integer.parseInt(this.ion) < 0) {
					reqIonStr = "Negative";
				}
			}
			catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
			out.println("***** MassBank Batch Service Results *****");
			out.println();
			out.println("Request Date: " + this.time);
			out.println("# Instrument Type: " + this.inst);
			out.println("# MS Type: " + this.ms);
			out.println("# Ion Mode: " + reqIonStr);
			out.println();
			out.println();
			
			// 結果出力
			String line;
			long queryCnt = 0;
			boolean readName = false;
			boolean readHit = false;
			boolean readNum = false;
			boolean isFinalLine = false;
			while ( ( line = in.readLine() ) != null ) {
				line = line.trim();
				isFinalLine = false;
				if (!readName) {
					queryCnt++;
					out.println("### Query " + nf.format(queryCnt) + " ###");
					out.println("# Name: " + line);
					readName = true;
				}
				else if (!readHit) {
					int num = Integer.parseInt(line);
					if ( num == -1 ) {
						out.println("[ERROR] Invalid query\n");
						break;
					}
					out.println("# Hit: " + nf.format(num));
					out.println();
					readHit = true;
				}
				else if (!readNum) {
					out.println("Top " + line + " List");
					out.println("Accession\tTitle\tFormula\tMass\tScore\tHit");
					out.println();
					readNum = true;
				}
				else {
					if (!line.equals("")) {
						String[] data = formatLine(line);
						StringBuilder sb = new StringBuilder();
						sb.append(data[0]).append("\t")
						  .append(data[1]).append("\t")
						  .append(data[2]).append("\t")
						  .append(data[3]).append("\t")
						  .append(data[4]).append("\t")
						  .append(data[5]);
						out.println(sb.toString());
					}
					else {
						out.println();
						out.println();
						readName = false;
						readHit = false;
						readNum = false;
						isFinalLine = true;
					}
				}
			}
			if (!isFinalLine) {
				out.println();
				out.println();
			}
			out.println("***** END ********************************");
			out.println();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { if (in != null) { in.close(); } } catch (IOException e) {}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
//	/**
//	 * 添付ファイル生成（HTML形式）
//	 * @param resultFile 結果ファイル
//	 * @param htmlFile 添付用HTMLファイル
//	 */
//	private void createHtmlFile(File resultFile, File htmlFile) {
//		NumberFormat nf = NumberFormat.getNumberInstance();
//		LineNumberReader in = null;
//		PrintWriter out = null;
//		try {
//			in = new LineNumberReader(new FileReader(resultFile));
//			out = new PrintWriter(new BufferedWriter(new FileWriter(htmlFile)));
//			
//			// ヘッダー出力
//			String reqIonStr = "Both";
//			try {
//				if (Integer.parseInt(this.ion) > 0) {
//					reqIonStr = "Positive";
//				}
//				else if(Integer.parseInt(this.ion) < 0) {
//					reqIonStr = "Negative";
//				}
//			}
//			catch (NumberFormatException nfe) {
//				nfe.printStackTrace();
//			}
//			out.println("<html>");
//			out.println("<head><title>MassBank Batch Service Results</title></head>");
//			out.println("<body>");
//			out.println("<h1><a href=\"" + serverUrl + "\" target=\"_blank\">MassBank</a> Batch Service Results</h1>");
//			out.println("<hr>");
//			out.println("<h2>Request Date : " + this.time +"</h2>");
//			out.println("Instrument Type : " + this.inst + "<br>");
//			out.println("MS Type : " + this.ms + "<br>");
//			out.println("Ion Mode : " + reqIonStr + "<br>");
//			out.println("<br><hr>");
//			
//			// 結果出力
//			String line;
//			long queryCnt = 0;
//			boolean readName = false;
//			boolean readHit = false;
//			boolean readNum = false;
//			while ( ( line = in.readLine() ) != null ) {
//				line = line.trim();
//				if (!readName) {
//					queryCnt++;
//					out.println("<h2>Query " + nf.format(queryCnt) + "</h2><br>");
//					out.println("Name: " + line + "<br>");
//					readName = true;
//				}
//				else if (!readHit) {
//					int num = Integer.parseInt(line);
//					if ( num == -1 ) {
//						out.println("<font color=\"red\">[ERROR] Invalid query</font><br>");
//						break;
//					}
//					out.println("Hit: " + nf.format(num) + "<br>");
//					readHit = true;
//				}
//				else if (!readNum) {
//					out.println("<table border=\"1\">");
//					out.println("<tr><td colspan=\"6\">Top " + line + " List</td></tr>");
//					out.println("<tr><th>Accession</th><th>Title</th><th>Formula</th><th>Mass</th><th>Score</th><th>Hit</th></tr>");
//					readNum = true;
//				}
//				else {
//					if (!line.equals("")) {
//						String[] data = formatLine(line);
//						String acc = data[0];
//						String title = data[1];
//						String formula = data[2];
//						String emass = data[3];
//						String score = data[4];
//						String hit = data[5];
//						
//						out.println("<tr>");
//						out.println("<td><a href=\"" + serverUrl + "jsp/FwdRecord.jsp?id=" + acc + "\" target=\"_blank\">" + acc + "</td>");
//						out.println("<td>" + title + "</td>");
//						out.println("<td>" + formula + "</td>");
//						out.println("<td>" + emass + "</td>");
//						out.println("<td>" + score + "</td>");
//						out.println("<td align=\"right\">" + hit + "</td>");
//						out.println("</tr>");
//					}
//					else {
//						out.println("</table>");
//						out.println("<hr>");
//						readName = false;
//						readHit = false;
//						readNum = false;
//					}
//				}
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		finally {
//			try { if (in != null) { in.close(); } } catch (IOException e) {}
//			if (out != null) {
//				out.flush();
//				out.close();
//			}
//		}
//	}

	/**
	 * サマリファイル生成（HTML形式）
	 * @param resultFile 結果ファイル
	 * @param htmlFile 添付用HTMLファイル
	 */
	private void createSummary(File resultFile, File htmlFile) {
		LineNumberReader in = null;
		PrintWriter out = null;
		try {
			//(1) 結果ファイルの読込み
			String line;
			int cnt = 0;
			ArrayList<String> nameList = new ArrayList<String>();
			ArrayList<String> top1LineList = new ArrayList<String>();
			TreeSet<String> top1IdList = new TreeSet<String>();
			in = new LineNumberReader(new FileReader(resultFile));
			while ( ( line = in.readLine() ) != null ) {
				line = line.trim();
				if ( line.equals("")) {
					cnt = 0;
				}
				else {
					cnt++;
					if ( cnt == 1 ) {
						nameList.add(line);
					}
					else if ( cnt == 2 ) {
						if ( line.equals("-1") ) {
							top1LineList.add("Invalid");
						}
						if ( line.equals("0") ) {
							top1LineList.add("0");
						}
					}
					else if ( cnt == 4 ) {
						String[] vals = line.split("\t");
						String id = vals[0];
						top1IdList.add(id);
						top1LineList.add(line);
					}
				}
			}

			//※ http://www.massbank.jp/ がサーバの場合のみKEGGに関する処理を行う
			HashMap<String, ArrayList> massbank2mapList = new HashMap<String, ArrayList>();	//(2)用
			HashMap<String, String> massbank2keggList = new HashMap<String, String>();		//(2)用
			HashMap<String, ArrayList> map2keggList = new HashMap<String, ArrayList>();		//(3)用
			ArrayList<String> mapNameList = new ArrayList<String>();						//(4)用
			boolean isKeggReturn = true;
			if (serverUrl.indexOf("www.massbank.jp") == -1) {
				isKeggReturn = false;
			}
			if ( isKeggReturn ) {

				//(2) KEGG ID, Map IDをDBから取得
				String where = "where MASSBANK in(";
				Iterator it = top1IdList.iterator();
				while ( it.hasNext() ) {
					String id = (String)it.next();
					where += "'" + id + "',";
				}
				where = where.substring(0, where.length()-1);
				where += ")";
				String sql = "select MASSBANK, t1.KEGG, MAP from "
						   + "(SELECT MASSBANK,KEGG FROM OTHER_DB_IDS " + where + ") t1, PATHWAY_CPDS t2"
						   + " where t1.KEGG=t2.KEGG order by MAP,MASSBANK";
	
				ArrayList<String> mapList = null;
				try {
					Class.forName("com.mysql.jdbc.Driver");
					String connectUrl = "jdbc:mysql://localhost/MassBank_General";
					Connection con = DriverManager.getConnection(connectUrl, "bird", "bird2006");
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					String prevId = "";
					while ( rs.next() ) {
						String id   = rs.getString(1);
						String kegg = rs.getString(2);
						String map  = rs.getString(3);
						if ( !id.equals(prevId) ) {
							if ( !prevId.equals("") ) {
								massbank2mapList.put(prevId, mapList);
							}
							mapList = new ArrayList<String>();
							massbank2keggList.put(id, kegg);
						}
						mapList.add(map);
						prevId = id;
					}
					massbank2mapList.put(prevId, mapList);
	
					rs.close();
					stmt.close();
					con.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				if (mapList != null) {
					
					//(3) Pathway Map色付けリスト作成
					it = massbank2mapList.keySet().iterator();
					while ( it.hasNext()) {
						String id = (String)it.next();
						String kegg = (String)massbank2keggList.get(id);
		
						ArrayList<String> list1 = massbank2mapList.get(id);
						for ( int i = 0; i < list1.size(); i++ ) {
							String map = list1.get(i);
							ArrayList<String> list2 = null;
							if ( map2keggList.containsKey(map) ) {
								list2 = map2keggList.get(map);
								list2.add(kegg);
							}
							else {
								list2 = new ArrayList<String>();
								list2.add(kegg);
								map2keggList.put(map, list2);
							}
						}
					}
		
					//(4) SOAPでPathway Map色付けメソッド実行
					it = map2keggList.keySet().iterator();
					List<Callable<HashMap<String, String>>> tasks = new ArrayList();
					while ( it.hasNext() ) {
						String map = (String)it.next();
						mapNameList.add(map);
						ArrayList<String> list = map2keggList.get(map);
						String[] cpds = list.toArray(new String[]{});
						Callable<HashMap<String, String>> task = new ColorPathway(map, cpds);
						tasks.add(task);
					}
					Collections.sort(mapNameList);
		
						// スレッドプール10個まで
					ExecutorService exsv = Executors.newFixedThreadPool(10);
					List<Future<HashMap<String, String>>> results = exsv.invokeAll(tasks);
		
						// Pathway mapの画像格納場所
					String saveRootPath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH) + "pathway";
					File rootDir = new File(saveRootPath);
					if ( !rootDir.exists() ) {
						rootDir.mkdir();
					}
//					String savePath = saveRootPath + File.separator + this.jobId;
//					File newDir = new File(savePath);
//					if ( !newDir.exists() ) {
//						newDir.mkdir();
//					}
	
					//(6) Pathway mapのURLを取得
					for ( Future<HashMap<String, String>> future: results ) {
						HashMap<String, String> res = future.get();
						it = res.keySet().iterator();
						String map = (String)it.next();
						String mapUrl = res.get(map);
						String filePath = saveRootPath + File.separator + this.jobId + "_" + map + ".png";
						FileUtil.downloadFile(mapUrl, filePath);
					}
				}
			}
			
			//(7) 結果出力
			out = new PrintWriter(new BufferedWriter(new FileWriter(htmlFile)));
				// ヘッダー出力
			String reqIonStr = "Both";
			try {
				if (Integer.parseInt(this.ion) > 0) {
					reqIonStr = "Positive";
				}
				else if(Integer.parseInt(this.ion) < 0) {
					reqIonStr = "Negative";
				}
			}
			catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
			String title = "Summary of Batch Service Results";
			out.println("<html>");
			out.println("<head>");
			out.println("<title>" + title + "</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>" + title + "</h1>");
			out.println("<hr>");
			out.println("<h3>Request Date : " + this.time +"</h3>");
			out.println("Instrument Type : " + this.inst + "<br>");
			out.println("MS Type : " + this.ms + "<br>");
			out.println("Ion Mode : " + reqIonStr + "<br>");
			out.println("<br>");
			out.println("<hr>");
			out.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
			String cols = String.valueOf(mapNameList.size());
			out.println("<tr>");
			out.println("<th bgcolor=\"LavenderBlush\" rowspan=\"2\">No.</th>");
			out.println("<th bgcolor=\"LavenderBlush\" rowspan=\"2\">Query&nbsp;Name</th>");
			out.println("<th bgcolor=\"LightCyan\" rowspan=\"2\">Score</th>");
			out.println("<th bgcolor=\"LightCyan\" rowspan=\"2\">MassBank&nbsp;ID</th>");
			out.println("<th bgcolor=\"LightCyan\" rowspan=\"2\">Record&nbsp;Title</th>");
			out.println("<th bgcolor=\"LightCyan\" rowspan=\"2\">Formula</th>");
			if ( isKeggReturn ) {
				out.println("<th bgcolor=\"LightYellow\" rowspan=\"2\">KEGG&nbsp;ID</th>");
				out.println("<th bgcolor=\"LightYellow\" colspan=\"" + cols + "\">Colored&nbsp;Pathway&nbsp;Maps</th>");
			}
			out.println("</tr>");
			out.print("<tr bgcolor=\"moccasin\">");
			for ( int i = 0; i < mapNameList.size(); i++ ) {
				out.print("<th>MAP" + String.valueOf(i+1) + "</th>");
			}
			out.println("</tr>");

			for ( int i = 0; i < nameList.size(); i++ ) {
				out.println("<tr>");
				String no = String.format("%5d", i+1);
				no = no.replace(" ", "&nbsp;");
				out.println("<td>" + no + "</td>");
					// Query Name
				String queryName = nameList.get(i);
				out.println("<td nowrap>" + queryName + "</td>");

				line = top1LineList.get(i);
				if ( line.equals("0") ) {
					if ( isKeggReturn ) {
						cols = String.valueOf(mapNameList.size()+5);
					}
					else {
						cols = String.valueOf(4);
					}
					out.println("<td colspan=\"" + cols + "\">No Hit Record</td>");
				}
				else if ( line.equals("Invalid") ) {
					if ( isKeggReturn ) {
						cols = String.valueOf(mapNameList.size()+5);
					}
					else {
						cols = String.valueOf(4);
					}
					out.println("<td colspan=\"" + cols + "\">Invalid Query</td>");
				}
				else {
					String[] data = formatLine(line);
					String id       = data[0];
					String recTitle = data[1];
					String formula  = data[2];
					String score    = data[4];

						// Score
					out.println("<td>" + score + "</td>");
						// MassBank ID & Link
					out.println("<td><a href=\"" + serverUrl +"jsp/FwdRecord.jsp?id=" + id + "\" target=\"_blank\">" + id + "</td>");
						// Record Title
					out.println("<td nowrap>" + recTitle + "</td>");
						// Formula
					out.println("<td nowrap>" + formula + "</td>");
						// KEGG ID & Link
					if ( isKeggReturn ) {
						String keggLink = "&nbsp;&nbsp;-";
						if ( massbank2keggList.containsKey(id) ) {
							String keggUrl = "http://www.genome.jp/dbget-bin/www_bget?";
							String kegg = massbank2keggList.get(id);
							switch (kegg.charAt(0)) {
							case 'C':
								keggUrl += "cpd:" + kegg;
								break;
							case 'D':
								keggUrl += "dr:" + kegg;
								break;
							case 'G':
								keggUrl += "gl:" + kegg;
								break;
							}
							keggLink = "<a href=\"" + keggUrl + "\" target=\"_blank\">" + kegg + "</a>";
						}
						out.println("<td>" + keggLink + "</td>");
							// Pathway Map Link
						if ( massbank2mapList.containsKey(id) ) {
							ArrayList<String> list = massbank2mapList.get(id);
							for ( int l1 = mapNameList.size() - 1; l1 >= 0; l1-- ) {
								boolean isFound = false;
								String map = "";
								for ( int l2 = list.size() - 1; l2 >= 0; l2-- ) {
									map = list.get(l2);
									if ( map.equals( mapNameList.get(l1) ) ) {
										isFound = true;
										break;
									}
								}
								if ( isFound ) {
									ArrayList<String> list2 = map2keggList.get(map);
									String mapUrl = serverUrl + "temp/pathway/" + this.jobId + "_" + map + ".png";
									out.println("<td nowrap><a href=\"" + mapUrl + "\" target=\"_blank\">map:" + map + "(" + list2.size() + ")</a></td>");
								}
								else {
									out.println("<td>&nbsp;&nbsp;-</td>");
								}
							}
						}
						else {
							for ( int l1 = mapNameList.size() - 1; l1 >= 0; l1-- ) {
								out.println("<td>&nbsp;&nbsp;-</td>");
							}
						}
					}
				}
				out.println("</tr>");
			}
			out.println("</table>");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { if (in != null) { in.close(); } } catch (IOException e) {}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
	/**
	 * 検索結果行のフォーマット
	 * Accession、Title、Formula、Ion、Score、Hit の順に
	 * 適切なフォーマットに変換した値を配列に格納して返却する
	 * @param line フォーマット対象
	 * @return フォーマット後の値を格納した配列
	 */
	private String[] formatLine(String line) {
		String[] splitLine = line.split("\t");
		String acc     = splitLine[0].trim();
		String title   = splitLine[1].trim();
		String formula = splitLine[2].trim();
		String emass   = splitLine[3].trim();
		String score   = "0." + splitLine[4].split("\\.")[1].trim().substring(0, 4);
		String hit     = splitLine[4].split("\\.")[0].trim();
		return new String[]{acc, title, formula, emass, score, hit};
	}
	
	/**
	 * 検索実行
	 */
	private boolean doSearch(String name, String peakData, int num) {
		// NAMEタグがない場合は名前をつける
		DecimalFormat df = new DecimalFormat("000000");
		String compoundName = name;
		if ( compoundName.equals("") ) {
			compoundName = "Compound_" + df.format(num + 1);
		}
		else {
			compoundName = compoundName.replaceFirst("^Name: *", "");
		}


		// 強度最大値を求める
		double max = 0;
		String[] vals = peakData.split(";");
		ArrayList<String> mzList = new ArrayList<String>();
		ArrayList<Double> inteList = new ArrayList<Double>();
		for ( int i = 0; i < vals.length; i++) {
			String pair = vals[i].trim();
			pair = pair.replaceAll(" +", ",");
			String[] items = pair.split(",");
			if ( items.length < 2 || 
				 !NumberUtils.isNumber(items[0]) || !NumberUtils.isNumber(items[1]) ) {
				// ERROR
				writer.println(compoundName + "\n-1\n" );
				writer.flush();
				return false;
			}
			double dblInte = Double.parseDouble(items[1]);
			if ( dblInte > max ) {
				max = dblInte;
			}
			mzList.add(items[0]);
			inteList.add(dblInte);
		}

		// 相対強度に変換した値をパラメータをセットする
		String paramPeak = "";
		for (int i = 0; i < mzList.size(); i++) {
			String mz = mzList.get(i);
			int inte = new Double(inteList.get(i) / max * 999).intValue();
			paramPeak +=  mz + "," + String.valueOf(inte) + "@";
		}

		// ** search spectra
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
		String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
	 			+ "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
	 			+ "&CUTOFF=5" + "&NUM=0&VAL=" + paramPeak + "&INST=" + this.inst + "&MS=" + this.ms.replaceAll("All", "all") + "&ION=" + this.ion + "&API=true";
		ArrayList<String> result = mbcommon.execDispatcher( this.serverUrl, typeName, param, true, null );
		int hitCnt = result.size();
		int sendCnt = hitCnt;
		if ( sendCnt > 20 ) {
			sendCnt = 20;
		}

		// ** save search results
		writer.println(compoundName);
		writer.println(hitCnt);
		writer.println(sendCnt);
		for ( int i = 0; i < sendCnt; i ++ ) {
			String rec = (String)result.get(i);
			writer.println(rec);
		}
		writer.println("");
		writer.flush();
		return true;
	}

	/**
	 * 終了グラグセット
	 */
	public void setTerminate() {
		isTerminated = true;
	}
}
