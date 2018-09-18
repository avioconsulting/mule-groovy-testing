package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class NoopValidator<T extends ConnectorInfo> implements
        IPayloadValidator<T> {
    @Override
    boolean isPayloadTypeValidationRequired(T messageProcessor) {
        return false
    }

    @Override
    boolean isContentTypeValidationRequired(T messageProcessor) {
        return false
    }

    @Override
    void validateContentType(EventWrapper event, List<String> validContentTypes) {
    }

    @Override
    void validatePayloadType(Object payload) {
    }
}
