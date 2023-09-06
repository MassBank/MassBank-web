package massbank.web;

import jakarta.servlet.http.HttpServletRequest;

import massbank.db.DatabaseManager;

public interface SearchFunction<E> {
	
	public void getParameters(HttpServletRequest request);
	
	public E search(DatabaseManager databaseManager);

}
