package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.module.http.internal.request.DefaultHttpRequester

class HttpGetTransformer implements MuleMessageTransformer {
    private final EventFactory eventFactory

    HttpGetTransformer(EventFactory eventFactory) {
        this.eventFactory = eventFactory
    }

    MuleEvent transform(MuleEvent muleEvent,
                        MessageProcessor originalProcessor) {
        assert originalProcessor instanceof DefaultHttpRequester
        // for GET requests, we don't want to pass on the payload
        originalProcessor.method == 'GET' ? eventFactory.getMuleEventWithPayload(null,
                                                                                 muleEvent) : muleEvent
    }
}
