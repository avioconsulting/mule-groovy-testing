package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class StreamingDisabledPayloadValidator implements IPayloadValidator, PayloadHelper {
    private final IPayloadValidator parentValidator

    StreamingDisabledPayloadValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        parentValidator.isPayloadTypeValidationRequired(messageProcessor)
    }

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        parentValidator.isContentTypeValidationRequired(messageProcessor)
    }

    void validateContentType(MuleEvent muleEvent, List<String> validContentTypes) {
        parentValidator.validateContentType(muleEvent,
                                            validContentTypes)
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'Check your mock endpoints!')
    }
}
