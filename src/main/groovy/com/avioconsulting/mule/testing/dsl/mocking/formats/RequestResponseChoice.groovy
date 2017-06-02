package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.mocking.MockedConnectorType
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class RequestResponseChoice {
    private final MessageProcessorMocker muleMocker
    private final MuleContext muleContext
    private final Class expectedPayloadType
    private final MockedConnectorType mockedConnectorType

    RequestResponseChoice(MessageProcessorMocker muleMocker,
                          MuleContext muleContext,
                          Class expectedPayloadType,
                          MockedConnectorType mockedConnectorType) {
        this.mockedConnectorType = mockedConnectorType
        this.expectedPayloadType = expectedPayloadType
        this.muleContext = muleContext
        this.muleMocker = muleMocker
    }

    def json(@DelegatesTo(JsonFormatter) Closure closure) {
        def formatter = new JsonFormatter(this.muleMocker,
                                          this.muleContext,
                                          expectedPayloadType,
                                          mockedConnectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    def xml(@DelegatesTo(XMLFormatter) Closure closure) {
        def formatter = new XMLFormatter(this.muleMocker,
                                         this.muleContext,
                                         mockedConnectorType)
        def code = closure.rehydrate(formatter, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }
}
