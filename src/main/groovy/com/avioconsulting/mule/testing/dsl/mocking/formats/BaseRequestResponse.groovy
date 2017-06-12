package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.HttpConnectorSpy
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.MessageProcessorMocker

abstract class BaseRequestResponse {
    private final MessageProcessorMocker muleMocker
    private final MuleContext muleContext
    private final List<Class> allowedPayloadTypes
    private final ConnectorType connectorType

    BaseRequestResponse(MessageProcessorMocker muleMocker,
                        MuleContext muleContext,
                        List<Class> allowedPayloadTypes,
                        ConnectorType connectorType) {
        this.connectorType = connectorType
        this.allowedPayloadTypes = allowedPayloadTypes
        this.muleContext = muleContext
        this.muleMocker = muleMocker
    }

    // TODO: Remove this once JSON Formatter/query params dependency is gone
    abstract HttpConnectorSpy getHttpConnectorSpy()

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatter(httpConnectorSpy,
                                          this.muleContext,
                                          allowedPayloadTypes,
                                          connectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        def transformer = code() as MuleMessageTransformer
        muleMocker.thenApply(transformer)
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        def formatter = new XMLFormatter(this.muleMocker,
                                         this.muleContext,
                                         connectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }
}
