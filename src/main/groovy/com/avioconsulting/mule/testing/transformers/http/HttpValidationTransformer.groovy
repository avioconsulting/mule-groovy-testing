package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.api.processor.MessageProcessor
import org.mule.api.transport.PropertyScope
import org.mule.module.http.internal.request.DefaultHttpRequester
import org.mule.module.http.internal.request.SuccessStatusCodeValidator

class HttpValidationTransformer implements IHaveStateToReset, MuleMessageTransformer {
    private Integer httpReturnCode

    HttpValidationTransformer() {
        reset()
    }

    MuleEvent transform(MuleEvent muleEvent,
                        MessageProcessor messageProcessor) {
        assert messageProcessor instanceof DefaultHttpRequester
        setStatusCode(muleEvent.message)
        def wrappedValidator = messageProcessor.responseValidator as SuccessStatusCodeValidator
        def responseValidator = new HttpStatusValidator(wrappedValidator,
                                                        messageProcessor)
        responseValidator.validate(muleEvent)
        return muleEvent
    }

    private def setStatusCode(MuleMessage message) {
        message.setProperty('http.status',
                            httpReturnCode,
                            PropertyScope.INBOUND)
    }

    def setHttpReturnCode(Integer code) {
        this.httpReturnCode = code
    }

    @Override
    def reset() {
        this.httpReturnCode = 200
    }
}
