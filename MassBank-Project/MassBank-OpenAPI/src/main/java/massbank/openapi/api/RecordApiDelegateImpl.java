package massbank.openapi.api;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import massbank.Record;
import massbank.db.DatabaseManager;

import java.sql.SQLException;
import java.util.List;

/**
 * A delegate to be called by the {@link RecordApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Service
public class RecordApiDelegateImpl implements RecordApiDelegate {
	/**
     * GET /record/{recordId} : Returns the content of one record
     * Returns the content of the record in MassBank format
     *
     * @param recordId The record id (required)
     * @return successful operation (status code 200)
     *         or The record with the recordId was not found (status code 404)
     *         or There was in internal error during database access (status code 500)
     * @see RecordApi#getRecord
     */
    public ResponseEntity<String> getRecord(String recordId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf(""))) {
                    String exampleString = "";
                    ApiUtil.setExampleResponse(request, "", exampleString);
                    break;
                }
            }
        });
        Record record = null;
		// load record from database
		DatabaseManager dbMan;
		try {
			dbMan = new DatabaseManager("MassBank");
			record = dbMan.getAccessionData(recordId);
			dbMan.closeConnection();
		} catch (SQLException | ConfigurationException e) {
			return new ResponseEntity<>("Database connection error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (record == null) {
			return new ResponseEntity<>("Record \"" + recordId + "\" not found", HttpStatus.NOT_FOUND);
		}
		String recordstring = record.toString();
		return new ResponseEntity<>(recordstring, HttpStatus.OK);
    }
	
}

/*
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
*/


