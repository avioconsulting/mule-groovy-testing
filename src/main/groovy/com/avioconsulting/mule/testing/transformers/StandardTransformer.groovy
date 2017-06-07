package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class StandardTransformer implements MuleMessageTransformer {
    OutputTransformer outputTransformer
    InputTransformer inputTransformer
    Closure closure

    MuleMessage transform(MuleMessage muleMessage) {
        assert outputTransformer
        assert inputTransformer
        assert closure
        def input = inputTransformer.transformInput(muleMessage)
        def result = closure(input)
        outputTransformer.transformOutput(result)
    }
}
