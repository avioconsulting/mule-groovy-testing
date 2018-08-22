package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.xml.XmlUtil
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class XMLGroovyParserTransformer extends XMLTransformer implements MuleMessageTransformer,
        ClosureMuleMessageHandler {
    private final Closure closure

    XMLGroovyParserTransformer(Closure closure,
                               EventFactory eventFactory,
                               IPayloadValidator payloadValidator) {
        super(eventFactory,
              payloadValidator)
        this.closure = closure
    }

    MuleEvent transform(MuleEvent muleEvent,
                        MessageProcessor messageProcessor) {
        validateContentType(muleEvent,
                            messageProcessor)
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
