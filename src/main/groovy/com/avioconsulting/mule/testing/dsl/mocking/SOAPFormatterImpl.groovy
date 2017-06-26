package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import com.avioconsulting.mule.testing.transformers.http.HttpConnectorErrorTransformer
import org.mule.api.MuleContext
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.munit.common.mocking.MunitSpy

class SOAPFormatterImpl extends XMLFormatterImpl implements SOAPFormatter {
    private HttpConnectorErrorTransformer httpConnectorErrorTransformer
    private final MunitSpy spy

    SOAPFormatterImpl(MuleContext muleContext,
                      MunitSpy spy,
                      IPayloadValidator payloadValidator) {
        super(muleContext, payloadValidator)
        this.spy = spy
    }

    def httpConnectError() {
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(muleContext)
        httpConnectorErrorTransformer.triggerConnectException()
        // avoid DSL weirdness
        return null
    }

    def httpTimeoutError() {
        httpConnectorErrorTransformer = new HttpConnectorErrorTransformer(muleContext)
        httpConnectorErrorTransformer.triggerTimeoutException()
        // avoid DSL weirdness
        return null
    }

    @Override
    MuleMessageTransformer getTransformer() {
        httpConnectorErrorTransformer ?: super.transformer
    }

    @Override
    IFormatter withNewPayloadValidator(IPayloadValidator validator) {
        new SOAPFormatterImpl(muleContext,
                              spy,
                              validator)
    }
}
