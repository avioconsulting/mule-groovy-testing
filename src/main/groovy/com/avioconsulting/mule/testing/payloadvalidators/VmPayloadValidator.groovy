package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class VmPayloadValidator implements IPayloadValidator,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(Processor vm) {
        true
    }

    boolean isContentTypeValidationRequired(Processor vm) {
        return false
    }

    void validateContentType(CoreEvent event, List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'VMs must have string payloads.')
    }
}
