package org.openapitools.api.impl;

import org.openapitools.api.*;
import org.openapitools.model.*;

import massbank.Record;
import massbank.db.DatabaseManager;

import java.io.File;

import java.util.List;
import org.openapitools.api.NotFoundException;

import java.io.InputStream;
import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class RecordApiServiceImpl extends RecordApiService {
    @Override
    public Response recordGet(String name, String formula, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response recordIdGet(String id, SecurityContext securityContext) throws NotFoundException {
    	Record record = null;
		// load record from database
		DatabaseManager dbMan;
		try {
			dbMan = new DatabaseManager("MassBank");
			record = dbMan.getAccessionData(id);
			dbMan.closeConnection();
		} catch (SQLException | ConfigurationException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
		}
		if (record == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("Record \"" + id + "\" not found").build();
		}

		String recordstring = record.toString();
		return Response.ok().entity(recordstring).build();
    }
    @Override
    public Response recordUploadPost(String filename, FormDataBodyPart fileBodypart, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
