package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

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
        def messageProps = getXmlAttributes(httpStatus,
                                            runtimeBridgeTestSide)
        switch (messageType) {
            case MessageType.Mule41Stream:
                return rewriteEvent.withNewStreamingPayload(xmlPayload,
                                                            XML_MEDIA_TYPE,
                                                            messageProps,
                                                            connectorInfo,
                                                            true)
            case MessageType.SoapMock:
                return rewriteEvent.withSoapMockPayload(xmlPayload,
                                                        connectorInfo,
                                                        messageProps)
            case MessageType.SoapInvocation:
                return rewriteEvent.withSoapInvokePayload(xmlPayload,
                                                          connectorInfo,
                                                          messageProps)
            default:
                throw new TestingFrameworkException("Unknown message type! ${messageType}")
        }
    }

    private def getXmlAttributes(Integer httpStatus,
                                 RuntimeBridgeTestSide runtimeBridgeTestSide) {
        // need some of these props for SOAP mock to work properly
        def status = httpStatus ?: 200
        getHttpResponseAttributes(status,
                                  'the reason',
                                  runtimeBridgeTestSide)
    }
}
