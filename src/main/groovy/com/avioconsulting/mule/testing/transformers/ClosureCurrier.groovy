package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

interface ClosureCurrier<T extends ConnectorInfo> {
    boolean isOnlyArgumentToBeCurried(Closure closure)

    Closure curryClosure(Closure closure,
                         MockEventWrapper muleEvent,
                         T messageProcessor)
}
