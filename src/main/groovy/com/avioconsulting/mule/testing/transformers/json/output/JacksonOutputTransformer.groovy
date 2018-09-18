package com.avioconsulting.mule.testing.transformers.json.output

import com.fasterxml.jackson.databind.ObjectMapper

class JacksonOutputTransformer extends
        Common {
    def mapper = new ObjectMapper()

    String getJsonOutput(Object response) {
        mapper.writer().writeValueAsString(response)
    }
}
