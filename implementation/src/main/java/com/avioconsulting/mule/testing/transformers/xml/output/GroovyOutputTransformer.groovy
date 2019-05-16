package com.avioconsulting.mule.testing.transformers.xml.output

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import com.avioconsulting.mule.testing.transformers.xml.XMLTransformer
import groovy.util.logging.Log4j2
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

@Log4j2
class GroovyOutputTransformer<T extends ConnectorInfo>  extends XMLTransformer<T> implements OutputTransformer {
    private final XMLMessageBuilder.MessageType messageType

    GroovyOutputTransformer(XMLMessageBuilder.MessageType messageType) {
        this.messageType = messageType
    }

    @Override
    EventWrapper transformOutput(Object reply,
                                 EventWrapper muleEvent,
                                 ConnectorInfo connectorInfo) {
        String outputXmlString
        if (reply instanceof File) {
            outputXmlString = reply.text
        } else {
            assert reply instanceof Node
            outputXmlString = XmlUtil.serialize(reply)
        }
        log.info 'Returning following XML back to Mule flow: {}',
                 outputXmlString
        this.xmlMessageBuilder.build(outputXmlString,
                                     muleEvent,
                                     connectorInfo,
                                     messageType,
                                     200)
    }
}
