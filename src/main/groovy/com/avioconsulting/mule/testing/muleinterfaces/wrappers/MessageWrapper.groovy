package com.avioconsulting.mule.testing.muleinterfaces.wrappers

interface MessageWrapper {
    Object getPayload()

    Class getDataTypeClass()

    Object getValueInsideTypedValue()

    String getMessageAsString()

    String getMimeType()
}
