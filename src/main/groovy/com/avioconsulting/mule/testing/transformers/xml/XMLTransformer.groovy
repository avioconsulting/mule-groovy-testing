package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator

abstract class XMLTransformer<T extends ConnectorInfo> {
    protected final XMLMessageBuilder xmlMessageBuilder
    private final IPayloadValidator<T> payloadValidator

    XMLTransformer(IPayloadValidator<T> payloadValidator) {
        this.payloadValidator = payloadValidator
        this.xmlMessageBuilder = new XMLMessageBuilder()
    }

    def validateContentType(EventWrapper muleEvent,
                            T messageProcessor) {
        if (payloadValidator.isContentTypeValidationRequired(messageProcessor)) {
            payloadValidator.validateContentType(muleEvent,
                                                 [
                                                         'application/xml',
                                                         'application/xml; charset=UTF-8'
                                                 ])
        }
    }
}
