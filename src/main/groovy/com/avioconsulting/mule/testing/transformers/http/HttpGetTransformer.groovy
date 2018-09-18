package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class HttpGetTransformer implements
        MuleMessageTransformer<HttpRequesterInfo> {
    void transform(MockEventWrapper muleEvent,
                   HttpRequesterInfo connectorInfo) {
        // for GET requests, we don't want to pass on the payload
        connectorInfo.method == 'GET' ? eventFactory.getMuleEventWithPayload(null,
                                                                             muleEvent) : muleEvent
    }
}
