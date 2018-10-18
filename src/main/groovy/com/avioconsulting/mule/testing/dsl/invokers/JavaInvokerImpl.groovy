package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class JavaInvokerImpl implements
        JavaInvoker,
        Invoker {
    private inputObject
    private final InvokerEventFactory eventFactory
    private final String flowName

    JavaInvokerImpl(InvokerEventFactory eventFactory,
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
}
