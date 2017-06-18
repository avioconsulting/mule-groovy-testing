package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MessagingException
import org.mule.api.MuleContext
import org.mule.munit.common.util.MunitMuleTestUtils

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    SOAPFormatterImpl(MuleContext muleContext,
                      IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
    }

    def httpConnectError() {
        if (transformer == null) {
            throw new Exception('Only invoke this closure inside your whenCalledWith... code')
        }
        def message = new DefaultMuleMessage('HTTP Connect Error!', muleContext)
        def event = new DefaultMuleEvent(message,
                                         MessageExchangePattern.REQUEST_RESPONSE,
                                         MunitMuleTestUtils.getTestFlow(muleContext))
        throw new MessagingException(event,
                                     new ConnectException('could not reach HTTP server'))
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new SOAPFormatterImpl(muleContext, validator)
    }
}
