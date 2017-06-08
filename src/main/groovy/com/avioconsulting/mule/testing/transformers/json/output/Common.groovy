package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.messages.JsonMessage
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

abstract class Common implements OutputTransformer, JsonMessage {
    private final MuleContext muleContext
    private boolean useStreaming

    Common(MuleContext muleContext) {
        this.useStreaming = true
        this.muleContext = muleContext
    }

    abstract String getJsonOutput(input)

    MuleMessage transformOutput(Object input) {
        def jsonString = getJsonOutput(input)
        getJSONMessage(jsonString,
                       muleContext,
                       200,
                       useStreaming)
    }

    def disableStreaming() {
        useStreaming = false
    }
}
