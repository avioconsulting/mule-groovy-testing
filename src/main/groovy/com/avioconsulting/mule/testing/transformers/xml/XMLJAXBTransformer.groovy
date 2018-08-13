package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

class XMLJAXBTransformer extends XMLTransformer implements MuleMessageTransformer,
        ClosureMuleMessageHandler {
    private final Closure closure
    private final JAXBMarshalHelper helper

    XMLJAXBTransformer(Closure closure,
                       MuleContext muleContext,
                       Class inputJaxbClass,
                       IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass)
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        validateContentType(incomingMessage)
        def payload = incomingMessage.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        if (nullPayload) {
            println 'Groovy Test WARNING: SOAP mock was sent a message with empty payload! using MuleMessage payload.'
            strongTypedPayload = incomingMessage
        } else {
            strongTypedPayload = helper.unmarshal(incomingMessage)
        }

        def forMuleMsg = withMuleMessage(this.closure, incomingMessage)
        def reply = forMuleMsg(strongTypedPayload)

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
