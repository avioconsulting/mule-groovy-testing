package com.avioconsulting.mule.testing.payload_types

import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.ResponseValidator

class HttpListenerPayloadValidator implements IPayloadValidator,
        IReceiveHttpOptions,
        PayloadHelper {
    private String httpVerb

    boolean isPayloadTypeValidationRequired() {
        // GET should not require a payload at all
        this.httpVerb != 'GET'
    }

    boolean isContentTypeValidationRequired() {
        return true
    }

    void validateContentType(MuleMessage message,
                             String expectedContentType) {
        validateContentType(message,
                            expectedContentType,
                            "This happened while calling your flow. Add a set-property before the end of the flow.")
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            "This happened while calling your flow. Check your input payload.")
    }

    def receive(Map queryParams, String fullPath, String httpVerb, ResponseValidator responseValidator) {
        this.httpVerb = httpVerb
    }
}
