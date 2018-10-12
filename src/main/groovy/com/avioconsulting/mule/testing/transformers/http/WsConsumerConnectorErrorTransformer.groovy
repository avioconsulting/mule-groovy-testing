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
    EventWrapper transform(EventWrapper muleEvent,
                           SoapConsumerInfo connectorInfo) {
        if (!triggerConnectException && !triggerTimeoutException) {
            return muleEvent
        }
        if (triggerConnectException) {
            def exceptionKlass = fetchAppClassLoader.appClassloader.loadClass('org.mule.runtime.soap.api.exception.DispatchingException')
            def exception = exceptionKlass.newInstance('An error occurred while sending the SOAP request')
            throw exception
        }
    }

    @Override
    def reset() {
        this.triggerConnectException = false
        this.triggerTimeoutException = false
    }
}
