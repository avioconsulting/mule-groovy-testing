package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope

import javax.xml.soap.MessageFactory

@Log4j2
class SoapApikitInvokerImpl extends SoapInvokerBaseImpl {
    private final String soapAction

    SoapApikitInvokerImpl(MuleContext muleContext,
                          EventFactory eventFactory,
                          String flowName,
                          String operation) {
        super(muleContext, eventFactory, flowName)
        this.soapAction = operation
    }

    @Override
    protected MuleMessage getMessage() {

        def doc = jaxbHelper.getMarshalledDocument(this.inputObject)
        def soapFactory = MessageFactory.newInstance()
        def msg = soapFactory.createMessage()
        def part = msg.getSOAPPart()
        def envelope = part.envelope
        def body = envelope.body
        body.addDocument(doc)
        def bos = new ByteArrayOutputStream()
        msg.writeTo(bos)
        def bytes = bos.toByteArray()
        def soapRequest = new String(bytes)
        log.info 'Put together a SOAP request payload of {}',
                 XmlUtil.serialize(soapRequest)
        def reader = new StringReader(soapRequest)
        this.xmlMessageBuilder.build(reader).with {
            setProperty('SOAPAction',
                        soapAction,
                        PropertyScope.INBOUND)
            // satisfy the need for checking for ?wsdl or ?xsd
            setProperty('http.query.params',
                        [:],
                        PropertyScope.INBOUND)
            it
        }
    }
}
