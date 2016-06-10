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
 * ジョブ管理データクラス
 *
 * ver 1.0.0 2010.04.15
 *
 ******************************************************************************/
package massbank;

public class JobInfo {
	private String jobId     = "";
	private String status    = "";
	private String sessionId = "";
	private String timeStamp = "";
	private String ipAddr    = "";
	private String mailAddr  = "";
	private String qFileName = "";
	private String qFileSize = "";
	private String sParam    = "";
	private String tempName  = "";
	private String result    = "";

	public JobInfo() {
	}

	public void setJobId(String val) {
		this.jobId = val;
	}
	public void setStatus(String val) {
		this.status = val;
	}
	public void setSessionId(String val) {
		this.sessionId = val;
	}
	public void setTimeStamp(String val) {
		this.timeStamp = val;
	}
	public void setIpAddr(String val) {
		this.ipAddr = val;
	}
	public void setMailAddr(String val) {
		this.mailAddr = val;
	}
	public void setQueryFileName(String val) {
		this.qFileName = val;
	}
	public void setQueryFileSize(String val) {
		this.qFileSize = val;
	}
	public void setTempName(String val) {
		this.tempName = val;
	}
	public void setSearchParam(String val) {
		this.sParam = val;
	}
	public void setResult(String val) {
		this.result = val;
	}

	public String getJobId() {
		return this.jobId;
	}
	public String getStatus() {
		return this.status;
	}
	public String getTimeStamp() {
		return this.timeStamp;
	}
	public String getSessionId() {
		return this.sessionId;
	}
	public String getIpAddr() {
		return this.ipAddr;
	}
	public String getMailAddr() {
		return this.mailAddr;
	}
	public String getQueryFileName() {
		return this.qFileName;
	}
	public String getQueryFileSize() {
		String val = this.qFileSize;
		if ( val.equals("") ) {
			val = "0";
		}
		return val;
	}
	public String getTempName() {
		return this.tempName;
	}
	public String getSearchParam() {
		return this.sParam;
	}
	public String getResult() {
		return this.result;
	}
}
