package io.swagger.api;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "Swagger Server", 
        version = "0.0.1", 
        description = "This is the MassBank REST API",
        termsOfService = "https://massbank.eu",
        contact = @Contact(email = "denbi-mash@ipb-halle.de"),
        license = @License(
            name = "GPL",
            url = "http://www.gnu.org/licenses/gpl-3.0"
        )
    )
)
public class Bootstrap {
}
