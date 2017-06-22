package com.avioconsulting.mule.testing.transformers.xml

import org.mule.api.MuleMessage

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement

class JAXBMarshalHelper {
    private final JAXBContext jaxbContext

    JAXBMarshalHelper(Class inputJaxbClass) {
        this.jaxbContext = JAXBContext.newInstance(inputJaxbClass.package.name)
    }

    StringReader getMarshalled(objectOrJaxbElement) {
        def marshaller = this.jaxbContext.createMarshaller()
        def stringWriter = new StringWriter()

        try {
            marshaller.marshal objectOrJaxbElement, stringWriter
            stringWriter.close()
            new StringReader(stringWriter.toString())
        }
        catch (e) {
            throw new Exception(
                    "Unmarshal problem. if ${objectOrJaxbElement.class.name} is not an XML Root element, you need to use ObjectFactory to wrap it in a JAXBElement object!",
                    e)
        }
    }

    def unmarshal(MuleMessage message) {
        def unmarshaller = this.jaxbContext.createUnmarshaller()
        // until successful/alternate path is a string
        def stream = message.payload instanceof String ? new StringReader(message.payload) : message.payload
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