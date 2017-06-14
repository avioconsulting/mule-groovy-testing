package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.spies.HttpConnectorSpy
import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import com.avioconsulting.mule.testing.transformers.HttpValidationTransformer
import org.mule.api.MuleContext
import org.mule.module.http.internal.request.ResponseValidator
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class HttpRequestResponseChoiceImpl extends StandardRequestResponseImpl
        implements HttpRequestResponseChoice,
                IReceiveHttpOptions {
    private HttpValidationTransformer httpValidationTransformer
    private Map queryParams
    private String fullPath
    private String httpVerb

    HttpRequestResponseChoiceImpl(MessageProcessorMocker muleMocker,
                                  MunitSpy spy,
                                  ProcessorLocator processorLocator,
                                  MuleContext muleContext,
                                  List<Class> allowedPayloadTypes,
                                  ConnectorType connectorType) {
        super(muleMocker,
              muleContext,
              allowedPayloadTypes,
              connectorType)
        httpValidationTransformer = new HttpValidationTransformer(muleContext)
        def httpConnectorSpy = new HttpConnectorSpy(processorLocator,
                                                    muleContext,
                                                    [this.httpValidationTransformer, this])
        spy.before(httpConnectorSpy)
    }

    private void appendHttpValidator() {
        this.transformerChain.addTransformer(httpValidationTransformer)
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        super.json(closure)
        appendHttpValidator()
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        super.xml(closure)
        appendHttpValidator()
    }

    def withHttpOptions(Closure closure) {
        if (!queryParams) {
            throw new Exception('Only invoke this closure inside your whenCalledWith... code')
        }
        closure(httpVerb, fullPath, queryParams)
    }

    def setHttpReturnCode(Integer code) {
        httpValidationTransformer.httpReturnCode = code
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
