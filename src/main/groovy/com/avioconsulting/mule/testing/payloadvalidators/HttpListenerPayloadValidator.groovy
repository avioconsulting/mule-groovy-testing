package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class HttpListenerPayloadValidator implements
        IPayloadValidator<HttpRequesterInfo>,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(HttpRequesterInfo messageProcessor) {
        assert false: 'DefaultHttpRequester'
        if (messageProcessor instanceof Object) {
            // GET should not require a payload at all
            messageProcessor.method != 'GET'
        } else {
            true
        }
    }

    boolean isContentTypeValidationRequired(HttpRequesterInfo messageProcessor) {
        return true
    }

    void validateContentType(EventWrapper event,
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
