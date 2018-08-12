package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

class XMLTransformer {
    protected final XMLMessageBuilder xmlMessageBuilder
    private final IPayloadValidator payloadValidator

    XMLTransformer(MuleContext muleContext,
                   IPayloadValidator payloadValidator) {
        this.payloadValidator = payloadValidator
        this.xmlMessageBuilder = new XMLMessageBuilder(muleContext,
                                                       false)
    }

    def validateContentType(MuleMessage muleMessage) {
        if (payloadValidator.contentTypeValidationRequired) {
            payloadValidator.validateContentType(muleMessage,
                                                 ['application/xml'])
        }
    }
}
