package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

interface IPayloadValidator {
    boolean isPayloadTypeValidationRequired(Processor messageProcessor)

    boolean isContentTypeValidationRequired(Processor messageProcessor)

    void validateContentType(CoreEvent event,
                             List<String> validContentTypes)

    void validatePayloadType(Object payload)
}