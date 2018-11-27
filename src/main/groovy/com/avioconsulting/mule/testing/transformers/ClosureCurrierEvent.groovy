package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class ClosureCurrierEvent<T extends MessageProcessor> implements ClosureCurrier<T> {
    @Override
    Closure curryClosure(Closure closure,
                         MuleEvent event,
                         T messageProcessor) {
        def types = closure.parameterTypes
        types.any() && types.last() == MuleEvent ? closure.rcurry(event) : closure
    }
}
