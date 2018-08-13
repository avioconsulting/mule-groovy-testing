package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import com.avioconsulting.mule.testing.spies.IReceiveMuleEvents
import org.mule.api.MuleEvent
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpMock implements MockProcess<DefaultHttpRequester> {
    private final List<IReceiveHttpOptions> optionReceivers
    private final List<IReceiveMuleEvents> muleEventReceivers
    private final MuleMessageTransformer mockTransformer
    private final EventFactory eventFactory

    HttpMock(List<IReceiveHttpOptions> optionReceivers,
             List<IReceiveMuleEvents> muleEventReceivers,
             MuleMessageTransformer mockTransformer,
             EventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.mockTransformer = mockTransformer
        this.optionReceivers = optionReceivers
        this.muleEventReceivers = muleEventReceivers
    }

    @Override
    MuleEvent process(MuleEvent muleEvent,
                      DefaultHttpRequester httpRequester) {
        def queryParams = getQueryParams(muleEvent,
                                         httpRequester)
        def headers = getHeaders(muleEvent,
                                 httpRequester)
        def fullPath = getFullPath(muleEvent,
                                   httpRequester)
        optionReceivers.each { receiver ->
            receiver.receive(queryParams,
                             headers,
                             fullPath,
                             httpRequester)
        }
        muleEventReceivers.each { r ->
            r.receive(muleEvent)
        }
        def responseMessage = this.mockTransformer.transform(muleEvent.message)
        eventFactory.getMuleEvent(responseMessage,
                                  muleEvent)
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
