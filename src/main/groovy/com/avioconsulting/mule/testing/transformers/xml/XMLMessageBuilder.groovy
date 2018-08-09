package com.avioconsulting.mule.testing.transformers.xml

import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

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

    private final MuleContext muleContext

    XMLMessageBuilder(MuleContext muleContext) {
        this.muleContext = muleContext
    }

    MuleMessage build(Reader reader,
                      Integer httpStatus = null) {
        def payload = getPayload(reader)
        constructXMLMessage(httpStatus, payload)
    }

    private static getPayload(InputStream stream) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader stream
        depthXmlStreamReaderKlass.newInstance(xmlReader)
    }

    private static getPayload(Reader reader) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader reader
        depthXmlStreamReaderKlass.newInstance(xmlReader)
    }

    private MuleMessage constructXMLMessage(Integer httpStatus,
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