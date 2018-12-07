package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder.MessageType

class XMLFormatterImpl<T extends ConnectorInfo> implements
        XMLFormatter,
        IFormatter<T> {
    protected MuleMessageTransformer<T> transformer
    private final MessageType messageType

    XMLFormatterImpl(MessageType messageType = MessageType.Mule41Stream) {
        // most of the time, this should be a sensible default
        this.messageType = messageType
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        transformer = new XMLJAXBTransformer<T>(closure,
                                                inputJaxbClass,
                                                messageType)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        transformer = new XMLMapTransformer(closure,
                                            messageType)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        transformer = new XMLGroovyParserTransformer(closure,
                                                     messageType)
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }
}
