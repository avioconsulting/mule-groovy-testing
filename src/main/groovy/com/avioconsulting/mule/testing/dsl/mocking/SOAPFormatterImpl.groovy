package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import groovy.xml.MarkupBuilder
import org.mule.api.MuleContext
import org.xml.sax.InputSource

import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    // don't want to tie ourselves to a given version of CXF/ws by expressing a compile dependency
    @Lazy
    private static Class soapFaultExceptionClass = {
        try {
            SOAPFormatterImpl.classLoader.loadClass('org.mule.module.ws.consumer.SoapFaultException')
        }
        catch (e) {
            throw new Exception('Was not able to load SoapFaultException properly. You need to have mule-module-ws in your project to use the XML functions. Consider adding the org.mule.modules:mule-module-ws:jar:3.9.1 dependency with at least test scope to your project',
                                e)
        }
    }()

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

    SOAPFormatterImpl(MuleContext muleContext,
                      IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
    }

    def httpConnectError() {
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(muleContext)
        httpConnectorErrorTransformer.triggerConnectException()
        // avoid DSL weirdness
        return null
    }

    def httpTimeoutError() {
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(muleContext)
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
        // TODO: need to arrange this to get event & message processor
        soapFaultExceptionClass.newInstance(muleEvent,
                                            soapFault,
                                            messageProcessor)
    }

    @Override
    MuleMessageTransformer getTransformer() {
        httpConnectorErrorTransformer ?: super.transformer
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new SOAPFormatterImpl(muleContext,
                              validator)
    }
}
