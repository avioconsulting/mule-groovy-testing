package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

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

    MuleMessage transform(MuleMessage muleMessage) {
        def input = inputTransformer.transformInput(muleMessage)
        def result = closure(input)
        outputTransformer.transformOutput(result)
    }
}
