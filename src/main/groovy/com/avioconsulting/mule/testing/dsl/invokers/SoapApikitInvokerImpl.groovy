package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.HttpAttributeBuilder
import com.avioconsulting.mule.testing.mulereplacements.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.FlowWrapper
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil

import javax.wsdl.factory.WSDLFactory
import javax.xml.namespace.QName
import javax.xml.soap.MessageFactory

@Log4j2
class SoapApikitInvokerImpl extends
        SoapInvokerBaseImpl implements
        HttpAttributeBuilder {
    private final String soapAction
    private final String flowName
    private final InvokerEventFactory eventFactory
    private final RuntimeBridgeTestSide runtimeBridgeTestSide

    SoapApikitInvokerImpl(InvokerEventFactory eventFactory,
                          String flowName,
                          String operation,
                          RuntimeBridgeTestSide runtimeBridgeTestSide) {
        this.runtimeBridgeTestSide = runtimeBridgeTestSide
        this.eventFactory = eventFactory
        this.flowName = flowName
        def flow = runtimeBridgeTestSide.getFlow(flowName)
        soapAction = deriveSoapAction(flow,
                                      operation,
                                      runtimeBridgeTestSide)
    }

    @Override
    EventWrapper getEvent() {
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
        def newEvent = eventFactory.getMuleEventWithPayload(null,
                                                            flowName)
        def muleEvent = this.xmlMessageBuilder.build(soapRequest,
                                                     newEvent)
        def additionalHeaders = [
                'SOAPAction': soapAction
        ]
        def attributes = getHttpListenerAttributes('/*',
                                                   'POST',
                                                   '/',
                                                   [:],
                                                   runtimeBridgeTestSide,
                                                   muleEvent.message.mimeType,
                                                   9999,
                                                   additionalHeaders)
        muleEvent = muleEvent.withNewAttributes(attributes)
        log.info "Put together SOAP/Mule message {}",
                 muleEvent.toString()
        muleEvent
    }

    private static String deriveSoapAction(FlowWrapper flow,
                                           String soapOperationName,
                                           RuntimeBridgeTestSide bridge) {
        def config = flow.getConfigurationInstance('apikit-soap:router')
        // config is SoapkitConfiguration
        def wsdlUrl = config.info.wsdlLocation as String
        def fact = WSDLFactory.newInstance()
        def reader = fact.newWSDLReader()
        def defin = reader.readWSDL(wsdlUrl)
        def bindings = defin.bindings.values()
        def operations = bindings.collect { binding ->
            binding.bindingOperations
        }.flatten()
        def op = operations.find { operation ->
            operation.name == soapOperationName
        }
        assert op: "Was unable to find operation ${soapOperationName}, operations found were: ${operations.collect { o -> o.name }}"
        def soapOperation = op.extensibilityElements.find() { el ->
            el.elementType == new QName('http://schemas.xmlsoap.org/wsdl/soap/',
                                        'operation')
        }
        assert soapOperation: "Expected a SOAP Action type attribute on the operation! e.g. <soap:operation\n" +
                "                    soapOperation=\"http://www.avioconsulting.com/services/SOAPTest/v1/SOAPTest\"/>"
        soapOperation.soapActionURI
    }
}
