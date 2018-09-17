package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

class XMLTransformer {
    protected final XMLMessageBuilder xmlMessageBuilder
    private final IPayloadValidator payloadValidator

    XMLTransformer(InvokerEventFactory eventFactory,
                   IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.xmlMessageBuilder = new XMLMessageBuilder(eventFactory,
                                                       false)
    }

    def validateContentType(Event muleEvent,
                            Processor messageProcessor) {
        if (payloadValidator.isContentTypeValidationRequired(messageProcessor)) {
            payloadValidator.validateContentType(muleEvent,
                                                 ['application/xml'])
        }
    }
}
