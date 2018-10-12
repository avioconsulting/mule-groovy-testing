package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.IFetchAppClassLoader
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.SoapConsumerInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class WsConsumerConnectorErrorTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<SoapConsumerInfo> {
    private boolean triggerConnectException
    private boolean triggerTimeoutException
    private final IFetchAppClassLoader fetchAppClassLoader

    WsConsumerConnectorErrorTransformer(IFetchAppClassLoader fetchAppClassLoader) {
        this.fetchAppClassLoader = fetchAppClassLoader
        reset()
    }

    def triggerConnectException() {
        this.triggerConnectException = true
    }

    def triggerTimeoutException() {
        this.triggerTimeoutException = true
    }


    @Override
    EventWrapper transform(EventWrapper event,
                           SoapConsumerInfo connectorInfo) {
        // TODO: Follow the HttpConnectorErrorTransformer pattern
        return null
    }

    @Override
    def reset() {
        this.triggerConnectException = false
        this.triggerTimeoutException = false
    }
}
