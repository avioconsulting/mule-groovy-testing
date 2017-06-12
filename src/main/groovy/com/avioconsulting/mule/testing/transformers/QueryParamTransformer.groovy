package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.dsl.mocking.QueryParamOptionsImpl
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.util.MunitMuleTestUtils

class QueryParamTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final OutputTransformer outputTransformer
    private final QueryParamOptionsImpl queryParamOptions
    private final MuleContext muleContext
    private final HttpConnectorSpy spy

    QueryParamTransformer(HttpConnectorSpy spy,
                          Closure closure,
                          OutputTransformer outputTransformer,
                          MuleContext muleContext) {
        this.spy = spy
        this.muleContext = muleContext
        this.outputTransformer = outputTransformer
        this.closure = closure
        this.queryParamOptions = new QueryParamOptionsImpl()
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def closureCode = closure.rehydrate(queryParamOptions, this, this)
        closureCode.resolveStrategy = Closure.DELEGATE_ONLY
        def results = closureCode(spy.queryParams,
                                  spy.fullPath,
                                  spy.httpVerb)
        def outputMessage = this.outputTransformer.transformOutput(results)
        def code = queryParamOptions.httpReturnCode
        if (code) {
            outputMessage.setProperty('http.status',
                                      queryParamOptions.httpReturnCode,
                                      PropertyScope.INBOUND)
            def dummyEvent = new DefaultMuleEvent(outputMessage,
                                                  MessageExchangePattern.REQUEST_RESPONSE,
                                                  MunitMuleTestUtils.getTestFlow(muleContext))
            this.spy.validate(dummyEvent)
        }
        outputMessage
    }
}
