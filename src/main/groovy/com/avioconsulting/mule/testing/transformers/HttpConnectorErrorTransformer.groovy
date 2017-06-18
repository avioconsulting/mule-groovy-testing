package com.avioconsulting.mule.testing.transformers

import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MessagingException
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.util.MunitMuleTestUtils

class HttpConnectorErrorTransformer implements MuleMessageTransformer {
    private final MuleContext muleContext
    private boolean triggerException

    HttpConnectorErrorTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def triggerException() {
        this.triggerException = true
    }

    MuleMessage transform(MuleMessage muleMessage) {
        if (!triggerException) {
            return muleMessage
        }

        def message = new DefaultMuleMessage('HTTP Connect Error!', muleContext)
        def event = new DefaultMuleEvent(message,
                                         MessageExchangePattern.REQUEST_RESPONSE,
                                         MunitMuleTestUtils.getTestFlow(muleContext))
        throw new MessagingException(event,
                                     new ConnectException('could not reach HTTP server'))
    }
}
