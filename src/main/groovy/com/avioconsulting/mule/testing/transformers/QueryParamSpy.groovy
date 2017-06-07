package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.MuleMessage
import org.mule.module.http.internal.ParameterMap
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.module.http.internal.request.HttpRequesterRequestBuilder
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.SpyProcess

class QueryParamSpy implements SpyProcess, MuleMessageTransformer {
    private final String connectorName
    private final Closure closure
    private final String dummyReply
    private results = null
    private final OutputTransformer transformer

    QueryParamSpy(String connectorName,
                  Closure closure,
                  String dummyReply,
                  OutputTransformer transformer) {
        this.transformer = transformer
        this.dummyReply = dummyReply
        this.closure = closure
        this.connectorName = connectorName
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        def ourRequester = getHttpRequester(incomingEvent)
        def requestBuilder = ourRequester.requestBuilder
        ParameterMap parameters = requestBuilder.getQueryParams(incomingEvent)
        def fullPath = requestBuilder.replaceUriParams(ourRequester.path, incomingEvent)
        results = closure(parameters, fullPath)
    }

    private DefaultHttpRequester getHttpRequester(MuleEvent incomingEvent) {
        // TODO: Use connector name and the annotations on each processor to find which one is us
        incomingEvent.flowConstruct.messageProcessors[2]
    }

    MuleMessage transform(MuleMessage muleMessage) {
        this.transformer.transformOutput(results)
    }
}
