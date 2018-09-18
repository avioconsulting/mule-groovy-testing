package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

class StandardTransformer implements MuleMessageTransformer {
    private final OutputTransformer outputTransformer
    private final InputTransformer inputTransformer
    private final Closure closure
    private final ClosureCurrier closureCurrier

    StandardTransformer(Closure closure,
                        ClosureCurrier closureCurrier,
                        InputTransformer inputTransformer,
                        OutputTransformer outputTransformer) {
        this.closureCurrier = closureCurrier
        this.closure = closure
        this.inputTransformer = inputTransformer
        this.outputTransformer = outputTransformer
    }

    void transform(MockEventWrapper muleEvent,
                   ConnectorInfo connectorInfo) {
        // if they're only requesting optional curried values (e.g. HTTP requestor params)
        // then we don't want to call their closure with an input value
        def onlyCurriedArgument = closureCurrier.isOnlyArgumentToBeCurried(closure)
        def curried = closureCurrier.curryClosure(closure,
                                                  muleEvent,
                                                  messageProcessor)
        def input = inputTransformer.transformInput(muleEvent,
                                                    messageProcessor)
        def result = onlyCurriedArgument ? curried() : curried(input)
        outputTransformer.transformOutput(result,
                                          muleEvent)
    }
}
