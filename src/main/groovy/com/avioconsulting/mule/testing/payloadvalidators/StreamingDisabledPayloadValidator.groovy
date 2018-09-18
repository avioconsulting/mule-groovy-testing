package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class StreamingDisabledPayloadValidator<T extends ConnectorInfo> implements
        IPayloadValidator<T>,
        PayloadHelper {
    private final IPayloadValidator parentValidator

    StreamingDisabledPayloadValidator(IPayloadValidator parentValidator) {
        this.parentValidator = parentValidator
    }

    boolean isPayloadTypeValidationRequired(T connectorInfo) {
        parentValidator.isPayloadTypeValidationRequired(connectorInfo)
    }

    boolean isContentTypeValidationRequired(T connectorInfo) {
        parentValidator.isContentTypeValidationRequired(connectorInfo)
    }

    void validateContentType(EventWrapper muleEvent,
                             List<String> validContentTypes) {
        parentValidator.validateContentType(muleEvent,
                                            validContentTypes)
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'Check your mock endpoints!')
    }
}
