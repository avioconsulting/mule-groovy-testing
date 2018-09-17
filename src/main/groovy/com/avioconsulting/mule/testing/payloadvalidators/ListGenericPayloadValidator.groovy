package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

class ListGenericPayloadValidator implements IPayloadValidator {
    private final Class listGenericType
    private final String expectedTypeLabel

    ListGenericPayloadValidator(Class listGenericType) {
        this.listGenericType = listGenericType
        this.expectedTypeLabel = "List<${listGenericType.name}>"
    }

    boolean isPayloadTypeValidationRequired(Processor messageProcessor) {
        return true
    }

    boolean isContentTypeValidationRequired(Processor messageProcessor) {
        return false
    }

    void validateContentType(Event event, List<String> validContentTypes) {
    }

    void validatePayloadType(Object result) {
        if (!(result instanceof List)) {
            throw new Exception(
                    "Must return a ${expectedTypeLabel} result from your mock instead of ${result} which is of type ${result.class}!")
        }
        if (result.empty) {
            // can't validate this
            return
        }
        def actualItem = result[0]
        if (!listGenericType.isInstance(actualItem)) {
            throw new Exception(
                    "Must return a ${expectedTypeLabel} result from your mock instead of List<${actualItem.class.name}>!")
        }
    }
}
