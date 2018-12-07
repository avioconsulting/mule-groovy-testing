package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class ApiTransformer implements MuleMessageTransformer<ConnectorInfo> {
    private final Closure closure

    ApiTransformer(Closure closure) {
        this.closure = closure
    }

    @Override
    EventWrapper transform(EventWrapper event,
                           ConnectorInfo connectorInfo) {
        return null
    }
}
