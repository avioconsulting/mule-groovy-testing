package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer

class XMLFormatterImpl implements XMLFormatter, IFormatter {
    protected MuleMessageTransformer transformer
    private final IPayloadValidator payloadValidator
    protected final EventFactory eventFactory
    private final String transformerUse

    XMLFormatterImpl(EventFactory eventFactory,
                     IPayloadValidator payloadValidator,
                     String transformerUse) {
        this.transformerUse = transformerUse
        this.eventFactory = eventFactory
        this.payloadValidator = payloadValidator
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        transformer = new XMLJAXBTransformer(closure,
                                             eventFactory,
                                             inputJaxbClass,
                                             payloadValidator,
                                             transformerUse)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        transformer = new XMLMapTransformer(closure,
                                            eventFactory,
                                            payloadValidator)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        transformer = new XMLGroovyParserTransformer(closure,
                                                     eventFactory,
                                                     payloadValidator)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }

    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new XMLFormatterImpl(eventFactory, validator)
    }

    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
