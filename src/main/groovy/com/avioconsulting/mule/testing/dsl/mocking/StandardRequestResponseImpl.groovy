package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

abstract class StandardRequestResponseImpl implements StandardRequestResponse {
    protected final TransformerChain transformerChain
    protected final MuleContext muleContext
    protected final IPayloadValidator payloadValidator

    StandardRequestResponseImpl(MessageProcessorMocker muleMocker,
                                MuleContext muleContext,
                                IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
        this.transformerChain = new TransformerChain(muleMocker)
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatterImpl(this.muleContext,
                                              payloadValidator)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        def formatter = new XMLFormatterImpl(this.muleContext,
                                             payloadValidator)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }
}
