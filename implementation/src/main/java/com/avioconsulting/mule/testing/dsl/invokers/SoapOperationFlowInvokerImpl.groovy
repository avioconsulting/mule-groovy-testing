package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import groovy.util.logging.Log4j2

@Log4j2
class SoapOperationFlowInvokerImpl extends
        SoapInvokerBaseImpl {
    private final String flowName
    private final InvokerEventFactory eventFactory

    SoapOperationFlowInvokerImpl(InvokerEventFactory eventFactory,
                                 RuntimeBridgeTestSide runtimeBridgeTestSide,
                                 String flowName) {
        super(flowName,
              runtimeBridgeTestSide)
        this.eventFactory = eventFactory
        this.flowName = flowName
    }

    /**
     * puts together a SOAP event that mirrors what you see AFTER the soap apikit router
     * @return
     */
    EventWrapper getEvent() {
        String xml = inputObject instanceof File ? inputObject.text : jaxbHelper.getMarshalled(inputObject)
        def newEvent = eventFactory.getMuleEventWithPayload(null,
                                                            flowName)
        this.xmlMessageBuilder.build(xml,
                                     newEvent,
                                     flow,
                                     XMLMessageBuilder.MessageType.SoapInvocation)
    }
}
