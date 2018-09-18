package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class StandardMock implements MockProcess<ProcessorWrapper> {
    private final MuleMessageTransformer mockTransformer

    StandardMock(MuleMessageTransformer mockTransformer) {
        this.mockTransformer = mockTransformer
    }

    @Override
    EventWrapper process(EventWrapper event,
                         ProcessorWrapper originalProcessor) {
        mockTransformer.transform(event,
                                  originalProcessor)
    }
}
