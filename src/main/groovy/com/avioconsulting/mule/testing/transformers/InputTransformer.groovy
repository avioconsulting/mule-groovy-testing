package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

interface InputTransformer {
    def transformInput(MuleEvent input,
                       MessageProcessor messageProcessor)

    def disableStreaming()
}
