package com.avioconsulting.mule.testing.payload_types

import org.mule.api.MuleMessage

class VmPayloadValidator implements IPayloadValidator,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired() {
        true
    }

    boolean isContentTypeValidationRequired() {
        return false
    }

    void validateContentType(MuleMessage message, String expectedContentType) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'Check your VM mock!')
    }
}
