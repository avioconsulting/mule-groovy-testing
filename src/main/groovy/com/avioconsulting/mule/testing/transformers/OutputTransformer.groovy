package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

interface OutputTransformer {
    EventWrapper transformOutput(input,
                                 EventWrapper originalMuleEvent)
}