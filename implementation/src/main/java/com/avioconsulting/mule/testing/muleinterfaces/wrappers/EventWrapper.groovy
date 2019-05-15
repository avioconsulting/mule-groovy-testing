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
                                     Object attributes)

    EventWrapper withSoapInvokePayload(String xmlPayload,
                                       ConnectorInfo connectorInfo,
                                       Object attributes)

    EventWrapper withNewPayload(Object payload,
                                String mediaType,
                                ConnectorInfo connectorInfo,
                                Object attributes)

    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Object attributes,
                                         ConnectorInfo connectorInfo,
                                         boolean useRepeatableStream)

    String getCorrelationId()
}
