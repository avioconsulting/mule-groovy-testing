package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface IPayloadValidator<T extends ConnectorInfo> {
    boolean isPayloadTypeValidationRequired(T messageProcessor)

    boolean isContentTypeValidationRequired(T messageProcessor)

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes)

    void validatePayloadType(Object payload)
}