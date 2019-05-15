package com.avioconsulting.mule.testing.transformers.xml.output

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import com.avioconsulting.mule.testing.transformers.xml.XMLTransformer
import groovy.util.logging.Log4j2

@Log4j2
class JaxbOutputTransformer<T extends ConnectorInfo> extends XMLTransformer<T> implements OutputTransformer {
    private final XMLMessageBuilder.MessageType messageType
    private final JAXBMarshalHelper helper

    JaxbOutputTransformer(XMLMessageBuilder.MessageType messageType,
                          JAXBMarshalHelper helper) {
        this.messageType = messageType
        this.helper = helper
    }

    @Override
    EventWrapper transformOutput(Object reply,
                                 EventWrapper event,
                                 ConnectorInfo connectorInfo) {
        String xml = reply instanceof File ? reply.text : helper.getMarshalled(reply)
        this.xmlMessageBuilder.build(xml,
                                     event,
                                     connectorInfo,
                                     messageType,
                                     200)
    }
}
