package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

class HttpRequestPayloadValidator implements IPayloadValidator,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        assert false : 'nie'
        //assert messageProcessor instanceof DefaultHttpRequester
        // GET should not require a payload at all
        messageProcessor.method != 'GET'
    }

    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        return true
    }

    void validateContentType(Event event,
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
