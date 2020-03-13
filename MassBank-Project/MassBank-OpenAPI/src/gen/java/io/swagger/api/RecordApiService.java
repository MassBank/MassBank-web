package io.swagger.api;

import io.swagger.api.*;


import org.glassfish.jersey.media.multipart.FormDataContentDisposition;


import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-23T13:41:36.240Z[GMT]")public abstract class RecordApiService {
    public abstract Response recordGet( @NotNull String search,SecurityContext securityContext) throws NotFoundException;
    public abstract Response recordIdGet(String id,SecurityContext securityContext) throws NotFoundException;
}
