package massbank.web;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;

public interface SearchFunction<E> {
	
	public void getParameters(HttpServletRequest request);
	
	public E search(DatabaseManager databaseManager);

}
