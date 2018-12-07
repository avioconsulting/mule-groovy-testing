package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import groovy.xml.XmlUtil

class XMLGroovyParserTransformer<T extends ConnectorInfo> extends
        XMLTransformer<T> implements
        MuleMessageTransformer<T> {
    private final Closure closure
    private final XMLMessageBuilder.MessageType messageType
    private final ClosureCurrier closureCurrier = new ClosureCurrier<T>()

    XMLGroovyParserTransformer(Closure closure,
                               XMLMessageBuilder.MessageType messageType) {
        this.messageType = messageType
        this.closure = closure
    }

    EventWrapper transform(EventWrapper muleEvent,
                           T connectorInfo) {
        def xmlString = connectorInfo.incomingBody ?: muleEvent.messageAsString
        def node = new XmlParser().parseText(xmlString) as Node
        def closure = closureCurrier.curryClosure(this.closure,
                                                  muleEvent,
                                                  connectorInfo)
        def reply = closure(node)
        String outputXmlString
        if (reply instanceof File) {
            outputXmlString = reply.text
        } else {
            assert reply instanceof Node
            outputXmlString = XmlUtil.serialize(reply)
        }

        this.xmlMessageBuilder.build(outputXmlString,
                                     muleEvent,
                                     connectorInfo,
                                     messageType,
                                     200)
    }
}