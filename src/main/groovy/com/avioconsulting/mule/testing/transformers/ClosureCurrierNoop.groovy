package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class ClosureCurrierNoop implements ClosureCurrier<Processor> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        return false
    }

    @Override
    Closure curryClosure(Closure closure,
                         CoreEvent muleEvent,
                         Processor messageProcessor) {
        closure
    }
}
