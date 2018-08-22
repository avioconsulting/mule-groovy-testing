package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

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

    MuleEvent transform(MuleEvent muleMessage,
                        MessageProcessor originalProcessor) {
        // needs to happen before inject because at that point transformers are actually running
        transformers.each { transformer ->
            if (transformer instanceof IHaveStateToReset) {
                transformer.reset()
            }
        }
        transformers.inject(muleMessage) { MuleEvent output, transformer ->
            transformer.transform(output)
        }
    }
}
