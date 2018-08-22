package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.mule.api.MuleEvent

@Log4j2
class SoapOperationFlowInvokerImpl extends SoapInvokerBaseImpl {
    private final String flowName

    SoapOperationFlowInvokerImpl(EventFactory eventFactory,
                                 String flowName) {
        super(eventFactory)
        this.flowName = flowName
    }

    MuleEvent getEvent() {
        StringReader reader
        if (inputObject instanceof File) {
            def xml = inputObject.text
            reader = new StringReader(xml)
        } else {
            reader = jaxbHelper.getMarshalled(inputObject) { String xml ->
                def parsedNode = new XmlParser().parseText(xml)
                def xmlOutput = new StringWriter()
                def printer = new XmlNodePrinter(new PrintWriter(xmlOutput))
                printer.print(parsedNode)
                log.info 'Put together SOAP request payload of {}',
                         XmlUtil.serialize(xmlOutput.toString())
            }
        }
        this.xmlMessageBuilder.build(reader,
                                     flowName)
    }
}
