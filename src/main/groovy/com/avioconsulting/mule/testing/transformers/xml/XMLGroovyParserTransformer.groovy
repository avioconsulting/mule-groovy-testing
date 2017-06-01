package com.avioconsulting.mule.testing.transformers.xml

import groovy.xml.XmlUtil
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class XMLGroovyParserTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final XMLMessageBuilder xmlMessageBuilder

    XMLGroovyParserTransformer(Closure closure,
                               MuleContext muleContext) {
        this.closure = closure
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext)
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        def xmlString = incomingMessage.payloadAsString
        def node = new XmlParser().parseText(xmlString) as Node
        def reply = closure(node)
        assert reply instanceof Node
        def outputXmlString = XmlUtil.serialize(reply)
        def reader = new StringReader(outputXmlString)
        this.xmlMessageBuilder.build(reader, 200)
    }
}
