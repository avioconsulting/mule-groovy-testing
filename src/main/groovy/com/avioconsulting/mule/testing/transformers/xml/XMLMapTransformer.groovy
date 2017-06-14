package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.payload_types.IPayloadValidator
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class XMLMapTransformer extends XMLTransformer implements MuleMessageTransformer {
    private final Closure closure

    XMLMapTransformer(Closure closure,
                      MuleContext muleContext,
                      IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
        this.closure = closure
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        validateContentType(incomingMessage)
        def xmlString = incomingMessage.payloadAsString
        def node = new XmlSlurper().parseText(xmlString) as GPathResult
        def asMap = convertToMap(node)
        def result = closure(asMap)
        String xmlReply
        if (result instanceof File) {
            xmlReply = result.text
        } else {
            assert result instanceof Map
            xmlReply = generateXmlFromMap(result)
        }
        def reader = new StringReader(xmlReply)
        this.xmlMessageBuilder.build(reader, 200)
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
