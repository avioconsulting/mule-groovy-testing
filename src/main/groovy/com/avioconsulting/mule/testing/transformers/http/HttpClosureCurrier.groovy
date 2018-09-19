package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier

class HttpClosureCurrier implements
        ClosureCurrier<HttpRequesterInfo> {
    @Override
    boolean isOnlyArgumentToBeCurried(Closure closure) {
        closure.parameterTypes.size() == 1 && shouldCurry(closure)
    }

    @Override
    Closure curryClosure(Closure closure,
                         EventWrapper muleEvent,
                         HttpRequesterInfo connectorInfo) {
        if (shouldCurry(closure)) {
            return closure.rcurry(connectorInfo)
        }
        return closure
    }

    private boolean shouldCurry(Closure closure) {
        closure.parameterTypes.last() == HttpRequesterInfo
    }
}
