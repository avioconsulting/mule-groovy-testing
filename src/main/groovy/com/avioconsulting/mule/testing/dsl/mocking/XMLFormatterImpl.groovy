package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.XMLGroovyParserTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLJAXBTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMapTransformer
import org.mule.api.MuleContext
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer

class XMLFormatterImpl implements XMLFormatter, IFormatter {
    protected final MuleContext muleContext
    protected MuleMessageTransformer transformer
    private final IPayloadValidator payloadValidator

    XMLFormatterImpl(MuleContext muleContext,
                     IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
    }

    def whenCalledWithJaxb(Class inputJaxbClass,
                           Closure closure) {
        transformer = new XMLJAXBTransformer(closure,
                                             muleContext,
                                             inputJaxbClass,
                                             payloadValidator)
    }

    def whenCalledWithMapAsXml(Closure closure) {
        transformer = new XMLMapTransformer(closure,
                                            muleContext,
                                            payloadValidator)
    }

    def whenCalledWithGroovyXmlParser(Closure closure) {
        transformer = new XMLGroovyParserTransformer(closure,
                                                     muleContext,
                                                     payloadValidator)
    }

    MuleMessageTransformer getTransformer() {
        transformer
    }

    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new XMLFormatterImpl(muleContext, validator)
    }

    IPayloadValidator getPayloadValidator() {
        payloadValidator
    }
}
