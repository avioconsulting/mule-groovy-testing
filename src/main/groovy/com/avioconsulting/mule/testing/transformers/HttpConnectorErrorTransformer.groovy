package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class HttpConnectorErrorTransformer implements MuleMessageTransformer {
    private final MuleContext muleContext
    private boolean triggerException

    HttpConnectorErrorTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def triggerException() {
        this.triggerException = true
    }

    MuleMessage transform(MuleMessage muleMessage) {
        if (!triggerException) {
            return muleMessage
        }
        throw new ConnectException('could not reach HTTP server')
    }
}
