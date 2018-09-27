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
        if (useStreaming) {
            return eventFactory.getStreamedMuleEventWithPayload(jsonString,
                                                                originalMuleEvent,
                                                                'application/json',
                                                                messageProps)
        }
        return eventFactory.getMuleEventWithPayload(jsonString,
                                                    originalMuleEvent,
                                                    'application/json',
                                                    messageProps)
    }

    def disableStreaming() {
        useStreaming = false
    }
}
