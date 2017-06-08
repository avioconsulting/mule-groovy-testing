package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.ConnectorType
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class RequestResponseChoice {
    private final MessageProcessorMocker muleMocker
    private final MuleContext muleContext
    private final List<Class> allowedPayloadTypes
    private final ConnectorType connectorType
    private final MunitSpy spy
    private final ProcessorLocator processorLocator

    RequestResponseChoice(MessageProcessorMocker muleMocker,
                          MunitSpy spy,
                          ProcessorLocator processorLocator,
                          MuleContext muleContext,
                          List<Class> allowedPayloadTypes,
                          ConnectorType connectorType) {
        this.processorLocator = processorLocator
        this.spy = spy
        this.connectorType = connectorType
        this.allowedPayloadTypes = allowedPayloadTypes
        this.muleContext = muleContext
        this.muleMocker = muleMocker
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatter(spy,
                                          processorLocator,
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
