package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.TransformerChain

abstract class StandardRequestResponseImpl implements StandardRequestResponse {
    protected final IPayloadValidator initialPayloadValidator
    protected IFormatter formatter
    private Closure closure
    private final EventFactory eventFactory
    private final ClosureCurrier closureCurrier
    private final String requestResponseUse

    StandardRequestResponseImpl(IPayloadValidator initialPayloadValidator,
                                EventFactory eventFactory,
                                ClosureCurrier closureCurrier,
                                String requestResponseUse) {
        this.requestResponseUse = requestResponseUse
        this.closureCurrier = closureCurrier
        this.eventFactory = eventFactory
        this.initialPayloadValidator = initialPayloadValidator
    }

    TransformerChain getTransformer() {
        def transformerChain = new TransformerChain()
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        transformerChain.addTransformer(formatter.transformer)
        transformerChain
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        formatter = new JsonFormatterImpl(initialPayloadValidator,
                                          eventFactory,
                                          closureCurrier)
        this.closure = closure
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        formatter = new XMLFormatterImpl(eventFactory,
                                         initialPayloadValidator,
                                         requestResponseUse)
        this.closure = closure
    }

    @Override
    def raw(@DelegatesTo(RawFormatter) Closure closure) {
        formatter = new RawFormatterImpl(eventFactory,
                                         initialPayloadValidator,
                                         closureCurrier)
        this.closure = closure
    }
}
