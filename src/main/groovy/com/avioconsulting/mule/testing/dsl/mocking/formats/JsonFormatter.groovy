package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.avioconsulting.mule.testing.transformers.QueryParamSpy
import com.avioconsulting.mule.testing.transformers.json.JSONJacksonRequestReplyTransformer
import com.avioconsulting.mule.testing.transformers.json.JSONMapRequestReplyTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class JsonFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType
    private final MockedConnectorType mockedConnectorType
    private final MunitSpy spy

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
    }

    def whenCalledWithQueryParams(Closure closure) {
        def transformer = new OutputTransformer() {
            @Override
            MuleMessage transformOutput(Object input) {
                assert input instanceof Map
                def transformer = new JSONMapRequestReplyTransformer(closure,
                                                                     muleContext,
                                                                     expectedPayloadType,
                                                                     mockedConnectorType)
                transformer.transformOutput(input)
            }
        }
        // TODO: Pass connector name in from the constructor
        // TODO: Detect whether a Map or Jackson object was passed in and call the appropriate transformer
        // TODO: in the anon class above
        def queryParamSpy = new QueryParamSpy('SomeSystem Call',
                                              closure,
                                              '[]',
                                              transformer)
        spy.before(queryParamSpy)
        this.messageProcessorMocker.thenApply(queryParamSpy)
    }

    def whenCalledWithMap(Closure closure) {
        def transformer = new JSONMapRequestReplyTransformer(closure,
                                                             muleContext,
                                                             expectedPayloadType,
                                                             mockedConnectorType)
        this.messageProcessorMocker.thenApply(transformer)
    }

    def whenCalledWithJackson(Class inputClass,
                              Closure closure) {
        def transformer = new JSONJacksonRequestReplyTransformer(closure,
                                                                 muleContext,
                                                                 inputClass,
                                                                 expectedPayloadType,
                                                                 mockedConnectorType)
        this.messageProcessorMocker.thenApply(transformer)
    }
}
