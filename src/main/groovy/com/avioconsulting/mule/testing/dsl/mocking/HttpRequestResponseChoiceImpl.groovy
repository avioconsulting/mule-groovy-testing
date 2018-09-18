package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.payloadvalidators.ContentTypeCheckDisabledValidator
import com.avioconsulting.mule.testing.payloadvalidators.HttpRequestPayloadValidator
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.HttpClosureCurrier
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpGetTransformer
import com.avioconsulting.mule.testing.transformers.http.HttpValidationTransformer

class HttpRequestResponseChoiceImpl extends StandardRequestResponseImpl<HttpRequesterInfo>
        implements HttpRequestResponseChoice {
    private final HttpValidationTransformer httpValidationTransformer
    private final HttpGetTransformer httpGetTransformer
    private final HttpConnectorErrorTransformer httpConnectorErrorTransformer
    private final InvokerEventFactory eventFactory

    HttpRequestResponseChoiceImpl(InvokerEventFactory eventFactory) {
        super(new HttpRequestPayloadValidator(),
              eventFactory,
              new HttpClosureCurrier(),
              'HTTP Request Mock')
        this.eventFactory = eventFactory
        httpValidationTransformer = new HttpValidationTransformer()
        httpGetTransformer = new HttpGetTransformer(eventFactory)
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer()
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
}
