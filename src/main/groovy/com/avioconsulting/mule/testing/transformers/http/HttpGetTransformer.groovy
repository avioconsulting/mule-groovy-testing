package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ProcessorWrapper

class HttpGetTransformer implements MuleMessageTransformer {
    private final EventFactory eventFactory

    HttpGetTransformer(EventFactory eventFactory) {
        this.eventFactory = eventFactory
    }

    EventWrapper transform(EventWrapper muleEvent,
                           ProcessorWrapper originalProcessor) {
        assert false: 'htttp requester class'
        //assert originalProcessor instanceof DefaultHttpRequester
        // for GET requests, we don't want to pass on the payload
        originalProcessor.method == 'GET' ? eventFactory.getMuleEventWithPayload(null,
                                                                                 muleEvent) : muleEvent
    }
}
