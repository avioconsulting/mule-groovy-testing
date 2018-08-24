package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

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

    MuleEvent transform(MuleEvent muleEvent,
                        MessageProcessor messageProcessor) {
        if (!triggerConnectException && !triggerTimeoutException) {
            return muleEvent
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
