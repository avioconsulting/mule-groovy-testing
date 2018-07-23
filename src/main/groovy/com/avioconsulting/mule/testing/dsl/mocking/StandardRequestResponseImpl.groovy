package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext

abstract class StandardRequestResponseImpl implements StandardRequestResponse {
    protected final MuleContext muleContext
    protected final IPayloadValidator initialPayloadValidator
    protected IFormatter formatter
    private Closure closure

    StandardRequestResponseImpl(MuleContext muleContext,
                                IPayloadValidator initialPayloadValidator) {
        this.initialPayloadValidator = initialPayloadValidator
        this.muleContext = muleContext
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
        formatter = new JsonFormatterImpl(this.muleContext,
                                          initialPayloadValidator)
        this.closure = closure
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        formatter = new XMLFormatterImpl(this.muleContext,
                                         initialPayloadValidator)
        this.closure = closure
    }

    @Override
    def raw(@DelegatesTo(RawFormatter) Closure closure) {
        formatter = new RawFormatterImpl(muleContext,
                                         initialPayloadValidator)
        this.closure = closure
    }
}
