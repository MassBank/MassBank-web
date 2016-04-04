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
 * FileDifference.java
 *
 * ver 1.0.0 2012.08.30
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class FileDifference {
	private List<String> addFilePathList = new ArrayList();
	private List<String> delFileList = new ArrayList();

	/**
	 * 
	 */
	public FileDifference(List<File> fileList1, List<File> fileList2) {
		File[] files1 = fileList1.toArray(new File[]{});
		File[] files2 = fileList2.toArray(new File[]{});
		for ( File f1 : files1 ) {
			String fileName1 = f1.getName();
			String path = f1.getPath();
			long time1 = f1.lastModified();
			boolean found = false;
			for ( File f2 : fileList2 ) {
				String fileName2 = f2.getName();
				if ( fileName1.equals(fileName2) ) {
					found = true;
					long time2 = f2.lastModified();
					if ( time1 != time2 ) {
						this.addFilePathList.add(path);
					}
					break;
				}
			}
			if ( !found ) {
				this.addFilePathList.add(path);
			}
		}
		for ( File f2 : files2 ) {
			String fileName2 = f2.getName();
			boolean found = false;
			for ( File f1 : files1 ) {
				String fileName1 = f1.getName();
				if ( fileName2.equals(fileName1) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				this.delFileList.add(fileName2);
			}
		}
	}

	/**
	 * 
	 */
	public String[] getAddFilePaths() {
		return (String[])addFilePathList.toArray(new String[]{});
	}

	/**
	 * 
	 */
	public String[] getDeleteFileNames() {
		return (String[])delFileList.toArray(new String[]{});
	}

}
