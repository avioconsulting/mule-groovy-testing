package com.avioconsulting.muletesting

import org.apache.cxf.staxutils.DepthXMLStreamReader
import org.glassfish.grizzly.memory.HeapMemoryManager
import org.glassfish.grizzly.utils.BufferInputStream
import com.avioconsulting.muletesting.transformers.XmlRequestReplyTransformer
import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.munit.common.mocking.Attribute
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.util.MunitMuleTestUtils

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.stream.XMLInputFactory

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

trait XmlTesting {
    static XMLGregorianCalendar getXmlDate(int year, int oneBasedMonth, int dayOfMonth) {
        def zeroBasedMonth = oneBasedMonth - 1
        def gregorian = new GregorianCalendar(year, zeroBasedMonth, dayOfMonth)
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }

    static XMLGregorianCalendar getXmlDateTime(int year, int oneBasedMonth, int dayOfMonth, int hourOfDay, int minute, int second = 0, String timeZoneId) {
        def zeroBasedMonth = oneBasedMonth - 1
        def gregorian = new GregorianCalendar(year, zeroBasedMonth, dayOfMonth, hourOfDay, minute, second)
        gregorian.setTimeZone(TimeZone.getTimeZone(timeZoneId))
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian)
    }

    def runMuleFlowWithXml(String flow, JAXBElement jaxbElement, boolean unmarshalResult = true, Integer expectedHttpStatus = 200, Map<String, Object> flowVars = [:]) {
        def message = getXmlMessageFromJaxbElement jaxbElement
        def inputEvent = new DefaultMuleEvent(message, MessageExchangePattern.REQUEST_RESPONSE, MunitMuleTestUtils.getTestFlow(muleContext))
        flowVars.each { entry ->
            inputEvent.setFlowVariable(entry.key, entry.value)
        }
        MuleEvent responseEvent = this.runFlow(flow, inputEvent)
        MuleMessage responseMessage = responseEvent.message
        assertThat responseMessage.getInboundProperty('http.status'),
                   is(equalTo(expectedHttpStatus))
        unmarshalResult ? unmarshalXmlResponse(responseMessage) : responseMessage
    }

    def unmarshalXmlResponse(MuleMessage message) {
        try {
            def unmarshaller = this.jaxbContext.createUnmarshaller()
            def factory = XMLInputFactory.newInstance()
            def xmlReader = factory.createXMLStreamReader message.payload
            return unmarshaller.unmarshal(xmlReader).value
        }
        catch (e) {
            throw new Exception('Unable to unmarshal SOAP response. Problem during flow execution?', e)
        }
    }

    def static MuleMessage getXmlErrorMessage(BufferedInputStream stream, Integer httpStatus) {
        // if you throw an exception (simulating failure) from a mock callback, Mule returns it to the catch
        // exception strategy as a BufferInputStream
        byte[] xmlBytes = stream.bytes
        def heapManager = new HeapMemoryManager()
        def buffer = heapManager.wrap(xmlBytes)
        def payload = new BufferInputStream(buffer)
        constructXmlTypeMessage(httpStatus, payload)
    }

    // incoming messages will not have a status
    def static MuleMessage getXmlMessage(readerOrStream, Integer httpStatus = null) {
        def payload = getNormalXmlPayload(readerOrStream)
        constructXmlTypeMessage(httpStatus, payload)
    }

    private static MuleMessage constructXmlTypeMessage(Integer httpStatus, payload) {
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

    private static getNormalXmlPayload(readerOrStream) {
        def factory = XMLInputFactory.newInstance()
        def xmlReader = factory.createXMLStreamReader readerOrStream
        new DepthXMLStreamReader(xmlReader)
    }

    private def MuleMessage getXmlMessageFromJaxbElement(JAXBElement jaxbElement) {
        def context = getJaxbContext()
        def marshaller = context.createMarshaller()
        def stringWriter = new StringWriter()
        marshaller.marshal jaxbElement, stringWriter
        stringWriter.close()
        def reader = new StringReader(stringWriter.toString())
        getXmlMessage reader
    }

    def MessageProcessorMocker mockSoapReply(String name = null, boolean untilSuccessful = false, testClosure) {
        def mock = whenMessageProcessor('consumer').ofNamespace('ws')
        if (name != null) {
            mock.withAttributes(Attribute.attribute('name').ofNamespace('doc').withValue(name))
        }
        MuleEvent alternateEvent = null
        def alternateFetcher = null
        // until successful messes up ability to get request in mock
        if (untilSuccessful) {
            alternateFetcher = { ['until-successful', alternateEvent.message] }
            def spyClosure = { muleEvent ->
                alternateEvent = muleEvent
            }
            spyMessageProcessor('until-successful')
                    .before(new TestSpyProcess(spyClosure))
        }

        applyXmlReplyCallback(mock, alternateFetcher, testClosure)
        mock
    }

    abstract String getMockResourcePath(String resource)

    BufferedInputStream getResource(String filename) {
        getMockResourceFetcher().getResource(filename)
    }

    ResourceFetcher getMockResourceFetcher() {
        new ResourceFetcher(this.&getMockResourcePath)
    }

    private def applyXmlReplyCallback(MessageProcessorMocker mock, alternateFetcher, testClosure) {
        mock.thenApply(new XmlRequestReplyTransformer(mock,
                                                      getJaxbContext(),
                                                      alternateFetcher,
                                                      this.getMockResourceFetcher(),
                                                      this.&getXmlMessage,
                                                      testClosure))
    }

    JAXBContext getJaxbContext() {
        throw new Exception('SOAP Mocks: You need to override the getJaxbContext method to return a JAXB context when using SOAP mocks')
    }
}
