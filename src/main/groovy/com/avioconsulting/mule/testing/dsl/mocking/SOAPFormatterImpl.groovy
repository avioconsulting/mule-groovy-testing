package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.api.MuleContext

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    SOAPFormatterImpl(MuleContext muleContext,
                      IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
    }

    def httpConnectError() {
        return null
    }
}
