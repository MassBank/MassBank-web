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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-17T13:53:26.722Z[GMT]")public abstract class RecordApiService {
    public abstract Response recordIdGet(String id,SecurityContext securityContext) throws NotFoundException;
}
