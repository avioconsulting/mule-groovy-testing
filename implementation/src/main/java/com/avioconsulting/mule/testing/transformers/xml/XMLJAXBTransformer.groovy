package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import groovy.util.logging.Log4j2

@Log4j2
class XMLJAXBTransformer<T extends ConnectorInfo> extends
        XMLTransformer implements
        MuleMessageTransformer {
    private final Closure closure
    private final JAXBMarshalHelper helper
    private final XMLMessageBuilder.MessageType messageType
    private final ClosureCurrier closureCurrier = new ClosureCurrier<T>()

    XMLJAXBTransformer(Closure closure,
                       Class inputJaxbClass,
                       XMLMessageBuilder.MessageType messageType,
                       RuntimeBridgeTestSide runtimeBridgeTestSide) {
        super(runtimeBridgeTestSide)
        this.messageType = messageType
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass)
    }

    EventWrapper transform(EventWrapper event,
                           ConnectorInfo connectorInfo) {
        def strongTypedPayload = helper.unmarshal(event,
                                                  connectorInfo)
        def closure = closureCurrier.curryClosure(this.closure,
                                                  event,
                                                  connectorInfo)
        def reply = closure(strongTypedPayload)
        // TODO: Remove this once we get closure context, see XMLTransformer
        if (impendingFault) {
            return event
        }
        String xml = reply instanceof File ? reply.text : helper.getMarshalled(reply)
        this.xmlMessageBuilder.build(xml,
                                     event,
                                     connectorInfo,
                                     messageType,
                                     200)
    }
}
