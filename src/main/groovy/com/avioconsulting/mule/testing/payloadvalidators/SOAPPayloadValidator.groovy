package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class SOAPPayloadValidator implements IPayloadValidator,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        true
    }

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        // ws-consumer sets this on its own
        return false
    }

    void validateContentType(MuleEvent event,
                             List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            'Check your WS-Consumer mock!')
    }
}
