package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.EventFactory
import groovy.util.logging.Log4j2
import groovy.xml.XmlUtil
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

@Log4j2
class SoapOperationFlowInvokerImpl extends SoapInvokerBaseImpl  {
    SoapOperationFlowInvokerImpl(MuleContext muleContext,
                                 EventFactory eventFactory,
                                 String flowName) {
        super(muleContext, eventFactory, flowName)
    }

    @Override
    protected  MuleMessage getMessage() {
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
        this.xmlMessageBuilder.build(reader)
    }
}
