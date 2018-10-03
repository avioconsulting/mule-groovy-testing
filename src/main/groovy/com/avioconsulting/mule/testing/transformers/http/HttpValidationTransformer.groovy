package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class HttpValidationTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<HttpRequesterInfo> {
    private Integer httpReturnCode
    private final TransformingEventFactory transformingEventFactory

    HttpValidationTransformer(TransformingEventFactory transformingEventFactory) {
        this.transformingEventFactory = transformingEventFactory
        reset()
    }

    EventWrapper transform(EventWrapper muleEvent,
                           HttpRequesterInfo connectorInfo) {
        def attributes = [
                'http.status': this.httpReturnCode
        ]
        muleEvent = transformingEventFactory.getMuleEventWithAttributes(muleEvent,
                                                                        attributes)
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
