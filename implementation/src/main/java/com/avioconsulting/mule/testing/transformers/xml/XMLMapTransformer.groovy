package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import groovy.util.logging.Log4j2
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

@Log4j2
class XMLMapTransformer<T extends ConnectorInfo> extends
        XMLTransformer<T> implements
        MuleMessageTransformer<T> {
    private final Closure closure
    private final XMLMessageBuilder.MessageType messageType
    private final ClosureCurrier closureCurrier = new ClosureCurrier<T>()

    XMLMapTransformer(Closure closure,
                      XMLMessageBuilder.MessageType messageType) {
        this.messageType = messageType
        this.closure = closure
    }

    EventWrapper transform(EventWrapper incomingEvent,
                           T connectorInfo) {
        def xmlString = connectorInfo.incomingBody ?: incomingEvent.messageAsString
        log.info 'Received XML of {}, converting to Groovy XML Map',
                 xmlString
        def node = new XmlSlurper().parseText(xmlString) as GPathResult
        def asMap = convertToMap(node)
        def closure = closureCurrier.curryClosure(this.closure,
                                                  incomingEvent,
                                                  connectorInfo)
        def result = closure(asMap)
        // TODO: Remove this once we get closure context, see XMLTransformer
        if (impendingFault) {
            return incomingEvent
        }
        String xmlReply
        if (result instanceof File) {
            xmlReply = result.text
        } else {
            assert result instanceof Map
            xmlReply = generateXmlFromMap(result)
        }
        log.info 'Returning following XML back to Mule flow: {}',
                 xmlReply
        this.xmlMessageBuilder.build(xmlReply,
                                     incomingEvent,
                                     connectorInfo,
                                     messageType,
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
