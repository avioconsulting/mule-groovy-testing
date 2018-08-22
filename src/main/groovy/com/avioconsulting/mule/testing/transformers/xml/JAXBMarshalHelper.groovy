package com.avioconsulting.mule.testing.transformers.xml


import org.w3c.dom.Document

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.parsers.DocumentBuilderFactory

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
        marshaller.marshal(objectOrJaxbElement, doc)
        doc
    }

    StringReader getMarshalled(objectOrJaxbElement,
                               Closure stringPreview = null) {
        def marshaller = this.jaxbContext.createMarshaller()
        def stringWriter = new StringWriter()

        try {
            marshaller.marshal objectOrJaxbElement, stringWriter
            stringWriter.close()
            def asString = stringWriter.toString()
            if (stringPreview) {
                stringPreview(asString)
            }
            new StringReader(asString)
        }
        catch (e) {
            throw new Exception(
                    "Unmarshal problem. if ${objectOrJaxbElement.class.name} is not an XML Root element, you need to use ObjectFactory to wrap it in a JAXBElement object!",
                    e)
        }
    }

    def unmarshal(Object payload) {
        def unmarshaller = this.jaxbContext.createUnmarshaller()
        // until successful/alternate path is a string
        def stream = payload instanceof String ? new StringReader(payload) : payload
        try {
            def result = unmarshaller.unmarshal(stream)
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