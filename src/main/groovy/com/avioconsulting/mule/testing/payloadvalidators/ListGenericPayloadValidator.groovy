package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class ListGenericPayloadValidator<T extends ConnectorInfo> implements
        IPayloadValidator<T> {
    private final Class listGenericType
    private final String expectedTypeLabel

    ListGenericPayloadValidator(Class listGenericType) {
        this.listGenericType = listGenericType
        this.expectedTypeLabel = "List<${listGenericType.name}>"
    }

    boolean isPayloadTypeValidationRequired(T messageProcessor) {
        return true
    }

    boolean isContentTypeValidationRequired(T messageProcessor) {
        return false
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes) {
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
