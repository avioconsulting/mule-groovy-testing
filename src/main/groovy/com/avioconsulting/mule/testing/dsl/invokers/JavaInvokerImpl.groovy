package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class JavaInvokerImpl implements JavaInvoker, Invoker {
    private final MuleContext muleContext
    private inputObject

    JavaInvokerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def inputPayload(Object inputObject) {
        this.inputObject = inputObject
    }

    MuleEvent getEvent(EventFactory eventFactory) {
        def message = new DefaultMuleMessage(inputObject, muleContext)
        eventFactory.getMuleEvent(message,
                                  MessageExchangePattern.REQUEST_RESPONSE)
    }

    def transformOutput(MuleEvent event) {
        event.message.payload
    }

    Invoker withNewPayloadValidator(IPayloadValidator validator) {
        // java side doesn't do payload validation
        return this
    }

    IPayloadValidator getPayloadValidator() {
        return null
    }
}
