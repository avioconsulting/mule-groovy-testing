package com.avioconsulting.mule.testing.payload_types

import org.mule.api.MuleMessage

class SOAPPayloadValidator implements IPayloadValidator, PayloadHelper {
    boolean isPayloadTypeValidationRequired() {
        true
    }

    boolean isContentTypeValidationRequired() {
        // ws-consumer sets this on its own
        return false
    }

    void validateContentType(MuleMessage message, String expectedContentType) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            'Check your WS-Consumer mock!')
    }
}
