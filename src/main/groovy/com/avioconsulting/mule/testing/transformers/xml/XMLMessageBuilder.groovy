package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.EventFactory
import org.mule.DefaultMuleMessage
import org.mule.api.MuleEvent

import javax.xml.stream.XMLInputFactory

class XMLMessageBuilder {
    // don't want to tie ourselves to a given version of CXF by expressing a compile dependency
    @Lazy
    private static Class depthXmlStreamReaderKlass = {
        try {
            XMLMessageBuilder.classLoader.loadClass('org.apache.cxf.staxutils.DepthXMLStreamReader')
        }
        catch (e) {
            throw new Exception('Was not able to load DepthXMLStreamReader properly. You need to have CXF in your project to use the XML functions. Consider adding the org.mule.modules:mule-module-cxf:jar:3.9.1 dependency with at least test scope to your project',
                                e)
        }
    }()

    @Lazy
    private static Class apikitStreamReaderClass = {
        try {
            XMLMessageBuilder.classLoader.loadClass('org.mule.module.soapkit.NamespaceRestorerXMLStreamReader')
        }
        catch (e) {
            throw new Exception('Was not able to load NamespaceRestorerXMLStreamReader properly. You need to have CXF in your project to use the XML functions. Consider adding the org.mule.modules:mule-module-apikit-soap:1.0.3 dependency with at least test scope to your project',
                                e)
        }
    }()

    private final EventFactory eventFactory
    private final boolean wrapWithApiKitStreamReader

    XMLMessageBuilder(EventFactory eventFactory,
                      boolean wrapWithApiKitStreamReader) {
        this.wrapWithApiKitStreamReader = wrapWithApiKitStreamReader
        this.eventFactory = eventFactory
    }

    MuleEvent build(Reader reader,
                    Integer httpStatus = null) {
        def payload = getPayload(reader)
        constructXMLMessage(httpStatus, payload)
    }

    private static getPayload(InputStream stream) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader stream
        depthXmlStreamReaderKlass.newInstance(xmlReader)
    }

    private getPayload(Reader reader) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader reader
        // SOAP Apikit flows have the xmlReader, which is usually a com.ctc.wstx.sr.ValidatingStreamReader
        // wrapped with apikitStreamReaderClass
        if (this.wrapWithApiKitStreamReader) {
            xmlReader = apikitStreamReaderClass.newInstance(xmlReader)
        }
        depthXmlStreamReaderKlass.newInstance(xmlReader)
    }

    private MuleEvent constructXMLMessage(Integer httpStatus,
                                          Object payload) {
        // need some of these props for SOAP mock to work properly
        def messageProps = [
                'content-type': 'text/xml; charset=utf-8'
        ]
        if (httpStatus != null) {
            messageProps['http.status'] = httpStatus
        }
        def outboundProps = null
        def attachments = null
        new DefaultMuleMessage(payload,
                               messageProps,
                               outboundProps,
                               attachments,
                               this.muleContext)
    }

}