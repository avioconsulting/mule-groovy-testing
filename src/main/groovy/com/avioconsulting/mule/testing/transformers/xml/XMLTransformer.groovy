package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class XMLTransformer {
    protected final XMLMessageBuilder xmlMessageBuilder
    private final IPayloadValidator payloadValidator

    XMLTransformer(EventFactory eventFactory,
                   IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.xmlMessageBuilder = new XMLMessageBuilder(eventFactory,
                                                       false)
    }

    def validateContentType(MuleEvent muleEvent,
                            MessageProcessor messageProcessor) {
        if (payloadValidator.isContentTypeValidationRequired(messageProcessor)) {
            payloadValidator.validateContentType(muleEvent,
                                                 ['application/xml'])
        }
    }
}
