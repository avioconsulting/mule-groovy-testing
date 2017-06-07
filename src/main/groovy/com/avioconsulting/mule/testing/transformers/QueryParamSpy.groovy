package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.mocking.QueryParamOptionsImpl
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope
import org.mule.module.http.internal.ParameterMap
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.SpyProcess
import org.mule.munit.common.util.MunitMuleTestUtils

class QueryParamSpy implements SpyProcess, MuleMessageTransformer {
    private final Closure closure
    private results = null
    private final OutputTransformer outputTransformer
    private final ProcessorLocator processorLocator
    private final QueryParamOptionsImpl queryParamOptions
    private DefaultHttpRequester httpRequester
    private final MuleContext muleContext

    QueryParamSpy(ProcessorLocator processorLocator,
                  Closure closure,
                  OutputTransformer outputTransformer,
                  MuleContext muleContext) {
        this.muleContext = muleContext
        this.processorLocator = processorLocator
        this.outputTransformer = outputTransformer
        this.closure = closure
        this.queryParamOptions = new QueryParamOptionsImpl()
    }

    void spy(MuleEvent incomingEvent) throws MuleException {
        httpRequester = processorLocator.getProcessor(incomingEvent) as DefaultHttpRequester
        def requestBuilder = httpRequester.requestBuilder
        ParameterMap parameters = requestBuilder.getQueryParams(incomingEvent)
        // make it easier to compare
        def stockMap = new HashMap(parameters)
        def fullPath = requestBuilder.replaceUriParams(httpRequester.path, incomingEvent)
        def httpVerb = httpRequester.method
        def code = closure.rehydrate(queryParamOptions, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        results = code(stockMap, fullPath, httpVerb)
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def outputMessage = this.outputTransformer.transformOutput(results)
        def code = queryParamOptions.httpReturnCode
        if (code) {
            outputMessage.setProperty('http.status',
                                      queryParamOptions.httpReturnCode,
                                      PropertyScope.INBOUND)
            def dummyEvent = new DefaultMuleEvent(outputMessage,
                                                  MessageExchangePattern.REQUEST_RESPONSE,
                                                  MunitMuleTestUtils.getTestFlow(muleContext))
            this.httpRequester.responseValidator.validate(dummyEvent)
        }
        outputMessage
    }
}
