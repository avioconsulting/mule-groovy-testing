package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset
import org.mule.runtime.api.message.Message
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class HttpValidationTransformer implements IHaveStateToReset, MuleMessageTransformer {
    private Integer httpReturnCode

    HttpValidationTransformer() {
        reset()
    }

    CoreEvent transform(CoreEvent muleEvent,
                        Processor messageProcessor) {
        assert false : 'http requester class stuff'
//        assert messageProcessor instanceof DefaultHttpRequester
//        setStatusCode(muleEvent.message)
//        def wrappedValidator = messageProcessor.responseValidator as SuccessStatusCodeValidator
//        def responseValidator = new HttpStatusValidator(wrappedValidator,
//                                                        messageProcessor)
//        responseValidator.validate(muleEvent)
//        return muleEvent
    }

    private def setStatusCode(Message message) {
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
