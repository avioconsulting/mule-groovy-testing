package com.avioconsulting.mule.testing.spies


import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.module.http.internal.request.DefaultHttpRequester
import com.avioconsulting.mule.testing.mulereplacements.SpyProcess

// spies are the only way to get access to the underlying event on mocked connectors
class HttpConnectorSpy implements SpyProcess {
    private final MuleContext muleContext
    private MuleEvent muleEvent
    private final List<IReceiveHttpOptions> optionReceivers
    private final List<IReceiveMuleEvents> muleEventReceivers

    HttpConnectorSpy(MuleContext muleContext,
                     List<IReceiveHttpOptions> optionReceivers,
                     List<IReceiveMuleEvents> muleEventReceivers) {
        this.muleEventReceivers = muleEventReceivers
        this.optionReceivers = optionReceivers
        this.muleContext = muleContext
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        muleEvent = incomingEvent
        def httpRequester = processorLocator.getProcessor(incomingEvent) as DefaultHttpRequester
        optionReceivers.each { receiver ->
            receiver.receive(getQueryParams(httpRequester),
                             getHeaders(httpRequester),
                             getFullPath(httpRequester),
                             httpRequester.method,
                             httpRequester.responseValidator)
        }
        muleEventReceivers.each { r ->
            r.receive(incomingEvent)
        }
    }

    private Map getQueryParams(DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getQueryParams(muleEvent))
    }

    private Map getHeaders(DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getHeaders(muleEvent))
    }

    private String getFullPath(DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        requestBuilder.replaceUriParams(httpRequester.path, muleEvent)
    }
}
