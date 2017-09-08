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
 * SVNOperation.java
 *
 * ver 1.0.1 2012.11.05
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNInfo;

public class SVNOperation {
	public static String WC_BACKUP_UPLOAD = "backup_upload";
	public static String WC_BACKUP_DOWNLOAD = "backup_download";
	public static String WC_OPEN_DATA = "open_data";
	private static HashMap<String, ReentrantLock> rlockList = new HashMap<String, ReentrantLock>();
	private ReentrantLock rlock = null;
	private String workCopyPath = "";
	private SVNClientWrapper svnClient = null;
	private StatusHandler status = null;
	private String ipAddress = "";
	private static final Logger logger = Logger.getLogger("global");


	/*
	 * constructor
	 */
	public SVNOperation(String repoDirName, String wcName) throws SVNException {
		repoDirName = SVNUtils.escapeDirName(repoDirName);

		String repoURL = SVNServiceManager.SVN_BASE_URL;
		if ( wcName.equals(WC_OPEN_DATA) ) {
			repoURL += "OpenData/record/" + repoDirName;
		}
		else {
			repoURL += "BackupData/" + repoDirName;
		}
		this.workCopyPath = SVNUtils.getWorkCopyPath(repoDirName, wcName);
		if ( !rlockList.containsKey(this.workCopyPath) ) {
			this.rlock = new ReentrantLock(true);
			this.rlockList.put(this.workCopyPath, this.rlock);
		}
		else {
			this.rlock = this.rlockList.get(this.workCopyPath);
		}
//		logger.severe("** LOCK:" + this.workCopyPath);
		this.rlock.lock();

		this.svnClient = new SVNClientWrapper(repoURL, this.workCopyPath);
		if ( wcName.equals(WC_BACKUP_UPLOAD) || wcName.equals(WC_OPEN_DATA) ) {
			checkout();
		}
		this.ipAddress = SVNUtils.getLocalIPAddress();
	}

	/*
	 *
	 */
	public boolean addEntry(String[] srcFilePaths, String destSubDirName) {
		String destPath = workCopyPath + File.separator;
		if ( !destSubDirName.equals("") ) {
			destPath += destSubDirName + File.separator;
		}
		try {
			for ( String path : srcFilePaths ) {
				FileUtils.copyFileToDirectory(new File(path), new File(destPath));
				String name = FilenameUtils.getName(path);
				this.svnClient.add(destPath + name);
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 *
	 */
	public boolean deleteEntry(String[] srcFileNames, String destSubDirName) {
		String destPath = workCopyPath + File.separator;
		if ( !destSubDirName.equals("") ) {
			destPath += destSubDirName + File.separator;
		}
		for ( int i = 0; i < 2; i++ ) {
			try {
				boolean ret = true;
				for ( String name : srcFileNames ) {
					String filePath = destPath + name;
					ret = this.svnClient.delete(filePath);
					if ( !ret ) {
						if ( isWcModified() ) {
							commit();
							break;
						}
					}
				}
				if ( ret ) { break; }
			}
			catch ( SVNException e ) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/*
	 *
	 */
	public boolean isWcModified() {
		boolean ret = false;
		try {
			this.status = this.svnClient.getStatus();
			if ( this.status != null ) {
				ret = status.isModified();
			}
		}
		catch ( SVNException e ) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * Gets the revision number of working copy
	 */
	public long getWcRevision() {
		long revison = 0;
		try {
			SVNInfo info = this.svnClient.getInfo();
			revison = info.getRevision().getNumber();
		}
		catch ( SVNException e ) {
			e.printStackTrace();
		}
		return revison;
	}

	/*
	 *
	 */
	public boolean checkout() {
		try {
			this.svnClient.checkout();
		}
		catch ( SVNException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * checkout
	 */
	public boolean checkout(long revision) {
		try {
			this.svnClient.checkout(revision);
		}
		catch ( SVNException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * Gets added files
	 */
	public String[] getAddedFiles() {
		if ( this.status == null ) {
			return null;
		}
		return this.status.getUpdatedFiles(StatusHandler.TYPE_ADDED);
	}

	/*
	 * Gets deleteed files
	 */
	public String[] getDeletedFiles() {
		if ( this.status == null ) {
			return null;
		}
		return status.getUpdatedFiles(StatusHandler.TYPE_DELETED);
	}

	/*
	 * commit
	 */
	public boolean commit() {
		try {
			this.svnClient.commit(this.ipAddress);
			return true;
		}
		catch ( SVNException e ) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Gets working copy path
	 */
	public String getWorkCopyPath() {
		return this.workCopyPath;
	}

	/*
	 *
	 */
	public boolean isMolfileUpdated() {
		if ( this.status == null ) {
			return false;
		}
		return this.status.isMolfileUpdated();
	}

	/*
	 *
	 */
	public void end() {
//		logger.severe("** UNLOCK:" + this.workCopyPath);
		this.rlock.unlock();
	}
}
