package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class TransformerChain implements MuleMessageTransformer {
    private final List<MuleMessageTransformer> transformers

    TransformerChain() {
        this.transformers = []
    }

    TransformerChain(MuleMessageTransformer... transformers) {
        this.transformers = transformers
    }

    def prependTransformer(MuleMessageTransformer transformer) {
        transformers.add(0, transformer)
    }

    def addTransformer(MuleMessageTransformer transformer) {
        transformers.add(transformer)
    }

    CoreEvent transform(CoreEvent muleMessage,
                        Processor originalProcessor) {
        // needs to happen before inject because at that point transformers are actually running
        transformers.each { transformer ->
            if (transformer instanceof IHaveStateToReset) {
                transformer.reset()
            }
        }
        transformers.inject(muleMessage) { CoreEvent output, transformer ->
            transformer.transform(output,
                                  originalProcessor)
        }
    }
}
