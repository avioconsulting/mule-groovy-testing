package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.transformers.OutputTransformer
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage

abstract class Common implements OutputTransformer {
    private final MuleContext muleContext
    private boolean useStreaming

    Common(MuleContext muleContext) {
        this.useStreaming = true
        this.muleContext = muleContext
    }

    abstract String getJsonOutput(input)

    MuleMessage transformOutput(Object input) {
        def jsonString = getJsonOutput(input)
        def messageProps = [
                'content-type': 'application/json; charset=utf-8'
        ]
        messageProps['http.status'] = 200
        def payload = useStreaming ? new ByteArrayInputStream(jsonString.bytes) : jsonString
        new DefaultMuleMessage(payload,
                               messageProps,
                               null,
                               null,
                               muleContext)
    }

    def disableStreaming() {
        useStreaming = false
    }
}
