package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.IFetchAppClassLoader
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
    private final IFetchAppClassLoader fetchAppClassLoader

    HttpConnectorErrorTransformer(IFetchAppClassLoader fetchAppClassLoader) {
        this.fetchAppClassLoader = fetchAppClassLoader
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
            def appClassLoader = fetchAppClassLoader.appClassloader
            def exceptionClass = appClassLoader.loadClass('org.mule.extension.http.api.error.HttpRequestFailedException')
            def messageClass = appClassLoader.loadClass('org.mule.runtime.api.i18n.I18nMessage')
            // TODO: Hard coded error message
            def msg = messageClass.newInstance("HTTP POST on resource 'http://localhost:443/some_path' failed: Connection refused.",
                                               -1)
            def errorTypeDefinitionClass = appClassLoader.loadClass('org.mule.extension.http.api.error.HttpError')
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
