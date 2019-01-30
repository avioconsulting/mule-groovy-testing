package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
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
    private final String host

    SoapApikitInvokerImpl(InvokerEventFactory eventFactory,
                          String flowName,
                          String operation,
                          String host,
                          RuntimeBridgeTestSide runtimeBridgeTestSide) {
        super(flowName,
              runtimeBridgeTestSide)
        this.host = host
        this.runtimeBridgeTestSide = runtimeBridgeTestSide
        this.eventFactory = eventFactory
        this.flowName = flowName
        soapAction = deriveSoapAction(this.flow,
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
                                                     newEvent,
                                                     flow,
                                                     XMLMessageBuilder.MessageType.Mule41Stream)
        def additionalHeaders = [
                'SOAPAction': soapAction
        ]
        def attributes = getHttpListenerAttributes('/*',
                                                   'POST',
                                                   '/',
                                                   [:],
                                                   runtimeBridgeTestSide,
                                                   muleEvent.message.mimeType,
                                                   host,
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
        def wsdlPath = config.info.wsdlLocation as String
        // wsdls are located in src/main/resources/api
        wsdlPath = "api/${wsdlPath}"
        // Using this app classloader here because the WSDL itself is a resource and we will be
        // unable to find it based on path alone
        def wsdlUrl = bridge.appClassloader.getResource(wsdlPath)
        assert wsdlUrl: "Was unable to locate WSDL at ${wsdlPath}. Should not have been able to get this far without that"
        def fact = WSDLFactory.newInstance()
        def reader = fact.newWSDLReader()
        def defin = reader.readWSDL(wsdlUrl.toString())
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
