package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class ClosureCurrierNoop implements ClosureCurrier<MessageProcessor> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        return false
    }

    @Override
    Closure curryClosure(Closure closure,
                         MuleEvent muleEvent,
                         MessageProcessor messageProcessor) {
        closure
    }
}
