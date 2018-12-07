package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder

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
        jaxbHelper.unmarshal(event,
                             flow)
    }
}
