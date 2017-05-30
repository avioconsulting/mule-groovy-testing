package com.avioconsulting.mule.testing.transformers.xml

import org.apache.cxf.staxutils.DepthXMLStreamReader
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

import javax.xml.bind.JAXBElement
import javax.xml.stream.XMLInputFactory

class SOAPTransformer implements MuleMessageTransformer {
    private final Closure closure
    private final MuleContext muleContext
    private final JAXBMarshalHelper helper

    SOAPTransformer(Closure closure,
                    MuleContext muleContext,
                    Class inputJaxbClass) {

        this.muleContext = muleContext
        this.closure = closure
        this.helper = new JAXBMarshalHelper(inputJaxbClass)
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        def payload = incomingMessage.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        if (nullPayload) {
            println 'Groovy Test WARNING: SOAP mock was sent a message with empty payload! using MuleMessage payload.'
            strongTypedPayload = incomingMessage
        } else {
            strongTypedPayload = helper.unmarshal(incomingMessage)
        }
        def reply = this.closure(strongTypedPayload)
        assert reply instanceof JAXBElement
        getXmlMessageFromJaxbElement(reply,
                                     200)
    }

    private MuleMessage getXmlMessageFromJaxbElement(JAXBElement jaxbElement,
                                                     Integer httpStatus) {
        def reader = helper.getMarshalled(jaxbElement)
        getXmlMessage reader, httpStatus
    }

    // incoming messages will not have a status
    private MuleMessage getXmlMessage(readerOrStream,
                                      Integer httpStatus = null) {
        def payload = getNormalXmlPayload(readerOrStream)
        constructXmlTypeMessage(httpStatus, payload)
    }

    private static getNormalXmlPayload(readerOrStream) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader readerOrStream
        new DepthXMLStreamReader(xmlReader)
    }

    private MuleMessage constructXmlTypeMessage(Integer httpStatus,
                                                payload) {
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
