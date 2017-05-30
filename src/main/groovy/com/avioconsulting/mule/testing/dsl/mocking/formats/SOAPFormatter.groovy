package com.avioconsulting.mule.testing.dsl.mocking.formats

import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class SOAPFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext

    SOAPFormatter(MessageProcessorMocker messageProcessorMocker,
                  MuleContext muleContext) {
        this.messageProcessorMocker = messageProcessorMocker
        this.muleContext = muleContext
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {

    }
}
