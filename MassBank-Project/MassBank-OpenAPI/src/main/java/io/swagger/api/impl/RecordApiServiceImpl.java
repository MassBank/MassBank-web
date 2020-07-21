package io.swagger.api.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.petitparser.context.Result;

import io.swagger.api.NotFoundException;
import io.swagger.api.RecordApiService;
import massbank.Record;
import massbank.RecordSearchParser;
import massbank.db.DatabaseManager;



@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-23T13:41:36.240Z[GMT]")public class RecordApiServiceImpl extends RecordApiService {
	@Override
    public Response recordGet( @NotNull String search, SecurityContext securityContext) throws NotFoundException {
        System.out.println(search);
       	RecordSearchParser parser = new RecordSearchParser();
       	Result res = parser.parse(search);

       	if (res.isFailure()) {
		System.out.println(res.getMessage());
		int position = res.getPosition();
		String[] tokens = search.split("\\n");

		int line = 0, col = 0, offset = 0;
		for (String token : tokens) {
			offset = offset + token.length() + 1;
			if (position < offset) {
				col = position - (offset - (token.length() + 1));
				System.out.println(tokens[line]);
				StringBuilder error_at = new StringBuilder(StringUtils.repeat(" ", col));
				error_at.append('^');
				System.out.println(error_at);
				break;
			}
			line++;
		}
	}
       	else {
       	    System.out.println("SUCCESS");
       	}

//		if (res.isFailure()) {
//        	return Response.status(Status.BAD_REQUEST).entity(Map.of("code", "400", "message", "Can not parse \"" + search + "\". Check syntax!")).build();
//        }


        //System.out.println(rootNode.toString());

        ArrayList<String> result = new ArrayList<>();
        result.add("AAA00001");
        result.add("AAA00002");
        result.add("AAA00003");
        return Response.ok().entity(result).build();
    }

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
    		return Response.status(Status.NOT_FOUND).entity("Record \""+id+"\" not found").build();
    	}

    	String recordstring = record.toString();
        return Response.ok().entity(recordstring).build();
    }

}



