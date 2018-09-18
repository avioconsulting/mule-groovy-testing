package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.MessageFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo

class HttpGetTransformer implements
        MuleMessageTransformer<HttpRequesterInfo> {
    private final MessageFactory messageFactory

    HttpGetTransformer(MessageFactory messageFactory) {
        this.messageFactory = messageFactory
    }

    void transform(MockEventWrapper muleEvent,
                   HttpRequesterInfo connectorInfo) {
        // for GET requests, we don't want to pass on the payload
        if (connectorInfo.method == 'GET') {
            def newMessage = messageFactory.buildMessage(null)
            muleEvent.changeMessage(newMessage)
        }
    }
}
