package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class StandardTransformer implements MuleMessageTransformer {
    private final OutputTransformer outputTransformer
    private final InputTransformer inputTransformer
    private final Closure closure
    private final ClosureCurrier closureCurrier
    private final ClosureCurrier eventCurrier

    StandardTransformer(Closure closure,
                        ClosureCurrier closureCurrier,
                        InputTransformer inputTransformer,
                        OutputTransformer outputTransformer) {
        this.closureCurrier = closureCurrier
        this.closure = closure
        this.inputTransformer = inputTransformer
        this.outputTransformer = outputTransformer
        this.eventCurrier = new ClosureCurrierEvent()
    }

    MuleEvent transform(MuleEvent muleEvent,
                        MessageProcessor messageProcessor) {
        def curried = closureCurrier.curryClosure(closure,
                                                  muleEvent,
                                                  messageProcessor)
        curried = this.eventCurrier.curryClosure(curried,
                                                 muleEvent,
                                                 messageProcessor)
        def input = inputTransformer.transformInput(muleEvent,
                                                    messageProcessor)
        def result = invokeClosure(curried,
                                   input)
        outputTransformer.transformOutput(result,
                                          muleEvent)
    }

    private def invokeClosure(Closure closure,
                              input) {
        def types = closure.parameterTypes
        def paramTypesSize = types.size()
        if (paramTypesSize == 0) {
            return closure()
        }
        if (paramTypesSize == 1) {
            return closure(input)
        }
        throw new RuntimeException("Expected a closure with only 1 remaining, non curried argument, but saw ${types}")
    }
}
