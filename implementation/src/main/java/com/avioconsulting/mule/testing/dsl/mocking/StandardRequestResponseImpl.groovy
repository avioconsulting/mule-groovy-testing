package com.avioconsulting.mule.testing.dsl.mocking


import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.TransformerChain

abstract class StandardRequestResponseImpl<T extends ConnectorInfo> implements
        StandardRequestResponse {
    protected IFormatter formatter
    private Closure closure
    private final ClosureCurrier closureCurrier
    private final String requestResponseUse

    StandardRequestResponseImpl(ClosureCurrier closureCurrier,
                                String requestResponseUse) {
        this.requestResponseUse = requestResponseUse
        this.closureCurrier = closureCurrier
    }

    TransformerChain<T> getTransformer() {
        def transformerChain = new TransformerChain<T>()
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        transformerChain.addTransformer(formatter.transformer)
        transformerChain
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        formatter = new JsonFormatterImpl(closureCurrier)
        this.closure = closure
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        formatter = new XMLFormatterImpl(requestResponseUse)
        this.closure = closure
    }

    @Override
    def raw(@DelegatesTo(RawFormatter) Closure closure) {
        formatter = new RawFormatterImpl<T>(closureCurrier)
        this.closure = closure
    }

    @Override
    def api(@DelegatesTo(RawFormatter) Closure closure) {
        formatter = new ApiFormatterImpl<T>(closureCurrier)
        this.closure = closure
    }
}
