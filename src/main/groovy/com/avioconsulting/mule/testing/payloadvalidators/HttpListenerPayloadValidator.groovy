package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.DefaultHttpRequester

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
                             List<String> validContentTypes) {
        validateContentType(message,
                            validContentTypes,
                            "This happened while calling your flow. Add a set-property before the end of the flow.")
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            "This happened while calling your flow. Check your input payload.")
    }

    def receive(Map queryParams,
                Map headers,
                String fullPath,
                DefaultHttpRequester httpRequester) {
        this.httpVerb = httpRequester.method
    }
}
