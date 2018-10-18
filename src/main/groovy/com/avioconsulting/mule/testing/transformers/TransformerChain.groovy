package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

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

    EventWrapper transform(EventWrapper event,
                           T connectorInfo) {
        // needs to happen before inject because at that point transformers are actually running
        transformers.each { transformer ->
            if (transformer instanceof IHaveStateToReset) {
                transformer.reset()
            }
        }
        def result = transformers.inject(event) { EventWrapper output, transformer ->
            transformer.transform(output,
                                  connectorInfo)
        }
        result
    }
}
