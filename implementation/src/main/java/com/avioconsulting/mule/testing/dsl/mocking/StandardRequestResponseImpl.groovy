package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.TransformerChain

abstract class StandardRequestResponseImpl<T extends ConnectorInfo> implements
        StandardRequestResponse {
    protected IFormatter formatter
    private Closure closure
    protected final ClosureCurrier closureCurrier
    private final RuntimeBridgeTestSide runtimeBridgeTestSide

    StandardRequestResponseImpl(RuntimeBridgeTestSide runtimeBridgeTestSide) {
        this.runtimeBridgeTestSide = runtimeBridgeTestSide
        this.closureCurrier = new ClosureCurrier<T>()
    }

    TransformerChain<T> getTransformer() {
        def transformerChain = new TransformerChain<T>()
        def code = closure.rehydrate(formatter,
                                     this,
                                     this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        transformerChain.addTransformer(formatter.transformer)
        transformerChain
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        useFormatter(new JsonFormatterImpl(closureCurrier,
                                           runtimeBridgeTestSide),
                     closure)
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        useFormatter(new XMLFormatterImpl(runtimeBridgeTestSide),
                     closure)
    }

    protected def useFormatter(IFormatter formatter,
                               Closure closure) {
        this.formatter = formatter
        this.closure = closure
    }

    @Override
    def raw(@DelegatesTo(RawFormatter) Closure closure) {
        formatter = new RawFormatterImpl<T>(closureCurrier)
        this.closure = closure
    }
}
