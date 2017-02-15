package com.avioconsulting.muletesting.transformers

import com.avioconsulting.muletesting.ResourceFetcher
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.MessageProcessorMocker

import javax.xml.bind.JAXBContext

class XmlRequestReplyTransformer implements MuleMessageTransformer {
    private final def testClosure
    private final MessageProcessorMocker mock
    private final def alternateFetcher
    private def lastStrongPayload
    private final ResourceFetcher mockResourceFetcher
    private final JAXBContext jaxbContext
    private final def messageFromPayload

    def XmlRequestReplyTransformer(MessageProcessorMocker mock,
                                   JAXBContext jaxbContext,
                                   alternateFetcher,
                                   ResourceFetcher mockResourceFetcher,
                                   messageFromPayload,
                                   testClosure) {
        this.messageFromPayload = messageFromPayload
        this.jaxbContext = jaxbContext
        this.lastStrongPayload = null
        this.mock = mock
        this.alternateFetcher = alternateFetcher
        this.testClosure = testClosure
        this.mockResourceFetcher = mockResourceFetcher
    }

    MuleMessage transform(MuleMessage incomingMessage) {
        def payload = incomingMessage.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        def alternate = alternateFetcher != null ? alternateFetcher() : null
        if (nullPayload && alternate != null) {
            println "Fetching message via alternate scope (${alternate[0]})"
            this.lastStrongPayload = strongTypedPayload = unmarshal alternate[1]
        } else if (nullPayload && this.lastStrongPayload != null) {
            strongTypedPayload = this.lastStrongPayload
            println "Groovy Test INFO: SOAP mock was sent a message with empty payload. Assuming this is SOAP call is in an until-successful block, so returning last payload of ${this.lastStrongPayload}"
        } else if (nullPayload && this.lastStrongPayload == null) {
            println 'Groovy Test WARNING: SOAP mock was sent a message with empty payload! using MuleMessage payload.'
            strongTypedPayload = incomingMessage
        } else {
            this.lastStrongPayload = strongTypedPayload = unmarshal incomingMessage
        }
        def replyResource = testClosure strongTypedPayload
        return getMockedXmlMessageReply(replyResource)
    }

    private def unmarshal(MuleMessage message) {
        def unmarshaller = this.jaxbContext.createUnmarshaller()
        // until successful/alternate path is a string
        def stream = message.payload instanceof String ? new StringReader(message.payload) : message.payload
        try {
            unmarshaller.unmarshal(stream).value
        }
        catch (e) {
            throw new Exception('SOAP Mocks: Unable to marshal message. Do you need a different JAXB context?', e)
        }
    }

    private def MuleMessage getMockedXmlMessageReply(String resource) {
        println "Constructing mock XML message with resource ${resource}"
        def stream = this.mockResourceFetcher.getResource(resource)
        def httpStatus = 200
        this.messageFromPayload stream, httpStatus
    }
}
