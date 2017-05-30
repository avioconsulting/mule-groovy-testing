package com.avioconsulting.mule.testing.dsl.mocking.formats

import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class RequestResponseChoice {
    private final MessageProcessorMocker muleMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType

    RequestResponseChoice(MessageProcessorMocker muleMocker,
                          MuleContext muleContext,
                          Class expectedPayloadType) {
        this.expectedPayloadType = expectedPayloadType
        this.muleContext = muleContext
        this.muleMocker = muleMocker
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatter(this.muleMocker,
                                          this.muleContext,
                                          expectedPayloadType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }
}
