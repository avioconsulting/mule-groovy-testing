package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext

abstract class StandardRequestResponseImpl implements StandardRequestResponse {
    protected final MuleContext muleContext
    protected final IPayloadValidator payloadValidator
    private ISelectPrimaryTransformer transformerSelector
    private Closure closure

    StandardRequestResponseImpl(MuleContext muleContext,
                                IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
    }

    TransformerChain getTransformer() {
        def transformerChain = new TransformerChain()
        def code = closure.rehydrate(transformerSelector, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        transformerChain.addTransformer(transformerSelector.transformer)
        transformerChain
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        transformerSelector = new JsonFormatterImpl(this.muleContext,
                                                    payloadValidator)
        this.closure = closure
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        transformerSelector = new XMLFormatterImpl(this.muleContext,
                                                   payloadValidator)
        this.closure = closure
    }
}
