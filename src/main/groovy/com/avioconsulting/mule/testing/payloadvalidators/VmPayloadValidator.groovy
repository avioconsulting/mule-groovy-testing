package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.VmInfo

class VmPayloadValidator implements
        IPayloadValidator<VmInfo>,
        PayloadHelper {
    boolean isPayloadTypeValidationRequired(VmInfo vm) {
        true
    }

    boolean isContentTypeValidationRequired(VmInfo vm) {
        return false
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes) {
    }

    void validatePayloadType(Object payload) {
        validatePayloadType(payload,
                            [String],
                            'VMs must have string payloads.')
    }
}
