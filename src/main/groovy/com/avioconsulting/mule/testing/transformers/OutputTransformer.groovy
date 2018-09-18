package com.avioconsulting.mule.testing.transformers


import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper

interface OutputTransformer {
    void transformOutput(input,
                         MockEventWrapper originalMuleEvent)

    def disableStreaming()
}