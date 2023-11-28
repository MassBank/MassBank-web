package massbank.web;

import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.ex.ConfigurationException;


public class SearchExecution {
	private HttpServletRequest request = null;
	
	public SearchExecution(HttpServletRequest request) throws SQLException, ConfigurationException {
		this.request = request;
	}
	
	public <E> E exec(SearchFunction<E> function) {
		function.getParameters(this.request);
		E result = function.search();
		return result;
	}
}
