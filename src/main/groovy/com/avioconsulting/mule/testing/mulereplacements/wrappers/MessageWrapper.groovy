package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface MessageWrapper {
    Object getPayload()
    Object getValueInsideTypedValue()
    String getMessageAsString()
}
