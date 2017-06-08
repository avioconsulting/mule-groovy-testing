package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.dsl.mocking.QueryParamOptions
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.QueryParamSpy
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.munit.common.mocking.MunitSpy

class JsonFormatter {
    private final MuleContext muleContext
    private final List<Class> allowedPayloadTypes
    private final ConnectorType connectorType
    private final MunitSpy spy
    private final ProcessorLocator processorLocator

    JsonFormatter(MunitSpy spy,
                  ProcessorLocator processorLocator,
                  MuleContext muleContext,
                  List<Class> allowedPayloadTypes,
                  ConnectorType connectorType) {
        this.processorLocator = processorLocator
        this.spy = spy
        this.connectorType = connectorType
        this.allowedPayloadTypes = allowedPayloadTypes
        this.muleContext = muleContext
    }

    def whenCalledWithQueryParams(@DelegatesTo(QueryParamOptions) Closure closure) {
        def transformer = new OutputTransformer() {
            @Override
            MuleMessage transformOutput(Object input) {
                def outputTransformer = new JacksonOutputTransformer(muleContext)
                outputTransformer.transformOutput(input)
            }

            def disableStreaming() {
            }
        }
        def queryParamSpy = new QueryParamSpy(processorLocator,
                                              closure,
                                              transformer,
                                              muleContext)
        spy.before(queryParamSpy)
        queryParamSpy
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                connectorType,
                                                allowedPayloadTypes,
                                                Map)
        def output = new JacksonOutputTransformer(muleContext)
        new StandardTransformer(closure, input, output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer(muleContext,
                                                connectorType,
                                                allowedPayloadTypes,
                                                inputClass)
        def output = new JacksonOutputTransformer(muleContext)
        new StandardTransformer(closure, input, output)
    }
}
