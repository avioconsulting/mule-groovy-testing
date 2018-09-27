package com.avioconsulting.mule.testing.payloadvalidators

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class ListenerPayloadValidator implements
        IPayloadValidator<ConnectorInfo>,
        PayloadHelper {

    boolean isPayloadTypeValidationRequired(ConnectorInfo messageProcessor) {
        if (messageProcessor instanceof HttpRequesterInfo) {
            // GET should not require a payload at all
            messageProcessor.method != 'GET'
        } else {
            true
        }
    }

    boolean isContentTypeValidationRequired(ConnectorInfo messageProcessor) {
        return true
    }

    void validateContentType(EventWrapper event,
                             List<String> validContentTypes) {
        validateContentType(event,
                            validContentTypes,
                            "This happened while calling your flow. Ensure your flow's DataWeaves or set-payloads set the mimeType you expect.")
    }

    void validatePayloadType(Object payload) {
        assert payload.class.name == 'org.mule.runtime.api.metadata.TypedValue'
        def dataType = payload.dataType
        if (dataType.isStreamType()) {
            throw new Exception("Expected a stream type for ${dataType}!")
        }
    }
}
