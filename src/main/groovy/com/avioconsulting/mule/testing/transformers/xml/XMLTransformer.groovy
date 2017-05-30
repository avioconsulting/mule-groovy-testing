package com.avioconsulting.mule.testing.transformers.xml

import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

import javax.xml.bind.JAXBElement

class XMLTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final JAXBMarshalHelper helper
    private final XMLMessageBuilder builder

    XMLTransformer(Closure closure,
                   MuleContext muleContext,
                   Class inputJaxbClass) {
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass)
        this.builder = new XMLMessageBuilder(muleContext)
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        def payload = incomingMessage.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        if (nullPayload) {
            println 'Groovy Test WARNING: SOAP mock was sent a message with empty payload! using MuleMessage payload.'
            strongTypedPayload = incomingMessage
        } else {
            strongTypedPayload = helper.unmarshal(incomingMessage)
        }
        def reply = this.closure(strongTypedPayload)
        assert reply instanceof JAXBElement
        def reader = helper.getMarshalled(reply)
        this.builder.build(reader, 200)
    }
}
