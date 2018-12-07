package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class MuleToHttpTransformer implements MuleMessageTransformer<HttpRequesterInfo> {
    private final List<HttpTransformer> transformers = []

    def prependTransformer(HttpTransformer transformer) {
        transformers.add(0,
                         transformer)
    }

    def addTransformer(HttpTransformer transformer) {
        transformers.add(transformer)
    }

    EventWrapper transform(EventWrapper event,
                           HttpRequesterInfo connectorInfo) {
        // needs to happen before inject because at that point transformers are actually running
        transformers.each { transformer ->
            if (transformer instanceof IHaveStateToReset) {
                transformer.reset()
            }
        }
        def result = transformers.inject(event) { EventWrapper output,
                                                  transformer ->
            transformer.transform(output,
                                  connectorInfo)
        }
        result
    }
}
