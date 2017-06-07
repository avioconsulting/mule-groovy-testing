package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.input.MapInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.MapOutputTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class JsonFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType
    private final MockedConnectorType mockedConnectorType
    private final MunitSpy spy
    private final StandardTransformer standardTransformer

    JsonFormatter(MessageProcessorMocker messageProcessorMocker,
                  MunitSpy spy,
                  MuleContext muleContext,
                  Class expectedPayloadType,
                  MockedConnectorType mockedConnectorType) {
        this.spy = spy
        this.mockedConnectorType = mockedConnectorType
        this.expectedPayloadType = expectedPayloadType
        this.muleContext = muleContext
        this.messageProcessorMocker = messageProcessorMocker
        standardTransformer = new StandardTransformer()
        messageProcessorMocker.thenApply(standardTransformer)
    }

    def whenCalledWithQueryParams(Closure closure) {
//        def transformer = new OutputTransformer() {
//            @Override
//            MuleMessage transformOutput(Object input) {
//                assert input instanceof Map
//                def transformer = new JSONMapRequestReplyTransformer(closure,
//                                                                     muleContext,
//                                                                     expectedPayloadType,
//                                                                     mockedConnectorType)
//                transformer.transformOutput(input)
//            }
//        }
//        // TODO: Pass connector name in from the constructor
//        // TODO: Detect whether a Map or Jackson object was passed in and call the appropriate transformer
//        // TODO: in the anon class above
//        def queryParamSpy = new QueryParamSpy('SomeSystem Call',
//                                              closure,
//                                              '[]',
//                                              transformer)
//        spy.before(queryParamSpy)
//        this.messageProcessorMocker.thenApply(queryParamSpy)
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
