package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import com.avioconsulting.mule.testing.transformers.json.JSONJacksonRequestReplyTransformer
import com.avioconsulting.mule.testing.transformers.json.JSONMapRequestReplyTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class JsonFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType
    private final MockedConnectorType mockedConnectorType

    JsonFormatter(MessageProcessorMocker messageProcessorMocker,
                  MuleContext muleContext,
                  Class expectedPayloadType,
                  MockedConnectorType mockedConnectorType) {
        this.mockedConnectorType = mockedConnectorType
        this.expectedPayloadType = expectedPayloadType
        this.muleContext = muleContext
        this.messageProcessorMocker = messageProcessorMocker
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
