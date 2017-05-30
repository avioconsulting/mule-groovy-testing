package com.avioconsulting.mule.testing.transformers.xml

import org.mule.api.MuleMessage

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement

class JAXBMarshalHelper {
    private final JAXBContext jaxbContext

    JAXBMarshalHelper(Class inputJaxbClass) {
        this.jaxbContext = JAXBContext.newInstance(inputJaxbClass.package.name)
    }

    StringReader getMarshalled(JAXBElement jaxbElement) {
        def marshaller = this.jaxbContext.createMarshaller()
        def stringWriter = new StringWriter()
        marshaller.marshal jaxbElement, stringWriter
        stringWriter.close()
        new StringReader(stringWriter.toString())
    }

    def unmarshal(MuleMessage message) {
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
}