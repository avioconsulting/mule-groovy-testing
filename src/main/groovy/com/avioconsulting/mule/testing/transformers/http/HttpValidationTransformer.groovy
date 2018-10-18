package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class HttpValidationTransformer implements
        IHaveStateToReset,
        MuleMessageTransformer<HttpRequesterInfo> {
    private Integer httpReturnCode

    HttpValidationTransformer() {
        reset()
    }

    EventWrapper transform(EventWrapper muleEvent,
                           HttpRequesterInfo connectorInfo) {
        def attributes = [
                'http.status': this.httpReturnCode
        ]
        muleEvent = muleEvent.withNewAttributes(attributes)
        if (!connectorInfo.validationEnabled) {
            return muleEvent
        }
        connectorInfo.validator.validate(this.httpReturnCode,
                                         'Test framework told us to',
                                         ['X-Some-Header': '123'])
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
