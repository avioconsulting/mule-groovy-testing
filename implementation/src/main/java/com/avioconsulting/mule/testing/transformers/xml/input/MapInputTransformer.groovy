package com.avioconsulting.mule.testing.transformers.xml.input

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.InputTransformer
import groovy.util.logging.Log4j2
import groovy.util.slurpersupport.GPathResult

@Log4j2
class MapInputTransformer<T extends ConnectorInfo> implements InputTransformer<T> {
    @Override
    def transformInput(EventWrapper incomingEvent,
                       T connectorInfo) {
        def xmlString = connectorInfo.supportsIncomingBody ? connectorInfo.incomingBody : incomingEvent.messageAsString
        log.info 'Received XML of {}, converting to Groovy XML Map',
                 xmlString
        def node = new XmlSlurper().parseText(xmlString) as GPathResult
        convertToMap(node)
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
}
