package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class XMLMapTransformer extends XMLTransformer implements MuleMessageTransformer {
    private final Closure closure

    XMLMapTransformer(Closure closure,
                      EventFactory eventFactory,
                      IPayloadValidator payloadValidator) {
        super(eventFactory,
              payloadValidator)
        this.closure = closure
    }

    MuleEvent transform(MuleEvent incomingEvent,
                        MessageProcessor messageProcessor) {
        validateContentType(incomingEvent,
                            messageProcessor)
        def xmlString = incomingEvent.messageAsString
        def node = new XmlSlurper().parseText(xmlString) as GPathResult
        def asMap = convertToMap(node)
        def closure = handleMuleEvent(closure,
                                      incomingEvent,
                                      messageProcessor)
        def result = closure(asMap)
        String xmlReply
        if (result instanceof File) {
            xmlReply = result.text
        } else {
            assert result instanceof Map
            xmlReply = generateXmlFromMap(result)
        }
        def reader = new StringReader(xmlReply)
        this.xmlMessageBuilder.build(reader,
                                     incomingEvent,
                                     200)
    }

    private static Map convertToMap(GPathResult node,
                                    boolean root = true) {
        def kidResults = node.children().collectEntries { GPathResult child ->
            [child.name(), child.childNodes() ? convertToMap(child,
                                                             false) : child.text()]
        }
        if (root) {
            [
                    (node.name()): kidResults
            ]
        } else {
            kidResults
        }
    }

    static String generateXmlFromMap(Map map) {
        new StringWriter().with { sw ->
            new MarkupBuilder(sw).with {
                map.collect { k, v ->
                    "$k" { v instanceof Map ? v.collect(owner) : mkp.yield(v) }
                }
            }
            return sw.toString()
        }
    }
}
