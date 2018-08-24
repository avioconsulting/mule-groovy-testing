package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class ListGenericPayloadValidator implements IPayloadValidator {
    private final Class listGenericType
    private final String expectedTypeLabel

    ListGenericPayloadValidator(Class listGenericType) {
        this.listGenericType = listGenericType
        this.expectedTypeLabel = "List<${listGenericType.name}>"
    }

    boolean isPayloadTypeValidationRequired(MessageProcessor messageProcessor) {
        return true
    }

    boolean isContentTypeValidationRequired(MessageProcessor messageProcessor) {
        return false
    }

    void validateContentType(MuleEvent event, List<String> validContentTypes) {
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
