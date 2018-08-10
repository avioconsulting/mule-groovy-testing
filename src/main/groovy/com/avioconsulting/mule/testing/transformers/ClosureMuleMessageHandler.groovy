package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage

trait ClosureMuleMessageHandler {
    Closure withMuleMessage(Closure closure, MuleMessage message) {
        closure.parameterTypes.last() == MuleMessage ? closure.rcurry(message) : closure
    }
}