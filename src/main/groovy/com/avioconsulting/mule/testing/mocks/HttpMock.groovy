package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class HttpMock implements MockProcess {
    @Override
    MuleEvent process(MuleEvent event,
                      MessageProcessor originalProcessor) {
        return null
    }
}
