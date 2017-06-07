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
    private results = null
    private final OutputTransformer outputTransformer
    private final ProcessorLocator processorLocator

    QueryParamSpy(ProcessorLocator processorLocator,
                  Closure closure,
                  OutputTransformer outputTransformer) {
        this.processorLocator = processorLocator
        this.outputTransformer = outputTransformer
        this.closure = closure
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        def ourRequester = processorLocator.getProcessor(incomingEvent) as DefaultHttpRequester
        def requestBuilder = ourRequester.requestBuilder
        ParameterMap parameters = requestBuilder.getQueryParams(incomingEvent)
        // make it easier to compare
        def stockMap = new HashMap(parameters)
        def fullPath = requestBuilder.replaceUriParams(ourRequester.path, incomingEvent)
        results = closure(stockMap, fullPath)
    }

    MuleMessage transform(MuleMessage muleMessage) {
        this.outputTransformer.transformOutput(results)
    }
}
