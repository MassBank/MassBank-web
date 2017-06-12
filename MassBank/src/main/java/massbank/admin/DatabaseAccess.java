/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * DBアクセスクラス（JDBC）
 *
 * ver 1.0.0 2009.02.02
 *
 ******************************************************************************/

package massbank.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBアクセスクラス（JDBC）
 */
public class DatabaseAccess {

	private String driver;
	private String url;
	private String user;
	private String password;
	private Connection con;
	private Statement stmt;
	
	/**
	 * コンストラクタ
	 * @param hostName ホスト名
	 * @param dbName DB名
	 */
	public DatabaseAccess(String hostName, String dbName) {
		if (hostName == null || hostName.equals("")) {
			hostName = "localhost";
		}
		if (dbName == null || dbName.equals("")) {
			dbName = "MassBank";
		}
		this.driver = "org.mariadb.jdbc.Driver";
		this.url = "jdbc:mysql://" + hostName + "/" + dbName;
		this.user = "bird";
		this.password = "bird2006";
	}

	/**
	 * データベースへの接続
	 * @return 結果
	 */
	public synchronized boolean open() {
		boolean ret = true;
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, password);
			stmt = con.createStatement();
		}
		catch (SQLException e) {
			e.printStackTrace();
			ret = false;
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	/**
	 * データベースから切断
	 * @return 結果
	 */
	public synchronized boolean close() {
		boolean ret = true;
		if ( stmt != null ) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
				ret = false;
			}
		}
		if ( con != null ) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
				ret = false;
			}
		}
		return ret;
	}
	
	/**
	 * 指定されたSQL文（参照系）を実行
	 * @param SQL文
	 * @return 結果セット
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String sql) throws SQLException { 
		return stmt.executeQuery(sql);
	}
	
	/**
	 * 指定されたSQL文（更新系）を実行
	 * @param SQL文
	 * @return 行数（何も返さない場合は0）
	 * @throws SQLException
	 */
	public int executeUpdate(String sql) throws SQLException { 
		return stmt.executeUpdate(sql);
	}
}
