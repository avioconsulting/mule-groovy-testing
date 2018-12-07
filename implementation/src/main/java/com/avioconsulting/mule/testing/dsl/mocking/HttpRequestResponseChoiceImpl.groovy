package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpGetTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpValidationTransformer
import com.avioconsulting.mule.testing.transformers.http.MuleToHttpTransformer

class HttpRequestResponseChoiceImpl extends
        StandardRequestResponseImpl<HttpRequesterInfo>
        implements
                HttpRequestResponseChoice {
    private final HttpValidationTransformer httpValidationTransformer
    private final HttpGetTransformer httpGetTransformer
    private final HttpConnectorErrorTransformer httpConnectorErrorTransformer

    HttpRequestResponseChoiceImpl(IFetchClassLoaders fetchAppClassLoader) {
        super('HTTP Request Mock')
        httpValidationTransformer = new HttpValidationTransformer()
        httpGetTransformer = new HttpGetTransformer()
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(fetchAppClassLoader)
    }

    TransformerChain<HttpRequesterInfo> getTransformer() {
        def code = closure.rehydrate(formatter,
                                     this,
                                     this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        def transformer = new MuleToHttpTransformer()
        // HTTP GET operations need to 'erase' payload before any attempts to deserialize the payload, etc.
        transformer.addTransformer(httpGetTransformer)
        // TODO: add formatter in
        transformer.addTransformer(httpValidationTransformer)
        transformer.addTransformer(httpConnectorErrorTransformer)
        transformer
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
