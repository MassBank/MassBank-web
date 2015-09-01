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
 * OpenDataUpdater.java
 *
 * ver 1.0.0 2012.09.07
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.text.SimpleDateFormat;
import org.apache.commons.io.FileUtils;
import massbank.GetConfig;
import massbank.MassBankEnv;

public class OpenDataUpdater extends ServiceBase {
	private String srcRecordPath = "";
	private String destRecordPath = "";

	/**
	 * 
	 */
	public  OpenDataUpdater(int startDelay, int interval) {
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
		ArrayList<String> targetNameList = new ArrayList();
		ArrayList<String> targetDbNameList = new ArrayList();
		for ( int i = 0; i < names.length; i++ ) {
			if ( !urls[i].equals(myServerUrl) && !dbNames2[i].equals("") ) {
				targetNameList.add(names[i]);
				targetDbNameList.add(dbNames2[i]);
			}
			else {
				targetNameList.add(names[i]);
				targetDbNameList.add(dbNames[i]);
			}
		}
		String[] targetNames = targetNameList.toArray(new String[]{});
		String[] targetDbNames = targetDbNameList.toArray(new String[]{});
		String dataRootPath = MassBankEnv.get(MassBankEnv.KEY_DATAROOT_PATH);

		if ( targetNames.length == 0 ) {
			return;
		}
		do {
			for ( int i = 0; i < targetNames.length; i++ ) {
				String repoDirName = targetNames[i];
				String dbName = targetDbNames[i];
				logger.info("[SVNService] OpenDataUpdater ** " + repoDirName + " **");
				String workCopyPath = SVNUtils.getWorkCopyPath(repoDirName, SVNOperation.WC_OPEN_DATA);
				this.srcRecordPath= dataRootPath + "annotation" + File.separator + dbName;
				this.destRecordPath = workCopyPath;
				File file = new File(srcRecordPath);
				if ( file.exists() ) {
					String[] fileNames = file.list();
					if ( fileNames.length > 0 ) {
						for ( int j = 0; j < fileNames.length; j++ ) {
							fileNames[j] = srcRecordPath + File.separator + fileNames[j];
						}
						String[] filePaths = SVNUtils.getLicenseOKFileList(fileNames);
						if ( filePaths.length > 0 && !isFileUpdating() ) {
							registerData(repoDirName);
						}
					}
				}

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
	private boolean isFileUpdating() {
		File file = new File(srcRecordPath);
		String[] extensions = {"txt"};
		List<File> fileList1 = (List<File>)FileUtils.listFiles(file, extensions, false);
		if ( fileList1 == null ) {
			return false;
		}
		try { sleep(30 * TIME_SEC); }
		catch (Exception e) {}
		List<File> fileList2 = (List<File>)FileUtils.listFiles(file, extensions, false);
		if ( fileList2 == null ) {
			return false;
		}
		FileDifference diff = new FileDifference(fileList1, fileList2);
		String[] addFilePaths = diff.getAddFilePaths();
		String[] delFileNames = diff.getDeleteFileNames();
		if ( addFilePaths.length == 0 || delFileNames.length == 0 ) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 */
	private void registerData(String repoDirName) {
		SVNOperation ope = null;
		try {
			ope = new SVNOperation(repoDirName, SVNOperation.WC_OPEN_DATA);
		}
		catch ( Exception e ) {
			e.printStackTrace();
			ope.end();
			return;
		}
		File file = new File(this.destRecordPath);
		if ( !file.exists() ) {
			file.mkdirs();
		}
		String[] extensions = {"txt"};
		List<File> srcFileList = (List<File>)FileUtils.listFiles(new File(this.srcRecordPath), extensions, false);
		List<File> destFileList = (List<File>)FileUtils.listFiles(new File(this.destRecordPath), extensions, false);
		FileDifference diff = new FileDifference(srcFileList, destFileList);
		String[] addFilePaths = diff.getAddFilePaths();
		String[] delFileNames = diff.getDeleteFileNames();
		if ( addFilePaths.length > 0 || delFileNames.length > 0 ) {
			String[] addFilePaths2 = SVNUtils.getLicenseOKFileList(addFilePaths);
			if ( addFilePaths2.length > 0 || delFileNames.length > 0 ) {
				try {
					if ( addFilePaths2.length > 0 ) {
						ope.addEntry(addFilePaths2, "");
					}
					if ( delFileNames.length > 0 ) {
						ope.deleteEntry(delFileNames, "");
					}
					ope.commit();
				}
				catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		}
		ope.end();
	}

}
