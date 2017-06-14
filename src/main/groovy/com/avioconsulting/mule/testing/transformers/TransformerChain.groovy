package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.MessageProcessorMocker

class TransformerChain implements MuleMessageTransformer {
    private final List<MuleMessageTransformer> transformers

    TransformerChain() {
        this.transformers = []
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
