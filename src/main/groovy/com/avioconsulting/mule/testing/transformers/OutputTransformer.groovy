package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface OutputTransformer {
    EventWrapper transformOutput(input,
                                 EventWrapper originalMuleEvent)

    def disableStreaming()
}