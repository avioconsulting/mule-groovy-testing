package com.avioconsulting.mule.testing.transformers

import org.mule.runtime.core.api.event.CoreEvent
import org.mule.runtime.core.api.processor.Processor

interface InputTransformer {
    def transformInput(CoreEvent input,
                       Processor messageProcessor)

    def disableStreaming()
}
