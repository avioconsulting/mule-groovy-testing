package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent

trait ClosureMuleMessageHandler {
    Closure withMuleEvent(Closure closure,
                          MuleEvent event) {
        closure.parameterTypes.last() == MuleEvent ? closure.rcurry(event) : closure
    }
}