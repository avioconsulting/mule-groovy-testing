package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ModuleExceptionWrapper

import java.util.concurrent.TimeoutException

trait HttpFunctionality {
    def getValidator(ClassLoader classLoader,
                     String successCodes = '0..399') {
        def validatorClass = classLoader
                .loadClass('org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator')
        validatorClass.newInstance(successCodes)
    }

    Exception getConnectionException(String uri,
                                     String method,
                                     ClassLoader appClassLoader) {
        getException(uri,
                     method,
                     new ConnectException('could not reach HTTP server'),
                     'Connection refused',
                     'CONNECTIVITY',
                     appClassLoader)
    }

    Exception getTimeoutException(String uri,
                                  String method,
                                  ClassLoader appClassLoader) {
        getException(uri,
                     method,
                     new TimeoutException('HTTP timeout!'),
                     'Some timeout error',
                     'TIMEOUT',
                     appClassLoader)
    }

    /**
     *
     * @param connectorInfo
     * @param cause
     * @param details
     * @param errorEnumCode - from HttpError
     */
    Exception getException(String uri,
                           String method,
                           Exception cause,
                           String details,
                           String errorEnumCode,
                           ClassLoader appClassLoader) {
        // we are mimicing how HttpRequestor throws this exception in a real scenario
        // have to do all of this with reflection since it's inside the app
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
}
