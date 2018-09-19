package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.util.logging.Log4j2

@Log4j2
class XMLJAXBTransformer<T extends ConnectorInfo> extends
        XMLTransformer implements
        MuleMessageTransformer,
        ClosureMuleMessageHandler {
    private final Closure closure
    private final JAXBMarshalHelper helper

    XMLJAXBTransformer(Closure closure,
                       Class inputJaxbClass,
                       IPayloadValidator<T> payloadValidator,
                       String transformerUse) {
        super(payloadValidator)
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass,
                                            transformerUse)
    }

    EventWrapper transform(EventWrapper event,
                           ConnectorInfo connectorInfo) {
        validateContentType(event,
                            connectorInfo)
        def payload = event.message.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        if (nullPayload) {
            log.warn('SOAP mock was sent a message with empty payload! using MuleMessage payload.')
            strongTypedPayload = event.message
        } else {
            strongTypedPayload = helper.unmarshal(payload)
        }

        def forMuleMsg = withMuleEvent(this.closure,
                                       event)
        def reply = forMuleMsg(strongTypedPayload)

        StringReader reader
        if (reply instanceof File) {
            def xml = reply.text
            reader = new StringReader(xml)
        } else {
            reader = helper.getMarshalled(reply)
        }

        this.xmlMessageBuilder.build(reader,
                                     event,
                                     200)
    }
}
