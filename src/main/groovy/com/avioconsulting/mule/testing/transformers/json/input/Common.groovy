package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.payloadvalidators.StreamingDisabledPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.transport.NullPayload

abstract class Common implements InputTransformer {
    private final MuleContext muleContext
    private IPayloadValidator payloadValidator

    Common(MuleContext muleContext,
           IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.muleContext = muleContext
    }

    def validateContentType(MuleMessage message) {
        // don't need content-type for VM or empty strings
        if (!payloadValidator.isPayloadTypeValidationRequired() || message.payloadAsString == '') {
            return
        }
        if (!payloadValidator.contentTypeValidationRequired) {
            return
        }
        payloadValidator.validateContentType(message,
                                             [
                                                     'application/json',
                                                     'application/json;charset=UTF-8',
                                                     'application/json;charset=utf-8',
                                                     'application/json;charset=windows-1252'
                                             ])
    }

    abstract def transform(String jsonString)

    def transformInput(MuleMessage muleMessage) {
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload instanceof NullPayload) {
            return null
        }
        if (payloadValidator.payloadTypeValidationRequired) {
            validatePayloadType(muleMessage)
        }
        // want to wait to do this after if the payload type check above since it consumes the string
        def jsonString = muleMessage.payloadAsString
        validateContentType(muleMessage)
        return transform(jsonString)
    }

    private void validatePayloadType(MuleMessage muleMessage) {
        if (!payloadValidator.isPayloadTypeValidationRequired()) {
            println 'Skipping payload type validation'
            return
        }
        payloadValidator.validatePayloadType(muleMessage.payload)
    }

    def disableStreaming() {
        this.payloadValidator = new StreamingDisabledPayloadValidator(payloadValidator)
    }
}
