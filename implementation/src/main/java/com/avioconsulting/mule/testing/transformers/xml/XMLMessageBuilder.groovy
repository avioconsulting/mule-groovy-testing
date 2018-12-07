package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class XMLMessageBuilder {
    private static final String XML_MEDIA_TYPE = 'application/xml'
    enum MessageType {
        Mule41Stream,
        Soap
    }

    EventWrapper build(String xmlPayload,
                       EventWrapper rewriteEvent,
                       ConnectorInfo connectorInfo,
                       MessageType messageType,
                       Integer httpStatus = null) {
        def messageProps = getXmlProperties(httpStatus)
        switch (messageType) {
            case MessageType.Mule41Stream:
                return rewriteEvent.withNewStreamingPayload(xmlPayload,
                                                            XML_MEDIA_TYPE,
                                                            messageProps,
                                                            connectorInfo,
                                                            true)
            case MessageType.Soap:
                return rewriteEvent.withSoapPayload(xmlPayload,
                                                    connectorInfo,
                                                    messageProps)
            default:
                throw new Exception("Unknown message type! ${messageType}")
        }
    }

    private static LinkedHashMap<String, String> getXmlProperties(Integer httpStatus) {
        // need some of these props for SOAP mock to work properly
        def messageProps = [:]
        if (httpStatus != null) {
            messageProps['http.status'] = httpStatus
        }
        messageProps
    }
}
