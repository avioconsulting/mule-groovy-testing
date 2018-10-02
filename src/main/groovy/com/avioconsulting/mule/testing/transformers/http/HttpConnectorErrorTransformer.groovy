package com.avioconsulting.mule.testing.transformers.http


import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

import java.util.concurrent.TimeoutException

class HttpConnectorErrorTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<HttpRequesterInfo> {
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

    EventWrapper transform(EventWrapper muleEvent,
                           HttpRequesterInfo connectorInfo) {
        if (!triggerConnectException && !triggerTimeoutException) {
            return muleEvent
        }
        if (triggerConnectException) {
            def muleClassLoader = muleEvent.muleClassLoader
            def exceptionClass = muleClassLoader.loadClass('org.mule.extension.http.api.error.HttpRequestFailedException')
            def messageClass = muleClassLoader.loadClass('org.mule.runtime.api.i18n.I18nMessage')
            def msg = messageClass.newInstance("HTTP someMethod failed.",
                                               -1)
            def errorTypeDefinitionClass = muleClassLoader.loadClass('org.mule.runtime.module.extension.internal.loader.enricher.ModuleErrors')
            def connectivityError = errorTypeDefinitionClass.enumConstants.find { c ->
                c.toString() == 'CONNECTIVITY'
            }
            def exception = exceptionClass.newInstance(msg,
                                                       new ConnectException('could not reach HTTP server'),
                                                       connectivityError)
            throw exception
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
