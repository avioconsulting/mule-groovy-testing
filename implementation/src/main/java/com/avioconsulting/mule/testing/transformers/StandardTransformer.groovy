package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class StandardTransformer<T extends ConnectorInfo> implements
        MuleMessageTransformer<T> {
    private final OutputTransformer outputTransformer
    private final InputTransformer<T> inputTransformer
    private final Closure closure
    private final ClosureCurrier closureCurrier

    StandardTransformer(Closure closure,
                        ClosureCurrier closureCurrier,
                        InputTransformer<T> inputTransformer,
                        OutputTransformer outputTransformer) {
        this.closureCurrier = closureCurrier
        this.closure = closure
        this.inputTransformer = inputTransformer
        this.outputTransformer = outputTransformer
    }

    EventWrapper transform(EventWrapper muleEvent,
                           T connectorInfo) {
        // if they're only requesting optional curried values (e.g. HTTP requestor params)
        // then we don't want to call their closure with an input value
        def curried = closureCurrier.curryClosure(closure,
                                                  muleEvent,
                                                  connectorInfo)
        def input = inputTransformer.transformInput(muleEvent,
                                                    connectorInfo)
        // there might be a better way to do this but this will allow specific connectors
        // to handle stuff inside "whenCalledWith" like error triggering, etc.
        def closureHandler = connectorInfo.closureEvaluator(muleEvent)
        curried = curried.rehydrate(closureHandler,
                                    curried.owner,
                                    curried.thisObject)
        def result = curried.parameterTypes.size() == 0 ? curried() : curried(input)
        outputTransformer.transformOutput(result,
                                          muleEvent,
                                          connectorInfo)
    }
}
