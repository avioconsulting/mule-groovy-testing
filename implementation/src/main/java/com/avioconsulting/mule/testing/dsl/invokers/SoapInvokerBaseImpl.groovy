package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import groovy.util.logging.Log4j2

@Log4j2
abstract class SoapInvokerBaseImpl implements
        Invoker,
        SoapInvoker {
    protected inputObject
    protected final XMLMessageBuilder xmlMessageBuilder
    protected JAXBMarshalHelper jaxbHelper
    protected final FlowWrapper flow

    SoapInvokerBaseImpl(String flowName,
                        RuntimeBridgeTestSide runtimeBridgeTestSide) {
        this.xmlMessageBuilder = new XMLMessageBuilder()
        this.flow = runtimeBridgeTestSide.getFlow(flowName)
    }

    @Override
    def inputJaxbPayload(Object inputObject) {
        this.inputObject = inputObject
        this.jaxbHelper = new JAXBMarshalHelper(inputObject.class)
    }

    @Override
    def transformOutput(EventWrapper event) {
        def payloadValue = event.message.payload.value
        // the "right" way to build a response payload when using SOAP APIKit is to build an application/java
        // object with a 'body' key with write "application/xml" after the key
        // this ensures that a content type is returned to the SOAP client
        if (payloadValue instanceof Map) {
            event = event.withNewPayload(payloadValue.body,
                                         flow,
                                         'application/xml')
        }
        else {
            log.warn 'A SOAP response was built that directly returns an XML payload without using application/java --- { body: {xmlHere} write "application/xml"}. This will cause 1.1.8 of the Mule SOAP APIKit router to NOT return an XML content type with its resposne which may break some clients (like Mule WSC).'
        }
        jaxbHelper.unmarshal(event,
                             flow)
    }
}
