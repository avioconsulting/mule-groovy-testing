package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.InputTransformer

abstract class Common<T extends ConnectorInfo> implements
        InputTransformer<T> {
    private IPayloadValidator<T> payloadValidator

    Common(IPayloadValidator<T> payloadValidator) {
        this.payloadValidator = payloadValidator
    }

    def validateContentType(EventWrapper event,
                            T messageProcessor) {
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
                                                     'application/json; charset=UTF-8',
                                                     'application/json;charset=utf-8',
                                                     'application/json;charset=windows-1252'
                                             ])
    }

    abstract def transform(String jsonString)

    def transformInput(EventWrapper muleEvent,
                       T messageProcessor) {
        // comes back from some Mule connectors like JSON
        if (muleEvent.message.payload == null) {
            return null
        }
        if (payloadValidator.isPayloadTypeValidationRequired(messageProcessor)) {
            validatePayloadType(muleEvent,
                                messageProcessor)
        }

        def jsonString = muleEvent.message.messageAsString
        validateContentType(muleEvent,
                            messageProcessor)
        return transform(jsonString)
    }

    private void validatePayloadType(EventWrapper muleEvent,
                                     T messageProcessor) {
        if (!payloadValidator.isPayloadTypeValidationRequired(messageProcessor)) {
            println 'Skipping payload type validation'
            return
        }
        payloadValidator.validatePayloadType(muleEvent.message.payload)
    }
}
