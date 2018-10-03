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
            throw getException(connectorInfo,
                               new ConnectException('could not reach HTTP server'),
                               'Connection refused',
                               'CONNECTIVITY')
        }
        if (triggerTimeoutException) {
            throw getException(connectorInfo,
                               new TimeoutException('HTTP timeout!'),
                               'Some timeout error',
                               'TIMEOUT')
        }
    }

    /**
     *
     * @param connectorInfo
     * @param cause
     * @param details
     * @param errorEnumCode - from HttpError
     */
    private Exception getException(HttpRequesterInfo connectorInfo,
                                   Exception cause,
                                   String details,
                                   String errorEnumCode) {
        // we are mimicing how HttpRequestor throws this exception in a real scenario
        // have to do all of this with reflection since it's inside the app
        def appClassLoader = fetchAppClassLoader.appClassloader
        def exceptionClass = appClassLoader.loadClass('org.mule.extension.http.api.error.HttpRequestFailedException')
        def messageClass = appClassLoader.loadClass('org.mule.runtime.api.i18n.I18nMessage')
        def msg = messageClass.newInstance("HTTP ${connectorInfo.method} on resource '${connectorInfo.uri}' failed: ${details}.",
                                           -1)
        def errorTypeDefinitionClass = appClassLoader.loadClass('org.mule.extension.http.api.error.HttpError')
        def connectivityError = errorTypeDefinitionClass.enumConstants.find { c ->
            c.toString() == errorEnumCode
        }
        assert connectivityError: "Could not locate ${errorEnumCode} in ${errorTypeDefinitionClass.enumConstants}"
        exceptionClass.newInstance(msg,
                                   cause,
                                   connectivityError) as Exception
    }

    @Override
    def reset() {
        this.triggerConnectException = false
        this.triggerTimeoutException = false
    }
}
