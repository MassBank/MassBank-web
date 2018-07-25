/*******************************************************************************
 *
 * Copyright (C) 2012 JST-BIRD MassBank
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
 * ファイル操作ユーティリティクラス
 *
 * ver 1.0.5 2012.02.20
 *
 ******************************************************************************/
package massbank.admin;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import massbank.CmdExecute;
import massbank.CmdResult;
import massbank.RecordParserDefinition;

public class FileUtil {
	private static final Logger logger = LogManager.getLogger(RecordParserDefinition.class);
	
	/** OS名 */
	//private static String OS_NAME = System.getProperty("os.name");

	/**
	 * ZIP形式の圧縮ファイルを作成する
	 * @param zipFilePath ZIPファイルのパス
	 * @param filePath 圧縮元ファイル
	 * @return true:成功 / false:失敗
	 */
	public static boolean makeZip(String zipFilePath, String filePath) {
		String[] cmd = new String[]{ "zip", "-oqj", zipFilePath, filePath };
		return command( cmd, true );
	}
	
	/**
	 * アーカイブを解凍する（ZIP形式）
	 * @param archivePath アーカイブのパス
	 * @param destPath 解凍先のパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean unZip(String archivePath, String destPath) {
		String[] cmd = new String[]{ "unzip", "-oq", archivePath, "-d", destPath };
		return command( cmd, true );
	}
	
	/**
	 * アーカイブを解凍する
	 * @param archivePath アーカイブのパス
	 * @param destPath 解凍先のパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean uncompress(String archivePath, String destPath) {
		// ドライブ名がある場合は取り除く
		int pos = archivePath.indexOf(":");
		if ( pos >= 0 ) {
			archivePath = archivePath.substring(pos + 1);
		}
		String cmd[] = new String[]{ "tar", "xfz", archivePath, "-C", destPath };
		return command( cmd, false );
	}

	/**
	 * ファイルをコピーする
	 * @deprecated Windowsで使用した場合はコピー先ファイルが不適切な所有者になる（OS依存）
	 * @param srcPath コピー元ファイルのパス
	 * @param destPath コピー先ファイルのパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean copyFile(String srcPath, String destPath) {
		String[] cmd = new String[]{ "cp", "-pf", srcPath, destPath };
		return command( cmd, false );
	}

	/**
	 * ディレクトリをコピーする
	 * @deprecated Windowsで使用した場合はコピー先ディレクトリが不適切な所有者になる（OS依存）
	 * @param srcPath コピー元ディレクトリのパス
	 * @param destPath コピー先ディレクトリのパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean copyDir(String srcPath, String destPath) {
		String[] cmd = new String[]{ "cp", "-pfr", srcPath, destPath };
		return command( cmd, false );
	}
	
	/**
	 * ファイルを削除する
	 * @param filePath 削除するファイルのパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean removeFile(String filePath) {
		String[] cmd = new String[]{ "rm", "-f", filePath };
		return command( cmd, false );
	}

	/**
	 * ディレクトリを削除する
	 * @param dirPath 削除するディレクトリのパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean removeDir(String dirPath) {
		String[] cmd = new String[]{ "rm", "-Rf", dirPath };
		return command( cmd, false );
	}

	/**
	 * 権限を変更する
	 * @param permission 権限
	 * @param path 権限変更対象のディレクトリもしくはフォルダパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean changeMode(String permission, String path) {
		String[] cmd = new String[]{ "chmod", "-R", permission, path };
		return command( cmd, false );
	}
	
	/**
	 * シェルを実行する
	 * @param filePath 実行するシェルのパス
	 * @return true:成功 / false:失敗
	 */
	public static boolean executeShell(String filePath) {
		String[] cmd = new String[]{ filePath };
		return command( cmd, false );
	}

	/**
	 * ファイルをダウンロードする
	 * @param srcUrl ファイルソースのURL
	 * @param savePath 格納先パス
	 * @return true:成功 / false:失敗
	 */
	public static boolean downloadFile(String srcUrl, String savePath ) {
		try {
			URL url = new URL( srcUrl );
			InputStream inpstrm = url.openStream();
			OutputStream outstrm = new FileOutputStream(savePath);
			byte buf[] = new byte[8192];
			int len = 0;
			while( ( len = inpstrm.read(buf) ) != -1 ) {
				outstrm.write( buf, 0, len );
			}
			outstrm.flush();
			outstrm.close();
			inpstrm.close();
		}
		catch ( Exception ex ) {
			logger.debug( ex.toString() );
			return false;
		}
		return true;
	}
	
	/**
	 * SQLファイルを実行する
	 * @param host リモートホスト名
	 * @param db 対象のDB名
	 * @param file 実行するファイル名
	 * @return true:成功 / false:失敗
	 */
	public static boolean execSqlFile(String host, String db, String file) {
		String opHost = "";
		if (host != null && !host.equals("")) {
			opHost = " --host=" + host;
		}
		String main = "mysql" + opHost + " --user=bird --password=bird2006 \"" + db + "\" < \"" + file + "\"";
		
		String[] cmd = null;
		cmd = new String[]{ "sh", "-c", main };
		
		return command( cmd, true );
	}
	
	/**
	 * SQLダンプを実行する
	 * @param host リモートホスト名
	 * @param db 対象のDB名
	 * @param tables 対象のテーブル
	 * @param file 出力するファイル名
	 * @return true:成功 / false:失敗
	 */
	public static boolean execSqlDump(String host, String db, String[] tables, String file) {
		String opHost = "";
		if (host != null && !host.equals("")) {
			opHost = " --host=" + host;
		}
		StringBuilder strTable = new StringBuilder();
		if (tables != null) {
			for (String table : tables) {
				strTable.append(" " + table);
			}
		}
		String main = "mysqldump" + opHost + " --user=bird --password=bird2006 \"" + db + "\"" + strTable.toString() + " > \"" + file + "\"";
		
		String[] cmd = null;
		cmd = new String[]{ "sh", "-c", main };
		
		return command( cmd, true );
	}
	
	/**
	 * コマンドを実行する
	 * @param cmd 実行コマンド
	 * @param isLongTimeOut タイムアウト値延長フラグ
	 * @return true:成功 / false:失敗
	 */
	public static boolean command(String[] cmd, boolean isLongTimeOut) {
		// コマンド実行
		CmdResult res = new CmdExecute(isLongTimeOut).exec(cmd);
			// エラー出力があればログに書き出す
		String err = res.getStderr();
		if ( !err.equals("") ) {
			String cmdline = "";
			for ( int i = 0; i < cmd.length; i++ ) {
				cmdline += cmd[i] + " ";
			}
			String crlf = System.getProperty("line.separator");
			String errMsg = crlf + "[Command] " + cmdline + crlf + "[Error Discription]" + crlf + err;
			logger.warn( errMsg );
		}
			// 終了コード取得
		if ( res.getStatus() != 0 ) {
			if ( err.indexOf("Using a password on the command line interface can be insecure") == -1 ) {
				return false;
			}
		}
		return true;
	}
}
