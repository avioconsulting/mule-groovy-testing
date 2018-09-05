package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.runtime.core.api.event.CoreEvent

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

    CoreEvent getEvent() {
        eventFactory.getMuleEventWithPayload(inputObject,
                                             flowName)
    }

    def transformOutput(CoreEvent event) {
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
