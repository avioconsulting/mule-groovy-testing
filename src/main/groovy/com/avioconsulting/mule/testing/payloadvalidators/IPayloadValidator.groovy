package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

interface IPayloadValidator {
    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor)

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor)

    void validateContentType(MuleEvent event,
                             List<String> validContentTypes)

    void validatePayloadType(Object payload)
}