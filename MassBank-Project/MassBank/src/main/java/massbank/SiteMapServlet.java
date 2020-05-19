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
package massbank;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redfin.sitemapgenerator.SitemapIndexGenerator;
import com.redfin.sitemapgenerator.WebSitemapGenerator;

/**
 * 
 * This servlet generates dynamic sitemap.xml files. It serves a index
 * at /sitemap.xml and the actual sitemaps at /sitemap/sitemap*.xml
 * 
 * @author rmeier
 * @version 18-05-2020
 *
 */
@WebServlet({"/sitemap.xml","/sitemap/*"})
public class SiteMapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(SiteMapServlet.class);
	// generated sitemaps represent state of the database at 'timestamp'
	private DatabaseTimestamp timestamp;
	
	public void init() throws ServletException {
		logger.trace("ServletContext.TEMPDIR: " + getServletContext().getAttribute(ServletContext.TEMPDIR));
		File tmpdir = (File)getServletContext().getAttribute(ServletContext.TEMPDIR);
		// remove old index
		try {
			FileUtils.deleteQuietly(new File(tmpdir, "sitemap.xml"));
			File sitemapDir = new File(tmpdir, "sitemap");
			if (sitemapDir.exists()) FileUtils.forceDelete(new File(tmpdir, "sitemap"));
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new ServletException("Could not clean sitemp directory.");
		}
		
		try {
			// create sitemap generator
			String sitemapbaseurl = Config.get().SitemapBaseURL();
			if (!sitemapbaseurl.endsWith("/")) sitemapbaseurl = sitemapbaseurl + "/";
			File sitemapDir = new File(tmpdir, "sitemap");
			sitemapDir.mkdir();
			WebSitemapGenerator wsg = new WebSitemapGenerator(sitemapbaseurl, sitemapDir);

			// add static content
			wsg.addUrl(sitemapbaseurl);
			wsg.addUrl(sitemapbaseurl + "Index");
			wsg.addUrl(sitemapbaseurl + "Search");
			wsg.addUrl(sitemapbaseurl + "RecordIndex");

			// add dynamic content
			DatabaseManager databaseManager= new DatabaseManager("MassBank");
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement("SELECT ACCESSION FROM RECORD");
			ResultSet res = stmnt.executeQuery();
			while (res.next()) {
				wsg.addUrl(sitemapbaseurl + "RecordDisplay?id=" + res.getString(1));
			}
			databaseManager.closeConnection();
			
			// write new sitemaps
			List<File> sitemaps=wsg.write();
			logger.trace("Files written:\n" + sitemaps);
			
			// write sitemap index
			SitemapIndexGenerator sig = new SitemapIndexGenerator(sitemapbaseurl, new File(tmpdir, "sitemap.xml"));
			for (File sitemap : sitemaps) {
				sig.addUrl(sitemapbaseurl+"sitemap/"+sitemap.getName());
			}
			sig.write();
			
			// get the current database timestamp			
			timestamp=new DatabaseTimestamp();		
		} catch (ConfigurationException | MalformedURLException | SQLException e) {
			logger.error(e.getMessage());
		}
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.trace("getPathInfo: " + request.getPathInfo());
		logger.trace("getServletPath: " + request.getServletPath());
		logger.trace("getRequestURI: " + request.getRequestURI() );
		
		try {
			if (timestamp.isOutdated()) init();
		} catch (SQLException | ConfigurationException e) {
			logger.error(e.getMessage());
		}
		
		File sitemap; 
		if ((request.getPathInfo() == null) && "/sitemap.xml".equals(request.getServletPath())) {
			sitemap=new File((File)getServletContext().getAttribute(ServletContext.TEMPDIR), "sitemap.xml");
			if (!sitemap.exists()) {
				// send 404 if index is missing
	            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
	            return;
	        }
		} else if (request.getServletPath().equals("/sitemap")) {
			
			sitemap=new File(((File)getServletContext().getAttribute(ServletContext.TEMPDIR)).toString() + File.separator + "sitemap" + File.separator + request.getPathInfo());
			
			if (!sitemap.exists()) {
				// send 404 if index is missing
	            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
	            return;
	        }
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
		}
		response.reset();
        response.setContentType("application/xml;charset=UTF-8");
        // write content to response
        Files.copy(sitemap.toPath(), response.getOutputStream());
    }

	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
