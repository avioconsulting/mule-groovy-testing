package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpGetTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpValidationTransformer

class HttpRequestResponseChoiceImpl extends
        StandardRequestResponseImpl<HttpRequesterInfo>
        implements
                HttpRequestResponseChoice {
    private final HttpValidationTransformer httpValidationTransformer
    private final HttpGetTransformer httpGetTransformer
    private final HttpConnectorErrorTransformer httpConnectorErrorTransformer

    HttpRequestResponseChoiceImpl(IFetchClassLoaders fetchAppClassLoader) {
        super()
        httpValidationTransformer = new HttpValidationTransformer()
        httpGetTransformer = new HttpGetTransformer()
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(fetchAppClassLoader)
    }

    TransformerChain<HttpRequesterInfo> getTransformer() {
        // ensure this is done last to trigger 'validation' on the mock's reply
        def transformerChain = super.transformer
        // HTTP GET operations need to 'erase' payload before any attempts to deserialize the payload, etc.
        transformerChain.prependTransformer(httpGetTransformer)
        transformerChain.addTransformer(httpValidationTransformer)
        transformerChain.addTransformer(httpConnectorErrorTransformer)
        transformerChain
    }

    def setHttpReturnCode(Integer code) {
        httpValidationTransformer.httpReturnCode = code
    }

    def httpConnectError() {
        this.httpConnectorErrorTransformer.triggerConnectException()
    }

    def httpTimeoutError() {
        this.httpConnectorErrorTransformer.triggerTimeoutException()
    }
}