package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.mule.runtime.core.api.MuleContext
import org.mule.runtime.core.api.construct.Flow
import org.mule.runtime.core.api.event.CoreEvent

import javax.xml.namespace.QName
import javax.xml.soap.MessageFactory

@Log4j2
class SoapApikitInvokerImpl extends SoapInvokerBaseImpl {
    private final String soapAction
    private final String flowName

    SoapApikitInvokerImpl(MuleContext muleContext,
                          EventFactory eventFactory,
                          String flowName,
                          String operation) {
        super(eventFactory)
        this.flowName = flowName
        def flow = muleContext.registry.lookupFlowConstruct(flowName) as Flow
        assert flow: "Could not find flow ${flowName}!"
        soapAction = deriveSoapAction(flow,
                                      operation)
    }

    @Override
    CoreEvent getEvent() {
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
        def muleEvent = this.xmlMessageBuilder.build(reader, flowName)
        def muleMessage = muleEvent.message.with {
            // without soapaction, you get weird null pointer exceptions
            setProperty('SOAPAction',
                        soapAction,
                        PropertyScope.INBOUND)
            // satisfy the need for checking for ?wsdl or ?xsd
            setProperty('http.query.params',
                        [:],
                        PropertyScope.INBOUND)
            it
        }
        log.info "Put together SOAP/Mule message {}",
                 muleMessage.toString()
        muleEvent
    }

    private static String deriveSoapAction(Flow flow,
                                           String soapOperationName) {
        def apiKitRouter = flow.messageProcessors.find { p ->
            // startsWith == avoid proxy class name stuff from cglib
            p.getClass().name.startsWith('org.mule.module.soapkit.Router')
        }
        assert apiKitRouter: "Expected flow ${flow.name} to have an apikit SOAP router!"
        def wsdlUrl = apiKitRouter.config.wsdlResource
        def wsdlFactoryClass = SoapApikitInvokerImpl.getClassLoader().loadClass('javax.wsdl.factory.WSDLFactory')
        assert wsdlFactoryClass: "Could not find javax.wsdl.factory.WSDLFactory. Is wsdl4j in the classpath?"
        // use reflection to not force SOAP on non-SOAP test library users
        def fact = wsdlFactoryClass.newInstance()
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
