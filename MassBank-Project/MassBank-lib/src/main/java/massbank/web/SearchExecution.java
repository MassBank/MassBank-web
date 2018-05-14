package massbank.web;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.configuration2.ex.ConfigurationException;
import massbank.DatabaseManager;


public class SearchExecution {
	private DatabaseManager database = null;
	private HttpServletRequest request = null;
	
	
	public SearchExecution(HttpServletRequest request) throws SQLException, ConfigurationException {
		this.database = new DatabaseManager("MassBank"); 
		this.request = request;
	}
	
	public ArrayList<String> exec(SearchFunction function) {
		function.getParameters(this.request);
		ArrayList<String> result = function.search(this.database.getConnection());
		this.database.closeConnection();
		return result;
	}
}
