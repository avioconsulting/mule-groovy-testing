package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.core.api.event.CoreEvent

interface OutputTransformer {
    CoreEvent transformOutput(input,
                              CoreEvent originalMuleEvent)
    def disableStreaming()
}