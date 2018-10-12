package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.w3c.dom.Document

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.parsers.DocumentBuilderFactory

@Log4j2
class JAXBMarshalHelper {
    private final JAXBContext jaxbContext
    private final String helperUse

    JAXBMarshalHelper(Class inputJaxbClass,
                      String helperUse) {
        this.helperUse = helperUse
        this.jaxbContext = JAXBContext.newInstance(inputJaxbClass.getPackage().name)
    }

    Document getMarshalledDocument(objectOrJaxbElement) {
        def dbf = DocumentBuilderFactory.newInstance()
        def deb = dbf.newDocumentBuilder()
        def doc = deb.newDocument()
        def marshaller = this.jaxbContext.createMarshaller()
        marshaller.marshal(objectOrJaxbElement, doc)
        doc
    }

    String getMarshalled(objectOrJaxbElement) {
        def marshaller = this.jaxbContext.createMarshaller()
        def stringWriter = new StringWriter()

        try {
            marshaller.marshal objectOrJaxbElement, stringWriter
            stringWriter.close()
            def asString = stringWriter.toString()
            // will pretty print the XML
            asString = XmlUtil.serialize(asString)
            log.info 'JAXB Marshaller for {}, marshalled a payload of {}',
                     this.helperUse,
                     asString
            asString
        }
        catch (e) {
            throw new Exception(
                    "Unmarshal problem. if ${objectOrJaxbElement.class.name} is not an XML Root element, you need to use ObjectFactory to wrap it in a JAXBElement object!",
                    e)
        }
    }

    def unmarshal(EventWrapper event) {
        def unmarshaller = this.jaxbContext.createUnmarshaller()
        def payloadAsStr = event.messageAsString
        log.info 'JAXB Unmarshaller for {}, received payload of {}',
                 this.helperUse,
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
            throw new Exception('SOAP Mocks: Unable to marshal message. Do you need a different JAXB context?', e)
        }
    }
}