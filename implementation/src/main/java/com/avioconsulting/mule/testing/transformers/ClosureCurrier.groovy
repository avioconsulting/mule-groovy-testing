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
        def lastParameter = parameterTypes.any() ? parameterTypes.last() : null
        // we don't want to curry events or message processors into closure arguments w/ no type
        // because the test user probably hasn't intended to grab the event or connector info in the case
        if (!lastParameter?.equals(Object) && lastParameter?.isAssignableFrom(object.getClass())) {
            closure = closure.rcurry(object)
        }
        closure
    }
}
