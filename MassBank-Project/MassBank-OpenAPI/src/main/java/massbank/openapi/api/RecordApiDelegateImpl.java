package massbank.openapi.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import massbank.Record;
import massbank.db.DatabaseManager;

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
		record = DatabaseManager.getAccessionData(recordId);
		if (record == null) {
			return new ResponseEntity<>("Record \"" + recordId + "\" not found", HttpStatus.NOT_FOUND);
		}
		String recordstring = record.toString();
		return new ResponseEntity<>(recordstring, HttpStatus.OK);
    }
	
}


