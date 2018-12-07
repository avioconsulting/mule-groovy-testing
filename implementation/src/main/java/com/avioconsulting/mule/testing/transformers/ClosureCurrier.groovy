package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class ClosureCurrier<T extends ConnectorInfo> {
    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         T messageProcessor) {
        closure = doCurry(closure,
                          muleEvent)
        doCurry(closure,
                messageProcessor)
    }

    private static Closure doCurry(Closure closure,
                                   Object object) {
        def parameterTypes = closure.parameterTypes
        if (parameterTypes.any() && parameterTypes.last().isAssignableFrom(object.getClass())) {
            closure = closure.rcurry(object)
        }
        closure
    }
}
