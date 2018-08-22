package com.avioconsulting.mule.testing.mocks


import org.mule.api.MuleEvent
import org.mule.module.http.internal.request.DefaultHttpRequester

trait HttpMock {
    private static Map getQueryParams(MuleEvent muleEvent,
                                      DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getQueryParams(muleEvent))
    }

    private static Map getHeaders(MuleEvent muleEvent,
                                  DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getHeaders(muleEvent))
    }

    private static String getFullPath(MuleEvent muleEvent,
                                      DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        requestBuilder.replaceUriParams(httpRequester.path, muleEvent)
    }
}
