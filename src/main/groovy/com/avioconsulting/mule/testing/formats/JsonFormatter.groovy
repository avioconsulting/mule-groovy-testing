package com.avioconsulting.mule.testing.formats

import com.avioconsulting.mule.testing.transformers.JSONJacksonRequestReplyTransformer
import com.avioconsulting.mule.testing.transformers.JSONMapRequestReplyTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class JsonFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext

    JsonFormatter(MessageProcessorMocker messageProcessorMocker,
                  MuleContext muleContext) {
        this.muleContext = muleContext
        this.messageProcessorMocker = messageProcessorMocker
    }

    def whenCalledWithMap(Closure closure) {
        def transformer = new JSONMapRequestReplyTransformer(closure, muleContext)
        this.messageProcessorMocker.thenApply(transformer)
    }

    def whenCalledWithJackson(Class inputClass,
                              Closure closure) {
        def transformer = new JSONJacksonRequestReplyTransformer(closure,
                                                                 muleContext,
                                                                 inputClass)
        this.messageProcessorMocker.thenApply(transformer)
    }
}
