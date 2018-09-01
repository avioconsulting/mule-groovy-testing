package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class ContentTypeCheckDisabledValidator implements IPayloadValidator,
        PayloadHelper {
    private final IPayloadValidator parentValidator

    ContentTypeCheckDisabledValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        parentValidator.isPayloadTypeValidationRequired(messageProcessor)
    }

    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        false
    }

    void validateContentType(CoreEvent event, List<String> validContentTypes) {
        parentValidator.validateContentType(event,
                                            validContentTypes)
    }

    void validatePayloadType(Object payload) {
        parentValidator.validatePayloadType(payload)
    }
}
