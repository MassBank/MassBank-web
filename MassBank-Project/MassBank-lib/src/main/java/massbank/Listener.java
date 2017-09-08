package massbank;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/*
 * Implementing a class which holds the context path and store
 * it in the application scope using a Listener. This is usefull
 * for access to resources by absolute path using the global
 * attribute "ctx". 
 * 
 */


@WebListener
public class Listener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sc = event.getServletContext();
        sc.setAttribute("ctx", "http://localhost:8080"+sc.getContextPath() );
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {}

}
