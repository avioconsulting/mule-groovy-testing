package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.payload_types.IFetchAllowedPayloadTypes
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JsonFormatterImpl implements JsonFormatter, ISelectPrimaryTransformer {
    private final MuleContext muleContext
    private final ConnectorType connectorType
    private MuleMessageTransformer transformer
    private final IFetchAllowedPayloadTypes fetchAllowedPayloadTypes

    JsonFormatterImpl(MuleContext muleContext,
                      IFetchAllowedPayloadTypes fetchAllowedPayloadTypes,
                      ConnectorType connectorType) {
        this.fetchAllowedPayloadTypes = fetchAllowedPayloadTypes
        this.connectorType = connectorType
        this.muleContext = muleContext
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                connectorType,
                                                fetchAllowedPayloadTypes,
                                                Map)
        def output = new JacksonOutputTransformer(muleContext)
        this.transformer = new StandardTransformer(closure, input, output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                connectorType,
                                                fetchAllowedPayloadTypes,
                                                inputClass)
        def output = new JacksonOutputTransformer(muleContext)
        this.transformer = new StandardTransformer(closure, input, output)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }
}
