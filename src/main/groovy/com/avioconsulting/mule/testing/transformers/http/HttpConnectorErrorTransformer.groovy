package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

import java.util.concurrent.TimeoutException

class HttpConnectorErrorTransformer implements IHaveStateToReset, MuleMessageTransformer {
    private boolean triggerConnectException
    private boolean triggerTimeoutException

    HttpConnectorErrorTransformer() {
        reset()
    }

    def triggerConnectException() {
        this.triggerConnectException = true
    }

    def triggerTimeoutException() {
        this.triggerTimeoutException = true
    }

    void transform(MockEventWrapper muleEvent,
                   ConnectorInfo connectorInfo) {
        if (!triggerConnectException && !triggerTimeoutException) {
            return
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
