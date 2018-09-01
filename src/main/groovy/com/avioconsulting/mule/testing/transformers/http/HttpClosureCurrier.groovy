package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mocks.HttpRequestInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class HttpClosureCurrier implements ClosureCurrier<Processor> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        closure.parameterTypes.size() == 1 && shouldCurry(closure)
    }

    @Override
    Closure curryClosure(Closure closure,
                         CoreEvent muleEvent,
                         Processor messageProcessor) {
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

    private static Map getQueryParams(CoreEvent muleEvent,
                                      Object httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getQueryParams(muleEvent))
    }

    private static Map getHeaders(CoreEvent muleEvent,
                                  Object httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getHeaders(muleEvent))
    }

    private static String getFullPath(CoreEvent muleEvent,
                                      Object httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        requestBuilder.replaceUriParams(httpRequester.path, muleEvent)
    }
}
