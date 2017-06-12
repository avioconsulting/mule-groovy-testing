package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import org.mule.api.MuleContext

class XMLFormatter {
    private final MuleContext muleContext
    private final ConnectorType connectorType

    XMLFormatter(MuleContext muleContext,
                 ConnectorType connectorType) {
        this.connectorType = connectorType
        this.muleContext = muleContext
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        new XMLJAXBTransformer(closure,
                               muleContext,
                               inputJaxbClass,
                               connectorType)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        new XMLMapTransformer(closure,
                              muleContext,
                              connectorType)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        new XMLGroovyParserTransformer(closure,
                                       muleContext,
                                       connectorType)
    }
}
