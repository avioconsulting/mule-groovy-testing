package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.ProcessorLocator
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.munit.common.mocking.SpyProcess

class HttpConnectorSpy implements SpyProcess {
    private final ProcessorLocator processorLocator
    private DefaultHttpRequester httpRequester
    private final MuleContext muleContext
    private MuleEvent muleEvent

    HttpConnectorSpy(ProcessorLocator processorLocator,
                     MuleContext muleContext) {
        this.muleContext = muleContext
        this.processorLocator = processorLocator
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        muleEvent = incomingEvent
        httpRequester = processorLocator.getProcessor(incomingEvent) as DefaultHttpRequester
    }

    Map getQueryParams() {
        def requestBuilder = httpRequester.requestBuilder
        // make it easier to compare
        new HashMap(requestBuilder.getQueryParams(muleEvent))
    }

    String getFullPath() {
        def requestBuilder = httpRequester.requestBuilder
        requestBuilder.replaceUriParams(httpRequester.path, muleEvent)
    }

    String getHttpVerb() {
        httpRequester.method
    }

    def validate(MuleEvent event) {
        httpRequester.responseValidator.validate(event)
    }
}
