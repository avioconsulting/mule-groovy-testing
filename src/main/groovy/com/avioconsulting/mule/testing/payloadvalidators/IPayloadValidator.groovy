package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

interface IPayloadValidator {
    boolean isPayloadTypeValidationRequired(Processor messageProcessor)

    boolean isContentTypeValidationRequired(Processor messageProcessor)

    void validateContentType(Event event,
                             List<String> validContentTypes)

    void validatePayloadType(Object payload)
}