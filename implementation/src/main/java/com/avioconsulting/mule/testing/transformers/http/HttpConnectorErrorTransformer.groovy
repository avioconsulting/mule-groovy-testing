package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ModuleExceptionWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

import java.util.concurrent.TimeoutException

class HttpConnectorErrorTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<HttpRequesterInfo> {
    private boolean triggerConnectException
    private boolean triggerTimeoutException
    protected final IFetchClassLoaders fetchAppClassLoader

    HttpConnectorErrorTransformer(IFetchClassLoaders fetchAppClassLoader) {
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
            throw getConnectionException(connectorInfo.uri,
                                         connectorInfo.method)
        }
        if (triggerTimeoutException) {
            throw getTimeoutException(connectorInfo.uri,
                                      connectorInfo.method)
        }
    }

    protected Exception getConnectionException(String uri,
                                               String method) {
        getException(uri,
                     method,
                     new ConnectException('could not reach HTTP server'),
                     'Connection refused',
                     'CONNECTIVITY')
    }

    protected Exception getTimeoutException(String uri,
                                            String method) {
        getException(uri,
                     method,
                     new TimeoutException('HTTP timeout!'),
                     'Some timeout error',
                     'TIMEOUT')
    }

    /**
     *
     * @param connectorInfo
     * @param cause
     * @param details
     * @param errorEnumCode - from HttpError
     */
    private Exception getException(String uri,
                                   String method,
                                   Exception cause,
                                   String details,
                                   String errorEnumCode) {
        // we are mimicing how HttpRequestor throws this exception in a real scenario
        // have to do all of this with reflection since it's inside the app
        def appClassLoader = fetchAppClassLoader.appClassloader
        def exceptionClass = appClassLoader.loadClass('org.mule.extension.http.api.error.HttpRequestFailedException')
        def messageClass = appClassLoader.loadClass('org.mule.runtime.api.i18n.I18nMessage')
        def msg = messageClass.newInstance("HTTP ${method} on resource '${uri}' failed: ${details}.",
                                           -1)
        def errorTypeDefinitionClass = appClassLoader.loadClass('org.mule.extension.http.api.error.HttpError')
        def connectivityError = errorTypeDefinitionClass.enumConstants.find { c ->
            c.toString() == errorEnumCode
        }
        assert connectivityError: "Could not locate ${errorEnumCode} in ${errorTypeDefinitionClass.enumConstants}"
        def exception = exceptionClass.newInstance(msg,
                                                   cause,
                                                   connectivityError) as Exception
        // errors using the underlying HTTP transport, whether SOAP (when set to custom) or HTTP request
        // have been observed to use the HTTP namespace
        new ModuleExceptionWrapper(exception,
                                   'HTTP')
    }

    @Override
    def reset() {
        this.triggerConnectException = false
        this.triggerTimeoutException = false
    }
}
