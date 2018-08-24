package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.payloadvalidators.StreamingDisabledPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.transport.NullPayload

abstract class Common implements InputTransformer {
    private IPayloadValidator payloadValidator

    Common(IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
    }

    def validateContentType(MuleEvent event,
                            MessageProcessor messageProcessor) {
        // don't need content-type for VM or empty strings
        if (!payloadValidator.isPayloadTypeValidationRequired(messageProcessor) || event.messageAsString == '') {
            return
        }
        if (!payloadValidator.isContentTypeValidationRequired(messageProcessor)) {
            return
        }
        payloadValidator.validateContentType(event,
                                             [
                                                     'application/json',
                                                     'application/json;charset=UTF-8',
                                                     'application/json;charset=utf-8',
                                                     'application/json;charset=windows-1252'
                                             ])
    }

    abstract def transform(String jsonString)

    def transformInput(MuleEvent muleEvent,
                       MessageProcessor messageProcessor) {
        // comes back from some Mule connectors like JSON
        if (muleEvent.message.payload instanceof NullPayload) {
            return null
        }
        if (payloadValidator.isPayloadTypeValidationRequired(messageProcessor)) {
            validatePayloadType(muleEvent,
                                messageProcessor)
        }
        // want to wait to do this after if the payload type check above since it consumes the string
        def jsonString = muleEvent.messageAsString
        validateContentType(muleEvent,
                            messageProcessor)
        return transform(jsonString)
    }

    private void validatePayloadType(MuleEvent muleEvent,
                                     MessageProcessor messageProcessor) {
        if (!payloadValidator.isPayloadTypeValidationRequired(messageProcessor)) {
            println 'Skipping payload type validation'
            return
        }
        payloadValidator.validatePayloadType(muleEvent.message.payload)
    }

    def disableStreaming() {
        this.payloadValidator = new StreamingDisabledPayloadValidator(payloadValidator)
    }
}
