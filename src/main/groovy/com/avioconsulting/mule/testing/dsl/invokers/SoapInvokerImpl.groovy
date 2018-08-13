package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent

@Log4j2
class SoapInvokerImpl implements SoapInvoker, Invoker {
    private inputObject
    private final MuleContext muleContext
    private final XMLMessageBuilder xmlMessageBuilder
    private JAXBMarshalHelper helper
    private final EventFactory eventFactory
    private final String flowName

    SoapInvokerImpl(MuleContext muleContext,
                    EventFactory eventFactory,
                    String flowName) {
        this.flowName = flowName
        this.muleContext = muleContext
        xmlMessageBuilder = new XMLMessageBuilder(muleContext,
                                                  true)
        this.eventFactory = eventFactory
    }

    @Override
    def inputJaxbPayload(Object inputObject) {
        this.inputObject = inputObject
        this.helper = new JAXBMarshalHelper(inputObject.class)
    }

    @Override
    MuleEvent getEvent() {
        StringReader reader
        if (inputObject instanceof File) {
            def xml = inputObject.text
            reader = new StringReader(xml)
        } else {
            reader = helper.getMarshalled(inputObject) { String xml ->
                def parsedNode = new XmlParser().parseText(xml)
                def xmlOutput = new StringWriter()
                def printer = new XmlNodePrinter(new PrintWriter(xmlOutput))
                printer.print(parsedNode)
                log.info 'Put together SOAP request payload of {}',
                         XmlUtil.serialize(xmlOutput.toString())
            }
        }
        def message = this.xmlMessageBuilder.build(reader)
        eventFactory.getMuleEvent(message,
                                  flowName,
                                  MessageExchangePattern.REQUEST_RESPONSE)
    }

    @Override
    def transformOutput(MuleEvent event) {
        def incomingMessage = event.message
        def payload = incomingMessage.payload
        def nullPayload = payload instanceof byte[] && payload.length == 0
        def strongTypedPayload
        if (nullPayload) {
            println 'Groovy Test WARNING: SOAP mock was sent a message with empty payload! using MuleMessage payload.'
            strongTypedPayload = incomingMessage
        } else {
            strongTypedPayload = helper.unmarshal(incomingMessage)
        }
        strongTypedPayload
    }

    @Override
    Invoker withNewPayloadValidator(IPayloadValidator validator) {
        // TODO: Deal with this and getPayloadValidator
        this
    }

    @Override
    IPayloadValidator getPayloadValidator() {
        return null
    }
}
