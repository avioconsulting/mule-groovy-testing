package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

interface ClosureCurrier<T extends MessageProcessor> {
    Closure curryClosure(Closure closure,
                         MuleEvent muleEvent,
                         T messageProcessor)
}
