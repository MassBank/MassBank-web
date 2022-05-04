package massbank.openapi.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import massbank.Record;
import massbank.db.DatabaseManager;

import java.util.List;

/**
 * A delegate to be called by the {@link RecordApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Service
public class RecordApiDelegateImpl implements RecordApiDelegate {
    /**
     * DELETE /record/{recordId} : delete a record
     * deletes the record with the specified id from the massBank database
     *
     * @param recordId The record id (required)
     * @return the record was successfully deleted (status code 204)
     *         or Access token is missing or invalid (status code 401)
     *         or Access token does not allow the operation (status code 403)
     *         or The record with the recordId was not found (status code 404)
     *         or There was in internal error during database access (status code 500)
     * @see RecordApi#deleteRecord
     */
    public ResponseEntity<Void> deleteRecord(String recordId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

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
    	Record record = null;
        // load record from database
        DatabaseManager dbMan;
        try {
                dbMan = new DatabaseManager("MassBank");
                record = dbMan.getAccessionData(recordId);
                dbMan.closeConnection();
        } catch (SQLException | ConfigurationException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
        }
        if (record == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Record \"" + id + "\" not found").build();
        }

        String recordstring = record.toString();
        return ResponseEntity.ok(recordstring);
    }

    /**
     * GET /record : Returns all records matching the search
     * This resource reports all record ids which fulfill the criteria in the search parameters, or all records if the query parameters are missing or empty
     *
     * @param name Search records with the specified string in RECORD_TITLE or CH$NAME. (optional)
     * @param formula Search records with the specified formula CH$FORMULA. (optional)
     * @return successful operation (status code 200)
     *         or The record or query was invalid (status code 400)
     *         or There was in internal error during database access (status code 500)
     * @see RecordApi#getRecordsIds
     */
    public ResponseEntity<List<String>> getRecordsIds(String name,
        String formula) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "\"PR030008\"";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /record : Uploads a record to the database
     * Uploads a new record to the massBank database if the ACCESSION in the record is unique
     *
     * @param body  (required)
     * @return Created a new record (status code 201)
     *         or Access token is missing or invalid (status code 401)
     *         or Access token does not allow the operation (status code 403)
     *         or Could not insert dataset, because it already exists (status code 409)
     *         or The record or query was invalid (status code 400)
     *         or There was in internal error during database access (status code 500)
     * @see RecordApi#postRecord
     */
    public ResponseEntity<Void> postRecord(String body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * PUT /record/{recordId} : replace a record
     * replaces the record in with the specified id by a new record, which must have the same ACCESSION
     *
     * @param recordId The record id (required)
     * @param body  (optional)
     * @return the record was successfully replaced (status code 204)
     *         or The record or query was invalid (status code 400)
     *         or Access token is missing or invalid (status code 401)
     *         or Access token does not allow the operation (status code 403)
     *         or The record with the recordId was not found (status code 404)
     *         or There was in internal error during database access (status code 500)
     * @see RecordApi#replaceRecord
     */
    public ResponseEntity<Void> replaceRecord(String recordId,
        String body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

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


