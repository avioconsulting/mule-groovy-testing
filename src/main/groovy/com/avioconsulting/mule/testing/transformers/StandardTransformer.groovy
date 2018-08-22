package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class StandardTransformer implements MuleMessageTransformer {
    private final OutputTransformer outputTransformer
    private final InputTransformer inputTransformer
    private final Closure closure

    StandardTransformer(Closure closure,
                        InputTransformer inputTransformer,
                        OutputTransformer outputTransformer) {
        this.closure = closure
        this.inputTransformer = inputTransformer
        this.outputTransformer = outputTransformer
    }

    MuleEvent transform(MuleEvent muleEvent,
                        MessageProcessor messageProcessor) {
        def input = inputTransformer.transformInput(muleEvent,
                                                    messageProcessor)
        def result = closure(input)
        outputTransformer.transformOutput(result,
                                          muleEvent)
    }
}
