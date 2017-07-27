package com.avioconsulting.mule.testing.payloadvalidators

import org.mule.api.MuleMessage

class ListGenericPayloadValidator implements IPayloadValidator {
    private final Class listGenericType
    private final String expectedTypeLabel

    ListGenericPayloadValidator(Class listGenericType) {
        this.listGenericType = listGenericType
        this.expectedTypeLabel = "List<${listGenericType.name}>"
    }

    boolean isPayloadTypeValidationRequired() {
        return true
    }

    boolean isContentTypeValidationRequired() {
        return false
    }

    void validateContentType(MuleMessage message, List<String> validContentTypes) {
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
