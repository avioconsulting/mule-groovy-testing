package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpRequestPayloadValidator implements IPayloadValidator,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        assert messageProcessor instanceof DefaultHttpRequester
        // GET should not require a payload at all
        messageProcessor.method != 'GET'
    }

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        return true
    }

    void validateContentType(MuleEvent event,
                             List<String> validContentTypes) {
        validateContentType(event,
                            validContentTypes,
                            'Check your mock endpoints.')
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream, String],
                            'Check your mock endpoints.')
    }
}
