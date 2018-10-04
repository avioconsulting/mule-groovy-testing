package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface EventWrapper {
    MessageWrapper getMessage()

    String getMessageAsString()

    EventWrapper withVariable(String variableName,
                              Object value)

    EventWrapper withNewAttributes(Map attributes)

    EventWrapper withNewPayload(Object payload,
                                String mediaType)

    EventWrapper withNewStreamingPayload(String payload,
                                         String mediaType,
                                         Map attributes)
}
