package org.openapitools.api;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.*;

import io.swagger.models.auth.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Bootstrap extends HttpServlet {
  @Override
  public void init(ServletConfig config) throws ServletException {
    Info info = new Info()
      .title("OpenAPI Server")
      .description("This is the MassBank REST API")
      .termsOfService("https://massbank.eu")
      .contact(new Contact()
        .email("denbi-mash@ipb-halle.de"))
      .license(new License()
        .name("GPL")
        .url("http://www.gnu.org/licenses/gpl-3.0"));

    ServletContext context = config.getServletContext();
    Swagger swagger = new Swagger().info(info);

    new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
  }
}
