package org.openapitools.api.factories;

import org.openapitools.api.RecordApiService;
import org.openapitools.api.impl.RecordApiServiceImpl;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class RecordApiServiceFactory {
    private static final RecordApiService service = new RecordApiServiceImpl();

    public static RecordApiService getRecordApi() {
        return service;
    }
}
