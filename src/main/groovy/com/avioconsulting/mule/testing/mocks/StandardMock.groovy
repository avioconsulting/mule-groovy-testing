package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class StandardMock implements MockProcess<MessageProcessor> {
    private final MuleMessageTransformer mockTransformer
    private final EventFactory eventFactory

    StandardMock(MuleMessageTransformer mockTransformer,
                 EventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.mockTransformer = mockTransformer
    }

    @Override
    MuleEvent process(MuleEvent event,
                      MessageProcessor originalProcessor) {
        def processedMessage = mockTransformer.transform(event.message)
        eventFactory.getMuleEvent(processedMessage,
                                  event)
    }
}
