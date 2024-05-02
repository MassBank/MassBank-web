/*******************************************************************************
 * Copyright (C) 2017 MassBank consortium
 * 
 * This file is part of MassBank.
 * 
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 ******************************************************************************/
package massbank.repository;

import java.util.stream.Stream;

import massbank.Record;

/**
 * Interface for a repository with some kind of data.
 * @author rmeier
 * @version 26-10-2023
 */
public interface RepositoryInterface {
	
	/**
	 * Return all Records of that repo in a stream.
	 */
	public Stream<Record> getRecords();
	/**
	 * Return a version String for the repo.
	 */
	public String getRepoVersion();
	/**
	 * Return the number of recordfiles in the repo.
	 */
	public int getSize();
}
