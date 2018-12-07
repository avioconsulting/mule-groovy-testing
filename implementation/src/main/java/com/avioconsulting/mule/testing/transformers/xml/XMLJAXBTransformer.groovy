package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.util.logging.Log4j2

@Log4j2
class XMLJAXBTransformer<T extends ConnectorInfo> extends
        XMLTransformer implements
        MuleMessageTransformer,
        ClosureMuleMessageHandler {
    private final Closure closure
    private final JAXBMarshalHelper helper
    private final XMLMessageBuilder.MessageType messageType

    XMLJAXBTransformer(Closure closure,
                       Class inputJaxbClass,
                       String transformerUse,
                       XMLMessageBuilder.MessageType messageType) {
        this.messageType = messageType
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass,
                                            transformerUse)
    }

    EventWrapper transform(EventWrapper event,
                           ConnectorInfo connectorInfo) {
        def strongTypedPayload = helper.unmarshal(event)
        def forMuleMsg = withMuleEvent(this.closure,
                                       event)
        def reply = forMuleMsg(strongTypedPayload)
        String xml = reply instanceof File ? reply.text : helper.getMarshalled(reply)
        this.xmlMessageBuilder.build(xml,
                                     event,
                                     connectorInfo,
                                     messageType,
                                     200)
    }
}
