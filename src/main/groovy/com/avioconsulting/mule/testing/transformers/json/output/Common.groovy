package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer

abstract class Common implements
        OutputTransformer {

    abstract String getJsonOutput(input)

    EventWrapper transformOutput(Object input,
                                 EventWrapper originalMuleEvent) {
        def jsonString = getJsonOutput(input)
        // TODO: Fix other cases that use 'content-type' to supply media type instead
        def messageProps = [
                'http.status': '200'
        ]
        originalMuleEvent.newStreamedEvent(jsonString,
                                           'application/json',
                                           messageProps)
    }
}
