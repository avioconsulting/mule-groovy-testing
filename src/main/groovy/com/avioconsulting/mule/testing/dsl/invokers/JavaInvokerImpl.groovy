package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

class JavaInvokerImpl implements JavaInvoker, Invoker {
    private final MuleContext muleContext
    private inputObject
    private final EventFactory eventFactory
    private final String flowName

    JavaInvokerImpl(EventFactory eventFactory,
                    String flowName) {
        this.flowName = flowName
        this.muleContext = muleContext
        this.eventFactory = eventFactory
    }

    def inputPayload(Object inputObject) {
        this.inputObject = inputObject
    }

    MuleEvent getEvent() {
        eventFactory.getMuleEventWithPayload(inputObject,
                                             flowName,
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
