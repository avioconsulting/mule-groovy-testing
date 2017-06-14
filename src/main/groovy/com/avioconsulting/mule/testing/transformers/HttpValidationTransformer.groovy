package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope
import org.mule.module.http.internal.request.ResponseValidator
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.util.MunitMuleTestUtils

class HttpValidationTransformer implements MuleMessageTransformer, IReceiveHttpOptions {
    private ResponseValidator responseValidator
    private Integer httpReturnCode = 200
    private final MuleContext muleContext

    HttpValidationTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    MuleMessage transform(MuleMessage muleMessage) {
        muleMessage.setProperty('http.status',
                                httpReturnCode,
                                PropertyScope.INBOUND)
        def dummyEvent = new DefaultMuleEvent(muleMessage,
                                              MessageExchangePattern.REQUEST_RESPONSE,
                                              MunitMuleTestUtils.getTestFlow(muleContext))
        this.responseValidator.validate(dummyEvent)
        return muleMessage
    }

    def setHttpReturnCode(Integer code) {
        this.httpReturnCode = code
    }

    def receive(Map queryParams,
                String fullPath,
                String httpVerb,
                ResponseValidator responseValidator) {
        this.responseValidator = responseValidator
    }
}
