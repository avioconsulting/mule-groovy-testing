package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.transformers.JSONJacksonRequestReplyTransformer
import com.avioconsulting.mule.testing.transformers.JSONMapRequestReplyTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class JsonFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType

    JsonFormatter(MessageProcessorMocker messageProcessorMocker,
                  MuleContext muleContext,
                  Class expectedPayloadType) {
        this.expectedPayloadType = expectedPayloadType
        this.muleContext = muleContext
        this.messageProcessorMocker = messageProcessorMocker
    }

    def whenCalledWithMap(Closure closure) {
        def transformer = new JSONMapRequestReplyTransformer(closure,
                                                             muleContext,
                                                             expectedPayloadType)
        this.messageProcessorMocker.thenApply(transformer)
    }

    def whenCalledWithJackson(Class inputClass,
                              Closure closure) {
        def transformer = new JSONJacksonRequestReplyTransformer(closure,
                                                                 muleContext,
                                                                 inputClass,
                                                                 expectedPayloadType)
        this.messageProcessorMocker.thenApply(transformer)
    }
}
