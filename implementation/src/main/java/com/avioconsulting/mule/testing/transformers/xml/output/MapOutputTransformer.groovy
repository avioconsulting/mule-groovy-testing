package com.avioconsulting.mule.testing.transformers.xml.output

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import com.avioconsulting.mule.testing.transformers.xml.XMLTransformer
import groovy.util.logging.Log4j2
import groovy.xml.MarkupBuilder

@Log4j2
class MapOutputTransformer<T extends ConnectorInfo>  extends XMLTransformer<T> implements OutputTransformer {
    private final XMLMessageBuilder.MessageType messageType

    MapOutputTransformer(XMLMessageBuilder.MessageType messageType) {
        this.messageType = messageType
    }

    @Override
    EventWrapper transformOutput(Object result,
                                 EventWrapper incomingEvent,
                                 ConnectorInfo connectorInfo) {
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
