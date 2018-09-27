package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class TransformerChain<T extends ConnectorInfo> implements
        MuleMessageTransformer<T> {
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

    EventWrapper transform(EventWrapper muleMessage,
                           T connectorInfo) {
        // needs to happen before inject because at that point transformers are actually running
        transformers.each { transformer ->
            if (transformer instanceof IHaveStateToReset) {
                transformer.reset()
            }
        }
        transformers.inject(muleMessage) { EventWrapper output, transformer ->
            def transformerResult = transformer.transform(output,
                                                          connectorInfo)
            // TODO: Remove this once everything is working
            assert transformerResult: "MockEventWrapper/EventWrapper in ${transformer} ??"
            transformerResult
        }
    }
}
