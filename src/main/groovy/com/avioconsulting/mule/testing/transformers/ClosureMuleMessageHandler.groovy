package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.core.api.event.CoreEvent

trait ClosureMuleMessageHandler {
    Closure withMuleEvent(Closure closure,
                          CoreEvent event) {
        closure.parameterTypes.last() == CoreEvent ? closure.rcurry(event) : closure
    }
}