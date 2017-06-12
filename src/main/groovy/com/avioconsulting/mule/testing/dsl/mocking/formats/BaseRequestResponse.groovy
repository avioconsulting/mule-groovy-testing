package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.HttpConnectorSpy
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

abstract class BaseRequestResponse {
    protected final TransformerChain transformerChain
    protected final MuleContext muleContext
    private final List<Class> allowedPayloadTypes
    private final ConnectorType connectorType

    BaseRequestResponse(MessageProcessorMocker muleMocker,
                        MuleContext muleContext,
                        List<Class> allowedPayloadTypes,
                        ConnectorType connectorType) {
        this.connectorType = connectorType
        this.allowedPayloadTypes = allowedPayloadTypes
        this.muleContext = muleContext
        this.transformerChain = new TransformerChain(muleMocker)
    }

    // TODO: Remove this once JSON Formatter/query params dependency is gone
    abstract HttpConnectorSpy getHttpConnectorSpy()

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatterImpl(httpConnectorSpy,
                                              this.muleContext,
                                              allowedPayloadTypes,
                                              connectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        def formatter = new XMLFormatterImpl(this.muleContext,
                                             connectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }
}
