package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.OutputTransformer
import com.fasterxml.jackson.databind.ObjectMapper

class JacksonOutputTransformer implements
        OutputTransformer {
    private boolean useRepeatableStream = true
    def mapper = new ObjectMapper()

    def nonRepeatableStream() {
        this.useRepeatableStream = false
    }

    EventWrapper transformOutput(Object input,
                                 EventWrapper originalMuleEvent,
                                 ConnectorInfo connectorInfo) {
        def jsonString = getJsonOutput(input)
        def messageProps = [
                'http.status': '200'
        ]
        originalMuleEvent.withNewStreamingPayload(jsonString,
                                                  'application/json',
                                                  messageProps,
                                                  connectorInfo,
                                                  this.useRepeatableStream)
    }

    String getJsonOutput(Object response) {
        mapper.writer().writeValueAsString(response)
    }
}
