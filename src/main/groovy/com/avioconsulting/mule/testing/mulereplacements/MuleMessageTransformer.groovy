package com.avioconsulting.mule.testing.mulereplacements

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

// TODO: Collapse this and MockProcess??
trait MuleMessageTransformer<T extends ConnectorInfo> implements MockProcess<T> {
    abstract void transform(MockEventWrapper var1,
                            T connectorInfo)

    void process(MockEventWrapper event,
                 T connectorInfo) {
        transform(event, connectorInfo)
    }
}