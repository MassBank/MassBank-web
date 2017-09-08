/*******************************************************************************
 *
 * Copyright (C) 2012 MassBank project
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
 * MSDBUpdateUtil.java
 *
 * ver 1.0.4 2013.02.26
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.logging.Logger;
import java.lang.StringBuilder;
import java.sql.Statement;
import java.sql.Connection;
import org.apache.commons.io.FileUtils;
import massbank.admin.FileUtil;

public class MSDBUpdateUtil {
	private static final Logger logger = Logger.getLogger("global");

	/**
	 *
	 */
	public static boolean updateFiles(File srcDir, File destDir, String[] extensions) {
		boolean isUpdated = false;
		if ( !srcDir.exists() ) {
			return false;
		}
		if ( !destDir.exists() ) {
			destDir.mkdirs();
		}
		try {
			List<File> srcFileList = (List<File>)FileUtils.listFiles(srcDir, extensions, false);
			List<File> destFileList = (List<File>)FileUtils.listFiles(destDir, extensions, false);
			FileDifference diff = new FileDifference(srcFileList, destFileList);
			String[] addFilePaths = diff.getAddFilePaths();
			String[] delFileNames = diff.getDeleteFileNames();
			if ( addFilePaths.length > 0 ) {
				isUpdated = true;
				for ( String path : addFilePaths ) {
					FileUtils.copyFileToDirectory(new File(path), destDir);
				}
			}
			if ( delFileNames.length > 0 ) {
				isUpdated = true;
				for ( String name : delFileNames ) {
					String path = destDir.getPath() + File.separator + name;
					FileUtils.deleteQuietly(new File(path));
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		return isUpdated;
	}

	/**
	 *
	 */
	public static boolean restoreDatabase(String dbName, String workCopyPath) {
		Connection con1 = SVNUtils.connectDB("");
		if ( con1 == null ) {
			return false;
		}
		try {
			Statement stmt = con1.createStatement();
			String sql = "CREATE DATABASE IF NOT EXISTS " + dbName;
			stmt.executeUpdate(sql);
			stmt.close();
			con1.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		String dumpFilePath = workCopyPath + File.separator
						+ "other" + File.separator + "massbank_backup.sql";
		File file = new File(dumpFilePath);
		if ( !file.exists() ) {
			logger.severe("[SVNService] File not found:" + dumpFilePath);
			return false;
		}
		// TODO: remove hardcoded DB hostname
		boolean ret = FileUtil.execSqlFile("127.0.0.1", dbName, dumpFilePath);
		if ( !ret ) {
			return false;
		}
		Connection con2 = SVNUtils.connectDB(dbName);
		if ( con2 == null ) {
			return false;
		}
		try {
			Statement stmt = con2.createStatement();
			String sql1 = "DROP TABLE IF EXISTS PEAK_HEAP";
			stmt.executeUpdate(sql1);
			String sql2 = "CREATE TABLE PEAK_HEAP(INDEX(ID),INDEX(MZ),INDEX(RELATIVE)) "
										+ "ENGINE=HEAP SELECT ID,MZ,RELATIVE FROM PEAK";
			stmt.executeUpdate(sql2);
			stmt.close();
			con2.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	public static boolean updateSubStructData(String serverUrl) {
		boolean ret = true;
		String cgiUrl = serverUrl + "cgi-bin/GenSubstructure.cgi";
		try {
			URL url = new URL(cgiUrl);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(10 * 1000);
			con.setReadTimeout(60 * 1000);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = "";
			StringBuilder res = new StringBuilder();
			while ( (line = in.readLine()) != null ) {
				res.append(line);
			}
			if ( res.indexOf("OK") == -1 ) {
				ret = false;
			}
		}
		catch ( IOException e ) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
}
