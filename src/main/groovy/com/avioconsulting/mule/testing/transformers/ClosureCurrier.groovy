package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface ClosureCurrier<T extends ConnectorInfo> {
    boolean isOnlyArgumentToBeCurried(Closure closure)

    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         T messageProcessor)
}
