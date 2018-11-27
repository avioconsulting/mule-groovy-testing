package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.EventFactory
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class RawTransformer implements InputTransformer, OutputTransformer {
    private final EventFactory eventFactory

    RawTransformer(EventFactory eventFactory) {
        this.eventFactory = eventFactory
    }

    @Override
    def transformInput(MuleEvent input,
                       MessageProcessor messageProcessor) {
        input.message.payload
    }

    @Override
    MuleEvent transformOutput(Object inputMessage,
                              MuleEvent originalMuleEvent) {
        eventFactory.getMuleEventWithPayload(inputMessage,
                                             originalMuleEvent)
    }

    @Override
    def disableStreaming() {
        // don't need to do anything
        return null
    }
}
