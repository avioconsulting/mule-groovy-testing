package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.QueryParamSpy
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.MapInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.MapOutputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class JsonFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType
    private final MockedConnectorType mockedConnectorType
    private final MunitSpy spy
    private final StandardTransformer standardTransformer
    private final ProcessorLocator processorLocator

    JsonFormatter(MessageProcessorMocker messageProcessorMocker,
                  MunitSpy spy,
                  ProcessorLocator processorLocator,
                  MuleContext muleContext,
                  Class expectedPayloadType,
                  MockedConnectorType mockedConnectorType) {
        this.processorLocator = processorLocator
        this.spy = spy
        this.mockedConnectorType = mockedConnectorType
        this.expectedPayloadType = expectedPayloadType
        this.muleContext = muleContext
        this.messageProcessorMocker = messageProcessorMocker
        standardTransformer = new StandardTransformer()
        messageProcessorMocker.thenApply(standardTransformer)
    }

    def whenCalledWithQueryParams(Closure closure) {
        def transformer = new OutputTransformer() {
            @Override
            MuleMessage transformOutput(Object input) {
                // TODO: Detect whether a Map or Jackson object was passed in and call the appropriate transformer
                assert input instanceof Map
                def transformer = new MapOutputTransformer(muleContext)
                transformer.transformOutput(input)
            }
        }
        def queryParamSpy = new QueryParamSpy(processorLocator,
                                              closure,
                                              '[]',
                                              transformer)
        spy.before(queryParamSpy)
        this.messageProcessorMocker.thenApply(queryParamSpy)
    }

    def whenCalledWithMap(Closure closure) {
        standardTransformer.inputTransformer = new MapInputTransformer(muleContext,
                                                                       mockedConnectorType,
                                                                       expectedPayloadType)
        standardTransformer.closure = closure
        standardTransformer.outputTransformer = new MapOutputTransformer(muleContext)
    }

    def whenCalledWithJackson(Class inputClass,
                              Closure closure) {
        standardTransformer.inputTransformer = new JacksonInputTransformer(muleContext,
                                                                           mockedConnectorType,
                                                                           expectedPayloadType,
                                                                           inputClass)
        standardTransformer.closure = closure
        standardTransformer.outputTransformer = new JacksonOutputTransformer(muleContext)
    }
}
