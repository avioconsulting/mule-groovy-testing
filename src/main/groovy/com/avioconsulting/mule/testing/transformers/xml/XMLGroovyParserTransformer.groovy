package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.xml.XmlUtil

class XMLGroovyParserTransformer<T extends ConnectorInfo> extends
        XMLTransformer<T> implements
        MuleMessageTransformer<T>,
        ClosureMuleMessageHandler {
    private final Closure closure

    XMLGroovyParserTransformer(Closure closure,
                               IPayloadValidator<T> payloadValidator) {
        super(payloadValidator)
        this.closure = closure
    }

    EventWrapper transform(EventWrapper muleEvent,
                           T connectorInfo) {
        validateContentType(muleEvent,
                            connectorInfo)
        def xmlString = muleEvent.messageAsString
        def node = new XmlParser().parseText(xmlString) as Node
        def forMuleMsg = withMuleEvent(closure,
                                       muleEvent)
        def reply = forMuleMsg(node)

        String outputXmlString
        if (reply instanceof File) {
            outputXmlString = reply.text
        } else {
            assert reply instanceof Node
            outputXmlString = XmlUtil.serialize(reply)
        }

        def reader = new StringReader(outputXmlString)
        this.xmlMessageBuilder.build(reader,
                                     muleEvent,
                                     200)
    }
}
