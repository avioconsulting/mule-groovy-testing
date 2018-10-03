package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface EventWrapper {
    MessageWrapper getMessage()

    String getMessageAsString()

    EventWrapper withVariable(String variableName,
                              Object value)
}
