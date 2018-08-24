package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpListenerPayloadValidator implements IPayloadValidator,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        if (messageProcessor instanceof DefaultHttpRequester) {
            // GET should not require a payload at all
            messageProcessor.method != 'GET'
        } else {
            true
        }
    }

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        return true
    }

    void validateContentType(MuleEvent event,
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
