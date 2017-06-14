package com.avioconsulting.mule.testing.payload_types

import org.mule.api.MuleMessage

class ContentTypeCheckDisabledValidator implements IPayloadValidator, PayloadHelper {
    private final IPayloadValidator parentValidator

    ContentTypeCheckDisabledValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired() {
        parentValidator.payloadTypeValidationRequired
    }

    boolean isContentTypeValidationRequired() {
        false
    }

    void validateContentType(MuleMessage message, String expectedContentType) {
        parentValidator.validateContentType(message, expectedContentType)
    }

    void validatePayloadType(Object payload) {
        parentValidator.validatePayloadType(payload)
    }
}
