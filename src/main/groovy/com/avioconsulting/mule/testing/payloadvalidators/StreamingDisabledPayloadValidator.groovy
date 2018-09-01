package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class StreamingDisabledPayloadValidator implements IPayloadValidator, PayloadHelper {
    private final IPayloadValidator parentValidator

    StreamingDisabledPayloadValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        parentValidator.isPayloadTypeValidationRequired(messageProcessor)
    }

    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        parentValidator.isContentTypeValidationRequired(messageProcessor)
    }

    void validateContentType(CoreEvent muleEvent, List<String> validContentTypes) {
        parentValidator.validateContentType(muleEvent,
                                            validContentTypes)
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'Check your mock endpoints!')
    }
}
