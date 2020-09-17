package io.swagger.api.factories;

import io.swagger.api.RecordApiService;
import io.swagger.api.impl.RecordApiServiceImpl;

public class RecordApiServiceFactory {
    private final static RecordApiService service = new RecordApiServiceImpl();

    public static RecordApiService getRecordApi() {
        return service;
    }
}
