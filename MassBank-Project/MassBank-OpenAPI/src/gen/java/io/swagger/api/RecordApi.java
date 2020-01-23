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


@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-23T13:41:36.240Z[GMT]")public class RecordApi  {
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
    
    
    @Produces({ "application/json" })
    @Operation(summary = "Returns all records matching the search", description = "This resource reports all record ids which fulfill the criteria in the search string. The search strings support the syntax described at [jirutka/rsql-parser](https://github.com/jirutka/rsql-parser).   The following selectors are supported: Selector | Operators | Argument Type,<br>Examples and Comments | Description ---|---|---|--- name | == | String with simple wildcard **\\***<br>Example:<br> 1. **name==nicotin\\*** matches **nicotine** and **nicotinamide** but not **1-methylnicotinamide**<br> 2. **name==\\*nicotin\\*** matches also **1-methylnicotinamide** | matches Strings in the `CH$NAME` field ---|---|---|---| ", tags={ "record" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        
        @ApiResponse(responseCode = "400", description = "syntax error"),
        
        @ApiResponse(responseCode = "500", description = "database error") })
    public Response recordGet(@Parameter(in = ParameterIn.QUERY, description = "the search string",required=true) @QueryParam("search") String search
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.recordGet(search,securityContext);
    }
    @GET
    @Path("/{id}")
    
    @Produces({ "text/plain" })
    @Operation(summary = "Returns the content of one record", description = "", tags={ "record" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class))),
        
        @ApiResponse(responseCode = "404", description = "record not found"),
        
        @ApiResponse(responseCode = "500", description = "database error") })
    public Response recordIdGet(@Parameter(in = ParameterIn.PATH, description = "The record id",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.recordIdGet(id,securityContext);
    }
}
