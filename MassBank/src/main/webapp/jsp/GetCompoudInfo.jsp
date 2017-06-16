<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
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
 * 化合物情報の取得
 *
 * ver 1.0.1 2010.11.22
 *
 ******************************************************************************/
%>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.MassBankEnv" %>
<%@ page import="massbank.admin.FileUtil" %>
<%!
	private String baseUrl = "";

	/**
	 * 画像URLの取得
	 */ 
	private String getImageUrl(String site, String fileName, boolean isSizeM) {
		String gifNewUrl = "";

		int siteNo = Integer.parseInt(site);
		GetConfig conf = new GetConfig(this.baseUrl);
		String[] svrUrls = conf.getSiteUrl();
		String[] dbNames = conf.getDbName();
		String dbName = dbNames[siteNo];
		String svrUrl = svrUrls[siteNo];
		if ( siteNo == 0 ) {
			svrUrl = this.baseUrl;
		}

		String gifUrl = svrUrl;
		if ( isSizeM ) {
			gifUrl += "DB/gif/";
		}
		else {
			gifUrl += "DB/gif_small/";
		}
		gifUrl += dbName + "/" + fileName;

		// 画像ファイルがフロントサーバにある場合
		if ( svrUrl.equals(this.baseUrl) ) {
			return gifUrl;
		}
		// 画像ファイルが連携サーバにある場合は、ダウンロードする
		else {
			String tempDir = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH);
			String saveFileName = "";
			int pos = gifUrl.lastIndexOf("/");
			if ( pos > 0 ) {
				String size = "M";
				if ( !isSizeM ) {
					size = "S";
				}
				saveFileName = size + "-" + gifUrl.substring(pos + 1);
			}

			if ( !fileName.equals("") ) {
				// 画像ファイルダウンロード
				FileUtil.downloadFile(gifUrl, tempDir + saveFileName);
				gifNewUrl = this.baseUrl + "temp/" + saveFileName;
			}
			return gifNewUrl;
		}
	}
%>
<%
	this.baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
	String name = "";
	if ( request.getParameter( "name" ) != null ) {
		name = request.getParameter( "name" );
	}
	String site = "";
	if ( request.getParameter( "site" ) != null ) {
		site = request.getParameter( "site" );
	}
	String id = "";
	if ( request.getParameter( "id" ) != null ) {
		id = request.getParameter( "id" );
	}

	String getUrl = this.baseUrl + "jsp/Dispatcher.jsp?type=gcinfo&name=" + URLEncoder.encode(name, "utf-8") + "&site=" + site + "&id=" + id;
	try {
		URL url = new URL(getUrl);
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String gifMFileName = "";
		String gifSFileName = "";
		String line = "";
		while ( (line = in.readLine()) != null ) {
			String[] item = line.split("\t");
			String val = item[0];
			if ( val.indexOf("---GIF:") >= 0 ) {
				gifMFileName = val.replace("---GIF:", "");
			}
			else if ( val.indexOf("---GIF_SMALL:") >= 0 ) {
				gifSFileName = val.replace("---GIF_SMALL:", "");
			}
			else if ( val.indexOf("---FORMULA:") >= 0 ) {
				out.println(val.replace("---", ""));
			}
			else if ( val.indexOf("---EXACT_MASS:") >= 0 ) {
				out.println(val.replace("---", ""));
			}
		}
		in.close();

		String gifMUrl = "";
		String gifSUrl = "";
		if ( !gifMFileName.equals("") ) {
			gifMUrl = getImageUrl(site, gifMFileName, true);
		}
		if ( !gifSFileName.equals("") ) {
			gifSUrl = getImageUrl(site, gifSFileName, false);
		}
		out.println("GIF:" + gifMUrl);
		out.println("GIF_SMALL:" + gifSUrl);
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
%>
