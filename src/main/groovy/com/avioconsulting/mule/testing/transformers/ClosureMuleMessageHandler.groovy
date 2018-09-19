package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

trait ClosureMuleMessageHandler {
    Closure withMuleEvent(Closure closure,
                          EventWrapper event) {
        closure.parameterTypes.last() == EventWrapper ? closure.rcurry(event) : closure
    }
}