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
 * SVNClientWrapper.java
 *
 * ver 1.0.1 2012.11.05
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;

public class SVNClientWrapper {
	private static final int ERROR_NEXT_THROW = -1;
	private static final int ERROR_NEXT_BREAK = 0;
	private static final int ERROR_NEXT_RETRY = 1;

	private static HashMap<String, SVNClientManager> managerList = new HashMap<String, SVNClientManager>();
	private SVNClientManager manager = null;
	private SVNURL repoURL = null;
	private String workCopyPath = "";
	private File workCopy = null;
	private String className = this.getClass().getCanonicalName();
	private final Logger logger = Logger.getLogger("global");

	/*
	 * constructor
	 */
	public SVNClientWrapper(String repoURL, String workCopyPath) throws SVNException {
		this.workCopyPath = workCopyPath;
		this.workCopy = new File(workCopyPath);
		this.repoURL = SVNURL.parseURIEncoded(repoURL);
		String[] vals = SVNServiceManager.SVN_INFO.split(":");
		if ( !managerList.containsKey(workCopyPath) ) {
			ISVNOptions options = SVNWCUtil.createDefaultOptions(false);
			ISVNAuthenticationManager authManager
				= SVNWCUtil.createDefaultAuthenticationManager(vals[0], vals[1]);
			this.manager = SVNClientManager.newInstance(options, authManager);
			managerList.put(this.workCopyPath, this.manager);
		}
		else {
			this.manager = this.managerList.get(this.workCopyPath);
		}
	}

	/*
	 * Schedules an unversioned item for addition to a repository thus putting it under version control.
	 */
	public void add(String filePath) throws SVNException {
		SVNWCClient client = this.manager.getWCClient();
		int cnt = 0;
		while (true) {
			try {
				client.doAdd(new File(filePath), true, false, true, false);
				return;
			}
			catch ( SVNException e ) {
				int ret = handleError("add", e, ++cnt);
				switch (ret) {
					case ERROR_NEXT_THROW: throw e;
					case ERROR_NEXT_BREAK: return;
					case ERROR_NEXT_RETRY: continue;
				}
			}
		}
	}


	/*
	 * Schedules a Working Copy item for deletion.
	 */
	public boolean delete(String filePath) throws SVNException {
		SVNWCClient client = this.manager.getWCClient();
		int cnt = 0;
		while (true) {
			try {
				client.doDelete(new File(filePath), true , false);
				return true;
			}
			catch ( SVNException e ) {
				SVNErrorCode errCode = e.getErrorMessage().getErrorCode();
				// E195006
				if ( errCode == SVNErrorCode.CLIENT_MODIFIED ) {
					String errMessage = errCode.toString();
					logger.warning("[SVNService] delete --> " + errMessage);
					return false;
				}
				int ret = handleError("delete", e, ++cnt);
				switch (ret) {
					case ERROR_NEXT_THROW: throw e;
					case ERROR_NEXT_BREAK: return true;
					case ERROR_NEXT_RETRY: continue;
				}
			}
		}
	}

