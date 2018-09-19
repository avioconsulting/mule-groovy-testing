package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

trait PayloadHelper {
    void validatePayloadType(Object payload,
                             List<Class> allowedPayloadTypes,
                             String context) {
        assert payload.class.name == 'org.mule.runtime.api.metadata.TypedValue'
        def actualPayload = payload.value
        def validType = allowedPayloadTypes.find { type ->
            type.isInstance(actualPayload)
        }
        if (!validType) {
            throw new Exception(
                    "Expected payload to be of type ${allowedPayloadTypes} here but it actually was ${actualPayload.class}. ${context}")
        }
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes,
                             String context) {
        def actualContentType = event.message.getOutboundProperty('Content-Type') as String
        if (!validContentTypes.contains(actualContentType)) {
            validContentTypes = validContentTypes.collect { type ->
                // Clarify 'not set' case in the error message
                type == null ? '(not set)' : type
            }
            throw new Exception(
                    "Expected Content-Type to be of type ${validContentTypes} but it actually was ${actualContentType}. ${context}")
        }
    }
}