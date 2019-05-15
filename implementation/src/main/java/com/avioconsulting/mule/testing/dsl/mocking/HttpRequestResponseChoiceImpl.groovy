package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.HttpGetTransformer

class HttpRequestResponseChoiceImpl extends
        StandardRequestResponseImpl<HttpRequesterInfo> {
    private final HttpGetTransformer httpGetTransformer

    HttpRequestResponseChoiceImpl(RuntimeBridgeTestSide runtimeBridgeTestSide) {
        httpGetTransformer = new HttpGetTransformer()
    }

    TransformerChain<HttpRequesterInfo> getTransformer() {
        // ensure this is done last to trigger 'validation' on the mock's reply
        def transformerChain = super.transformer
        // HTTP GET operations need to 'erase' payload before any attempts to deserialize the payload, etc.
        transformerChain.prependTransformer(httpGetTransformer)
        transformerChain
    }
}
