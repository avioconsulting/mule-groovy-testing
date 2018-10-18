package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder.MessageType

class XMLFormatterImpl<T extends ConnectorInfo> implements
        XMLFormatter,
        IFormatter<T> {
    protected MuleMessageTransformer<T> transformer
    private final String transformerUse
    private final MessageType messageType

    XMLFormatterImpl(String transformerUse,
                     // most of the time, this should be a sensible default
                     MessageType messageType = MessageType.Mule41Stream) {
        this.messageType = messageType
        this.transformerUse = transformerUse
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        transformer = new XMLJAXBTransformer<T>(closure,
                                                inputJaxbClass,
                                                transformerUse,
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
