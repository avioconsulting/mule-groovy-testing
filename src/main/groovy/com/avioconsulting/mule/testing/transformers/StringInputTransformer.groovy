package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class StringInputTransformer implements InputTransformer {
    private final IPayloadValidator payloadValidator

    StringInputTransformer(IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
    }

    def transformInput(CoreEvent muleEvent,
                       Processor messageProcessor) {
        def muleMessage = muleEvent.message
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload == null) {
            return null
        }
        if (muleMessage.payload.class != String) {
            throw new Exception(
                    "Expected payload to be of type String here but it actually was ${muleMessage.payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        validateContentType(muleEvent,
                            messageProcessor)
        muleMessage.payload
    }

    def disableStreaming() {
        // we already expect a string
    }

    private def validateContentType(CoreEvent muleEvent,
                                    Processor messageProcessor) {
        if (!payloadValidator.isContentTypeValidationRequired(messageProcessor)) {
            return
        }
        def validContentTypes = [
                'text/plain',
                null // HTTP is text/plain by default
        ]
        payloadValidator.validateContentType(muleEvent,
                                             validContentTypes)
    }
}
