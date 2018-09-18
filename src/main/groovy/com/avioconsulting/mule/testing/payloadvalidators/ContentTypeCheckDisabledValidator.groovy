package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class ContentTypeCheckDisabledValidator<T extends ConnectorInfo> implements
        IPayloadValidator<T>,
        PayloadHelper {
    private final IPayloadValidator<T> parentValidator

    ContentTypeCheckDisabledValidator(IPayloadValidator<T> parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired(T messageProcessor) {
        parentValidator.isPayloadTypeValidationRequired(messageProcessor)
    }

    boolean isContentTypeValidationRequired(T messageProcessor) {
        false
    }

    void validateContentType(EventWrapper event, List<String> validContentTypes) {
        parentValidator.validateContentType(event,
                                            validContentTypes)
    }

    void validatePayloadType(Object payload) {
        parentValidator.validatePayloadType(payload)
    }
}
