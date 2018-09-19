package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface MuleMessageTransformer<T extends ConnectorInfo> {
    EventWrapper transform(EventWrapper var1,
                           T connectorInfo)
}