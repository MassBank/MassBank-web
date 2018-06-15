package massbank.web;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;

public interface SearchFunction<E> {
	
	public void getParameters(HttpServletRequest request);
	
	public ArrayList<E> search(DatabaseManager databaseManager);

}
