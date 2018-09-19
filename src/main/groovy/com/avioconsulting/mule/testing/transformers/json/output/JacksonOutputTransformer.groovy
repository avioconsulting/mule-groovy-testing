package com.avioconsulting.mule.testing.transformers.json.output

import com.avioconsulting.mule.testing.TransformingEventFactory
import com.fasterxml.jackson.databind.ObjectMapper

class JacksonOutputTransformer extends
        Common {
    def mapper = new ObjectMapper()

    JacksonOutputTransformer(TransformingEventFactory eventFactory) {
        super(eventFactory)
    }

    String getJsonOutput(Object response) {
        mapper.writer().writeValueAsString(response)
    }
}
