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
 * コマンド実行結果格納データクラス
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/
package massbank.admin;

import java.io.Serializable;

	public final class CmdResult implements Serializable {
		private int status = 0;
		private String stdout = "";
		private String stderr = "";

	/**
	 * 終了コードを設定します
	 */
	public void setStatus(int status){
		this.status = status;
	}
	/**
	 * 標準出力の内容を設定します
	 */
	public void setStdout(String msg){
		this.stdout = msg;
	}
	/**
	 * エラー出力の内容を設定します
	 */
	public void setStderr(String msg){
		this.stderr = msg;
	}
	/**
	 * 終了コードを取得します
	 */
	public int getStatus(){
		return this.status;
	}
	/**
	 * 標準出力の内容を取得します
	 */
	public String getStdout(){
		return this.stdout;
	}
	/**
	 * エラー出力の内容を取得します
	 */
	public String getStderr(){
		return this.stderr;
	}
}
