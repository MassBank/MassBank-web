package io.swagger.api.factories;

import io.swagger.api.RecordApiService;
import io.swagger.api.impl.RecordApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-17T13:53:26.722Z[GMT]")public class RecordApiServiceFactory {
    private final static RecordApiService service = new RecordApiServiceImpl();

    public static RecordApiService getRecordApi() {
        return service;
    }
}
