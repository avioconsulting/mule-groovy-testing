package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.api.event.Event
import org.mule.runtime.core.api.processor.Processor

interface InputTransformer {
    def transformInput(Event input,
                       Processor messageProcessor)

    def disableStreaming()
}
