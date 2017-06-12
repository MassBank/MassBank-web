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
 * [WEB-API] ジョブステータスデータクラス
 *
 * ver 1.0.0 2010.04.15
 *
 ******************************************************************************/
package massbank.api;

import massbank.JobManager;

public class JobStatus {

	private int statusCode = 0;
	private String status = "";
	private String reqDate = "";

	/**
	 * コンストラクタ
	 */
	public JobStatus() {
	}

	/**
	 * ステータスをセットする
	 */
	public void setStatus(String val) {
		this.status = val;
		if ( val.equals(JobManager.STATE_WAIT) ) {
			statusCode = 0;
		}
		else if ( val.equals(JobManager.STATE_RUN) ) {
			statusCode = 1;
		}
		else if ( val.equals(JobManager.STATE_COMPLETE) ) {
			statusCode = 2;
		}
	}

	/**
	 * ジョブ受付日時をセットする
	 */
	public void setRequestDate(String val) {
		this.reqDate = val;
	}


	/**
	 * ステータスを取得する
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * ステータスコードを取得する
	 */
	public String getStatusCode() {
		return String.valueOf(this.statusCode);
	}

	/**
	 * ジョブ受付日時をセットする
	 */
	public String getRequestDate() {
		return this.reqDate;
	}
}