	/*
	 * Recursively cleans up the working copy, removing locks and resuming unfinished operations.
	 */
	public void cleanup() {
		SVNWCClient client = this.manager.getWCClient();
		try {
			client.doCleanup(this.workCopy);
		}
		catch ( SVNException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * Reverts all local changes made to a Working Copy item(s) thus bringing it to a 'pristine' state.
	 */
	public void revert() {
		try {
			SVNWCClient client = this.manager.getWCClient();
			client.doRevert(this.workCopy, true);
		}
		catch ( SVNException e ) {
			e.printStackTrace();
		}
	}


	/*
	 * Collects and returns information on a single Working Copy item. 
	 */
	public SVNInfo getInfo() throws SVNException {
		SVNWCClient client = this.manager.getWCClient();
		return client.doInfo(this.workCopy, SVNRevision.BASE);
	}

	/*
	 * Committs local changes made to the Working Copy items.
	 */
	public void commit(String commitMsg) throws SVNException {
		SVNCommitClient client = this.manager.getCommitClient();
		File[] commitDirs = new File[]{this.workCopy};
		int cnt = 0;
		while (true) {
			try {
				client.doCommit(commitDirs, true, commitMsg, false, true);
				return;
			}
			catch ( SVNException e ) {
				int ret = handleError("commit", e, ++cnt);
				switch (ret) {
					case ERROR_NEXT_THROW: throw e;
					case ERROR_NEXT_BREAK: return;
					case ERROR_NEXT_RETRY: continue;
				}
			}
		}
	}

	/*
	 * Committs a creation of a new directory
	 */
	public void mkdir() {
		SVNCommitClient client = this.manager.getCommitClient();
		try {
			client.doMkDir(new SVNURL[]{this.repoURL}, "");
		}
		catch ( SVNException e ) {
			e.printStackTrace();
		}
	}

	/*
	 *  Checks out a Working Copy from a repository.
	 */
	public long checkout() throws SVNException {
		return checkout(SVNRevision.HEAD.getNumber());
	}

	/*
	 *  Checks out a Working Copy from a repository.
	 */
	public long checkout(long revision) throws SVNException {
		SVNUpdateClient client = this.manager.getUpdateClient();
		client.setIgnoreExternals(false);
		SVNRevision getRevision = SVNRevision.create(revision);
		int cnt = 0;
		while (true) {
			try {
				long retRevision = client.doCheckout(this.repoURL,
						this.workCopy, getRevision, getRevision, true);
				return retRevision;
			}
			catch ( SVNException e ) {
				int ret = handleError("checkout", e, ++cnt);
				switch (ret) {
					case ERROR_NEXT_THROW: throw e;
					case ERROR_NEXT_BREAK: return 0;
					case ERROR_NEXT_RETRY: continue;
				}
			}
		}
	}

	/*
	 * Collects status information on a single Working Copy item.
	 */
	public StatusHandler getStatus() throws SVNException {
		SVNStatusClient client = this.manager.getStatusClient();
		StatusHandler status = null;
		boolean reportAll = false;
		boolean checkouted = false;
		int cnt = 0;
		while (true) {
			try {
				StatusHandler sh = new StatusHandler(checkouted);
				long revision = client.doStatus(this.workCopy, true, true, reportAll, false, true, sh);
				return sh;
			}
			catch ( SVNException e1 ) {
				SVNErrorCode errCode = e1.getErrorMessage().getErrorCode();
				// E155007
				if ( errCode == SVNErrorCode.WC_NOT_DIRECTORY ) {
					try {
						checkout();
						reportAll = checkouted = true;
					}
					catch ( SVNException e2 ) {
						throw e2;
					}
				}
				else {
					int ret = handleError("delete", e1, ++cnt);
					switch (ret) {
						case ERROR_NEXT_THROW: throw e1;
						case ERROR_NEXT_BREAK: return null;
						case ERROR_NEXT_RETRY: continue;
					}
				}
			}
		}
	}

	/*
	 * Error handling
	 */
	private int handleError(String methodName, SVNException e, int cnt) {
		if ( SVNServiceManager.isTerminated ) {
			return ERROR_NEXT_BREAK;
		}

		int ret = ERROR_NEXT_RETRY;
		SVNErrorCode errCode = e.getErrorMessage().getErrorCode();
		String errMessage = errCode.toString();
		logger.warning("[SVNService] "+ methodName + " --> " + errMessage);
		int maxRetry = 5;
		if ( methodName.equals("add") || methodName.equals("delete") ) {
			maxRetry = 1;
		}

		// E155004, E155010
		if ( errCode == SVNErrorCode.WC_LOCKED || errCode == SVNErrorCode.WC_PATH_NOT_FOUND ) {
			logger.warning("[SVNService] WC:" + workCopyPath);
			try { Thread.sleep(2000); }
			catch ( java.lang.InterruptedException ex ){}

			if ( cnt <= maxRetry ) {
				if ( cnt == maxRetry ) {
					cleanup();
				}
				logger.info("[SVNService] retry " + String.valueOf(cnt));
				ret = ERROR_NEXT_RETRY;
			}
			else {
				ret = ERROR_NEXT_BREAK;
			}
		}
		else if ( cnt == 1 ) {
			// E155000
			if ( errCode == SVNErrorCode.WC_OBSTRUCTED_UPDATE ) {
				cleanDirectory();
			}
			// E155015
			else if ( errCode == SVNErrorCode.WC_FOUND_CONFLICT ) {
				revert();
			}
			// E155032
			else if ( errCode == SVNErrorCode.WC_DB_ERROR ) {
				cleanDirectory();
			}
			// E170000
			else if ( errCode == SVNErrorCode.RA_ILLEGAL_URL ) {
				logger.info("[SVNService] URL:" + this.repoURL);
				mkdir();
			}
			// E170001, E175002, E200005
			else if ( errCode == SVNErrorCode.RA_NOT_AUTHORIZED 
				   || errCode == SVNErrorCode.RA_DAV_REQUEST_FAILED
				   || errCode == SVNErrorCode.UNVERSIONED_RESOURCE  ) {
				ret = ERROR_NEXT_BREAK;
			}
			// E175005
			else if ( errCode == SVNErrorCode.RA_DAV_ALREADY_EXISTS ) {
				revert();
			}
			// E200030
			else if ( errCode == SVNErrorCode.SQLITE_ERROR ) {
				cleanup();
			}
		}
		else {
			ret = ERROR_NEXT_THROW;
		}
		return ret;
	}

	/*
	 * clean directory
	 */
	private void cleanDirectory() {
		logger.info("[SVNService] cleanDirectory");
		try {
			FileUtils.cleanDirectory(this.workCopy);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}
