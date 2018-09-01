package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import groovy.util.logging.Log4j2
import org.mule.runtime.core.api.event.CoreEvent

@Log4j2
class SoapOperationFlowInvokerImpl extends SoapInvokerBaseImpl {
    private final String flowName

    SoapOperationFlowInvokerImpl(EventFactory eventFactory,
                                 String flowName) {
        super(eventFactory)
        this.flowName = flowName
    }

    CoreEvent getEvent() {
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
