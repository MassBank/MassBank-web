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
 * SVNServiceManager.java
 *
 * ver 1.0.0 2012.08.30
 *
 ******************************************************************************/
package massbank.svn;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NumberUtils;

public class SVNServiceManager extends HttpServlet {
	public static String SVN_BASE_URL = "";
	public static String SVN_INFO = "";
	public static boolean isTerminated = false;
	private RegistrationCommitter committer = null;
	private MSDBUpdater updaterDB = null;
	private OpenDataUpdater updaterOpen = null;
	private final Logger logger = Logger.getLogger("global");

	/**
	 * Initialization
	 */
	public void init() throws ServletException {
		this.SVN_BASE_URL = getInitParameter("URL");
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream("svn.info");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String data = reader.readLine();
			this.SVN_INFO = Crypt.decrypt("massbank", data);
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return;
		}
		String[] serviceNames = {"RegistrationCommitter", "MSDBUpdater", "OpenDataUpdater"};
		for ( int i = 0; i < serviceNames.length; i++ ) {
			String val = getInitParameter(serviceNames[i]);
			if ( val == null || val.equals("") ) {
				continue;
			}
			String[] items = val.split(",");
			String enable = items[0].trim().toLowerCase();
			int startDelay = 0; 
			int interval = 0; 
			if ( items.length == 3 ) {
				startDelay = NumberUtils.stringToInt(items[1].trim());
				interval = NumberUtils.stringToInt(items[2].trim());
			}
			if ( enable.equals("true") ) {
				if ( serviceNames[i].equals("RegistrationCommitter") ) {
					this.committer = new RegistrationCommitter(startDelay, interval);
					this.committer.start();
				}
				if ( serviceNames[i].equals("MSDBUpdater") ) {
					this.updaterDB = new MSDBUpdater(startDelay, interval);
					this.updaterDB.start();
				}
				if ( serviceNames[i].equals("OpenDataUpdater") ) {
					this.updaterOpen = new OpenDataUpdater(startDelay, interval);
					this.updaterOpen.start();
				}
			}
		}
	}

	/**
	 * サービス終了処理を行う
	 */
	public void destroy() {
		this.isTerminated = true;
		terminate(this.committer);
		terminate(this.updaterDB);
		terminate(this.updaterOpen);
	}

	/*
	 *
	 */
	private void terminate(ServiceBase obj) {
		if ( obj == null ) {
			return;
		}
		obj.setTerminate();
		obj.interrupt();
		do {
			try { obj.join(200); }
			catch ( Exception e ) {}
		}
		while ( obj.isAlive() );

	}
}
