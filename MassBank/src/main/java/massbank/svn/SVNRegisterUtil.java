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
 * SVNRegisterUtil.java
 *
 * ver 1.0.1 2013.02.26
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import massbank.GetConfig;
import massbank.MassBankEnv;


public class SVNRegisterUtil {

	/**
	 * Adding records
	 */
	public static boolean updateRecords(String targetDbName)  {
		return update(targetDbName, true);
	}

	/**
	 * Adding molfiles
	 */
	public static boolean updateMolfiles(String targetDbName)  {
		return update(targetDbName, false);
	}

	/**
	 * Update to the svn
	 */
	private static boolean update(String targetDbName, boolean isRecord) {
		String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
		GetConfig conf = new GetConfig(baseUrl);
		String[] confNames = conf.getSiteName();
		String[] confDbNames = conf.getDbName();
		boolean found = false;
		int i = 0;
		for ( i = 0; i < confDbNames.length; i++ ) {
			if ( targetDbName.equals(confDbNames[i]) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			return false;
		}

		String basePath = "";
		String[] extensions = null;
		String subDirName = "";
		if ( isRecord ) {
			basePath = MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
			extensions = new String[]{"txt"};
			subDirName = "record";
		}
		else {
			basePath = MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH);
			extensions = new String[]{"mol", "tsv"};
			subDirName = "molfile";
		}
		String repoDirName = confNames[i];
		String path1 = basePath + targetDbName;
		String path2 = SVNUtils.getWorkCopyPath(repoDirName, SVNOperation.WC_BACKUP_UPLOAD)
																	+ File.separator + subDirName;
		SVNOperation ope = null;
		try {
			ope = new SVNOperation(repoDirName, SVNOperation.WC_BACKUP_UPLOAD);
			File dir1 = new File(path1);
			File dir2 = new File(path2);
			if ( !dir2.exists() ) {
				dir2.mkdirs();
			}
			List<File> fileList1 = (List<File>)FileUtils.listFiles(dir1, extensions, false);
			List<File> fileList2 = (List<File>)FileUtils.listFiles(dir2, extensions, false);
			FileDifference diff = new FileDifference(fileList1, fileList2);
			String[] addFilePaths = diff.getAddFilePaths();
			String[] delFileNames = diff.getDeleteFileNames();
			if ( addFilePaths.length > 0 ) {
				ope.addEntry(addFilePaths, subDirName);
			}
			if ( delFileNames.length > 0 ) {
				ope.deleteEntry(delFileNames, subDirName);
			}
			ope.end();
			return true;
		}
		catch ( Exception e ) {
			e.printStackTrace();
			ope.end();
			return false;
		}
	}
}
