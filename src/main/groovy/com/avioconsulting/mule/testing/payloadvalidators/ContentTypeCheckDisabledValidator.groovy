package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class ContentTypeCheckDisabledValidator implements IPayloadValidator,
        PayloadHelper {
    private final IPayloadValidator parentValidator

    ContentTypeCheckDisabledValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        parentValidator.isPayloadTypeValidationRequired(messageProcessor)
    }

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        false
    }

    void validateContentType(MuleEvent event, List<String> validContentTypes) {
        parentValidator.validateContentType(event,
                                            validContentTypes)
    }

    void validatePayloadType(Object payload) {
        parentValidator.validatePayloadType(payload)
    }
}
