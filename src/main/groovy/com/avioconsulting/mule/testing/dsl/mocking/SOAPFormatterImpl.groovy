package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.IFetchClassLoaders
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.CustomErrorWrapperException
import com.avioconsulting.mule.testing.transformers.http.WsConsumerConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import groovy.xml.DOMBuilder

import javax.xml.namespace.QName

class SOAPFormatterImpl extends
        XMLFormatterImpl implements
        SOAPFormatter {
    @Lazy
    private Class muleSoapFaultClass = {
        try {
            fetchAppClassLoader.appClassloader.loadClass('org.mule.runtime.soap.api.exception.SoapFaultException')
        }
        catch (e) {
            throw new Exception('Was not able to load SoapFaultException properly. Do you have thw WSC consumer module in your POM?',
                                e)
        }
    }()

    private WsConsumerConnectorErrorTransformer httpConnectorErrorTransformer
    private final IFetchClassLoaders fetchAppClassLoader

    SOAPFormatterImpl(IFetchClassLoaders fetchAppClassLoader) {
        super('SOAP/WS Consumer Mock',
              XMLMessageBuilder.MessageType.Soap)
        this.fetchAppClassLoader = fetchAppClassLoader
    }

    def httpConnectError() {
        httpConnectorErrorTransformer = new WsConsumerConnectorErrorTransformer(fetchAppClassLoader)
        httpConnectorErrorTransformer.triggerConnectException()
        // avoid DSL weirdness
        return null
    }

    def httpTimeoutError() {
        httpConnectorErrorTransformer = new WsConsumerConnectorErrorTransformer(fetchAppClassLoader)
        httpConnectorErrorTransformer.triggerTimeoutException()
        // avoid DSL weirdness
        return null
    }

    @Override
    def soapFault(String message,
                  QName faultCode,
                  QName subCode) {
        soapFault(message,
                  faultCode,
                  subCode) { builder ->
            null
        }
    }

    @Override
    def soapFault(String message,
                  QName faultCode,
                  QName subCode,
                  Closure closure) {
        def detailResult = closure(DOMBuilder.newInstance())
        def detailString = detailResult ? detailResult.serialize() : '<detail/>'
        def cxfExceptionStub = new Exception('Normally this would be a org.apache.cxf.binding.soap.SoapFault exception but that one is harder to find in the ClassLoader model and org.mule.runtime.soap.api.exception.SoapFaultException, which is the direct cause anyways, has all of the details available')
        def muleException = muleSoapFaultClass.newInstance(faultCode,
                                                           subCode,
                                                           '<?xml version="1.0" encoding="UTF-8"?>' + detailString,
                                                           message,
                                                           null, // node
                                                           null,
                                                           cxfExceptionStub) as Throwable
        throw new CustomErrorWrapperException(muleException,
                                              'WSC',
                                              'SOAP_FAULT')
    }

    @Override
    MuleMessageTransformer getTransformer() {
        if (httpConnectorErrorTransformer) {
            return httpConnectorErrorTransformer
        }
        return super.transformer
    }
}
