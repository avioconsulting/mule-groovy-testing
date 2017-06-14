package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleMessage

interface IPayloadValidator {
    boolean isPayloadTypeValidationRequired()

    boolean isContentTypeValidationRequired()

    void validateContentType(MuleMessage message,
                             String expectedContentType)

    void validatePayloadType(Object payload)
}