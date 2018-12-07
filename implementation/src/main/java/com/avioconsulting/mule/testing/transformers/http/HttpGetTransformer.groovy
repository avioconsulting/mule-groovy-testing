package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo

class HttpGetTransformer implements HttpTransformer {
    @Override
    HttpState transform(HttpState httpState,
                        HttpRequesterInfo connectorInfo) {
        // for GET requests, we don't want to pass on the payload
        if (connectorInfo.method == 'GET') {
            // a wildcard mediatype should work for this
            return new HttpState(httpState.httpStatus,
                                 null,
                                 '*/*')
        }
        httpState
    }
}
