package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator

class StringInputTransformer<T extends ConnectorInfo> implements
        InputTransformer<T> {
    private final IPayloadValidator<T> payloadValidator

    StringInputTransformer(IPayloadValidator<T> payloadValidator) {
        this.payloadValidator = payloadValidator
    }

    def transformInput(EventWrapper muleEvent,
                       T connectorInfo) {
        def muleMessage = muleEvent.message
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload == null) {
            return null
        }
        if (muleMessage.dataTypeClass != String) {
            throw new Exception(
                    "Expected payload to be of type String here but it actually was ${muleMessage.payload.class}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        validateContentType(muleEvent,
                            connectorInfo)
        muleMessage.payload
    }

    def disableStreaming() {
        // we already expect a string
    }

    private def validateContentType(EventWrapper muleEvent,
                                    T connectorInfo) {
        if (!payloadValidator.isContentTypeValidationRequired(connectorInfo)) {
            return
        }
        def validContentTypes = [
                'text/plain',
                '*/*', // an unknown type in Mule 4.x, which is fine for a String
                null // HTTP is text/plain by default
        ]
        payloadValidator.validateContentType(muleEvent,
                                             validContentTypes)
    }
}
