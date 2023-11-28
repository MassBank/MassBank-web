package massbank.web;

import jakarta.servlet.http.HttpServletRequest;

public interface SearchFunction<E> {
	
	public void getParameters(HttpServletRequest request);
	
	public E search();

}
