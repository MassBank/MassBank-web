package io.swagger.api.impl;

import io.swagger.api.*;



import io.swagger.api.NotFoundException;
import massbank.DatabaseManager;
import massbank.Record;

import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-17T13:53:26.722Z[GMT]")public class RecordApiServiceImpl extends RecordApiService {
    @Override
    public Response recordIdGet(String id, SecurityContext securityContext) throws NotFoundException {
    	Record record = null;
    	// load record from database
    	DatabaseManager dbMan;
		try {
			dbMan = new DatabaseManager("MassBank");
			record	= dbMan.getAccessionData(id);
	    	dbMan.closeConnection();
		} catch (SQLException | ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();		
		}
    	if(record == null) {
    		return Response.status(Status.NOT_FOUND).entity("Record "+id+" not found").build();
    	}
    	
    	String recordstring = record.toString();
        return Response.ok().entity(recordstring).build();
    }
}
