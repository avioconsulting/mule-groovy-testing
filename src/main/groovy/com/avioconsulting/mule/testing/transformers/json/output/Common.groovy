package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MockEventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer

abstract class Common implements
        OutputTransformer {
    private boolean useStreaming
    protected final InvokerEventFactory eventFactory

    Common(InvokerEventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.useStreaming = true
    }

    abstract String getJsonOutput(input)

    void transformOutput(Object input,
                         MockEventWrapper originalMuleEvent) {
        def jsonString = getJsonOutput(input)
        def messageProps = [
                'content-type': 'application/json; charset=utf-8'
        ]
        messageProps['http.status'] = 200
        def payload = useStreaming ? new ByteArrayInputStream(jsonString.bytes) : jsonString
        eventFactory.getMuleEventWithPayload(payload,
                                             originalMuleEvent,
                                             messageProps)
    }

    def disableStreaming() {
        useStreaming = false
    }
}
