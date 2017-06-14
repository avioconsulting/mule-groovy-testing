package com.avioconsulting.mule.testing.spies

import com.avioconsulting.mule.testing.ProcessorLocator
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.munit.common.mocking.SpyProcess

class HttpConnectorSpy implements SpyProcess {
    private final ProcessorLocator processorLocator
    private final MuleContext muleContext
    private MuleEvent muleEvent
    private final List<IReceiveHttpOptions> optionReceivers

    HttpConnectorSpy(ProcessorLocator processorLocator,
                     MuleContext muleContext,
                     List<IReceiveHttpOptions> optionReceivers) {
        this.optionReceivers = optionReceivers
        this.muleContext = muleContext
        this.processorLocator = processorLocator
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        muleEvent = incomingEvent
        def httpRequester = processorLocator.getProcessor(incomingEvent) as DefaultHttpRequester
        optionReceivers.each { receiver ->
            receiver.receive(getQueryParams(httpRequester),
                             getFullPath(httpRequester),
                             httpRequester.method,
                             httpRequester.responseValidator)
        }
    }

    private Map getQueryParams(DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getQueryParams(muleEvent))
    }

    private String getFullPath(DefaultHttpRequester httpRequester) {
        def requestBuilder = httpRequester.requestBuilder
        requestBuilder.replaceUriParams(httpRequester.path, muleEvent)
    }
}
