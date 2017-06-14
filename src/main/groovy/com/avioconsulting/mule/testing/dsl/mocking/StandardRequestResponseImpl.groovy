package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payload_types.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

abstract class StandardRequestResponseImpl implements StandardRequestResponse {
    protected final TransformerChain transformerChain
    protected final MuleContext muleContext
    protected final IPayloadValidator fetchAllowedPayloadTypes

    StandardRequestResponseImpl(MessageProcessorMocker muleMocker,
                                MuleContext muleContext,
                                IPayloadValidator fetchAllowedPayloadTypes) {
        this.fetchAllowedPayloadTypes = fetchAllowedPayloadTypes
        this.muleContext = muleContext
        this.transformerChain = new TransformerChain(muleMocker)
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatterImpl(this.muleContext,
                                              fetchAllowedPayloadTypes)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        def formatter = new XMLFormatterImpl(this.muleContext)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }
}
