package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder

abstract class SoapInvokerBaseImpl implements Invoker, SoapInvoker {
    protected inputObject
    protected final XMLMessageBuilder xmlMessageBuilder
    protected JAXBMarshalHelper jaxbHelper
    protected final InvokerEventFactory eventFactory

    SoapInvokerBaseImpl(InvokerEventFactory eventFactory) {
        this.xmlMessageBuilder = new XMLMessageBuilder(eventFactory,
                                                       true)
        this.eventFactory = eventFactory
    }

    @Override
    def inputJaxbPayload(Object inputObject) {
        this.inputObject = inputObject
        this.jaxbHelper = new JAXBMarshalHelper(inputObject.class,
                                                'SOAP Invoker/HTTP Listener Reply')
    }

    @Override
    def transformOutput(EventWrapper event) {
        def incomingMessage = event.message
        def payload = incomingMessage.payload
        jaxbHelper.unmarshal(payload)
    }

    @Override
    Invoker withNewPayloadValidator(IPayloadValidator validator) {
        // TODO: Deal with this and getPayloadValidator
        this
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        return null
    }
}
