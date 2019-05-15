package com.avioconsulting.mule.testing.dsl.mocking


import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.SoapConsumerInfo
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.SoapFaultTransformer
import com.avioconsulting.mule.testing.transformers.http.WsConsumerConnectorErrorTransformer

class SOAPRequestResponseImpl extends
        StandardRequestResponseImpl<SoapConsumerInfo> {
    private final WsConsumerConnectorErrorTransformer httpConnectorErrorTransformer
    private final SoapFaultTransformer soapFaultTransformer

    SOAPRequestResponseImpl(RuntimeBridgeTestSide runtimeBridgeTestSide,
                            Closure closure) {
        super(runtimeBridgeTestSide)
        this.httpConnectorErrorTransformer = new WsConsumerConnectorErrorTransformer(runtimeBridgeTestSide)
        this.soapFaultTransformer = new SoapFaultTransformer(runtimeBridgeTestSide)
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
