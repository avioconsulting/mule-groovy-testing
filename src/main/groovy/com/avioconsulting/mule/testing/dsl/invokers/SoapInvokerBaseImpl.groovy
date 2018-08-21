package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage

abstract class SoapInvokerBaseImpl implements Invoker, SoapInvoker {
    protected inputObject
    private final MuleContext muleContext
    protected final XMLMessageBuilder xmlMessageBuilder
    protected JAXBMarshalHelper jaxbHelper
    private final EventFactory eventFactory
    protected final String flowName

    SoapInvokerBaseImpl(MuleContext muleContext,
                        EventFactory eventFactory,
                        String flowName) {
        this.flowName = flowName
        this.muleContext = muleContext
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext,
                                                       true)
        this.eventFactory = eventFactory
    }

    @Override
    def inputJaxbPayload(Object inputObject) {
        this.inputObject = inputObject
        this.jaxbHelper = new JAXBMarshalHelper(inputObject.class)
    }

    protected abstract MuleMessage getMessage()

    @Override
    MuleEvent getEvent() {
        eventFactory.getMuleEvent(getMessage(),
                                  flowName,
                                  MessageExchangePattern.REQUEST_RESPONSE)
    }

    @Override
    def transformOutput(MuleEvent event) {
        def incomingMessage = event.message
        def payload = incomingMessage.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        if (nullPayload) {
            println 'Groovy Test WARNING: SOAP mock was sent a message with empty payload! using MuleMessage payload.'
            strongTypedPayload = incomingMessage
        } else {
            strongTypedPayload = jaxbHelper.unmarshal(incomingMessage)
        }
        strongTypedPayload
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
