package com.avioconsulting.mule.testing.mocks


import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class StandardMock implements MockProcess<MessageProcessor> {
    private final MuleMessageTransformer mockTransformer

    StandardMock(MuleMessageTransformer mockTransformer) {
        this.mockTransformer = mockTransformer
    }

    @Override
    MuleEvent process(MuleEvent event,
                      MessageProcessor originalProcessor) {
        mockTransformer.transform(event,
                                  originalProcessor)
    }
}
