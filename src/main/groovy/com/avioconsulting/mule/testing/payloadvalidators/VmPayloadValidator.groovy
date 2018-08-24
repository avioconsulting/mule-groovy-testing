package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class VmPayloadValidator implements IPayloadValidator,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(MessageProcessor vm) {
        true
    }

    boolean isContentTypeValidationRequired(MessageProcessor vm) {
        return false
    }

    void validateContentType(MuleEvent event, List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'VMs must have string payloads.')
    }
}
