package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface EventWrapper {
    MessageWrapper getMessage()

    String getMessageAsString()

    EventWrapper withVariable(String variableName,
                              Object value)

    EventWrapper withNewAttributes(Map attributes)

    EventWrapper createNewEventFromOld(Object payload,
                                       String mediaType)

    // TODO: Consistent naming
    EventWrapper newStreamedEvent(String payload,
                                  String mediaType,
                                  Map attributes)
}
