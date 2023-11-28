/*******************************************************************************
 * Copyright (C) 2021 MassBank consortium
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redfin.sitemapgenerator.SitemapIndexGenerator;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;

import massbank.db.DatabaseManager;
import massbank.db.DatabaseTimestamp;
import massbank.repository.RepositoryInterface;

/**
 * 
 * This servlet generates dynamic sitemap files. It serves a index
 * at /sitemapindex.xml and the actual sitemaps at /sitemap/sitemap*.xml
 * 
 * @author rmeier
 * @version 18-05-2020
 *
 */
@WebServlet({"/sitemapindex.xml", "/sitemap/*"})
public class SiteMapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(SiteMapServlet.class);
	// generated sitemaps represent state of the database at 'timestamp'
	private DatabaseTimestamp timestamp;
	
	public void init() throws ServletException {
		logger.trace("ServletContext.TEMPDIR: " + getServletContext().getAttribute(ServletContext.TEMPDIR));
		File tmpdir = (File)getServletContext().getAttribute(ServletContext.TEMPDIR);
		// remove old index
		for (File file : tmpdir.listFiles()) {
			if (file.getName().matches("sitemap.*\\.xml$")) {
				logger.trace("Remove old sitemap: " + file.toString());
				file.delete();
			}
		}
				
		Properties properties = new Properties();
		try {
			InputStream inStream = RepositoryInterface.class.getResourceAsStream("/project.properties");
			if (inStream == null) {
				logger.error("Error finding project.properties. File will be ignored.");
				properties.setProperty("version", "N/A");
				properties.setProperty("timestamp", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
			} else {
				properties.load(inStream);
			}
		} catch (IOException e) {
			logger.error("Error reading project.properties. File will be ignored.\n", e);
			properties.setProperty("version", "N/A");
			properties.setProperty("timestamp", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
		}
		
		Instant softwareTimestamp = ZonedDateTime.parse(properties.getProperty("timestamp"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
		timestamp = new DatabaseTimestamp();
		
		try {
			// create sitemap generator
			String sitemapbaseurl = Config.get().SitemapBaseURL();
			if (!sitemapbaseurl.endsWith("/")) sitemapbaseurl = sitemapbaseurl + "/";
			WebSitemapGenerator wsg = new WebSitemapGenerator(sitemapbaseurl, tmpdir);

			// add static content
			wsg.addUrl(new WebSitemapUrl.Options(sitemapbaseurl + "Index").lastMod(Date.from(softwareTimestamp)).build());
			wsg.addUrl(new WebSitemapUrl.Options(sitemapbaseurl + "Search").lastMod(Date.from(softwareTimestamp)).build());
			wsg.addUrl(new WebSitemapUrl.Options(sitemapbaseurl + "RecordIndex").lastMod(Date.from(softwareTimestamp)).build());

			// add dynamic content
			try (Connection con = DatabaseManager.getConnection()) {
				try (PreparedStatement stmnt = con.prepareStatement("SELECT ACCESSION,RECORD_TIMESTAMP FROM RECORD")) {
					try (ResultSet res = stmnt.executeQuery()) {
						while (res.next()) {
							String accession = res.getString(1);
							Date recordTimestamp = res.getTimestamp(2);
							recordTimestamp = recordTimestamp.before(Date.from(softwareTimestamp)) ? Date.from(softwareTimestamp) : recordTimestamp;
							wsg.addUrl(new WebSitemapUrl.Options(sitemapbaseurl + "RecordDisplay?id=" + accession).lastMod(recordTimestamp).build());
						}
					}
				}
			}
			
			// write new sitemaps
			List<File> sitemaps=wsg.write();
			logger.trace("File(s) written:\n" + sitemaps);
			
			// write sitemap index
			SitemapIndexGenerator sig = new SitemapIndexGenerator(sitemapbaseurl, new File(tmpdir, "sitemapindex.xml"));
			for (File sitemap : sitemaps) {
				sig.addUrl(sitemapbaseurl + "sitemap/" + sitemap.getName());
			}
			sig.write();
		} catch (MalformedURLException | SQLException e) {
			logger.error(e.getMessage());
		}
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.trace("getPathInfo: " + request.getPathInfo());
		logger.trace("getServletPath: " + request.getServletPath());
		logger.trace("getRequestURI: " + request.getRequestURI() );
		
		if (timestamp.isOutdated()) init();
				
		File sitemap; 
		if ((request.getPathInfo() == null) && "/sitemapindex.xml".equals(request.getServletPath())) {
			sitemap=new File((File)getServletContext().getAttribute(ServletContext.TEMPDIR), request.getServletPath());
			if (!sitemap.exists()) {
				// send 404 if index is missing
	            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
	            return;
	        }
		} else if (request.getServletPath().equals("/sitemap")) {
			sitemap=new File((File)getServletContext().getAttribute(ServletContext.TEMPDIR), request.getPathInfo());
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
