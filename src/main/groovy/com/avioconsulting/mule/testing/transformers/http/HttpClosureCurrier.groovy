package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mocks.HttpRequestInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import org.mule.api.MuleEvent
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpClosureCurrier implements ClosureCurrier<DefaultHttpRequester> {
    @Override
    Closure curryClosure(Closure closure,
                         MuleEvent muleEvent,
                         DefaultHttpRequester messageProcessor) {
        if (shouldCurry(closure)) {
            def requestInfo = new HttpRequestInfo(messageProcessor.method,
                                                  getFullPath(muleEvent,
                                                              messageProcessor),
                                                  getQueryParams(muleEvent,
                                                                 messageProcessor),
                                                  getHeaders(muleEvent,
                                                             messageProcessor))
            return closure.rcurry(requestInfo)
        }
        return closure
    }

    private boolean shouldCurry(Closure closure) {
        def types = closure.parameterTypes
        types.any() && types.last() == HttpRequestInfo
    }

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
