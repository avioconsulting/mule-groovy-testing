package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

class HttpListenerPayloadValidator implements IPayloadValidator,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        assert false : 'DefaultHttpRequester'
        if (messageProcessor instanceof Object) {
            // GET should not require a payload at all
            messageProcessor.method != 'GET'
        } else {
            true
        }
    }

    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        return true
    }

    void validateContentType(Event event,
                             List<String> validContentTypes) {
        validateContentType(event,
                            validContentTypes,
                            "This happened while calling your flow. Add a set-property before the end of the flow.")
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            "This happened while calling your flow. Check your input payload.")
    }
}
