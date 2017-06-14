package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleMessage

trait PayloadHelper {
    void validatePayloadType(Object payload,
                             List<Class> allowedPayloadTypes,
                             String context) {
        def validType = allowedPayloadTypes.find { type ->
            type.isInstance(payload)
        }
        if (!validType) {
            throw new Exception(
                    "Expected payload to be of type ${allowedPayloadTypes} here but it actually was ${payload.class}. ${context}")
        }
    }

    void validateContentType(MuleMessage message,
                             String expectedContentType,
                             String context) {
        def actualContentType = message.getOutboundProperty('Content-Type') as String
        if (actualContentType != expectedContentType) {
            throw new Exception(
                    "Expected Content-Type to be of type ${expectedContentType} but it actually was ${actualContentType}. ${context}")
        }
    }
}