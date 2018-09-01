package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class SOAPPayloadValidator implements IPayloadValidator,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        true
    }

    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        // ws-consumer sets this on its own
        return false
    }

    void validateContentType(CoreEvent event,
                             List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            'Check your WS-Consumer mock!')
    }
}
