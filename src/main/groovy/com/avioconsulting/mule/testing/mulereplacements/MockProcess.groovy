package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

interface MockProcess<T extends ConnectorInfo> {
    void process(MockEventWrapper event,
                 T connectorInfo)
}
