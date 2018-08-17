package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.xml.XmlUtil
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

class XMLGroovyParserTransformer extends XMLTransformer implements MuleMessageTransformer,
        ClosureMuleMessageHandler{
    private final Closure closure

    XMLGroovyParserTransformer(Closure closure,
                               MuleContext muleContext,
                               IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
        this.closure = closure
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        validateContentType(incomingMessage)
        def xmlString = incomingMessage.payloadAsString
        def node = new XmlParser().parseText(xmlString) as Node
        def forMuleMsg = withMuleMessage(closure, incomingMessage)
        def reply = forMuleMsg(node)

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
