package com.avioconsulting.muletesting

import com.avioconsulting.muletesting.transformers.JSONRequestReplyTransformer
import com.avioconsulting.muletesting.transformers.SimpleClosureTransformer
import com.avioconsulting.muletesting.transformers.YieldType
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.SuccessStatusCodeValidator
import org.mule.munit.common.mocking.Attribute
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.util.MunitMuleTestUtils

trait RESTTesting {
    abstract MuleContext getMuleContext()

    abstract MessageProcessorMocker whenMessageProcessor(String name)

    MessageProcessorMocker mockRESTPostReply(String name,
                                             Class expectedRequestJsonClass,
                                             YieldType yieldType = YieldType.Map,
                                             Closure testClosure) {
        def mock = getHttpRequestMock(name)
        mock.thenApply(new JSONRequestReplyTransformer(
                expectedRequestJsonClass,
                yieldType,
                testClosure))
        mock
    }

    private MessageProcessorMocker getHttpRequestMock(String name) {
        whenMessageProcessor('request')
                .ofNamespace('http')
                .withAttributes(Attribute.attribute('name').ofNamespace('doc').withValue(name))
    }

    MessageProcessorMocker mockRESTGetReply(String name, Closure testClosure) {
        def mock = getHttpRequestMock(name)
        mock.thenApply(new SimpleClosureTransformer(testClosure))
        mock
    }

    def throwHttpStatusBasedException(MuleMessage errorResponse) {
        def errorEvent = new DefaultMuleEvent(errorResponse,
                                              MessageExchangePattern.REQUEST_RESPONSE,
                                              MunitMuleTestUtils.getTestFlow(muleContext))
        new SuccessStatusCodeValidator('200').validate(errorEvent)
    }
}
