package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.api.event.Event

interface OutputTransformer {
    Event transformOutput(input,
                              Event originalMuleEvent)
    def disableStreaming()
}