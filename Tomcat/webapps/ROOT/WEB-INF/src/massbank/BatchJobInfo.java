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
 * バッチ検索ジョブ管理データクラス
 *
 * ver 1.0.3 2010.04.08
 *
 ******************************************************************************/
package massbank;

public class BatchJobInfo {
	private String sessionId = "";
	private String timeStamp = "";
	private String ipAddr    = "";
	private String mailAddr  = "";
	private String fileName  = "";
	private String fileSize  = "";
	private String tempName  = "";
	private String status    = "";
	private String instType  = "";
	private String ionMode   = "";
	public static final int SESSION_ID = 0;
	public static final int TIME_STAMP = 1;
	public static final int IP_ADDR    = 2;
	public static final int MAIL_ADDR  = 3;
	public static final int FILE_NAME  = 4;
	public static final int FILE_SIZE  = 5;
	public static final int TEMP_NAME  = 6;
	public static final int STATUS     = 7;
	public static final int INST_TYPE  = 8;
	public static final int ION_MODE   = 9;
	
	public BatchJobInfo() {
	}
	public BatchJobInfo(String[] items) {
		this.sessionId = items[SESSION_ID];
		this.timeStamp = items[TIME_STAMP];
		this.ipAddr    = items[IP_ADDR];
		this.mailAddr  = items[MAIL_ADDR];
		this.fileName  = items[FILE_NAME];
		this.fileSize  = items[FILE_SIZE];
		this.tempName  = items[TEMP_NAME];
		this.status    = items[STATUS];
		this.instType  = items[INST_TYPE].replaceAll("@", ",");
		this.ionMode   = items[ION_MODE];
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
	public void setFileName(String val) {
		this.fileName = val;
	}
	public void setFileSize(String val) {
		this.fileSize = val;
	}
	public void setTempName(String val) {
		this.tempName = val;
	}
	public void setStatus(String val) {
		this.status = val;
	}
	public void setInstType(String[] vals) {
		String tmpInstType = "";
		for (String val : vals) {
			tmpInstType += val.trim() + ",";
		}
		if (!tmpInstType.equals("")) {
			tmpInstType = tmpInstType.substring(0, tmpInstType.length()-1);
		}
		this.instType = tmpInstType;
	}
	public void setIonMode(String val) {
		this.ionMode = val;
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
	public String getTimeStamp() {
		return this.timeStamp;
	}
	public String getFileName() {
		return this.fileName;
	}
	public String getFileSize() {
		return this.fileSize;
	}
	public String getTempName() {
		return this.tempName;
	}
	public String getStatus() {
		return this.status;
	}
	public String getInstType(boolean isCsv) {
		if (isCsv) {
			return this.instType;
		}
		else {
			return this.instType.replaceAll(",", "@");			
		}
	}
	public String getIonMode() {
		return this.ionMode;
	}
}
