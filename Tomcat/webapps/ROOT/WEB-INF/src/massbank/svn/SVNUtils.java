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
 * SVNUtils.java
 *
 * ver 1.0.1 2012.10.04
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import massbank.MassBankEnv;

public class SVNUtils {

	/**
	 * 
	 */
	public static String escapeDirName(String dirName) {
		String name1 = StringUtils.replaceChars(dirName, ",.", "  ");
		String name2 = name1.trim().replaceAll(" {2,}", " ");
		String name3 = name2.replaceAll(" ", "_");
		return name3;
	}

	/**
	 * 
	 */
	public static String getWorkCopyPath(String repoDirName, String wcName) {
		repoDirName = escapeDirName(repoDirName);
		String workCopyBasePath = MassBankEnv.get(MassBankEnv.KEY_APACHE_APPROOT_PATH)
			 									+ "svn_wc" + File.separator + wcName;
		String workCopyPath = workCopyBasePath + File.separator;
		if ( wcName.equals(SVNOperation.WC_OPEN_DATA) ) {
			workCopyPath += "record" + File.separator;
		}
		workCopyPath += repoDirName;
		return workCopyPath;
	}


	/**
	 * 
	 */
	public static String getLocalIPAddress() {
		String address = "";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while ( interfaces.hasMoreElements() ){
				NetworkInterface network = interfaces.nextElement();
				Enumeration<InetAddress> addresses = network.getInetAddresses();
				while ( addresses.hasMoreElements() ){
					InetAddress inet  = addresses.nextElement();
					if ( !inet.isLoopbackAddress() && inet instanceof Inet4Address ) {
						return inet.getHostAddress();
					}
				}
			}
			address = InetAddress.getLocalHost().getHostAddress();
		}
		catch ( Exception e ) {}
		return address;
	}

	/**
	 * 
	 */
	public static String[] getLicenseOKFileList(String[] paths) {
		List<String> licenseOKList = new ArrayList();
		for ( int i = 0; i < paths.length; i++ ) {
			String filePath = paths[i];
			boolean found = false;
			try {
				List<String> lines = FileUtils.readLines(new File(filePath));
				for ( int l = 0; l < lines.size(); l++ ) {
					String line = lines.get(l);
					if ( line.startsWith("LICENSE: CC") ) {
						found = true;
						break;
					}
					else if ( line.startsWith("CH$NAME") ) {
						break;
					}
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
			if ( found ) {
				licenseOKList.add(filePath);
			}
		}
		return (String[])licenseOKList.toArray(new String[]{});
	}

	/**
	 *
	 */
	public static Connection connectDB(String dbName) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost/" + dbName;
			Connection con = DriverManager.getConnection(url, "bird", "bird2006");
			return con;
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 */
	public static boolean checkDBExists(String dbName) {
		if ( dbName.equals("") ) {
			return false;
		}
		Connection con = connectDB(dbName);
		if ( con == null ) {
			return false;
		}
		try {
			Statement stmt = con.createStatement();
			String sql = "SHOW TABLES LIKE 'SPECTRUM'";
			ResultSet rs = stmt.executeQuery(sql);
			String val = "";
			if ( rs.first() ) {
				val = rs.getString(1);
			}
			con.close();
			if ( !val.equals("") ) {
				return true;
			}
			else { 
				return false;
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
}
