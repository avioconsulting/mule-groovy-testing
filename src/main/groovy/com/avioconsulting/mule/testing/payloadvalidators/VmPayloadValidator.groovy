package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class VmPayloadValidator implements
        IPayloadValidator<ConnectorInfo>,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(ConnectorInfo vm) {
        true
    }

    boolean isContentTypeValidationRequired(ConnectorInfo vm) {
        return false
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        // practically required a string in Mule 3.x, 4.1 should not have that restriction
    }
}
