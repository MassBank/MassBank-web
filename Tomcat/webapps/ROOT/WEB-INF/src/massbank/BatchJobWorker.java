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
 * ver 1.0.3 2010.04.08
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
import java.text.NumberFormat;
import java.util.ArrayList;

import massbank.admin.AdminCommon;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;

public class BatchJobWorker extends Thread { 
	private String sessionId;
	private String timeStamp;
	private String mailAddress;
	private String fileName;
	private String inst;
	private String ion;
	private String name = "";
	private String peak = "";
	private int sendLen = 0;
	private final int LIMIT = 33554432; // 32 GB
	private String serverUrl = "";
	private boolean isTerminated = false;

	/**
	 * コンストラクタ
	 * @param jobInfo
	 */
	public BatchJobWorker(BatchJobInfo jobInfo) {
		this.sessionId   = jobInfo.getSessionId();
		this.timeStamp   = jobInfo.getTimeStamp();
		this.mailAddress = jobInfo.getMailAddr();
		this.fileName    = jobInfo.getTempName();
		this.inst        = jobInfo.getInstType(true);
		this.ion         = jobInfo.getIonMode();
		if (this.inst.equals("")) { this.inst = "ALL"; }
		if (this.ion.equals("")) { this.ion = "1"; }
	}

	public void run() {
		File attacheDir = null;
		try {
			GetConfig conf = new GetConfig(BatchService.BASE_URL);
			this.serverUrl = conf.getServerUrl();

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
				line = line.trim();
				if ( line.startsWith("//") ) {
					continue;
				}
				else if ( line.matches("^Name:.*") ) {
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
			
			// メール送信情報生成
			AdminCommon admin = new AdminCommon(BatchService.BASE_URL, BatchService.REAL_PATH);
			SendMailInfo info = new SendMailInfo(admin.getMailSmtp(), admin.getMailFrom(), this.mailAddress);
			info.setFromName(admin.getMailName());
			info.setSubject("MassBank Batch Service Results");
			info.setContents( "Dear Users,\n\nThank you for using MassBank Batch Service.\n"
							+ "\n"
							+ "The results for your request dated '" + time + "' are attached to this e-mail.\n"
							+ "\n"
							+ "--\n"
							+ "MassBank.jp - High Resolution Mass Spectral Database\n"
							+ "  URL: http://www.massbank.jp/\n"
							+ "  E-mail: massbank@iab.keio.ac.jp");
			
			
			// 添付ファイル生成一時ディレクトリ
			attacheDir = new File(tempDir + "/batch_" + RandomStringUtils.randomAlphanumeric(9));
			while (attacheDir.exists()) {
				attacheDir = new File(tempDir + "/batch_" + RandomStringUtils.randomAlphanumeric(9));
			}
			attacheDir.mkdir();
			
			// 添付ファイル生成（テキスト形式）
			File textFile = new File(attacheDir.getPath() + "/MassBankResults.txt");
			textFile.createNewFile();
			createTextFile(time, f2, textFile);
			
			// 添付ファイル生成（HTML形式）
			File htmlFile = new File(attacheDir.getPath() + "/MassBankResults.html");
			htmlFile.createNewFile();
			createHtmlFile(time, f2, htmlFile);
			
			info.setFiles(new File[]{textFile, htmlFile});
			
			// メール送信
			SendMail.send(info);

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
		finally {
			if ( attacheDir != null && attacheDir.isDirectory() ) {
				try { FileUtils.forceDelete(attacheDir); } catch(IOException e) { e.printStackTrace(); }
			}
		}
	}
	
	/**
	 * 添付ファイル生成（テキスト形式）
	 * @param time リクエスト受け取り時間 
	 * @param resultFile 結果ファイル
	 * @param textFile 添付用テキストファイル
	 */
	private void createTextFile(String time, File resultFile, File textFile) {
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
			out.println("Request Date: " + time);
			out.println("# Instrument Type: " + this.inst);
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
				isFinalLine = false;
				if (in.getLineNumber() < 4) {
					continue;
				}
				if (!readName) {
					queryCnt++;
					out.println("### Query " + nf.format(queryCnt) + " ###");
					out.println("# Name: " + line.trim());
					readName = true;
				}
				else if (!readHit) {
					out.println("# Hit: " + nf.format(Integer.parseInt(line.trim())));
					out.println();
					readHit = true;
				}
				else if (!readNum) {
					out.println("Top " + line.trim() + " List");
					out.println("Accession\tTitle\tFormula\tIon\tScore\tHit");
					out.println();
					readNum = true;
				}
				else {
					if (!line.trim().equals("")) {
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
			out.println("##### END #####");
			out.println();
			out.println("**********************************************************");
			out.println("*  MassBank.jp - High Resolution Mass Spectral Database  *");
			out.println("*    URL: http://www.massbank.jp/                        *");
			out.println("**********************************************************");
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
	
	/**
	 * 添付ファイル生成（HTML形式）
	 * @param time リクエスト受け取り時間 
	 * @param resultFile 結果ファイル
	 * @param htmlFile 添付用HTMLファイル
	 */
	private void createHtmlFile(String time, File resultFile, File htmlFile) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		LineNumberReader in = null;
		PrintWriter out = null;
		try {
			in = new LineNumberReader(new FileReader(resultFile));
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
			out.println("<html>");
			out.println("<head><title>MassBank Batch Service Results</title></head>");
			out.println("<body>");
			out.println("<h1><a href=\"http://www.massbank.jp/\" target=\"_blank\">MassBank</a> Batch Service Results</h1>");
			out.println("<hr>");
			out.println("<h2>Request Date : " + time +"<h2>");
			out.println("Instrument Type : " + this.inst + "<br>");
			out.println("Ion Mode : " + reqIonStr + "<br>");
			out.println("<br><hr>");
			
			// 結果出力
			String line;
			long queryCnt = 0;
			boolean readName = false;
			boolean readHit = false;
			boolean readNum = false;
			while ( ( line = in.readLine() ) != null ) {
				if (in.getLineNumber() < 4) {
					continue;
				}
				if (!readName) {
					queryCnt++;
					out.println("<h2>Query " + nf.format(queryCnt) + "</h2><br>");
					out.println("Name: " + line.trim() + "<br>");
					readName = true;
				}
				else if (!readHit) {
					out.println("Hit: " + nf.format(Integer.parseInt(line.trim())) + "<br>");
					readHit = true;
				}
				else if (!readNum) {
					out.println("<table border=\"1\">");
					out.println("<tr><td colspan=\"6\">Top " + line.trim() + " List</td></tr>");
					out.println("<tr><th>Accession</th><th>Title</th><th>Formula</th><th>Ion</th><th>Score</th><th>Hit</th></tr>");
					readNum = true;
				}
				else {
					if (!line.trim().equals("")) {
						String[] data = formatLine(line);
						String acc = data[0];
						String title = data[1];
						String formula = data[2];
						String ion = data[3];
						String score = data[4];
						String hit = data[5];
						
						out.println("<tr>");
						out.println("<td><a href=\"http://www.massbank.jp/jsp/FwdRecord.jsp?id=" + acc + "\" target=\"_blank\">" + acc + "</td>");
						out.println("<td>" + title + "</td>");
						out.println("<td>" + formula + "</td>");
						out.println("<td>" + ion + "</td>");
						out.println("<td>" + score + "</td>");
						out.println("<td align=\"right\">" + hit + "</td>");
						out.println("</tr>");
					}
					else {
						out.println("</table>");
						out.println("<hr>");
						readName = false;
						readHit = false;
						readNum = false;
					}
				}
			}
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
		String acc = splitLine[1].trim();
		String title = splitLine[0].trim();
		String formula = splitLine[3].trim();
		String ion = "[-]";
		try {
			int tmpIon = Integer.parseInt(splitLine[2].trim());
			if (tmpIon > 0) {
				ion = "[P]";
			}
			else if(tmpIon < 0) {
				ion = "[N]";
			}
		}
		catch(NumberFormatException e) {
		}
		String score = "0." + splitLine[4].split("\\.")[1].trim().substring(0, 4);
		String hit = splitLine[4].split("\\.")[0].trim();
		return new String[]{acc, title, formula, ion, score, hit};
	}
	
	/**
	 * 検索実行
	 * @param writer
	 */
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
	 			+ "&CUTOFF=5" + "&NUM=0&VAL=" + peak + "&INST=" + inst + "&ION=" + ion;
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

	/**
	 * 終了処理
	 */
	public void setTerminate() {
		isTerminated = true;
	}
}
