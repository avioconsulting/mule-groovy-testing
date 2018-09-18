package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

// TODO: Collapse this and MockProcess??
trait MuleMessageTransformer implements MockProcess<ConnectorInfo> {
    abstract void transform(MockEventWrapper var1,
                            ConnectorInfo connectorInfo)

    void process(MockEventWrapper event,
                 ConnectorInfo connectorInfo) {
        transform(event, connectorInfo)
    }
}