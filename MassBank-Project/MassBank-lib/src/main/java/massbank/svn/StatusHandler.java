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
 * StatusHandler.java
 *
 * ver 1.0.0 2012.08.30
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;

public class StatusHandler implements ISVNStatusHandler, ISVNEventHandler {
	public static final int TYPE_ALL     = 0;
	public static final int TYPE_ADDED   = 1;
	public static final int TYPE_DELETED = 2;
	private boolean checkouted = false;
	private boolean isModified = false;
	private boolean isMolUpdated = false;
	private List<String> updatedFileList = new ArrayList();

	public StatusHandler(boolean checkouted) {
		this.checkouted = checkouted;
	}
	public void handleEvent(SVNEvent event, double progress) {}
	public void checkCancelled() throws SVNCancelException {}

	/*
	 * 
	 */
	public void handleStatus(SVNStatus status) {
		File file = status.getFile();
		if ( file.isDirectory() ) {
			return;
		}
		String filePath = file.getPath();
		SVNStatusType statusType = null;
		statusType = status.getRemoteContentsStatus();
		if ( statusType == SVNStatusType.STATUS_ADDED
		  || statusType == SVNStatusType.STATUS_NONE
		  || statusType == SVNStatusType.STATUS_DELETED ) {
			String type = statusType.toString();
			this.updatedFileList.add(type + "=" + filePath);
			this.isModified = true;
			String dirName = File.separator + "molfile" + File.separator;
			if ( filePath.indexOf(dirName) >= 0 ) {
				isMolUpdated = true;
			}
		} 
	}

	/*
	 * 
	 */
	public boolean isModified() {
		return this.isModified;
	}

	/*
	 * 
	 */
	public boolean isMolfileUpdated() {
		return this.isMolUpdated;
	}

	/*
	 * 
	 */
	public String[] getUpdatedFiles(int type) {
		List<String> fileNameList = new ArrayList();
		String key = "";
		if ( this.checkouted ) {
			if ( type == TYPE_ADDED )        { key = "none";  }
			else if ( type == TYPE_DELETED ) { key = "";      }
		}
		else {
			if ( type == TYPE_ADDED )        { key = "added"; }
			else if ( type == TYPE_DELETED ) { key = "none";  }
		}
		for ( String val : updatedFileList ) {
			String[] items = val.split("=");
			if ( type == TYPE_ALL || items[0].equals(key) ) {
				fileNameList.add(items[1]);
			}
		}
		Collections.sort(fileNameList);
		return fileNameList.toArray(new String[]{});
	}
}
