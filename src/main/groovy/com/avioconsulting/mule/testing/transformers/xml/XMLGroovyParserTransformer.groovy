package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.dsl.mocking.ConnectorType
import groovy.xml.XmlUtil
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class XMLGroovyParserTransformer extends XMLTransformer implements MuleMessageTransformer {
    private final Closure closure

    XMLGroovyParserTransformer(Closure closure,
                               MuleContext muleContext,
                               ConnectorType mockedConnectorType) {
        super(muleContext, mockedConnectorType)
        this.closure = closure
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        validateContentType(incomingMessage)
        def xmlString = incomingMessage.payloadAsString
        def node = new XmlParser().parseText(xmlString) as Node
        def reply = closure(node)

        String outputXmlString
        if (reply instanceof File) {
            outputXmlString = reply.text
        } else {
            assert reply instanceof Node
            outputXmlString = XmlUtil.serialize(reply)
        }

        def reader = new StringReader(outputXmlString)
        this.xmlMessageBuilder.build(reader, 200)
    }
}
