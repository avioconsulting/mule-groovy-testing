package com.avioconsulting.mule.testing.payloadvalidators

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

    void validateContentType(MuleMessage message, List<String> validContentTypes) {
        parentValidator.validateContentType(message, validContentTypes)
    }

    void validatePayloadType(Object payload) {
        parentValidator.validatePayloadType(payload)
    }
}
