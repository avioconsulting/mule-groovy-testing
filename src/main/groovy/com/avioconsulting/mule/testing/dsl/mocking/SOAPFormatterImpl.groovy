package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.IFetchAppClassLoader
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.CustomErrorWrapperException
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.http.WsConsumerConnectorErrorTransformer
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
    private final IFetchAppClassLoader fetchAppClassLoader

    SOAPFormatterImpl(IPayloadValidator payloadValidator,
                      IFetchAppClassLoader fetchAppClassLoader) {
        super(payloadValidator,
              'SOAP/WS Consumer Mock')
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
        def cxfExceptionStub = new Exception('Normally this would be a org.apache.cxf.binding.soap.SoapFault exception but that one is harder to find in the ClassLoader model and org.mule.runtime.soap.api.exception.SoapFaultException, which is the direct cause anyways, has all of the details available')
        def muleException = muleSoapFaultClass.newInstance(faultCode,
                                                           subCode,
                                                           '<?xml version="1.0" encoding="UTF-8"?><detail/>',
                                                           message,
                                                           null, // node
                                                           null,
                                                           cxfExceptionStub) as Throwable
        throw new CustomErrorWrapperException(muleException,
                                              'WSC',
                                              'SOAP_FAULT')
    }

    @Override
    def soapFault(String message,
                  QName faultCode,
                  QName subCode,
                  Closure closure) {
        def result = closure(DOMBuilder.newInstance())
        def soapFault = cxfSoapFaultClass.newInstance(message, faultCode)
        soapFault.detail = result
        soapFault.addSubCode(subCode)
        // for the last step, we need the MuleEvent
        def wsConsumerException = this.soapFaultTransformer.createSoapFaultException(soapFault)
        throw wsConsumerException
    }

    @Override
    MuleMessageTransformer getTransformer() {
        if (httpConnectorErrorTransformer) {
            return httpConnectorErrorTransformer
        }
        return super.transformer
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new SOAPFormatterImpl(validator,
                              fetchAppClassLoader)
    }
}
