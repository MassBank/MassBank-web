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
 * MSDBUpdater.java
 *
 * ver 1.0.1 2013.02.26
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.ArrayList;
import massbank.GetConfig;
import massbank.MassBankEnv;

public class MSDBUpdater extends ServiceBase {
	public static final String BACKUP_IDENTIFIER = "_backup";
	private String serverUrl = "";

	/**
	 * 
	 */
	public MSDBUpdater(int startDelay, int interval) {
		super(startDelay, interval);
	}

	/**
	 * Starting the thread.
	 */
	public void run() {
		try { sleep(this.startDelay * TIME_SEC); }
		catch (Exception e) {}

		GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		String myServerUrl = conf.getServerUrl();
		String[] urls = conf.getSiteUrl();
		String[] names = conf.getSiteName();
		String[] dbNames = conf.getDbName();
		String[] dbNames2 = conf.getSecondaryDBName();
		this.serverUrl = urls[GetConfig.MYSVR_INFO_NUM];
		ArrayList<String> targetNameList = new ArrayList<String>();
		ArrayList<String> targetDbNameList = new ArrayList<String>();
		for ( int i = 1; i < urls.length; i++ ) {
			if ( !urls[i].equals(myServerUrl) && !dbNames2[i].equals("") ) {
				targetNameList.add(names[i]);
				targetDbNameList.add(dbNames2[i]);
			}
			else if ( urls[i].equals(myServerUrl) && dbNames[i].indexOf(BACKUP_IDENTIFIER) != -1 ) {
				targetNameList.add(names[i]);
				targetDbNameList.add(dbNames[i]);
			}
		}
		String[] targetNames = targetNameList.toArray(new String[]{});
		String[] targetDbNames = targetDbNameList.toArray(new String[]{});
		String dataRootPath = MassBankEnv.get(MassBankEnv.KEY_DATAROOT_PATH);

		if ( targetNames.length  == 0 ) {
			return;
		}
		do {
			for ( int i = 0; i < targetNames.length; i++ ) {
				String repoDirName = targetNames[i];
				String dbName = targetDbNames[i];
				logger.info("[SVNService] MSDBUpdater ** " + repoDirName + " -> DB:" + dbName + " **");
				boolean isError = false;
				SVNOperation ope = null;
				try {
					ope = new SVNOperation(repoDirName, SVNOperation.WC_BACKUP_DOWNLOAD);
				}
				catch ( Exception e ) {
					e.printStackTrace();
					isError = true;
				}
				if ( !isError && ope.isWcModified() ) {
					long oldRevision = ope.getWcRevision();
					boolean ret1 = ope.checkout();
				}
				ope.end();

				String workCopyPath = SVNUtils.getWorkCopyPath(repoDirName, SVNOperation.WC_BACKUP_DOWNLOAD);
				boolean ret2 = updateDB(dbName, dataRootPath, workCopyPath);

				try { sleep(this.interval * TIME_SEC); }
				catch (Exception e) {}
				if ( this.isTerminated ) {
					return;
				}
			}
		}
		while(true);
	}

	/**
	 *
	 */
	private boolean updateDB(String dbName, String dataRootPath, String workCopyPath) {
		File srcDir1  = new File(workCopyPath + File.separator + "record");
		File destDir1 = new File(dataRootPath + "annotation" + File.separator + dbName);
		String[] extensions1 = new String[]{"txt"};
		File srcDir2  = new File(workCopyPath + File.separator + "molfile");
		File destDir2 = new File(dataRootPath + "molfile" + File.separator + dbName);
		String[] extensions2 = new String[]{"mol", "tsv"};
		boolean isRecUpdated = MSDBUpdateUtil.updateFiles(srcDir1, destDir1, extensions1);
		boolean isMolUpdated = MSDBUpdateUtil.updateFiles(srcDir2, destDir2, extensions2);
		boolean ret = true;
		if ( isRecUpdated || isMolUpdated ) {
			ret = MSDBUpdateUtil.restoreDatabase(dbName, workCopyPath);
			if ( ret && isMolUpdated ) {
				ret = MSDBUpdateUtil.updateSubStructData(this.serverUrl);
			}
		}
		return ret;
	}
}
