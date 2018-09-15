package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator

class JavaInvokerImpl implements JavaInvoker, Invoker {
    private inputObject
    private final EventFactory eventFactory
    private final String flowName

    JavaInvokerImpl(EventFactory eventFactory,
                    String flowName) {
        this.flowName = flowName
        this.eventFactory = eventFactory
    }

    def inputPayload(Object inputObject) {
        this.inputObject = inputObject
    }

    EventWrapper getEvent() {
        eventFactory.getMuleEventWithPayload(inputObject,
                                             flowName)
    }

    def transformOutput(EventWrapper event) {
        event.message.valueInsideTypedValue
    }

    Invoker withNewPayloadValidator(IPayloadValidator validator) {
        // java side doesn't do payload validation
        return this
    }

    IPayloadValidator getPayloadValidator() {
        return null
    }
}
