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

    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Map attributes)
}
