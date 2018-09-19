package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class ClosureCurrierNoop<T extends ConnectorInfo> implements
        ClosureCurrier<T> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        return false
    }

    @Override
    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         T connectorInfo) {
        closure
    }
}
