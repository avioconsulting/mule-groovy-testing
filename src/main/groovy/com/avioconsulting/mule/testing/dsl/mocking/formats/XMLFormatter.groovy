package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class XMLFormatter {
    private final MessageProcessorMocker messageProcessorMocker
    private final MuleContext muleContext
    private final ConnectorType connectorType

    XMLFormatter(MessageProcessorMocker messageProcessorMocker,
                 MuleContext muleContext,
                 ConnectorType connectorType) {
        this.connectorType = connectorType
        this.messageProcessorMocker = messageProcessorMocker
        this.muleContext = muleContext
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        def soapTransformer = new XMLJAXBTransformer(closure,
                                                     muleContext,
                                                     inputJaxbClass,
                                                     connectorType)
        messageProcessorMocker.thenApply(soapTransformer)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        def transformer = new XMLMapTransformer(closure,
                                                muleContext,
                                                connectorType)
        messageProcessorMocker.thenApply(transformer)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        def transformer = new XMLGroovyParserTransformer(closure,
                                                         muleContext,
                                                         connectorType)
        messageProcessorMocker.thenApply(transformer)
    }
}
