package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.SoapConsumerInfo

class SOAPPayloadValidator implements
        IPayloadValidator<SoapConsumerInfo>,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(SoapConsumerInfo messageProcessor) {
        true
    }

    boolean isContentTypeValidationRequired(SoapConsumerInfo messageProcessor) {
        // ws-consumer sets this on its own
        return false
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [InputStream],
                            'Check your WS-Consumer mock!')
    }
}
