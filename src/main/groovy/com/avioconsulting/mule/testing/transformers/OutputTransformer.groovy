package com.avioconsulting.mule.testing.transformers

import org.mule.api.MuleMessage

interface OutputTransformer {
    MuleMessage transformOutput(input)

    def disableStreaming()
}