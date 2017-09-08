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
 * FileUpload 共通クラス
 *
 * ver 1.0.0 2009.02.02
 *
 ******************************************************************************/
package massbank;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

/**
 * FileUpload 共通クラス
 */
public class FileUpload extends DiskFileUpload {

	// デフォルトアップロードパス
	public static final String UPLOAD_PATH = System.getProperty("java.io.tmpdir");
	
	// 出力先パス
	private String outPath = UPLOAD_PATH;
	
	// リクエスト情報
	private HttpServletRequest req = null;

	// multipart/form-dataリスト
	private List<FileItem> fileItemList = null;
	
	// アップロードファイル情報<ファイル名, アップロード結果>
	private HashMap<String, Boolean> upFileMap = null;

	/**
	 * コンストラクタ
	 * @param req リクエスト
	 * @param outPath 出力先パス
	 */
	public FileUpload(HttpServletRequest req, String outPath ) {
		super();
		setSizeMax(-1);					// サイズ
		setSizeThreshold(1024);			// バッファサイズ
		setRepositoryPath(outPath);		// 保存先フォルダ
		setHeaderEncoding("utf-8");		// 文字エンコーディング
		this.req = req;
		this.outPath = outPath;
	}
	
	/**
	 * リクエストパラメータ解析
	 * multipart/form-dataに含まれている通常リクエスト情報を取得する
	 * 失敗した場合はnullを返却する
	 * @return 通常リクエスト情報MAP<キー, 値>
	 */
	public HashMap<String, String[]> getRequestParam() {
		
		if (fileItemList == null) {
			try {
				fileItemList = (List<FileItem>)parseRequest(req);
			}
			catch (FileUploadException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		HashMap<String, String[]> reqParamMap = new HashMap<String, String[]>();
		for (FileItem fItem : fileItemList) {
			
			// 通常フィールドの場合（リクエストパラメータの値が配列でない場合）
			if (fItem.isFormField()) {
				String key = fItem.getFieldName();
				String val = fItem.getString();
				if ( key != null && !key.equals("") ) {
					reqParamMap.put(key, new String[]{val});
				}
			}
		}
		return reqParamMap;
	}
	
	/**
	 * ファイルアップロード
	 * multipart/form-dataに含まれているファイルをアップロードする
	 * 失敗した場合はnullを返却する
	 * @return アップロードファイル情報MAP<ファイル名, アップロード結果>
	 */
	public HashMap<String, Boolean> doUpload() {
		
		if (fileItemList == null) {
			try {
			     fileItemList = (List<FileItem>)parseRequest(req);
			}
			catch (FileUploadException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		upFileMap = new HashMap<String, Boolean>();
		for (FileItem fItem : fileItemList) {
			
			// ファイルフィールドの場合
			if ( !fItem.isFormField() ) {
				
				String key = "";
				boolean val = false;
				
				// ファイル名取得（環境依存でパス情報が含まれる場合がある）
				String filePath = (fItem.getName() != null) ? fItem.getName() : "";
				
				// ファイル名取得（確実にファイル名のみを取得）
				String fileName = (new File(filePath)).getName();
				int pos = fileName.lastIndexOf("\\");
				fileName = fileName.substring( pos + 1 );
				pos = fileName.lastIndexOf("/");
				fileName = fileName.substring( pos + 1 );
				
				// ファイルアップロード
				if ( !fileName.equals("") ) {
					key = fileName;
					File upFile = new File( outPath + "/" + fileName); 
					try {
						fItem.write( upFile );
						val = true;
					}
					catch ( Exception e) {
						e.printStackTrace();
						upFile.delete();
					}	
				}
				upFileMap.put(key, val);
			}
		}
		
		return upFileMap;
	}
	
	/**
	 * FileItemの削除
	 * ファイルアップロードに関係する一時ディスク領域も含む、
	 * ストレージ上のファイルアイテムを削除する。
	 * FileItemインスタンスがガベージコレクションにかかった時にこのストレージは削除されるが、
	 * このメソッドは素早く確実に削除を実施する。
	 * 全てのmultipart/form-dataに含まれている情報を取得後に呼び出すこと。
	 */
	public void deleteFileItem() {
		if (fileItemList != null) {
			for (FileItem fItem : fileItemList) {
				fItem.delete();
			}			
		}
	}
	
	/**
	 * アップロードされたファイルの全削除
	 */
	public void deleteAllFile() {
		String fileName;
		boolean upResult;
		File targetFile;
		for (Map.Entry<String, Boolean> e : upFileMap.entrySet()) {
			fileName = e.getKey();
			upResult = e.getValue();
			targetFile = new File( outPath + "/" + fileName );
			if ( upResult && targetFile.exists() ) {
				targetFile.delete();
			}
		}
	}
	
	/**
	 * アップロードされたファイルの削除
	 * @param fileName 削除対象ファイル名
	 */
	public void deleteFile(String fileName) {
		File targetFile = new File ( outPath + "/" + fileName );
		if ( targetFile.exists() ) {
			targetFile.delete();
		}
	}
}
