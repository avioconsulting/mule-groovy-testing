package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

class HttpValidationTransformer implements
        IHaveStateToReset,
        HttpTransformer {
    private Integer httpReturnCode

    HttpValidationTransformer() {
        reset()
    }

    def setHttpReturnCode(Integer code) {
        this.httpReturnCode = code
    }

    @Override
    def reset() {
        this.httpReturnCode = 200
    }

    @Override
    HttpState transform(HttpState httpState,
                        HttpRequesterInfo connectorInfo) {
        httpState = new HttpState(this.httpReturnCode,
                                  httpState.body,
                                  httpState.mediaType)
        if (!connectorInfo.validationEnabled) {
            return httpState
        }
        connectorInfo.validator.validate(this.httpReturnCode,
                                         'Test framework told us to',
                                         ['X-Some-Header': '123'])
        return httpState
    }
}
