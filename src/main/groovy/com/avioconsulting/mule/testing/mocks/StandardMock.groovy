package com.avioconsulting.mule.testing.mocks


import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

class StandardMock implements MockProcess<Processor> {
    private final MuleMessageTransformer mockTransformer

    StandardMock(MuleMessageTransformer mockTransformer) {
        this.mockTransformer = mockTransformer
    }

    @Override
    CoreEvent process(CoreEvent event,
                      Processor originalProcessor) {
        mockTransformer.transform(event,
                                  originalProcessor)
    }
}
