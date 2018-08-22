package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpListenerPayloadValidator implements IPayloadValidator,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(MessageProcessor httpRequester) {
        assert httpRequester instanceof DefaultHttpRequester
        // GET should not require a payload at all
        httpRequester.method != 'GET'
    }

    boolean isContentTypeValidationRequired(MessageProcessor httpRequester) {
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
