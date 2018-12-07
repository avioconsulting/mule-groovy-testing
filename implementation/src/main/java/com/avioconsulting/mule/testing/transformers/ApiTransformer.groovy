package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ReturnWrapper

class ApiTransformer implements MuleMessageTransformer<ConnectorInfo> {
    private final Closure closure

    ApiTransformer(Closure closure) {
        this.closure = closure
    }

    @Override
    EventWrapper transform(EventWrapper event,
                           ConnectorInfo connectorInfo) {
        def closureResponse = closure(connectorInfo.parameters)
        String mediaType = null
        Object payload = null
        if (closureResponse instanceof ReturnWrapper) {
            payload = closureResponse.payload
            mediaType = closureResponse.mediaType
        } else {
            payload = closureResponse
        }
        event.withNewPayload(payload,
                             mediaType)
    }
}
