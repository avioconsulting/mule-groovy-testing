package com.avioconsulting.mule.testing.payload_types

import org.mule.api.MuleMessage

class StreamingDisabledPayloadValidator implements IPayloadValidator, PayloadHelper {
    private final IPayloadValidator parentValidator

    StreamingDisabledPayloadValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired() {
        parentValidator.payloadTypeValidationRequired
    }

    boolean isContentTypeValidationRequired() {
        parentValidator.contentTypeValidationRequired
    }

    void validateContentType(MuleMessage message, String expectedContentType) {
        parentValidator.validateContentType(message, expectedContentType)
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'Check your mock endpoints!')
    }
}
