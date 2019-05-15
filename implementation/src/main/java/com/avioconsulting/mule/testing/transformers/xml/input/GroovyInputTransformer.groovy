package com.avioconsulting.mule.testing.transformers.xml.input

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.InputTransformer
import groovy.util.logging.Log4j2

@Log4j2
class GroovyInputTransformer<T extends ConnectorInfo> implements InputTransformer<T> {
    @Override
    def transformInput(EventWrapper incomingEvent,
                       T connectorInfo) {
        def xmlString = connectorInfo.supportsIncomingBody ? connectorInfo.incomingBody : incomingEvent.messageAsString
        log.info 'Received XML of {}, converting to Groovy node',
                 xmlString
        new XmlParser().parseText(xmlString) as Node
    }
}
