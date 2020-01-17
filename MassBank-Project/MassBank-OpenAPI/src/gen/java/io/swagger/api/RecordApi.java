package io.swagger.api;


import io.swagger.api.RecordApiService;
import io.swagger.api.factories.RecordApiServiceFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;


@Path("/record")


@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-17T13:53:26.722Z[GMT]")public class RecordApi  {
   private final RecordApiService delegate;

   public RecordApi(@Context ServletConfig servletContext) {
      RecordApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("RecordApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (RecordApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = RecordApiServiceFactory.getRecordApi();
      }

      this.delegate = delegate;
   }

    @GET
    @Path("/{id}")
    
    @Produces({ "text/plain" })
    @Operation(summary = "Returns the content of one record", description = "", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class))),
        
        @ApiResponse(responseCode = "400", description = "record not found") })
    public Response recordIdGet(@Parameter(in = ParameterIn.PATH, description = "The record id",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.recordIdGet(id,securityContext);
    }
}
