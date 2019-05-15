package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder.MessageType
import com.avioconsulting.mule.testing.transformers.xml.XMLTransformer

class XMLFormatterImpl<T extends ConnectorInfo> implements
        XMLFormatter,
        IFormatter<T> {
    protected MuleMessageTransformer<T> transformer
    private XMLTransformer xmlTransformer
    private final MessageType messageType

    XMLFormatterImpl(MessageType messageType = MessageType.Mule41Stream) {
        // most of the time, this should be a sensible default
        this.messageType = messageType
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        def t = new XMLJAXBTransformer<T>(closure,
                                          inputJaxbClass,
                                          messageType)
        this.transformer = t
        this.xmlTransformer = t
    }

    def whenCalledWithMapAsXml(Closure closure) {
        def t = new XMLMapTransformer(closure,
                                      messageType)
        this.transformer = t
        this.xmlTransformer = t
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        def t = new XMLGroovyParserTransformer(closure,
                                               messageType)
        this.transformer = t
        this.xmlTransformer = t
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }
}
