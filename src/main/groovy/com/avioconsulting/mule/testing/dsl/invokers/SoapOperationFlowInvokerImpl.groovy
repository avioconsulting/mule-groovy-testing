package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import groovy.util.logging.Log4j2

@Log4j2
class SoapOperationFlowInvokerImpl extends
        SoapInvokerBaseImpl {
    private final String flowName
    private final InvokerEventFactory eventFactory

    SoapOperationFlowInvokerImpl(InvokerEventFactory eventFactory,
                                 String flowName) {
        this.eventFactory = eventFactory
        this.flowName = flowName
    }

    EventWrapper getEvent() {
        String xml
        if (inputObject instanceof File) {
            xml = inputObject.text
        } else {
            def reader = jaxbHelper.getMarshalled(inputObject)
            xml = reader.text
        }
        def newEvent = eventFactory.getMuleEventWithPayload(null,
                                                            flowName)
        this.xmlMessageBuilder.build(xml,
                                     newEvent)
    }
}
