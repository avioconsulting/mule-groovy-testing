package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.w3c.dom.Document

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.parsers.DocumentBuilderFactory

@Log4j2
class JAXBMarshalHelper {
    private final JAXBContext jaxbContext

    JAXBMarshalHelper(Class inputJaxbClass) {
        this.jaxbContext = JAXBContext.newInstance(inputJaxbClass.getPackage().name)
    }

    Document getMarshalledDocument(objectOrJaxbElement) {
        def dbf = DocumentBuilderFactory.newInstance()
        def deb = dbf.newDocumentBuilder()
        def doc = deb.newDocument()
        def marshaller = this.jaxbContext.createMarshaller()
        marshaller.marshal(objectOrJaxbElement,
                           doc)
        doc
    }

    String getMarshalled(objectOrJaxbElement) {
        def marshaller = this.jaxbContext.createMarshaller()
        def stringWriter = new StringWriter()

        try {
            marshaller.marshal objectOrJaxbElement,
                               stringWriter
            stringWriter.close()
            def asString = stringWriter.toString()
            // will pretty print the XML
            asString = XmlUtil.serialize(asString)
            log.info 'JAXB marshalled a payload of {}',
                     asString
            asString
        }
        catch (e) {
            throw new TestingFrameworkException(
                    "Unmarshal problem. if ${objectOrJaxbElement.class.name} is not an XML Root element, you need to use ObjectFactory to wrap it in a JAXBElement object!",
                    e)
        }
    }

    def unmarshal(EventWrapper event,
                  ConnectorInfo connectorInfo) {
        def unmarshaller = this.jaxbContext.createUnmarshaller()
        def payloadAsStr = connectorInfo.supportsIncomingBody ? connectorInfo.incomingBody : event.messageAsString
        log.info 'JAXB Unmarshaller received payload of {}',
                 payloadAsStr
        try {
            def result = unmarshaller.unmarshal(new StringReader(payloadAsStr))
            if (result instanceof JAXBElement) {
                result.value
            } else {
                result
            }
        }
        catch (e) {
            throw new TestingFrameworkException('SOAP Mocks: Unable to marshal message. Do you need a different JAXB context?',
                                                e)
        }
    }
}
