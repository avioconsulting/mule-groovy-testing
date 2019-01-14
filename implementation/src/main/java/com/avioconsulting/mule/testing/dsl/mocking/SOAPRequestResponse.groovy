package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.SoapConsumerInfo
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.SoapFaultTransformer
import com.avioconsulting.mule.testing.transformers.http.WsConsumerConnectorErrorTransformer

class SOAPRequestResponse extends
        StandardRequestResponseImpl<SoapConsumerInfo> {
    private final WsConsumerConnectorErrorTransformer httpConnectorErrorTransformer
    private final SoapFaultTransformer soapFaultTransformer

    SOAPRequestResponse(IFetchClassLoaders fetchAppClassLoader,
                        Closure closure) {
        this.httpConnectorErrorTransformer = new WsConsumerConnectorErrorTransformer(fetchAppClassLoader)
        this.soapFaultTransformer = new SoapFaultTransformer(fetchAppClassLoader)
        def soapFormatter = new SOAPFormatterImpl(httpConnectorErrorTransformer,
                                                  soapFaultTransformer)
        useFormatter(soapFormatter,
                     closure)
    }

    @Override
    TransformerChain<SoapConsumerInfo> getTransformer() {
        def transformerChain = super.transformer
        transformerChain.addTransformer(soapFaultTransformer)
        transformerChain.addTransformer(httpConnectorErrorTransformer)
        return transformerChain
    }
}
