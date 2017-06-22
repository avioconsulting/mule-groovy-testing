package com.avioconsulting.mule.testing.transformers.xml

import org.apache.cxf.staxutils.DepthXMLStreamReader
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

import javax.xml.stream.XMLInputFactory

class XMLMessageBuilder {
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
        new DepthXMLStreamReader(xmlReader)
    }

    private static getPayload(Reader reader) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader reader
        new DepthXMLStreamReader(xmlReader)
    }

    private MuleMessage constructXMLMessage(Integer httpStatus,
                                            DepthXMLStreamReader payload) {
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