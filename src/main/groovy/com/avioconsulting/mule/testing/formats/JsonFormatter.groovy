package com.avioconsulting.mule.testing.formats

import com.avioconsulting.mule.testing.transformers.JSONRequestReplyTransformer
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

    def whenCalledViaMap(Closure closure) {
        def transformer = new JSONRequestReplyTransformer(closure, muleContext)
        this.messageProcessorMocker.thenApply(transformer)
    }
}
