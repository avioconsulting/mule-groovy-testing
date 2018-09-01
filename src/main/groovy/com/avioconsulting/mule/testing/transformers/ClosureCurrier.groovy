package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

interface ClosureCurrier<T extends Processor> {
    boolean isOnlyArgumentToBeCurried(Closure closure)

    Closure curryClosure(Closure closure,
                         CoreEvent muleEvent,
                         T messageProcessor)
}
