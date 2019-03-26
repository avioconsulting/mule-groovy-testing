package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.transformers.http.SoapFaultTransformer
import com.avioconsulting.mule.testing.transformers.http.WsConsumerConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder

import javax.xml.namespace.QName

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    private final WsConsumerConnectorErrorTransformer errorTransformer
    private final SoapFaultTransformer soapFaultTransformer

    SOAPFormatterImpl(WsConsumerConnectorErrorTransformer errorTransformer,
                      SoapFaultTransformer soapFaultTransformer) {
        super(XMLMessageBuilder.MessageType.SoapMock)
        this.soapFaultTransformer = soapFaultTransformer
        this.errorTransformer = errorTransformer
    }

    @Override
    def httpConnectError() {
        errorTransformer.triggerConnectException()
        notifyImpendingFault()
        return null
    }

    @Override
    def httpTimeoutError() {
        errorTransformer.triggerTimeoutException()
        notifyImpendingFault()
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
                  Closure detailMarkupBuilderClosure) {
        soapFaultTransformer.triggerSoapFault(message,
                                              faultCode,
                                              subCode,
                                              detailMarkupBuilderClosure)
        notifyImpendingFault()
        return null
    }
}
