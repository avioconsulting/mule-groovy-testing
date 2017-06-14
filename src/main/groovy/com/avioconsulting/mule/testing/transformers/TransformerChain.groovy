package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class TransformerChain implements MuleMessageTransformer {
    private final List<MuleMessageTransformer> transformers

    TransformerChain() {
        this.transformers = []
    }

    def prependTransformer(MuleMessageTransformer transformer) {
        transformers.add(0, transformer)
    }

    def addTransformer(MuleMessageTransformer transformer) {
        transformers.add(transformer)
    }

    MuleMessage transform(MuleMessage muleMessage) {
        transformers.inject(muleMessage) { MuleMessage output, transformer ->
            transformer.transform(output)
        }
    }
}
