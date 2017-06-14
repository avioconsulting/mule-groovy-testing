package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.HttpConnectorSpy
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.QueryParamTransformer
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class JsonFormatterImpl implements JsonFormatter, ISelectPrimaryTransformer {
    private final MuleContext muleContext
    private final List<Class> allowedPayloadTypes
    private final ConnectorType connectorType
    private final HttpConnectorSpy spy
    private MuleMessageTransformer transformer

    JsonFormatterImpl(HttpConnectorSpy spy,
                      MuleContext muleContext,
                      List<Class> allowedPayloadTypes,
                      ConnectorType connectorType) {
        this.spy = spy
        this.connectorType = connectorType
        this.allowedPayloadTypes = allowedPayloadTypes
        this.muleContext = muleContext
    }

    // TODO: Move this outside of JSON formatter, is an HTTP only thing
    def whenCalledWithQueryParams(@DelegatesTo(QueryParamOptions) Closure closure) {
        if (connectorType == ConnectorType.VM) {
            throw new Exception('only supported in HTTP!')
        }
        def transformer = new OutputTransformer() {
            @Override
            MuleMessage transformOutput(Object input) {
                def outputTransformer = new JacksonOutputTransformer(muleContext)
                outputTransformer.transformOutput(input)
            }

            def disableStreaming() {
            }
        }
        this.transformer = new QueryParamTransformer(spy,
                                                     closure,
                                                     transformer,
                                                     muleContext)
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                connectorType,
                                                allowedPayloadTypes,
                                                Map)
        def output = new JacksonOutputTransformer(muleContext)
        this.transformer = new StandardTransformer(closure, input, output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                connectorType,
                                                allowedPayloadTypes,
                                                inputClass)
        def output = new JacksonOutputTransformer(muleContext)
        this.transformer = new StandardTransformer(closure, input, output)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }
}
