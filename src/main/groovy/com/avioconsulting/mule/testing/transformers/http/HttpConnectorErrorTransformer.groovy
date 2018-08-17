package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.transformers.IHaveStateToReset
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

import java.util.concurrent.TimeoutException

class HttpConnectorErrorTransformer implements IHaveStateToReset, MuleMessageTransformer {
    private final MuleContext muleContext
    private boolean triggerConnectException
    private boolean triggerTimeoutException

    HttpConnectorErrorTransformer(MuleContext muleContext) {
        this.muleContext = muleContext
        reset()
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

    @Override
    def reset() {
        this.triggerConnectException = false
        this.triggerTimeoutException = false
    }
}
