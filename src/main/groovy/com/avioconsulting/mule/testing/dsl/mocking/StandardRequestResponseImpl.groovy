package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.payload_types.IFetchAllowedPayloadTypes
import com.avioconsulting.mule.testing.transformers.TransformerChain
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

abstract class StandardRequestResponseImpl implements StandardRequestResponse {
    protected final TransformerChain transformerChain
    protected final MuleContext muleContext
    private final ConnectorType connectorType
    private final IFetchAllowedPayloadTypes fetchAllowedPayloadTypes

    StandardRequestResponseImpl(MessageProcessorMocker muleMocker,
                                MuleContext muleContext,
                                IFetchAllowedPayloadTypes fetchAllowedPayloadTypes,
                                ConnectorType connectorType) {
        this.fetchAllowedPayloadTypes = fetchAllowedPayloadTypes
        this.connectorType = connectorType
        this.muleContext = muleContext
        this.transformerChain = new TransformerChain(muleMocker)
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatterImpl(this.muleContext,
                                              fetchAllowedPayloadTypes,
                                              connectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        def formatter = new XMLFormatterImpl(this.muleContext,
                                             connectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        this.transformerChain.addTransformer(formatter.transformer)
    }
}
