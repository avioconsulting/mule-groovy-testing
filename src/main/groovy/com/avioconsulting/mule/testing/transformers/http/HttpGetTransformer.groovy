package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

class HttpGetTransformer implements MuleMessageTransformer {
    private final InvokerEventFactory eventFactory

    HttpGetTransformer(InvokerEventFactory eventFactory) {
        this.eventFactory = eventFactory
    }

    void transform(MockEventWrapper muleEvent,
                   ConnectorInfo connectorInfo) {
        assert false: 'htttp requester class'
        //assert originalProcessor instanceof DefaultHttpRequester
        // for GET requests, we don't want to pass on the payload
        originalProcessor.method == 'GET' ? eventFactory.getMuleEventWithPayload(null,
                                                                                 muleEvent) : muleEvent
    }
}
