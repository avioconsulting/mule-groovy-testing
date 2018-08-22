package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import org.mule.api.MuleEvent

abstract class SoapInvokerBaseImpl implements Invoker, SoapInvoker {
    protected inputObject
    protected final XMLMessageBuilder xmlMessageBuilder
    protected JAXBMarshalHelper jaxbHelper
    protected final EventFactory eventFactory

    SoapInvokerBaseImpl(EventFactory eventFactory) {
        this.xmlMessageBuilder = new XMLMessageBuilder(eventFactory,
                                                       true)
        this.eventFactory = eventFactory
    }

    @Override
    def inputJaxbPayload(Object inputObject) {
        this.inputObject = inputObject
        this.jaxbHelper = new JAXBMarshalHelper(inputObject.class)
    }

    @Override
    def transformOutput(MuleEvent event) {
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
