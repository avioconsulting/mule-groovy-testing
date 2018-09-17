package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import org.mule.runtime.api.event.Event

abstract class Common implements OutputTransformer {
    private boolean useStreaming
    protected final EventFactory eventFactory

    Common(EventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.useStreaming = true
    }

    abstract String getJsonOutput(input)

    Event transformOutput(Object input,
                              Event originalMuleEvent) {
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
