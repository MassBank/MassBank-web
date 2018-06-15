package massbank.web;

import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

public interface SearchFunction<E> {
	
	public void getParameters(HttpServletRequest request);
	
	public ArrayList<E> search(Connection connection);

}
