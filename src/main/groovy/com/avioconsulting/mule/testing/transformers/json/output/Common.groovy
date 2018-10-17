package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer

abstract class Common implements
        OutputTransformer {

    abstract String getJsonOutput(input)

    EventWrapper transformOutput(Object input,
                                 EventWrapper originalMuleEvent) {
        def jsonString = getJsonOutput(input)
        def messageProps = [
                'http.status': '200'
        ]
        originalMuleEvent.withNewStreamingPayload(jsonString,
                                                  'application/json',
                                                  messageProps)
    }
}
