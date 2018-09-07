package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ProcessorWrapper

class ClosureCurrierNoop implements ClosureCurrier<ProcessorWrapper> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        return false
    }

    @Override
    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         ProcessorWrapper messageProcessor) {
        closure
    }
}
