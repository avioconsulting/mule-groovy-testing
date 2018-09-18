package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface ClosureCurrier<T extends ProcessorWrapper> {
    boolean isOnlyArgumentToBeCurried(Closure closure)

    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         T messageProcessor)
}
