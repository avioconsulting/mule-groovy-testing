package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.ResponseValidator

class HttpRequestPayloadValidator implements IPayloadValidator,
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
                            'Check your mock endpoints.')
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream, String],
                            'Check your mock endpoints.')
    }

    def receive(Map queryParams,
                Map headers,
                String fullPath,
                String httpVerb,ResponseValidator responseValidator) {
        this.httpVerb = httpVerb
    }
}
