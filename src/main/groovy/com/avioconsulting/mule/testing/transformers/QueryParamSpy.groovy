package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.ProcessorLocator
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.MuleMessage
import org.mule.module.http.internal.ParameterMap
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.SpyProcess

class QueryParamSpy implements SpyProcess, MuleMessageTransformer {
    private final Closure closure
    private final String dummyReply
    private results = null
    private final OutputTransformer transformer
    private final ProcessorLocator processorLocator

    QueryParamSpy(ProcessorLocator processorLocator,
                  Closure closure,
                  String dummyReply,
                  OutputTransformer transformer) {
        this.processorLocator = processorLocator
        this.transformer = transformer
        this.dummyReply = dummyReply
        this.closure = closure
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        def ourRequester = processorLocator.getProcessor(incomingEvent) as DefaultHttpRequester
        def requestBuilder = ourRequester.requestBuilder
        ParameterMap parameters = requestBuilder.getQueryParams(incomingEvent)
        def fullPath = requestBuilder.replaceUriParams(ourRequester.path, incomingEvent)
        results = closure(parameters, fullPath)
    }

    MuleMessage transform(MuleMessage muleMessage) {
        this.transformer.transformOutput(results)
    }
}
