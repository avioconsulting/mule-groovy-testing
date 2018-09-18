package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class HttpValidationTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<HttpRequesterInfo> {
    private Integer httpReturnCode

    HttpValidationTransformer() {
        reset()
    }

    void transform(MockEventWrapper muleEvent,
                   HttpRequesterInfo connectorInfo) {
        assert false: 'http requester class stuff'
//        assert messageProcessor instanceof DefaultHttpRequester
//        setStatusCode(muleEvent.message)
//        def wrappedValidator = messageProcessor.responseValidator as SuccessStatusCodeValidator
//        def responseValidator = new HttpStatusValidator(wrappedValidator,
//                                                        messageProcessor)
//        responseValidator.validate(muleEvent)
//        return muleEvent
    }

    private def setStatusCode(Object message) {
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
