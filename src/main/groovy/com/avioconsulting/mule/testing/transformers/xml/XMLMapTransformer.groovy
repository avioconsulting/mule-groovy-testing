package com.avioconsulting.mule.testing.transformers.xml

import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class XMLMapTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final XMLMessageBuilder xmlMessageBuilder

    XMLMapTransformer(Closure closure,
                      MuleContext muleContext) {
        this.closure = closure
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext)
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        def xmlString = incomingMessage.payloadAsString
        def node = new XmlSlurper().parseText(xmlString) as GPathResult
        def asMap = convertToMap(node)
        def result = closure(asMap)
        assert result instanceof Map
        def xmlReply = generateXmlFromMap(result)
        def reader = new StringReader(xmlReply)
        this.xmlMessageBuilder.build(reader, 200)
    }

    private static Map convertToMap(GPathResult node) {
        def kidResults = node.children().collectEntries { GPathResult child ->
            [child.name(), child.childNodes() ? convertToMap(child) : child.text()]
        }
        [
                (node.name()): kidResults
        ]
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
