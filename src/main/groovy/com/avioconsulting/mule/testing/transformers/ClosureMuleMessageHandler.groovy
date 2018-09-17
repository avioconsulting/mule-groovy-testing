package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.api.event.Event

trait ClosureMuleMessageHandler {
    Closure withMuleEvent(Closure closure,
                          Event event) {
        closure.parameterTypes.last() == Event ? closure.rcurry(event) : closure
    }
}