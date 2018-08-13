package com.avioconsulting.mule.testing.dsl.mocking


import com.avioconsulting.mule.testing.payloadvalidators.ContentTypeCheckDisabledValidator
import com.avioconsulting.mule.testing.payloadvalidators.HttpRequestPayloadValidator
import com.avioconsulting.mule.testing.spies.HttpConnectorSpy
import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpGetTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpValidationTransformer
import org.mule.api.MuleContext
import org.mule.module.http.internal.request.ResponseValidator

class HttpRequestResponseChoiceImpl extends StandardRequestResponseImpl
        implements HttpRequestResponseChoice, IReceiveHttpOptions {
    private final HttpValidationTransformer httpValidationTransformer
    private final HttpGetTransformer httpGetTransformer
    private final HttpConnectorErrorTransformer httpConnectorErrorTransformer
    private Map queryParams
    private Map headers
    private String fullPath
    private String httpVerb

    HttpRequestResponseChoiceImpl(Object spy,
                                  MuleContext muleContext) {
        super(muleContext,
              new HttpRequestPayloadValidator())
        def payloadTypeFetcher = initialPayloadValidator as HttpRequestPayloadValidator
        httpValidationTransformer = new HttpValidationTransformer(muleContext)
        httpGetTransformer = new HttpGetTransformer(muleContext)
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(muleContext)
        def httpPathEtcReceivers = [this.httpValidationTransformer,
                                    this,
                                    payloadTypeFetcher,
                                    httpGetTransformer]
        def muleEventReceivers = [httpValidationTransformer]
        def httpConnectorSpy = new HttpConnectorSpy(muleContext,
                                                    httpPathEtcReceivers,
                                                    muleEventReceivers)
        spy.before(httpConnectorSpy)
    }

    TransformerChain getTransformer() {
        // ensure this is done last to trigger 'validation' on the mock's reply
        def transformerChain = super.transformer
        // HTTP GET operations need to 'erase' payload before any attempts to deserialize the payload, etc.
        transformerChain.prependTransformer(httpGetTransformer)
        transformerChain.addTransformer(httpValidationTransformer)
        transformerChain.addTransformer(httpConnectorErrorTransformer)
        transformerChain
    }

    def withHttpOptions(Closure closure) {
        if (queryParams == null) {
            throw new Exception('Only invoke this closure inside your whenCalledWith... code')
        }
        closure(httpVerb, fullPath, queryParams)
    }

    @Override
    def withHttpOptionsIncludingHeaders(Closure closure) {
        if (queryParams == null) {
            throw new Exception('Only invoke this closure inside your whenCalledWith... code')
        }
        closure(httpVerb, fullPath, queryParams, headers)
    }

    def setHttpReturnCode(Integer code) {
        httpValidationTransformer.httpReturnCode = code
    }

    def disableContentTypeCheck() {
        def existingValidator = this.formatter.payloadValidator
        this.formatter = this.formatter.withNewPayloadValidator(
                new ContentTypeCheckDisabledValidator(existingValidator))
    }

    def httpConnectError() {
        this.httpConnectorErrorTransformer.triggerConnectException()
    }

    def httpTimeoutError() {
        this.httpConnectorErrorTransformer.triggerTimeoutException()
    }

    def receive(Map queryParams,
                Map headers,
                String fullPath,
                String httpVerb,
                ResponseValidator responseValidator) {
        this.queryParams = queryParams
        this.headers = headers
        this.fullPath = fullPath
        this.httpVerb = httpVerb
    }
}
