package com.avioconsulting.mule.testing.transformers.xml

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.apache.logging.log4j.CloseableThreadContext
import org.w3c.dom.Document

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stax.StAXSource
import javax.xml.transform.stream.StreamResult

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

    private def withLog4j2Context(String specifics,
                                  Closure closure) {
        def context = CloseableThreadContext.push("${helperUse} ${specifics}")
        try {
            closure()
        }
        finally {
            context.close()
        }
    }

    StringReader getMarshalled(objectOrJaxbElement) {
        def marshaller = this.jaxbContext.createMarshaller()
        def stringWriter = new StringWriter()
        withLog4j2Context('marshaller (converts from Java object to XML)') {
            try {
                marshaller.marshal objectOrJaxbElement, stringWriter
                stringWriter.close()
                def asString = stringWriter.toString()
                // will pretty print the XML
                asString = XmlUtil.serialize(asString)
                log.info 'Created a payload of {}',
                         asString
                new StringReader(asString)
            }
            catch (e) {
                def ex = new Exception(
                        "Marshal problem. if ${objectOrJaxbElement.class.name} is not an XML Root element, you need to use ObjectFactory to wrap it in a JAXBElement object!",
                        e)
                log.error 'Cannot marshal',
                          ex
                throw ex
            }
        }
    }

    def unmarshal(Object payload) {
        def unmarshaller = this.jaxbContext.createUnmarshaller()
        withLog4j2Context('unmarshaller (converts from XML to Java object)') {
            String payloadAsStr
            switch (payload) {
            // until successful/alternate path is a string
                case String:
                    payloadAsStr = payload
                    break
                case InputStream:
                    payloadAsStr = payload.text
                    break
                case XMLStreamReader:
                    assert payload instanceof XMLStreamReader
                    // the default TransformerFactory.newInstance() method returned a transformer
                    // that does not work properly with StAXSource, so using one that is compatible
                    def transformer = TransformerFactory.newInstance(TransformerFactoryImpl.name,
                                                                     Thread.currentThread().contextClassLoader)
                            .newTransformer()
                    transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
                    def writer = new StringWriter()
                    transformer.transform(new StAXSource(payload),
                                          new StreamResult(writer))
                    payloadAsStr = writer.toString()
                    break
                default:
                    def e = new Exception("do not know how to handle XML payload of ${payload.getClass().name}")
                    log.error 'Cannot unmarshal',
                              e
                    throw e
            }
            log.info 'Received payload of {}, will now convert to a Java object',
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
                def ex = new Exception('SOAP Mocks: Unable to unmarshal message. Do you need a different JAXB context?',
                                       e)
                log.error 'Unable to unmarshal',
                          ex
                throw ex
            }
        }
    }
}