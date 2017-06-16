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
 * RegistrationCommitter.java
 *
 * ver 1.0.0 2012.09.05
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import massbank.GetConfig;
import massbank.MassBankEnv;
import massbank.admin.FileUtil;

public class RegistrationCommitter extends ServiceBase {
	public static boolean isActive = false;

	/**
	 * Constructor
	 */
	public  RegistrationCommitter(int startDelay, int interval) {
		super(startDelay, interval);
	}

	/**
	 * Starting the thread.
	 */
	public void run() {
		try { sleep(this.startDelay * TIME_SEC); }
		catch (Exception e) {}
		this.isActive = true;

		GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		String myServerUrl = conf.getServerUrl();
		String[] urls = conf.getSiteUrl();
		String[] names = conf.getSiteName();
		String[] dbNames = conf.getDbName();
		ArrayList<String> targetNameList = new ArrayList();
		ArrayList<String> targetDbNameList = new ArrayList();
		for ( int i = 0; i < urls.length; i++ ) {
			if ( i == 0 || urls[i].equals(myServerUrl) ) {
				targetNameList.add(names[i]);
				targetDbNameList.add(dbNames[i]);
			}
		}
		String[] targetNames = targetNameList.toArray(new String[]{});
		String[] targetDbNames = targetDbNameList.toArray(new String[]{});

		String tempPath = System.getProperty("java.io.tmpdir");
		String dumpFilePath = tempPath + File.separator + "massbank_backup.sql";
		String[] tableNames = { "SPECTRUM", "RECORD", "PEAK", "CH_NAME", "CH_LINK",
								"INSTRUMENT", "TREE", "MOLFILE" };

		if ( targetNames.length == 0 ) {
			return;
		}
		do {
			for ( int i = 0; i < targetNames.length; i++ ) {
				String repoDirName = targetNames[i];
				String dbName = targetDbNames[i];
				logger.info("[SVNService] RegistrationCommitter ** " + repoDirName + " **");
				SVNOperation ope = null;
				try {
					ope = new SVNOperation(repoDirName, SVNOperation.WC_BACKUP_UPLOAD);
					if ( ope.isWcModified() ) {
						boolean ret = FileUtil.execSqlDump("", dbName, tableNames, dumpFilePath);
						if ( ret ) {
							ope.addEntry(new String[]{dumpFilePath}, "other");
							ope.commit();
							FileUtils.deleteQuietly(new File(dumpFilePath));
						}
					}
				}
				catch ( Exception e ) {
					e.printStackTrace();
				}
				ope.end();

				try { sleep(this.interval * TIME_SEC); }
				catch (Exception e) {}
				if ( this.isTerminated ) {
					return;
				}
			}
		}
		while(true);
	}
}
