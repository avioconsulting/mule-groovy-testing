package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.spies.IReceiveHttpOptions
import com.avioconsulting.mule.testing.spies.IReceiveMuleEvents
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope
import org.mule.module.http.internal.request.ResponseValidator
import org.mule.module.http.internal.request.SuccessStatusCodeValidator
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class HttpValidationTransformer implements MuleMessageTransformer,
        IReceiveHttpOptions,
        IReceiveMuleEvents {
    private ResponseValidator responseValidator
    private Integer httpReturnCode = 200
    private final MuleContext muleContext
    private MuleEvent muleEvent
    private final ProcessorLocator locator

    HttpValidationTransformer(MuleContext muleContext,
                              ProcessorLocator locator) {
        this.locator = locator
        this.muleContext = muleContext
    }

    MuleMessage transform(MuleMessage muleMessage) {
        setStatusCode(muleMessage)
        setStatusCode(muleEvent.message)
        this.responseValidator.validate(muleEvent)
        return muleMessage
    }

    private def setStatusCode(MuleMessage message) {
        message.setProperty('http.status',
                            httpReturnCode,
                            PropertyScope.INBOUND)
    }

    def setHttpReturnCode(Integer code) {
        this.httpReturnCode = code
    }

    def receive(Map queryParams,
                String fullPath,
                String httpVerb,
                ResponseValidator responseValidator) {
        // see HttpStatusValidator for why we wrap this
        def wrappedValidator = responseValidator as SuccessStatusCodeValidator
        this.responseValidator = new HttpStatusValidator(wrappedValidator,
                                                         locator)
    }

    def receive(MuleEvent muleEvent) {
        this.muleEvent = muleEvent
    }
}