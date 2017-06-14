package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.payload_types.IPayloadValidator
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.transport.NullPayload

class StringInputTransformer implements InputTransformer {
    private final MuleContext muleContext
    private final IPayloadValidator payloadValidator

    StringInputTransformer(IPayloadValidator payloadValidator,
                           MuleContext muleContext) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
    }

    def transformInput(MuleMessage muleMessage) {
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload instanceof NullPayload) {
            return null
        }
        if (muleMessage.payload.class != String) {
            throw new Exception(
                    "Expected payload to be of type String here but it actually was ${muleMessage.payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        validateContentType(muleMessage)
        muleMessage.payload
    }

    def disableStreaming() {
        // we already expect a string
    }

    private def validateContentType(MuleMessage muleMessage) {
        if (!payloadValidator.contentTypeValidationRequired) {
            return
        }
        payloadValidator.validateContentType(muleMessage,
                                             'text/plain')
    }
}
