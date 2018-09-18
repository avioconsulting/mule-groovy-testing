package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureMuleMessageHandler
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

class XMLMapTransformer<T extends ConnectorInfo> extends
        XMLTransformer<T> implements
        MuleMessageTransformer<T>,
        ClosureMuleMessageHandler {
    private final Closure closure

    XMLMapTransformer(Closure closure,
                      IPayloadValidator<T> payloadValidator) {
        super(payloadValidator)
        this.closure = closure
    }

    void transform(MockEventWrapper incomingEvent,
                   ConnectorInfo connectorInfo) {
        validateContentType(incomingEvent,
                            connectorInfo)
        def xmlString = incomingEvent.messageAsString
        def node = new XmlSlurper().parseText(xmlString) as GPathResult
        def asMap = convertToMap(node)
        def forMuleMsg = withMuleEvent(closure, incomingEvent)
        def result = forMuleMsg(asMap)
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
            [child.name(), child.childNodes() ? convertToMap(child, false) : child.text()]
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
