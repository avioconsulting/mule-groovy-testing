package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.IFetchAppClassLoader
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.SoapConsumerInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

import java.util.concurrent.TimeoutException

class WsConsumerConnectorErrorTransformer extends
        HttpConnectorErrorTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<SoapConsumerInfo> {
    // all SOAP requests are POSTs
    private static final String SOAP_METHOD = 'POST'
    private boolean triggerConnectException
    private boolean triggerTimeoutException

    WsConsumerConnectorErrorTransformer(IFetchAppClassLoader fetchAppClassLoader) {
        super(fetchAppClassLoader)
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
        Throwable exception = null
        if (triggerConnectException) {
            // when custom transports (aka HTTP reequest configs) are used, then these exceptions behave more like
            // exceptions coming out of the HTTP requester
            if (connectorInfo.customHttpTransportConfigured) {
                exception = getConnectionException(connectorInfo.uri,
                                                   SOAP_METHOD)
            } else {
                def exceptionKlass = fetchAppClassLoader.appClassloader.loadClass('org.mule.runtime.soap.api.exception.DispatchingException')
                exception = exceptionKlass.newInstance('An error occurred while sending the SOAP request') as Throwable
            }
        }
        if (triggerTimeoutException) {
            def exceptionKlass = fetchAppClassLoader.appClassloader.loadClass('org.mule.runtime.soap.api.exception.DispatchingException')
            exception = exceptionKlass.newInstance('The SOAP request timed out',
                                                   new TimeoutException('HTTP timeout!')) as Throwable
        }
        assert exception: 'should not make it thus far without one'
        throw exception
    }

    @Override
    def reset() {
        this.triggerConnectException = false
        this.triggerTimeoutException = false
    }
}
