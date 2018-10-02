package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.MessageFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class HttpValidationTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<HttpRequesterInfo> {
    private Integer httpReturnCode
    private final MessageFactory messageFactory

    HttpValidationTransformer(MessageFactory messageFactory) {
        this.messageFactory = messageFactory
        reset()
    }

    EventWrapper transform(EventWrapper muleEvent,
                           HttpRequesterInfo connectorInfo) {
        assert muleEvent instanceof MockEventWrapper
        def message = messageFactory.withNewAttributes(muleEvent.message,
                                                       [
                                                               'http.status': this.httpReturnCode
                                                       ])
        muleEvent.changeMessage(message)
        if (!connectorInfo.validationEnabled) {
            return muleEvent
        }
        // TODO: Fix this reason/header if it matters
        connectorInfo.validator.validate(this.httpReturnCode,
                                         'foo',
                                         [header1: '123'])
        return muleEvent
    }

    def setHttpReturnCode(Integer code) {
        this.httpReturnCode = code
    }

    @Override
    def reset() {
        this.httpReturnCode = 200
    }
}
