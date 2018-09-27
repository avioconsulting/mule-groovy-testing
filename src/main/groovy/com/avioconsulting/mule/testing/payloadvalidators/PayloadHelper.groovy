package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapperImpl

trait PayloadHelper {
    void validatePayloadType(Object payload,
                             List<Class> allowedPayloadTypes,
                             String context,
                             boolean streamIsAllowed = false) {
        if (streamIsAllowed && MessageWrapperImpl.isPayloadStreaming(payload)) {
            return
        }
        def actualPayload = MessageWrapperImpl.unwrapTypedValue(payload)
        if (streamIsAllowed && actualPayload.class.name.contains('ManagedCursorStreamProvider')) {
            // for some reason, the flag isPayloadStreaming looks at returns false
            return
        }
        def validType = allowedPayloadTypes.find { type ->
            type.isInstance(actualPayload)
        }
        if (!validType) {
            def payloadTypesText = allowedPayloadTypes.collect { klass ->
                klass.name
            }
            if (streamIsAllowed) {
                payloadTypesText << '(Stream)'
            }
            throw new Exception(
                    "Expected payload to be of type ${payloadTypesText} here but it actually was ${actualPayload.class}. ${context}")
        }
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes,
                             String context) {
        // in Mule 4.x, the dataType/mimeType on the payload seems to function as the content type
        def actualContentType = event.message.mimeType
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