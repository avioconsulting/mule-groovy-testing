package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo

class XMLMessageBuilder implements HttpAttributeBuilder {
    private static final String XML_MEDIA_TYPE = 'application/xml'
    enum MessageType {
        Mule41Stream,
        SoapMock,
        SoapInvocation
    }

    EventWrapper build(String xmlPayload,
                       EventWrapper rewriteEvent,
                       ConnectorInfo connectorInfo,
                       MessageType messageType,
                       Integer httpStatus = null) {
        def attributes = getXmlAttributes(httpStatus,
                                          connectorInfo,
                                          rewriteEvent)
        switch (messageType) {
            case MessageType.Mule41Stream:
                return rewriteEvent.withNewStreamingPayload(xmlPayload,
                                                            XML_MEDIA_TYPE,
                                                            attributes,
                                                            connectorInfo,
                                                            true)
            case MessageType.SoapMock:
                return rewriteEvent.withSoapMockPayload(xmlPayload,
                                                        connectorInfo,
                                                        attributes)
            case MessageType.SoapInvocation:
                return rewriteEvent.withSoapInvokePayload(xmlPayload,
                                                          connectorInfo,
                                                          attributes)
            default:
                throw new TestingFrameworkException("Unknown message type! ${messageType}")
        }
    }

    private static def getXmlAttributes(Integer httpStatus,
                                        ConnectorInfo connectorInfo,
                                        EventWrapper originalEvent) {
        if (!(connectorInfo instanceof HttpRequesterInfo)) {
            return originalEvent.message.attributes
        }
        connectorInfo.getHttpResponseAttributes(httpStatus ?: 200,
                                                'the reason')
    }
}
