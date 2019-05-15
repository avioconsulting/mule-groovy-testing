package com.avioconsulting.mule.testing.transformers.http


import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
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
        def attributes = getHttpResponseAttributes(this.httpReturnCode,
                                                   'the reason',
                                                   runtimeBridgeTestSide)
        muleEvent = muleEvent.withNewAttributes(attributes)
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
