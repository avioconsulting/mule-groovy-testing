package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class HttpRequestPayloadValidator implements
        IPayloadValidator<HttpRequesterInfo>,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(HttpRequesterInfo messageProcessor) {
        assert false: 'nie'
        //assert messageProcessor instanceof DefaultHttpRequester
        // GET should not require a payload at all
        messageProcessor.method != 'GET'
    }

    boolean isContentTypeValidationRequired(HttpRequesterInfo messageProcessor) {
        return true
    }

    void validateContentType(EventWrapper event,
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
