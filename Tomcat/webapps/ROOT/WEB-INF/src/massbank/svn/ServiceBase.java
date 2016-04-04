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
 * ServiceBase.java
 *
 * ver 1.0.0 2012.08.30
 *
 ******************************************************************************/
package massbank.svn;

import java.util.logging.Logger;

public class ServiceBase extends Thread {
	public static final int TIME_SEC = 1000;
	protected boolean isTerminated = false;
	protected int startDelay = 30;
	protected int interval = 60;
	protected final Logger logger = Logger.getLogger("global");


	/**
	 * 
	 */
	public ServiceBase(int startDelay, int interval) {
		if ( startDelay >= 10 ) { this.startDelay = startDelay; }
		if ( interval >= 10 ) { this.interval = interval; }
	}

	/**
	 * 
	 */
	public void run() {
	}

	/**
	 * 終了フラグをセットする
	 */
	public void setTerminate() {
		this.isTerminated = true;
	}
}
