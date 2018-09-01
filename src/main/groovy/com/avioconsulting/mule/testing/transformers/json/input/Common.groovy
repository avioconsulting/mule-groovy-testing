package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.payloadvalidators.StreamingDisabledPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

abstract class Common implements InputTransformer {
    private IPayloadValidator payloadValidator

    Common(IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
    }

    def validateContentType(CoreEvent event,
                            Processor messageProcessor) {
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

    def transformInput(CoreEvent muleEvent,
                       Processor messageProcessor) {
        // comes back from some Mule connectors like JSON
        if (muleEvent.message.payload == null) {
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

    private void validatePayloadType(CoreEvent muleEvent,
                                     Processor messageProcessor) {
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
