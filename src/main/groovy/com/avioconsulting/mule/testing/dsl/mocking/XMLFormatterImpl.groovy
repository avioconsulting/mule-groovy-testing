package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class XMLFormatterImpl implements XMLFormatter, ISelectPrimaryTransformer {
    private final MuleContext muleContext
    private final ConnectorType connectorType
    private MuleMessageTransformer transformer

    XMLFormatterImpl(MuleContext muleContext,
                     ConnectorType connectorType) {
        this.connectorType = connectorType
        this.muleContext = muleContext
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        transformer = new XMLJAXBTransformer(closure,
                                             muleContext,
                                             inputJaxbClass,
                                             connectorType)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        transformer = new XMLMapTransformer(closure,
                                            muleContext,
                                            connectorType)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        transformer = new XMLGroovyParserTransformer(closure,
                                                     muleContext,
                                                     connectorType)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }
}
