package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.TransformerChain

abstract class StandardRequestResponseImpl<T extends ConnectorInfo> implements
        StandardRequestResponse {
    protected final IPayloadValidator initialPayloadValidator
    protected IFormatter formatter
    private Closure closure
    private final ClosureCurrier closureCurrier
    private final String requestResponseUse
    private final TransformingEventFactory eventFactory

    StandardRequestResponseImpl(IPayloadValidator initialPayloadValidator,
                                ClosureCurrier closureCurrier,
                                String requestResponseUse,
                                TransformingEventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.requestResponseUse = requestResponseUse
        this.closureCurrier = closureCurrier
        this.initialPayloadValidator = initialPayloadValidator
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
        formatter = new JsonFormatterImpl(initialPayloadValidator,
                                          closureCurrier,
                                          eventFactory)
        this.closure = closure
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        formatter = new XMLFormatterImpl(initialPayloadValidator,
                                         requestResponseUse)
        this.closure = closure
    }

    @Override
    def raw(@DelegatesTo(RawFormatter) Closure closure) {
        formatter = new RawFormatterImpl<T>(eventFactory,
                                            initialPayloadValidator,
                                            closureCurrier)
        this.closure = closure
    }
}
