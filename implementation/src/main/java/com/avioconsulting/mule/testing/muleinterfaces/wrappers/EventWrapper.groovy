package com.avioconsulting.mule.testing.muleinterfaces.wrappers

interface EventWrapper {
    MessageWrapper getMessage()

    String getMessageAsString()

    Object getVariable(String variableName)

    EventWrapper withVariable(String variableName,
                              Object value)

    EventWrapper withVariable(String variableName,
                              Object value,
                              String mediaType)

    EventWrapper withNewAttributes(attributes)

    EventWrapper withNewPayload(Object payload,
                                ConnectorInfo connectorInfo,
                                String mediaType)

    EventWrapper withSoapMockPayload(String xmlPayload,
                                     ConnectorInfo connectorInfo,
                                     Map attributes)

    EventWrapper withSoapInvokePayload(String xmlPayload,
                                       ConnectorInfo connectorInfo,
                                       Map attributes)

    EventWrapper withNewPayload(Object payload,
                                String mediaType,
                                ConnectorInfo connectorInfo,
                                Map attributes)

    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Map attributes,
                                         ConnectorInfo connectorInfo,
                                         boolean useRepeatableStream)

    String getCorrelationId()
}
