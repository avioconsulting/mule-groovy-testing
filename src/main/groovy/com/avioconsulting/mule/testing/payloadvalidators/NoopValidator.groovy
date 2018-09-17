package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

class NoopValidator implements IPayloadValidator {
    @Override
    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        return false
    }

    @Override
    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        return false
    }

    @Override
    void validateContentType(Event event, List<String> validContentTypes) {
    }

    @Override
    void validatePayloadType(Object payload) {
    }
}
