package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ProcessorWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.xml.XmlUtil

class XMLGroovyParserTransformer extends XMLTransformer implements MuleMessageTransformer,
        ClosureMuleMessageHandler {
    private final Closure closure

    XMLGroovyParserTransformer(Closure closure,
                               InvokerEventFactory eventFactory,
                               IPayloadValidator payloadValidator) {
        super(eventFactory,
              payloadValidator)
        this.closure = closure
    }

    EventWrapper transform(EventWrapper muleEvent,
                           ProcessorWrapper messageProcessor) {
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
