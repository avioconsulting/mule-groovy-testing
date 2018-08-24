package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent

interface OutputTransformer {
    MuleEvent transformOutput(input,
                              MuleEvent originalMuleEvent)

    def disableStreaming()
}