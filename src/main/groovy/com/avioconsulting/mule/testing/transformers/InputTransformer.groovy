package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage

interface InputTransformer {
    def transformInput(MuleMessage input)
    def disableStreaming()
}
