package com.avioconsulting.mule.testing.transformers.xml

import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class XMLJAXBTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final JAXBMarshalHelper helper
    private final XMLMessageBuilder xmlMessageBuilder

    XMLJAXBTransformer(Closure closure,
                       MuleContext muleContext,
                       Class inputJaxbClass) {
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass)
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext)
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

        StringReader reader
        if (reply instanceof File) {
            def xml = reply.text
            reader = new StringReader(xml)
        } else {
            reader = helper.getMarshalled(reply)
        }

        this.xmlMessageBuilder.build(reader, 200)
    }
}
