package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mocks.HttpRequestInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ProcessorWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier

class HttpClosureCurrier implements ClosureCurrier<ProcessorWrapper> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        closure.parameterTypes.size() == 1 && shouldCurry(closure)
    }

    @Override
    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         ProcessorWrapper messageProcessor) {
        assert false : ' http requester class??'
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
        closure.parameterTypes.last() == HttpRequestInfo
    }

    private static Map getQueryParams(EventWrapper muleEvent,
                                      Object httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getQueryParams(muleEvent))
    }

    private static Map getHeaders(EventWrapper muleEvent,
                                  Object httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getHeaders(muleEvent))
    }

    private static String getFullPath(EventWrapper muleEvent,
                                      Object httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        requestBuilder.replaceUriParams(httpRequester.path, muleEvent)
    }
}
