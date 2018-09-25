package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer

abstract class Common implements
        OutputTransformer {
    private boolean useStreaming
    private final TransformingEventFactory eventFactory

    Common(TransformingEventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.useStreaming = true
    }

    abstract String getJsonOutput(input)

    EventWrapper transformOutput(Object input,
                                 EventWrapper originalMuleEvent) {
        def jsonString = getJsonOutput(input)
        // TODO: Fix other cases that use 'content-type' to supply media type instead
        def messageProps = [
                'http.status': '200'
        ]
        // TODO: This is not the same as the ManagedCursorStreamProvider stream an HTTP listener
        // uses, but it might work fine
        def payload = useStreaming ? new ByteArrayInputStream(jsonString.bytes) : jsonString
        eventFactory.getMuleEventWithPayload(payload,
                                             originalMuleEvent,
                                             'application/json',
                                             messageProps)
    }

    def disableStreaming() {
        useStreaming = false
    }
}
