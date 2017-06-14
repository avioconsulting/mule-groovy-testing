package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleMessage

interface IPayloadValidator {
    boolean isPayloadTypeValidationRequired()

    boolean isContentTypeValidationRequired()

    void validateContentType(MuleMessage message,
                             List<String> validContentTypes)

    void validatePayloadType(Object payload)
}