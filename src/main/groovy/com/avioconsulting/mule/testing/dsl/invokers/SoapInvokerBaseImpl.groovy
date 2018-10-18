package com.avioconsulting.mule.testing.dsl.invokers


import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder

abstract class SoapInvokerBaseImpl implements
        Invoker,
        SoapInvoker {
    protected inputObject
    protected final XMLMessageBuilder xmlMessageBuilder
    protected JAXBMarshalHelper jaxbHelper

    SoapInvokerBaseImpl() {
        this.xmlMessageBuilder = new XMLMessageBuilder()
    }

    @Override
    def inputJaxbPayload(Object inputObject) {
        this.inputObject = inputObject
        this.jaxbHelper = new JAXBMarshalHelper(inputObject.class,
                                                'SOAP Invoker/HTTP Listener Reply')
    }

    @Override
    def transformOutput(EventWrapper event) {
        jaxbHelper.unmarshal(event)
    }
}
