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
 * package massbank;
 * 
 ******************************************************************************/
package massbank;

import javax.servlet.http.HttpServlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import massbank.web.SearchExecution;
import massbank.web.recordindex.RecordIndexCount;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

/**
 * 
 * This servlet generates dynamic content for the RecordIndex.
 * 
 * @author rmeier
 * @version 30-04-2019
 *
 */
@WebServlet("/RecordIndex")
public class RecordIndex extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(RecordIndex.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Preprocess request: load list of mass spectrometry information in JSP.
		try {
			RecordIndexCountResult result = new SearchExecution(request).exec(new RecordIndexCount());

			request.setAttribute("sites", result.mapSiteToRecordCount);
			request.setAttribute("instruments", result.mapInstrumentToRecordCount);
			request.setAttribute("mstypes", result.mapMsTypeToRecordCount);
			request.setAttribute("ionmodes", result.mapIonModeToRecordCount);
			request.setAttribute("symbols", result.mapSymbolToCount);
			request.setAttribute("spectra", result.spectraCount);
			request.setAttribute("compounds", result.compoundCount);

			request.getRequestDispatcher("/RecordIndex.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Error preparing record index", e);
		}
	}
}
