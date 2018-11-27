package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.ClosureCurrierEvent
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class XMLTransformer {
    protected final XMLMessageBuilder xmlMessageBuilder
    private final IPayloadValidator payloadValidator
    private final ClosureCurrier eventCurrier

    XMLTransformer(EventFactory eventFactory,
                   IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.xmlMessageBuilder = new XMLMessageBuilder(eventFactory,
                                                       false)
        this.eventCurrier = new ClosureCurrierEvent()
    }

    def validateContentType(MuleEvent muleEvent,
                            MessageProcessor messageProcessor) {
        if (payloadValidator.isContentTypeValidationRequired(messageProcessor)) {
            payloadValidator.validateContentType(muleEvent,
                                                 ['application/xml'])
        }
    }

    Closure handleMuleEvent(Closure closure,
                            MuleEvent muleEvent,
                            MessageProcessor messageProcessor) {
        eventCurrier.curryClosure(closure,
                                  muleEvent,
                                  messageProcessor)
    }
}
