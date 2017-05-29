package com.avioconsulting.muletesting.transformers

import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class SimpleClosureTransformer implements MuleMessageTransformer {
    private final Closure closure

    SimpleClosureTransformer(Closure closure) {
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        this.closure(muleMessage)
    }
}
