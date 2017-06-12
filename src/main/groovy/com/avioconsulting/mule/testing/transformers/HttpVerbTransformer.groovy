package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class HttpVerbTransformer implements MuleMessageTransformer {
    private final HttpConnectorSpy spy
    private final Closure closure
    private final MuleContext muleContext

    HttpVerbTransformer(HttpConnectorSpy spy,
                        Closure closure,
                        MuleContext muleContext) {
        this.muleContext = muleContext
        this.closure = closure
        this.spy = spy
    }

    MuleMessage transform(MuleMessage muleMessage) {
        closure(spy.httpVerb)
        return muleMessage
    }
}
