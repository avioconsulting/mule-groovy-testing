package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import groovy.util.logging.Log4j2

@Log4j2
class SoapOperationFlowInvokerImpl extends SoapInvokerBaseImpl {
    private final String flowName

    SoapOperationFlowInvokerImpl(EventFactory eventFactory,
                                 String flowName) {
        super(eventFactory)
        this.flowName = flowName
    }

    EventWrapper getEvent() {
        StringReader reader
        if (inputObject instanceof File) {
            def xml = inputObject.text
            reader = new StringReader(xml)
        } else {
            reader = jaxbHelper.getMarshalled(inputObject)
        }
        this.xmlMessageBuilder.build(reader,
                                     flowName)
    }
}
