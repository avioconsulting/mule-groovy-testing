package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.transformers.xml.XMLTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class XMLFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext

    XMLFormatter(MessageProcessorMocker messageProcessorMocker,
                 MuleContext muleContext) {
        this.messageProcessorMocker = messageProcessorMocker
        this.muleContext = muleContext
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        def soapTransformer = new XMLTransformer(closure,
                                                 muleContext,
                                                 inputJaxbClass)
        messageProcessorMocker.thenApply(soapTransformer)
    }
}
