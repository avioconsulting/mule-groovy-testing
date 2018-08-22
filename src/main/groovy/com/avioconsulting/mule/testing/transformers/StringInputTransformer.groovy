package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.transport.NullPayload

class StringInputTransformer implements InputTransformer {
    private final MuleContext muleContext
    private final IPayloadValidator payloadValidator

    StringInputTransformer(IPayloadValidator payloadValidator,
                           MuleContext muleContext) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
    }

    def transformInput(MuleEvent muleEvent,
                       MessageProcessor messageProcessor) {
        def muleMessage = muleEvent.message
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload instanceof NullPayload) {
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

    private def validateContentType(MuleEvent muleEvent,
                                    MessageProcessor messageProcessor) {
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
