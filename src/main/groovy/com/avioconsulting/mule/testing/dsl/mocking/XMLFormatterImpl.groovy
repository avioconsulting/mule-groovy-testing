package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer

class XMLFormatterImpl<T extends ConnectorInfo> implements
        XMLFormatter,
        IFormatter<T> {
    protected MuleMessageTransformer<T> transformer
    private final IPayloadValidator<T> payloadValidator
    private final String transformerUse

    XMLFormatterImpl(IPayloadValidator<T> payloadValidator,
                     String transformerUse) {
        this.transformerUse = transformerUse
        this.payloadValidator = payloadValidator
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        transformer = new XMLJAXBTransformer<T>(closure,
                                                inputJaxbClass,
                                                payloadValidator,
                                                transformerUse)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        transformer = new XMLMapTransformer(closure,
                                            payloadValidator)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        transformer = new XMLGroovyParserTransformer(closure,
                                                     payloadValidator)
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }

    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new XMLFormatterImpl(eventFactory, validator)
    }

    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
