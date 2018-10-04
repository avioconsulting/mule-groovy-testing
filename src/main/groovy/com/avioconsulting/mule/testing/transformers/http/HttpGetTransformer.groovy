package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class HttpGetTransformer implements
        MuleMessageTransformer<HttpRequesterInfo> {
    EventWrapper transform(EventWrapper muleEvent,
                           HttpRequesterInfo connectorInfo) {
        // for GET requests, we don't want to pass on the payload
        if (connectorInfo.method == 'GET') {
            // a wildcard mediatype should work for this
            return muleEvent.withNewPayload(null,
                                            '*/*')
        }
        muleEvent
    }
}
