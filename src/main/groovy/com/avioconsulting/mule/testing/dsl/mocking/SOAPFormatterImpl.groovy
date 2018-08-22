package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.TransformerChain
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.xml.SoapFaultTransformer
import groovy.xml.MarkupBuilder
import org.xml.sax.InputSource

import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    // don't want to tie ourselves to a given version of CXF/ws by expressing a compile dependency
    @Lazy
    private static Class soapFaultClass = {
        try {
            SOAPFormatterImpl.classLoader.loadClass('org.apache.cxf.binding.soap.SoapFault')
        }
        catch (e) {
            throw new Exception('Was not able to load SoapFault properly. You need to have CXF in your project to use the XML functions. Consider adding the org.mule.modules:mule-module-cxf:jar:3.9.1 dependency with at least test scope to your project',
                                e)
        }
    }()

    private HttpConnectorErrorTransformer httpConnectorErrorTransformer
    private SoapFaultTransformer soapFaultTransformer

    SOAPFormatterImpl(EventFactory eventFactory,
                      IPayloadValidator payloadValidator) {
        super(eventFactory, payloadValidator)
        this.soapFaultTransformer = new SoapFaultTransformer()
    }

    def httpConnectError() {
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer()
        httpConnectorErrorTransformer.triggerConnectException()
        // avoid DSL weirdness
        return null
    }

    def httpTimeoutError() {
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer()
        httpConnectorErrorTransformer.triggerTimeoutException()
        // avoid DSL weirdness
        return null
    }

    @Override
    def soapFault(String message,
                  QName faultCode,
                  QName subCode,
                  Closure markupBuilderClosure) {
        def stringWriter = new StringWriter()
        def markupBuilder = new MarkupBuilder(stringWriter)
        markupBuilderClosure(markupBuilder)
        def dbf = DocumentBuilderFactory.newInstance()
        def db = dbf.newDocumentBuilder()
        def document = db.parse(new InputSource(new StringReader(stringWriter.toString())))
        def element = document.documentElement
        def soapFault = soapFaultClass.newInstance(message, faultCode)
        soapFault.detail = element
        soapFault.addSubCode(subCode)
        // for the last step, we need the MuleEvent
        def wsConsumerException = this.soapFaultTransformer.createSoapFaultException(soapFault)
        throw wsConsumerException
    }

    @Override
    MuleMessageTransformer getTransformer() {
        if (httpConnectorErrorTransformer) {
            return httpConnectorErrorTransformer
        }
        new TransformerChain(this.soapFaultTransformer,
                             super.transformer)
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new SOAPFormatterImpl(eventFactory,
                              validator)
    }
}
