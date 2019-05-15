package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder.MessageType
import com.avioconsulting.mule.testing.transformers.xml.input.GroovyInputTransformer
import com.avioconsulting.mule.testing.transformers.xml.input.JaxbInputTransformer
import com.avioconsulting.mule.testing.transformers.xml.input.MapInputTransformer
import com.avioconsulting.mule.testing.transformers.xml.output.GroovyOutputTransformer
import com.avioconsulting.mule.testing.transformers.xml.output.JaxbOutputTransformer
import com.avioconsulting.mule.testing.transformers.xml.output.MapOutputTransformer

class XMLFormatterImpl<T extends ConnectorInfo> implements
        XMLFormatter,
        IFormatter<T> {
    protected MuleMessageTransformer<T> transformer
    private final MessageType messageType
    private final ClosureCurrier<T> closureCurrier

    XMLFormatterImpl(MessageType messageType = MessageType.Mule41Stream) {
        // most of the time, this should be a sensible default
        this.messageType = messageType
        this.closureCurrier = new ClosureCurrier<>()
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        def helper = new JAXBMarshalHelper(inputJaxbClass)
        def t = new StandardTransformer(closure,
                                        closureCurrier,
                                        new JaxbInputTransformer<T>(helper),
                                        new JaxbOutputTransformer(messageType,
                                                                  helper))
        this.transformer = t
    }

    def whenCalledWithMapAsXml(Closure closure) {
        def t = new StandardTransformer(closure,
                                        closureCurrier,
                                        new MapInputTransformer(),
                                        new MapOutputTransformer(messageType))
        this.transformer = t
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        def t = new StandardTransformer(closure,
                                        closureCurrier,
                                        new GroovyInputTransformer<T>(),
                                        new GroovyOutputTransformer(messageType))
        this.transformer = t
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }
}
