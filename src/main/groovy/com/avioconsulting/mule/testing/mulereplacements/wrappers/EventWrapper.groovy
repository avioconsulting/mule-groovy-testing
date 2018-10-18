package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface EventWrapper {
    MessageWrapper getMessage()

    String getMessageAsString()

    Object getVariable(String variableName)

    EventWrapper withVariable(String variableName,
                              Object value)

    EventWrapper withNewAttributes(attributes)

    EventWrapper withNewPayload(Object payload,
                                String mediaType)

    EventWrapper withSoapPayload(String xmlPayload,
                                 Map attributes)

    EventWrapper withNewPayload(Object payload,
                                String mediaType,
                                Map attributes)

    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Map attributes,
                                         boolean useRepeatableStream)
}
