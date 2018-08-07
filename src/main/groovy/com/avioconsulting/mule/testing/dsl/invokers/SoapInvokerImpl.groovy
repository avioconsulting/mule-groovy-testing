package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import org.mule.DefaultMuleEvent
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.munit.common.util.MunitMuleTestUtils

class SoapInvokerImpl implements SoapInvoker, Invoker {
    private inputObject
    private final MuleContext muleContext
    private final XMLMessageBuilder xmlMessageBuilder
    private JAXBMarshalHelper helper

    SoapInvokerImpl(MuleContext muleContext) {
        this.muleContext = muleContext
        xmlMessageBuilder = new XMLMessageBuilder(muleContext)
    }

    @Override
    def inputPayload(Object inputObject) {
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
            reader = helper.getMarshalled(inputObject)
        }
        def message = this.xmlMessageBuilder.build(reader, 200)
        new DefaultMuleEvent(message,
                             MessageExchangePattern.REQUEST_RESPONSE,
                             MunitMuleTestUtils.getTestFlow(muleContext))
    }

    @Override
    def transformOutput(MuleEvent event) {
        // TODO: Deal with this
        event.message.payload
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
