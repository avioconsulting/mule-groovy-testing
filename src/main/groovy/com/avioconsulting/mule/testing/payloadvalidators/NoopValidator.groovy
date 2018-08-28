package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class NoopValidator implements IPayloadValidator {
    @Override
    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        return false
    }

    @Override
    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        return false
    }

    @Override
    void validateContentType(MuleEvent event, List<String> validContentTypes) {
    }

    @Override
    void validatePayloadType(Object payload) {
    }
}
