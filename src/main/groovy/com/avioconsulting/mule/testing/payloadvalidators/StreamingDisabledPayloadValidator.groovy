package com.avioconsulting.mule.testing.payloadvalidators

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

    void validateContentType(MuleMessage message, List<String> validContentTypes) {
        parentValidator.validateContentType(message, validContentTypes)
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'Check your mock endpoints!')
    }
}
