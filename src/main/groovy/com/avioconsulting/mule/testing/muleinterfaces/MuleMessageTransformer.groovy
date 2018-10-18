package com.avioconsulting.mule.testing.muleinterfaces

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

interface MuleMessageTransformer<T extends ConnectorInfo> {
    EventWrapper transform(EventWrapper var1,
                           T connectorInfo)
}