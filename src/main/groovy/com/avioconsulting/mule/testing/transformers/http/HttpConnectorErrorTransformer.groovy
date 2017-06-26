package com.avioconsulting.mule.testing.transformers.http

import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

import java.util.concurrent.TimeoutException

class HttpConnectorErrorTransformer implements MuleMessageTransformer {
    private final MuleContext muleContext
    private boolean triggerConnectException
    private boolean triggerTimeoutException

    HttpConnectorErrorTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    def triggerConnectException() {
        this.triggerConnectException = true
    }

    def triggerTimeoutException() {
        this.triggerTimeoutException = true
    }

    MuleMessage transform(MuleMessage muleMessage) {
        if (!triggerConnectException && !triggerTimeoutException) {
            return muleMessage
        }
        if (triggerConnectException) {
            throw new ConnectException('could not reach HTTP server')
        }
        if (triggerTimeoutException) {
            throw new TimeoutException('HTTP timeout!')
        }
    }
}
