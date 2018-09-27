package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface MessageWrapper {
    Object getPayload()
    Class getDataTypeClass()
    Object getValueInsideTypedValue()
    String getMessageAsString()
    String getMimeType()
}
