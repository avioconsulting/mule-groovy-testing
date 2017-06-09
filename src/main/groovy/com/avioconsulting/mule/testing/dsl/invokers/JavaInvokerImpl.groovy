package com.avioconsulting.mule.testing.dsl.invokers

import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.munit.common.util.MunitMuleTestUtils

class JavaInvokerImpl implements JavaInvoker, Invoker {
    private final MuleContext muleContext
    private inputObject

    JavaInvokerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def inputPayload(Object inputObject) {
        this.inputObject = inputObject
    }

    MuleEvent getEvent() {
        def message = new DefaultMuleMessage(inputObject, muleContext)
        new DefaultMuleEvent(message,
                             MessageExchangePattern.REQUEST_RESPONSE,
                             MunitMuleTestUtils.getTestFlow(muleContext))
    }

    def transformOutput(MuleEvent event) {
        event.message.payload
    }
}
