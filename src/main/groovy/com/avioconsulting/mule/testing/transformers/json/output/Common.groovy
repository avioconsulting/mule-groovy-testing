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
