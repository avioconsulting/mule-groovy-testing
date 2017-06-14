package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.payloadvalidators.ContentTypeCheckDisabledValidator
import com.avioconsulting.mule.testing.payloadvalidators.HttpRequestPayloadValidator
import com.avioconsulting.mule.testing.spies.HttpConnectorSpy
import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import com.avioconsulting.mule.testing.transformers.HttpGetTransformer
import com.avioconsulting.mule.testing.transformers.HttpValidationTransformer
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext
import org.mule.module.http.internal.request.ResponseValidator
import org.mule.munit.common.mocking.MunitSpy

class HttpRequestResponseChoiceImpl extends StandardRequestResponseImpl
        implements HttpRequestResponseChoice, IReceiveHttpOptions {
    private HttpValidationTransformer httpValidationTransformer
    private HttpGetTransformer httpGetTransformer
    private Map queryParams
    private String fullPath
    private String httpVerb

    HttpRequestResponseChoiceImpl(MunitSpy spy,
                                  ProcessorLocator processorLocator,
                                  MuleContext muleContext) {
        super(muleContext,
              new HttpRequestPayloadValidator())
        def payloadTypeFetcher = initialPayloadValidator as HttpRequestPayloadValidator
        httpValidationTransformer = new HttpValidationTransformer(muleContext)
        httpGetTransformer = new HttpGetTransformer(muleContext)
        def httpPathEtcReceivers = [this.httpValidationTransformer,
                                    this,
                                    payloadTypeFetcher,
                                    httpGetTransformer]
        def httpConnectorSpy = new HttpConnectorSpy(processorLocator,
                                                    muleContext,
                                                    httpPathEtcReceivers)
        spy.before(httpConnectorSpy)
    }

    TransformerChain getTransformer() {
        // ensure this is done last to trigger 'validation' on the mock's reply
        def transformerChain = super.transformer
        // HTTP GET operations need to 'erase' payload before any attempts to deserialize the payload, etc.
        transformerChain.prependTransformer(httpGetTransformer)
        transformerChain.addTransformer(httpValidationTransformer)
        transformerChain
    }

    def withHttpOptions(Closure closure) {
        if (queryParams == null) {
            throw new Exception('Only invoke this closure inside your whenCalledWith... code')
        }
        closure(httpVerb, fullPath, queryParams)
    }

    def setHttpReturnCode(Integer code) {
        httpValidationTransformer.httpReturnCode = code
    }

    def disableContentTypeCheck() {
        def existingValidator = this.formatter.payloadValidator
        this.formatter = this.formatter.withNewPayloadValidator(
                new ContentTypeCheckDisabledValidator(existingValidator))
    }

    def receive(Map queryParams,
                String fullPath,
                String httpVerb,
                ResponseValidator responseValidator) {
        this.queryParams = queryParams
        this.fullPath = fullPath
        this.httpVerb = httpVerb
    }
}
