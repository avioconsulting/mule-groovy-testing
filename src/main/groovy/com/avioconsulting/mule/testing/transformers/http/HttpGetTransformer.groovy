package com.avioconsulting.mule.testing.transformers.http

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class HttpGetTransformer implements MuleMessageTransformer {
    private final EventFactory eventFactory

    HttpGetTransformer(EventFactory eventFactory) {
        this.eventFactory = eventFactory
    }

    CoreEvent transform(CoreEvent muleEvent,
                        Processor originalProcessor) {
        assert false :'htttp requester class'
        //assert originalProcessor instanceof DefaultHttpRequester
        // for GET requests, we don't want to pass on the payload
        originalProcessor.method == 'GET' ? eventFactory.getMuleEventWithPayload(null,
                                                                                 muleEvent) : muleEvent
    }
}
